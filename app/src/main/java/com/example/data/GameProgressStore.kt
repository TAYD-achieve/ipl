package com.example.data

import com.example.simulation.MatchEngine
import com.example.simulation.SeasonSimulator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Central State Management Provider & Store for IPL Franchise Tycoon.
 * Encapsulates the game's overall progress, season status, saved franchise data,
 * and league state into a unified, reactive store powered by Kotlin StateFlows.
 */
class GameProgressStore(private val repository: AppRepository) {

    private var storeScope: CoroutineScope? = null
    private var collectionJob: Job? = null

    // --- Active Save Slot & Timeline State ---
    private val _activeSlotId = MutableStateFlow<String?>(null)
    val activeSlotId: StateFlow<String?> = _activeSlotId.asStateFlow()

    private val _activeSaveSlot = MutableStateFlow<SaveSlotEntity?>(null)
    val activeSaveSlot: StateFlow<SaveSlotEntity?> = _activeSaveSlot.asStateFlow()

    val currentSeason: StateFlow<Int> = _activeSaveSlot
        .map { it?.currentSeason ?: 1 }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, 1)

    val currentMatchDay: StateFlow<Int> = _activeSaveSlot
        .map { it?.currentMatchDay ?: 1 }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, 1)

    val seasonPhase: StateFlow<String> = _activeSaveSlot
        .map { slot ->
            when {
                slot == null -> "IDLE"
                slot.isGameOver -> "GAME_OVER"
                slot.currentMatchDay <= 14 -> "REGULAR_SEASON"
                slot.currentMatchDay == 15 -> "PLAYOFF_QUALIFIERS"
                slot.currentMatchDay == 16 -> "PLAYOFF_QUALIFIER_2"
                slot.currentMatchDay == 17 -> "PLAYOFF_FINAL"
                else -> "SEASON_COMPLETED"
            }
        }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, "IDLE")

    val seasonProgressPercentage: StateFlow<Float> = _activeSaveSlot
        .map { slot ->
            val day = slot?.currentMatchDay ?: 1
            (day / 17f).coerceIn(0f, 1f)
        }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, 0f)

    val statusText: StateFlow<String> = _activeSaveSlot
        .map { slot ->
            if (slot == null) return@map "No Franchise Loaded"
            val s = slot.currentSeason
            val d = slot.currentMatchDay
            when {
                slot.isGameOver -> "Season $s • Franchise Archived / Game Over"
                d <= 14 -> "Season $s • Matchday $d of 14 (Regular League)"
                d == 15 -> "Season $s • Playoffs: Qualifier 1 & Eliminator"
                d == 16 -> "Season $s • Playoffs: Qualifier 2"
                d == 17 -> "Season $s • Grand Championship Final"
                else -> "Season $s • Season Completed (Awaiting Next Season)"
            }
        }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, "No Franchise Loaded")

    val isPlayoffs: StateFlow<Boolean> = _activeSaveSlot
        .map { (it?.currentMatchDay ?: 1) > 14 }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, false)

    // --- Saved Franchise State ---
    private val _activeFranchiseTeam = MutableStateFlow<TeamEntity?>(null)
    val activeFranchiseTeam: StateFlow<TeamEntity?> = _activeFranchiseTeam.asStateFlow()

    private val _franchiseRoster = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val franchiseRoster: StateFlow<List<PlayerEntity>> = _franchiseRoster.asStateFlow()

    private val _franchiseFinances = MutableStateFlow<List<FinanceEntity>>(emptyList())
    val franchiseFinances: StateFlow<List<FinanceEntity>> = _franchiseFinances.asStateFlow()

    val franchiseBalance: StateFlow<Long> = _activeFranchiseTeam
        .map { it?.balance ?: 0L }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, 0L)

    private val _franchiseCoaches = MutableStateFlow<List<CoachEntity>>(emptyList())
    val franchiseCoaches: StateFlow<List<CoachEntity>> = _franchiseCoaches.asStateFlow()

    private val _franchiseSponsors = MutableStateFlow<List<SponsorEntity>>(emptyList())
    val franchiseSponsors: StateFlow<List<SponsorEntity>> = _franchiseSponsors.asStateFlow()

    private val _franchiseTrophies = MutableStateFlow<List<TrophyEntity>>(emptyList())
    val franchiseTrophies: StateFlow<List<TrophyEntity>> = _franchiseTrophies.asStateFlow()

    private val _franchiseAchievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val franchiseAchievements: StateFlow<List<AchievementEntity>> = _franchiseAchievements.asStateFlow()

    // --- League & Global State ---
    private val _leagueTeams = MutableStateFlow<List<TeamEntity>>(emptyList())
    val leagueTeams: StateFlow<List<TeamEntity>> = _leagueTeams.asStateFlow()

    private val _allPlayers = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val allPlayers: StateFlow<List<PlayerEntity>> = _allPlayers.asStateFlow()

    private val _allMatches = MutableStateFlow<List<MatchEntity>>(emptyList())
    val allMatches: StateFlow<List<MatchEntity>> = _allMatches.asStateFlow()

    private val _currentSeasonMatches = MutableStateFlow<List<MatchEntity>>(emptyList())
    val currentSeasonMatches: StateFlow<List<MatchEntity>> = _currentSeasonMatches.asStateFlow()

    private val _pointsTable = MutableStateFlow<List<SeasonSimulator.PointsTableEntry>>(emptyList())
    val pointsTable: StateFlow<List<SeasonSimulator.PointsTableEntry>> = _pointsTable.asStateFlow()

    private val _newsFeed = MutableStateFlow<List<NewsEntity>>(emptyList())
    val newsFeed: StateFlow<List<NewsEntity>> = _newsFeed.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, 0)

    /**
     * Binds the central store to a specific save slot and initializes reactive database observers.
     */
    fun bindToSlot(slotId: String, username: String, scope: CoroutineScope) {
        this.storeScope = scope
        _activeSlotId.value = slotId

        collectionJob?.cancel()
        collectionJob = scope.launch {
            // 1. Observe Save Slot Entity
            launch {
                repository.getSaveSlotByIdFlow(slotId).collect { slot ->
                    _activeSaveSlot.value = slot
                }
            }

            // 2. Observe Teams & identify Franchise Team
            launch {
                repository.getTeamsForSlot(slotId).collect { teams ->
                    _leagueTeams.value = teams
                    val userTeam = teams.find { !it.isAi }
                    _activeFranchiseTeam.value = userTeam
                }
            }

            // 3. Observe All Players & Roster
            launch {
                repository.getPlayersForSlot(slotId).collect { players ->
                    _allPlayers.value = players
                    val userTeamId = _activeFranchiseTeam.value?.teamId
                    if (userTeamId != null) {
                        _franchiseRoster.value = players.filter { it.teamId == userTeamId }
                    } else {
                        // fallback check when team isn't populated yet
                        val fallbackTeam = _leagueTeams.value.find { !it.isAi }
                        if (fallbackTeam != null) {
                            _franchiseRoster.value = players.filter { it.teamId == fallbackTeam.teamId }
                        }
                    }
                }
            }

            // 4. Observe Matches & Calculate Points Table
            launch {
                repository.getMatchesForSlot(slotId).collect { matches ->
                    _allMatches.value = matches
                    val season = _activeSaveSlot.value?.currentSeason ?: 1
                    val seasonMatches = matches.filter { it.season == season }
                    _currentSeasonMatches.value = seasonMatches

                    val teams = _leagueTeams.value
                    if (teams.isNotEmpty()) {
                        val table = SeasonSimulator.calculatePointsTable(teams, seasonMatches)
                        _pointsTable.value = table
                    }
                }
            }

            // 5. Observe Coaches
            launch {
                repository.getCoachesForSlot(slotId).collect { items ->
                    _franchiseCoaches.value = items
                }
            }

            // 6. Observe Sponsors
            launch {
                repository.getSponsorsForSlot(slotId).collect { items ->
                    _franchiseSponsors.value = items
                }
            }

            // 7. Observe Finances
            launch {
                repository.getFinancesForSlot(slotId).collect { items ->
                    _franchiseFinances.value = items
                }
            }

            // 8. Observe Trophies
            launch {
                repository.getTrophiesForSlot(slotId).collect { items ->
                    _franchiseTrophies.value = items
                }
            }

            // 9. Observe News
            launch {
                repository.getNewsForSlot(slotId).collect { items ->
                    _newsFeed.value = items
                }
            }

            // 10. Observe Notifications
            launch {
                repository.getNotificationsForSlot(slotId).collect { items ->
                    _notifications.value = items
                }
            }

            // 11. Observe Achievements for User
            launch {
                if (username.isNotEmpty()) {
                    repository.getAchievementsForUser(username).collect { items ->
                        _franchiseAchievements.value = items
                    }
                }
            }
        }
    }

    /**
     * Advances the game timeline to the next matchday.
     * Handles transitions from regular league into playoffs and season completion.
     */
    suspend fun advanceMatchDay(): Result<String> = withContext(Dispatchers.IO) {
        val slot = _activeSaveSlot.value ?: return@withContext Result.failure(Exception("No active slot loaded."))
        val currentDay = slot.currentMatchDay
        val season = slot.currentSeason
        val slotId = slot.slotId

        try {
            val nextDay = currentDay + 1
            val updatedSlot = slot.copy(currentMatchDay = nextDay, lastPlayedTime = System.currentTimeMillis())
            repository.insertSaveSlot(updatedSlot)

            // Trigger Playoff Schedule Generation on Day 15
            if (nextDay == 15) {
                generatePlayoffRound1(slotId, season)
            } else if (nextDay == 16) {
                generatePlayoffRound2(slotId, season)
            } else if (nextDay == 17) {
                generatePlayoffFinal(slotId, season)
            } else if (nextDay > 17) {
                return@withContext Result.success("Season $season Concluded! Ready for Championship awards.")
            }

            Result.success("Advanced to Matchday $nextDay (Season $season)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generatePlayoffRound1(slotId: String, season: Int) {
        val table = _pointsTable.value
        if (table.size < 4) return
        val top4 = table.take(4)

        val q1 = MatchEntity(
            matchId = "${slotId}_S${season}_PLAYOFF_Q1",
            slotId = slotId,
            season = season,
            matchDay = 15,
            teamAId = top4[0].teamId,
            teamBId = top4[1].teamId,
            isPlayoff = true,
            playoffType = "Q1",
            weather = "SUNNY"
        )

        val elim = MatchEntity(
            matchId = "${slotId}_S${season}_PLAYOFF_ELIM",
            slotId = slotId,
            season = season,
            matchDay = 15,
            teamAId = top4[2].teamId,
            teamBId = top4[3].teamId,
            isPlayoff = true,
            playoffType = "EL",
            weather = "OVERCAST"
        )

        repository.insertMatches(listOf(q1, elim))
        repository.insertNews(
            NewsEntity(
                slotId = slotId,
                title = "🏆 Season $season Playoffs Set!",
                content = "Top 4 teams have qualified for the playoffs! ${top4[0].teamName} faces ${top4[1].teamName} in Qualifier 1, while ${top4[2].teamName} battles ${top4[3].teamName} in the Eliminator.",
                category = "GENERAL"
            )
        )
    }

    private suspend fun generatePlayoffRound2(slotId: String, season: Int) {
        val matches = _allMatches.value.filter { it.season == season && it.matchDay == 15 }
        val q1 = matches.find { it.playoffType == "Q1" } ?: return
        val elim = matches.find { it.playoffType == "EL" } ?: return

        // Loser of Q1 vs Winner of Eliminator
        val q1LoserId = if (q1.winnerId == q1.teamAId) q1.teamBId else q1.teamAId
        val elimWinnerId = elim.winnerId ?: elim.teamAId

        val q2 = MatchEntity(
            matchId = "${slotId}_S${season}_PLAYOFF_Q2",
            slotId = slotId,
            season = season,
            matchDay = 16,
            teamAId = q1LoserId,
            teamBId = elimWinnerId,
            isPlayoff = true,
            playoffType = "Q2",
            weather = "SUNNY"
        )
        repository.insertMatch(q2)
    }

    private suspend fun generatePlayoffFinal(slotId: String, season: Int) {
        val q1 = _allMatches.value.find { it.season == season && it.playoffType == "Q1" } ?: return
        val q2 = _allMatches.value.find { it.season == season && it.playoffType == "Q2" } ?: return

        val q1WinnerId = q1.winnerId ?: q1.teamAId
        val q2WinnerId = q2.winnerId ?: q2.teamAId

        val finalMatch = MatchEntity(
            matchId = "${slotId}_S${season}_PLAYOFF_FINAL",
            slotId = slotId,
            season = season,
            matchDay = 17,
            teamAId = q1WinnerId,
            teamBId = q2WinnerId,
            isPlayoff = true,
            playoffType = "FI",
            weather = "SUNNY"
        )
        repository.insertMatch(finalMatch)
        repository.insertNews(
            NewsEntity(
                slotId = slotId,
                title = "🔥 GRAND FINALE READY!",
                content = "The stage is set! Two titans meet in Matchday 17 to crown the Season $season Champions!",
                category = "GENERAL"
            )
        )
    }

    /**
     * Concludes the current season, awards trophies and prize money, and starts the next season.
     */
    suspend fun advanceToNextSeason(winnerTeamId: String, winnerTeamName: String): Result<String> = withContext(Dispatchers.IO) {
        val slot = _activeSaveSlot.value ?: return@withContext Result.failure(Exception("No active slot loaded."))
        val oldSeason = slot.currentSeason
        val newSeason = oldSeason + 1
        val slotId = slot.slotId
        val userTeam = _activeFranchiseTeam.value

        try {
            // Award Trophy if user won
            if (userTeam != null && winnerTeamId == userTeam.teamId) {
                repository.insertTrophy(
                    TrophyEntity(
                        slotId = slotId,
                        teamId = userTeam.teamId,
                        title = "Season $oldSeason League Champion",
                        season = oldSeason,
                        description = "Won the grand IPL Championship trophy in Season $oldSeason!"
                    )
                )
                // Prize money ₹20 Crores
                recordFranchiseTransaction("INCOME", "PRIZE_MONEY", 200000000L, "Season $oldSeason Championship Prize Money!")
            } else if (userTeam != null) {
                // Participation / League ranking payout (₹5 Crores)
                recordFranchiseTransaction("INCOME", "PRIZE_MONEY", 50000000L, "Season $oldSeason League Participation Reward.")
            }

            // Post Season Recap News
            repository.insertNews(
                NewsEntity(
                    slotId = slotId,
                    title = "🎉 Season $oldSeason Concluded! $winnerTeamName Crowned Champions!",
                    content = "$winnerTeamName lifts the trophy! Franchises are now preparing budgets and squads for Season $newSeason.",
                    category = "GENERAL"
                )
            )

            // Generate New Season Schedule
            val teams = _leagueTeams.value
            if (teams.isNotEmpty()) {
                val newSchedule = SeasonSimulator.generateSchedule(slotId, teams, newSeason)
                repository.insertMatches(newSchedule)
            }

            // Reset matchday to 1, advance season
            val nextSeasonSlot = slot.copy(currentSeason = newSeason, currentMatchDay = 1, lastPlayedTime = System.currentTimeMillis())
            repository.insertSaveSlot(nextSeasonSlot)

            Result.success("Welcome to Season $newSeason! All budgets, squads, and schedules refreshed.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Safely executes a financial transaction for the active franchise team, tagged with current season & matchday.
     */
    suspend fun recordFranchiseTransaction(
        type: String, // "INCOME" or "EXPENSE"
        category: String, // "SPONSORS", "MERCHANDISE", "TRANSFERS", "STADIUM", "PRIZE_MONEY"
        amount: Long,
        description: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        val team = _activeFranchiseTeam.value ?: return@withContext false
        val slot = _activeSaveSlot.value ?: return@withContext false

        val newBal = if (type == "INCOME") team.balance + amount else team.balance - amount
        if (newBal < 0 && type == "EXPENSE") return@withContext false // Insufficient funds

        repository.insertTeam(team.copy(balance = newBal))
        repository.insertFinance(
            FinanceEntity(
                slotId = team.slotId,
                type = type,
                category = category,
                amount = amount,
                season = slot.currentSeason,
                matchDay = slot.currentMatchDay
            )
        )

        if (description.isNotEmpty()) {
            repository.insertNotification(
                NotificationEntity(
                    slotId = team.slotId,
                    message = "💰 $description (₹${formatCurrency(amount)})"
                )
            )
        }
        true
    }

    /**
     * Saves a snapshot of current franchise progress and updates timestamp.
     */
    suspend fun saveProgressSnapshot(): Result<String> = withContext(Dispatchers.IO) {
        val slot = _activeSaveSlot.value ?: return@withContext Result.failure(Exception("No active save slot."))
        try {
            repository.insertSaveSlot(slot.copy(lastPlayedTime = System.currentTimeMillis()))
            Result.success("Franchise Progress Saved successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        val slotId = _activeSlotId.value ?: return@withContext
        repository.markAllNotificationsRead(slotId)
    }

    private fun formatCurrency(amount: Long): String {
        return when {
            amount >= 10000000L -> "${String.format("%.2f", amount / 10000000.0)} Cr"
            amount >= 100000L -> "${String.format("%.2f", amount / 100000.0)} Lakh"
            else -> amount.toString()
        }
    }

    /**
     * Retrieves dynamic team strength metrics from the state manager.
     * Combines roster attributes, coaching staff boosts, and fan support / morale form.
     */
    fun getTeamStrengthMetrics(teamId: String): MatchEngine.TeamStrengthSummary {
        val team = _leagueTeams.value.find { it.teamId == teamId }
        val players = _allPlayers.value.filter { it.teamId == teamId }
        val coaches = _franchiseCoaches.value.filter { it.teamId == teamId }

        val baseStrength = MatchEngine.calculateTeamStrength(players)

        // Coach boost: Each coach adds +1 to +3 based on rating
        val coachBoost = if (coaches.isNotEmpty()) {
            (coaches.map { it.rating }.sum() / (coaches.size * 25)).coerceIn(0, 5)
        } else 0

        // Team Morale & Fan Support dynamic boost
        val fanSupport = team?.fanHappiness ?: 50
        val avgMorale = if (players.isNotEmpty()) players.map { it.morale }.average().toInt() else 75
        val moraleBoost = ((avgMorale - 50) / 7).coerceIn(-7, 7) // dynamic OVR swing from -7 to +7 based on squad morale!
        val fanBoost = ((fanSupport - 50) / 15).coerceIn(-2, 3)

        val totalBoost = baseStrength.captaincyBoost + coachBoost + moraleBoost + fanBoost
        val newOverall = (baseStrength.overall + coachBoost + moraleBoost + fanBoost).coerceIn(40, 99)

        return baseStrength.copy(
            overall = newOverall,
            captaincyBoost = totalBoost
        )
    }

    /**
     * Recalculates and updates the season standings synchronously using the latest matches.
     */
    suspend fun updateSeasonStandings(): List<SeasonSimulator.PointsTableEntry> = withContext(Dispatchers.IO) {
        val slotId = _activeSlotId.value ?: return@withContext _pointsTable.value
        val matches = repository.getMatchesForSlotSync(slotId)
        _allMatches.value = matches
        val season = _activeSaveSlot.value?.currentSeason ?: 1
        val seasonMatches = matches.filter { it.season == season }
        _currentSeasonMatches.value = seasonMatches

        val teams = _leagueTeams.value.ifEmpty { repository.getTeamsForSlotSync(slotId) }
        if (teams.isNotEmpty()) {
            val table = SeasonSimulator.calculatePointsTable(teams, seasonMatches)
            _pointsTable.value = table
            return@withContext table
        }
        _pointsTable.value
    }
}
