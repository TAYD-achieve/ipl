package com.example.simulation

import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Match Simulation Engine for Scheduled Games.
 * Calculates match outcomes for scheduled fixtures based on dynamic team strength metrics
 * retrieved directly from the central state manager (GameProgressStore), saves results,
 * applies player statistics updates, generates news, and updates the season standings.
 */
object ScheduledMatchSimulationEngine {

    data class SimulationSummary(
        val matchesSimulated: Int,
        val matchResults: List<MatchEntity>,
        val updatedStandings: List<SeasonSimulator.PointsTableEntry>,
        val message: String
    )

    /**
     * Simulates a single scheduled match using team strength metrics from the state manager
     * and updates the season standings.
     */
    suspend fun simulateScheduledGame(
        match: MatchEntity,
        repository: AppRepository,
        progressStore: GameProgressStore
    ): MatchEntity? = withContext(Dispatchers.IO) {
        if (match.isPlayed) return@withContext match

        val teamAObj = repository.getTeamById(match.teamAId) ?: return@withContext null
        val teamBObj = repository.getTeamById(match.teamBId) ?: return@withContext null
        val playersA = repository.getPlayersForTeamSync(match.teamAId)
        val playersB = repository.getPlayersForTeamSync(match.teamBId)

        // Retrieve dynamic team strength metrics from the state manager
        val strengthA = progressStore.getTeamStrengthMetrics(match.teamAId)
        val strengthB = progressStore.getTeamStrengthMetrics(match.teamBId)

        val userTeamId = progressStore.activeFranchiseTeam.value?.teamId ?: ""

        // Run MatchEngine simulation with overridden state manager strength metrics
        val simRes = MatchEngine.simulateMatch(
            teamA = teamAObj,
            teamAPlayers = playersA,
            teamB = teamBObj,
            teamBPlayers = playersB,
            weather = match.weather,
            userTeamId = userTeamId,
            userBattingTactic = "NORMAL",
            userBowlingTactic = "STANDARD",
            teamAStrengthOverride = strengthA,
            teamBStrengthOverride = strengthB
        )

        val updatedMatch = match.copy(
            teamAScore = simRes.teamAScoreText,
            teamBScore = simRes.teamBScoreText,
            isPlayed = true,
            winnerId = simRes.winnerId,
            commentary = simRes.commentary,
            tossWinnerId = simRes.tossWinnerId,
            tossDecision = simRes.tossDecision
        )

        // Save match in database
        repository.insertMatch(updatedMatch)

        // Apply player stats updates
        applyPlayerStatsUpdates(simRes.playerStatsUpdates, repository)

        // Adjust player morale based on match outcome
        val winA = simRes.winnerId == match.teamAId
        val winB = simRes.winnerId == match.teamBId
        val uPlayers = playersA.ifEmpty { repository.getPlayersForTeamSync(match.teamAId) }
        val bPlayers = playersB.ifEmpty { repository.getPlayersForTeamSync(match.teamBId) }
        uPlayers.forEach { p -> repository.insertPlayer(p.copy(morale = (p.morale + if (winA) 5 else -7).coerceIn(20, 100))) }
        bPlayers.forEach { p -> repository.insertPlayer(p.copy(morale = (p.morale + if (winB) 5 else -7).coerceIn(20, 100))) }

        // If user team was involved, handle ticket earnings, stadium capacity boosted fan growth, and news
        val uTeam = progressStore.activeFranchiseTeam.value
        if (uTeam != null && (match.teamAId == uTeam.teamId || match.teamBId == uTeam.teamId)) {
            val isHome = match.teamAId == uTeam.teamId
            val isWin = updatedMatch.winnerId == uTeam.teamId
            val baseFans = if (isWin) 35000L else 10000L
            val capacityBoost = (uTeam.stadiumCapacity / 1000L) * (if (isWin) 1200L else 400L)
            val newFollowers = uTeam.followers + baseFans + capacityBoost

            if (isHome) {
                val ticketIncome = (uTeam.stadiumCapacity * 800L).coerceIn(10000000L, 80000000L)
                val finalBal = if (isWin) uTeam.balance + ticketIncome else uTeam.balance + (ticketIncome / 2)
                repository.insertTeam(uTeam.copy(balance = finalBal, followers = newFollowers))
                repository.insertFinance(
                    FinanceEntity(
                        slotId = uTeam.slotId,
                        type = "INCOME",
                        category = "TICKETS",
                        amount = ticketIncome,
                        season = progressStore.currentSeason.value,
                        matchDay = match.matchDay
                    )
                )
            } else {
                repository.insertTeam(uTeam.copy(followers = newFollowers))
            }

            val winnerName = if (simRes.winnerId == uTeam.teamId) uTeam.name else if (simRes.winnerId == teamAObj.teamId) teamAObj.name else teamBObj.name
            val newsTitle = if (simRes.winnerId == uTeam.teamId) "⚡ Thrilling Sim Win for ${uTeam.name}!" else "⚡ ${winnerName} Clinches Victory in Sim!"
            repository.insertNews(
                NewsEntity(
                    slotId = uTeam.slotId,
                    title = newsTitle,
                    content = "Matchday ${match.matchDay} simulated fixture concluded with $winnerName victorious. Final scores: ${simRes.teamAScoreText} vs ${simRes.teamBScoreText}.",
                    category = "PERFORMANCE"
                )
            )
        }

        // Update season standings synchronously in state manager
        progressStore.updateSeasonStandings()

        updatedMatch
    }

    /**
     * Simulates all unplayed scheduled games for a given matchday/round using state manager
     * team strength metrics and updates the season standings.
     */
    suspend fun simulateScheduledRound(
        round: Int,
        repository: AppRepository,
        progressStore: GameProgressStore
    ): SimulationSummary = withContext(Dispatchers.IO) {
        val allMatches = progressStore.currentSeasonMatches.value.ifEmpty {
            progressStore.activeSlotId.value?.let { repository.getMatchesForSlotSync(it) } ?: emptyList()
        }
        val unplayedInRound = allMatches.filter { it.matchDay == round && !it.isPlayed }

        val simulatedList = mutableListOf<MatchEntity>()
        for (match in unplayedInRound) {
            val res = simulateScheduledGame(match, repository, progressStore)
            if (res != null) simulatedList.add(res)
        }

        val standings = progressStore.updateSeasonStandings()

        SimulationSummary(
            matchesSimulated = simulatedList.size,
            matchResults = simulatedList,
            updatedStandings = standings,
            message = "Simulated ${simulatedList.size} scheduled games for Round $round based on state manager team strengths. Standings updated!"
        )
    }

    /**
     * Simulates all scheduled AI vs AI games up to the next match involving the user's franchise team.
     */
    suspend fun simulateUpToNextUserMatch(
        repository: AppRepository,
        progressStore: GameProgressStore
    ): SimulationSummary = withContext(Dispatchers.IO) {
        val userTeamId = progressStore.activeFranchiseTeam.value?.teamId ?: ""
        val allMatches = progressStore.currentSeasonMatches.value.sortedBy { it.matchDay }
        val unplayed = allMatches.filter { !it.isPlayed }

        val simulatedList = mutableListOf<MatchEntity>()
        for (match in unplayed) {
            // If user is playing in this match, stop auto-simulating
            if (match.teamAId == userTeamId || match.teamBId == userTeamId) {
                break
            }
            val res = simulateScheduledGame(match, repository, progressStore)
            if (res != null) simulatedList.add(res)
        }

        val standings = progressStore.updateSeasonStandings()

        SimulationSummary(
            matchesSimulated = simulatedList.size,
            matchResults = simulatedList,
            updatedStandings = standings,
            message = "Simulated ${simulatedList.size} scheduled AI fixtures. Season standings updated! Your next fixture is ready."
        )
    }

    /**
     * Simulates all remaining scheduled games in the regular season.
     */
    suspend fun simulateRemainingSeason(
        repository: AppRepository,
        progressStore: GameProgressStore
    ): SimulationSummary = withContext(Dispatchers.IO) {
        val allMatches = progressStore.currentSeasonMatches.value.sortedBy { it.matchDay }
        val unplayed = allMatches.filter { !it.isPlayed && !it.isPlayoff }

        val simulatedList = mutableListOf<MatchEntity>()
        for (match in unplayed) {
            val res = simulateScheduledGame(match, repository, progressStore)
            if (res != null) simulatedList.add(res)
        }

        val standings = progressStore.updateSeasonStandings()

        SimulationSummary(
            matchesSimulated = simulatedList.size,
            matchResults = simulatedList,
            updatedStandings = standings,
            message = "Simulated ${simulatedList.size} remaining regular season matches. Final standings locked!"
        )
    }

    private suspend fun applyPlayerStatsUpdates(updates: List<MatchEngine.PlayerStatsUpdate>, repository: AppRepository) {
        for (up in updates) {
            val pl = repository.getPlayerById(up.playerId) ?: continue
            val newRuns = pl.runs + up.runsAdded
            val newWickets = pl.wickets + up.wicketsAdded
            val newMatches = pl.matchesPlayed + 1
            val newCenturies = pl.centuries + up.centuriesAdded
            val newFiveWickets = pl.fiveWickets + up.fiveWicketsAdded

            val prevBalls = if (pl.strikeRate > 0) (pl.runs.toDouble() / pl.strikeRate) * 100.0 else 0.0
            val totBalls = prevBalls + up.ballsFaced
            val newSR = if (totBalls > 0) ((newRuns.toDouble() / totBalls) * 100.0) else pl.strikeRate

            val matchEcon = if (up.ballsBowled > 0) (up.runsConceded.toDouble() / (up.ballsBowled / 6.0)) else 0.0
            val newEcon = if (pl.economy == 0.0 && matchEcon > 0) matchEcon else if (matchEcon > 0) ((pl.economy * pl.matchesPlayed) + matchEcon) / newMatches else pl.economy

            repository.insertPlayer(
                pl.copy(
                    matchesPlayed = newMatches,
                    runs = newRuns,
                    wickets = newWickets,
                    strikeRate = Math.round(newSR * 100.0) / 100.0,
                    economy = Math.round(newEcon * 100.0) / 100.0,
                    centuries = newCenturies,
                    fiveWickets = newFiveWickets
                )
            )
        }
    }
}
