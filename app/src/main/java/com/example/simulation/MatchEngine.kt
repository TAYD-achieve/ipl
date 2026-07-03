package com.example.simulation

import com.example.data.PlayerEntity
import com.example.data.TeamEntity
import kotlin.random.Random

object MatchEngine {

    data class BatsmanScorecard(
        val name: String,
        val runs: Int = 0,
        val balls: Int = 0,
        val fours: Int = 0,
        val sixes: Int = 0,
        val dismissal: String = "not out",
        val strikeRate: Double = 0.0
    )

    data class BowlerScorecard(
        val name: String,
        val overs: Double = 0.0,
        val runs: Int = 0,
        val wickets: Int = 0,
        val economy: Double = 0.0,
        val maidens: Int = 0
    )

    data class InningsResult(
        val totalRuns: Int,
        val totalWickets: Int,
        val totalOvers: Double,
        val batsmanScores: List<BatsmanScorecard>,
        val bowlerScores: List<BowlerScorecard>,
        val inningsCommentary: List<String>
    )

    data class TeamStrengthSummary(
        val overall: Int,
        val batting: Int,
        val bowling: Int,
        val fielding: Int,
        val fitness: Int,
        val captaincyBoost: Int
    )

    data class MatchSimulationResult(
        val teamAScoreText: String,
        val teamBScoreText: String,
        val winnerId: String,
        val commentary: String, // Semi-colon separated events
        val tossWinnerId: String,
        val tossDecision: String,
        val weather: String,
        val injuredPlayerName: String? = null,
        val injuredPlayerId: String? = null,
        val injuryDuration: Int = 0,
        val orangeCapPlayerName: String? = null,
        val purpleCapPlayerName: String? = null,
        val playerStatsUpdates: List<PlayerStatsUpdate> = emptyList(),
        val manOfTheMatch: String? = null,
        val manOfTheMatchStats: String? = null,
        val teamAStrengthRating: Int = 75,
        val teamBStrengthRating: Int = 75,
        val topBatsmanName: String? = null,
        val topBatsmanStats: String? = null,
        val topBowlerName: String? = null,
        val topBowlerStats: String? = null,
        val winMarginText: String = ""
    )

    data class PlayerStatsUpdate(
        val playerId: String,
        val runsAdded: Int,
        val ballsFaced: Int = 0,
        val wicketsAdded: Int,
        val ballsBowled: Int = 0,
        val runsConceded: Int = 0,
        val centuriesAdded: Int = 0,
        val fiveWicketsAdded: Int = 0,
        val matchesAdded: Int = 1
    )

    /**
     * Calculates the comprehensive team strength rating based on player attributes,
     * recent form, experience, fielding prowess, and leadership/captaincy boosts.
     */
    fun calculateTeamStrength(players: List<PlayerEntity>): TeamStrengthSummary {
        if (players.isEmpty()) {
            return TeamStrengthSummary(70, 70, 70, 70, 70, 0)
        }
        val sortedByBatting = players.sortedByDescending { it.batting + (it.form * 0.2).toInt() }
        val topBatters = sortedByBatting.take(7)
        val avgBatting = if (topBatters.isNotEmpty()) topBatters.map { it.batting + ((it.form - 50) / 4) }.average().toInt().coerceIn(40, 99) else 70

        val sortedByBowling = players.sortedByDescending { it.bowling + (it.form * 0.2).toInt() }
        val topBowlers = sortedByBowling.take(5)
        val avgBowling = if (topBowlers.isNotEmpty()) topBowlers.map { it.bowling + ((it.form - 50) / 4) }.average().toInt().coerceIn(40, 99) else 70

        val avgFielding = players.map { it.fielding }.average().toInt().coerceIn(40, 99)
        val avgFitness = players.map { it.fitness }.average().toInt().coerceIn(40, 99)

        // Captaincy boost from player with highest leadership
        val maxLeadership = players.maxOfOrNull { it.leadership } ?: 50
        val captaincyBoost = when {
            maxLeadership >= 90 -> 4
            maxLeadership >= 80 -> 2
            maxLeadership >= 70 -> 1
            else -> 0
        }

        val overall = ((avgBatting * 0.35) + (avgBowling * 0.35) + (avgFielding * 0.15) + (avgFitness * 0.15)).toInt().coerceIn(40, 99) + captaincyBoost

        return TeamStrengthSummary(
            overall = overall.coerceIn(40, 99),
            batting = avgBatting,
            bowling = avgBowling,
            fielding = avgFielding,
            fitness = avgFitness,
            captaincyBoost = captaincyBoost
        )
    }

    fun simulateMatch(
        teamA: TeamEntity,
        teamAPlayers: List<PlayerEntity>,
        teamB: TeamEntity,
        teamBPlayers: List<PlayerEntity>,
        weather: String = "SUNNY",
        userTeamId: String = "",
        userBattingTactic: String = "NORMAL",
        userBowlingTactic: String = "STANDARD",
        teamAStrengthOverride: TeamStrengthSummary? = null,
        teamBStrengthOverride: TeamStrengthSummary? = null
    ): MatchSimulationResult {
        // Calculate dynamic team strengths (using override from state manager if provided)
        val strengthA = teamAStrengthOverride ?: calculateTeamStrength(teamAPlayers)
        val strengthB = teamBStrengthOverride ?: calculateTeamStrength(teamBPlayers)

        // 1. Toss System
        val tossWinner = if (Random.nextBoolean()) teamA else teamB
        val tossDecision = if (Random.nextBoolean()) "BAT" else "BOWL"
        
        val firstInningsBattingTeam = if (tossWinner.teamId == teamA.teamId) {
            if (tossDecision == "BAT") teamA else teamB
        } else {
            if (tossDecision == "BAT") teamB else teamA
        }
        val firstInningsBowlingTeam = if (firstInningsBattingTeam.teamId == teamA.teamId) teamB else teamA

        val firstInningsBattingPlayers = if (firstInningsBattingTeam.teamId == teamA.teamId) teamAPlayers else teamBPlayers
        val firstInningsBowlingPlayers = if (firstInningsBowlingTeam.teamId == teamA.teamId) teamAPlayers else teamBPlayers
        val firstInningsBattingStrength = if (firstInningsBattingTeam.teamId == teamA.teamId) strengthA else strengthB
        val firstInningsBowlingStrength = if (firstInningsBowlingTeam.teamId == teamA.teamId) strengthA else strengthB

        val commentaryList = mutableListOf<String>()
        commentaryList.add("🏟️ WELCOME TO THE MATCHDAY: ${teamA.name} vs ${teamB.name} at ${teamA.stadiumName}!")
        commentaryList.add("📊 TEAM STRENGTH ANALYSIS: ${teamA.name} (OVR: ${strengthA.overall} | BAT: ${strengthA.batting} | BOWL: ${strengthA.bowling}) vs ${teamB.name} (OVR: ${strengthB.overall} | BAT: ${strengthB.batting} | BOWL: ${strengthB.bowling})")
        
        if (strengthA.captaincyBoost > 0 || strengthB.captaincyBoost > 0) {
            val capMsg = mutableListOf<String>()
            if (strengthA.captaincyBoost > 0) capMsg.add("${teamA.name} (+${strengthA.captaincyBoost})")
            if (strengthB.captaincyBoost > 0) capMsg.add("${teamB.name} (+${strengthB.captaincyBoost})")
            commentaryList.add("👑 Leadership Edge: ${capMsg.joinToString(" vs ")}")
        }
        
        commentaryList.add("🌤️ Weather Conditions: $weather. ${getWeatherImpactText(weather)}")
        commentaryList.add("🪙 THE TOSS: ${tossWinner.name} won the toss and elected to $tossDecision first!")

        // 2. Play Innings 1
        commentaryList.add("▶️ INNINGS 1 COMMENCES: ${firstInningsBattingTeam.name} batting first.")
        val innings1 = playInnings(
            battingTeam = firstInningsBattingTeam,
            battingLineup = firstInningsBattingPlayers,
            bowlingTeam = firstInningsBowlingTeam,
            bowlingLineup = firstInningsBowlingPlayers,
            target = null,
            weather = weather,
            battingStrength = firstInningsBattingStrength,
            bowlingStrength = firstInningsBowlingStrength,
            inningsNumber = 1,
            userTeamId = userTeamId,
            userBattingTactic = userBattingTactic,
            userBowlingTactic = userBowlingTactic
        )
        commentaryList.addAll(innings1.inningsCommentary)
        commentaryList.add("🏁 END OF INNINGS 1: ${firstInningsBattingTeam.name} posts ${innings1.totalRuns}/${innings1.totalWickets} in ${innings1.totalOvers} overs (RR: ${String.format("%.2f", innings1.totalRuns / innings1.totalOvers.coerceAtLeast(1.0))}).")

        // 3. Play Innings 2
        val target = innings1.totalRuns + 1
        val secondInningsBattingTeam = firstInningsBowlingTeam
        val secondInningsBowlingTeam = firstInningsBattingTeam
        val secondInningsBattingPlayers = firstInningsBowlingPlayers
        val secondInningsBowlingPlayers = firstInningsBattingPlayers
        val secondInningsBattingStrength = firstInningsBowlingStrength
        val secondInningsBowlingStrength = firstInningsBattingStrength

        commentaryList.add("▶️ INNINGS 2 COMMENCES: ${secondInningsBattingTeam.name} needs $target runs to win off 20 overs (Req RR: ${String.format("%.2f", target / 20.0)}).")
        val innings2 = playInnings(
            battingTeam = secondInningsBattingTeam,
            battingLineup = secondInningsBattingPlayers,
            bowlingTeam = secondInningsBowlingTeam,
            bowlingLineup = secondInningsBowlingPlayers,
            target = target,
            weather = weather,
            battingStrength = secondInningsBattingStrength,
            bowlingStrength = secondInningsBowlingStrength,
            inningsNumber = 2,
            userTeamId = userTeamId,
            userBattingTactic = userBattingTactic,
            userBowlingTactic = userBowlingTactic
        )
        commentaryList.addAll(innings2.inningsCommentary)
        commentaryList.add("🏁 END OF INNINGS 2: ${secondInningsBattingTeam.name} finishes at ${innings2.totalRuns}/${innings2.totalWickets} in ${innings2.totalOvers} overs.")

        // Determine winner
        val winnerId = if (innings2.totalRuns >= target) {
            secondInningsBattingTeam.teamId
        } else if (innings2.totalRuns == innings1.totalRuns) {
            // Super over / Tie-breaker -> higher team strength or random
            if (strengthA.overall > strengthB.overall) teamA.teamId else teamB.teamId
        } else {
            firstInningsBattingTeam.teamId
        }

        val winnerName = if (winnerId == teamA.teamId) teamA.name else teamB.name
        val winMarginText = if (innings2.totalRuns >= target) {
            "won by ${10 - innings2.totalWickets} wickets!"
        } else if (innings2.totalRuns == innings1.totalRuns) {
            "won in a thrilling Super Over Tie-Breaker!"
        } else {
            "won by ${innings1.totalRuns - innings2.totalRuns} runs!"
        }
        commentaryList.add("🏆 MATCH CONCLUDED: $winnerName $winMarginText")

        // 4. Man of the Match (MVP) & Cap Holders Calculation
        val allPlayers = teamAPlayers + teamBPlayers
        val mvpList = allPlayers.distinctBy { it.playerId }.mapNotNull { p ->
            val bScore = innings1.batsmanScores.find { it.name == p.name } ?: innings2.batsmanScores.find { it.name == p.name }
            val bowlScore = innings1.bowlerScores.find { it.name == p.name } ?: innings2.bowlerScores.find { it.name == p.name }
            
            val runs = bScore?.runs ?: 0
            val balls = bScore?.balls ?: 0
            val fours = bScore?.fours ?: 0
            val sixes = bScore?.sixes ?: 0
            val wkt = bowlScore?.wickets ?: 0
            val econ = bowlScore?.economy ?: 0.0
            val overs = bowlScore?.overs ?: 0.0
            
            if (runs == 0 && balls == 0 && wkt == 0 && overs == 0.0) return@mapNotNull null
            
            var pts = runs * 1.3 + (fours * 1.0) + (sixes * 2.5)
            if (runs >= 30) pts += 10.0
            if (runs >= 50) pts += 25.0
            if (runs >= 100) pts += 50.0
            if (balls > 10 && (runs.toDouble() / balls) >= 1.6) pts += 15.0
            
            pts += wkt * 28.0
            if (wkt >= 3) pts += 20.0
            if (wkt >= 5) pts += 45.0
            if (overs >= 2.0 && econ < 6.5) pts += 15.0
            
            // Winning team bonus
            val pTeamId = if (teamAPlayers.any { it.playerId == p.playerId }) teamA.teamId else teamB.teamId
            if (pTeamId == winnerId) pts += 15.0
            
            val summaryParts = mutableListOf<String>()
            if (runs > 0 || balls > 0) summaryParts.add("$runs off ${balls}b (${fours}x4, ${sixes}x6)")
            if (wkt > 0 || overs > 0) summaryParts.add("$wkt/${bowlScore?.runs ?: 0} (${overs}v)")
            val summary = summaryParts.joinToString(", ").ifEmpty { "Great contribution" }
            
            Triple(p, pts, summary)
        }.sortedByDescending { it.second }

        val topMvp = mvpList.firstOrNull()
        val mvpName = topMvp?.first?.name
        val mvpStats = topMvp?.third
        if (mvpName != null) {
            commentaryList.add("🌟 MAN OF THE MATCH: $mvpName ($mvpStats) awarded for stellar performance!")
        }

        // Top Batsman & Bowler of the match
        val allBattingScores = innings1.batsmanScores + innings2.batsmanScores
        val bestBat = allBattingScores.maxByOrNull { it.runs }
        val topBatStats = bestBat?.let { "${it.runs} off ${it.balls}b (${it.fours}x4, ${it.sixes}x6)" }

        val allBowlingScores = innings1.bowlerScores + innings2.bowlerScores
        val bestBowl = allBowlingScores.maxByOrNull { it.wickets * 100 - it.runs }
        val topBowlStats = bestBowl?.let { "${it.wickets}/${it.runs} in ${it.overs} overs (Econ: ${String.format("%.2f", it.economy)})" }

        // 5. Injury simulation
        var injuredName: String? = null
        var injuredId: String? = null
        var injuryDur = 0
        if (Random.nextFloat() < 0.07f) { // 7% chance of injury
            val selected = allPlayers.random()
            injuredName = selected.name
            injuredId = selected.playerId
            injuryDur = Random.nextInt(1, 4) // 1-3 matches recovery
            commentaryList.add("⚠️ INJURY UPDATE: $injuredName suffered an injury during play and will miss $injuryDur match(es).")
        }

        // Construct team score texts
        val scoreAText = if (firstInningsBattingTeam.teamId == teamA.teamId) {
            "${innings1.totalRuns}/${innings1.totalWickets} (${innings1.totalOvers})"
        } else {
            "${innings2.totalRuns}/${innings2.totalWickets} (${innings2.totalOvers})"
        }

        val scoreBText = if (firstInningsBattingTeam.teamId == teamB.teamId) {
            "${innings1.totalRuns}/${innings1.totalWickets} (${innings1.totalOvers})"
        } else {
            "${innings2.totalRuns}/${innings2.totalWickets} (${innings2.totalOvers})"
        }

        // 6. Accumulate detailed player statistics updates
        val statsUpdates = mutableListOf<PlayerStatsUpdate>()
        allPlayers.forEach { p ->
            val bScore = innings1.batsmanScores.find { it.name == p.name } ?: innings2.batsmanScores.find { it.name == p.name }
            val bowlScore = innings1.bowlerScores.find { it.name == p.name } ?: innings2.bowlerScores.find { it.name == p.name }
            
            val runs = bScore?.runs ?: 0
            val ballsFaced = bScore?.balls ?: 0
            val wickets = bowlScore?.wickets ?: 0
            val runsConceded = bowlScore?.runs ?: 0
            val oversBowled = bowlScore?.overs ?: 0.0
            val ballsBowled = (oversBowled.toInt() * 6) + ((oversBowled - oversBowled.toInt()) * 10).roundToNearestInt()
            val centuries = if (runs >= 100) 1 else 0
            val fiveWk = if (wickets >= 5) 1 else 0

            statsUpdates.add(
                PlayerStatsUpdate(
                    playerId = p.playerId,
                    runsAdded = runs,
                    ballsFaced = ballsFaced,
                    wicketsAdded = wickets,
                    ballsBowled = ballsBowled,
                    runsConceded = runsConceded,
                    centuriesAdded = centuries,
                    fiveWicketsAdded = fiveWk,
                    matchesAdded = 1
                )
            )
        }

        return MatchSimulationResult(
            teamAScoreText = scoreAText,
            teamBScoreText = scoreBText,
            winnerId = winnerId,
            commentary = commentaryList.joinToString(";"),
            tossWinnerId = tossWinner.teamId,
            tossDecision = tossDecision,
            weather = weather,
            injuredPlayerName = injuredName,
            injuredPlayerId = injuredId,
            injuryDuration = injuryDur,
            orangeCapPlayerName = bestBat?.name,
            purpleCapPlayerName = bestBowl?.name,
            playerStatsUpdates = statsUpdates,
            manOfTheMatch = mvpName,
            manOfTheMatchStats = mvpStats,
            teamAStrengthRating = strengthA.overall,
            teamBStrengthRating = strengthB.overall,
            topBatsmanName = bestBat?.name,
            topBatsmanStats = topBatStats,
            topBowlerName = bestBowl?.name,
            topBowlerStats = topBowlStats,
            winMarginText = winMarginText
        )
    }

    private fun playInnings(
        battingTeam: TeamEntity,
        battingLineup: List<PlayerEntity>,
        bowlingTeam: TeamEntity,
        bowlingLineup: List<PlayerEntity>,
        target: Int?,
        weather: String,
        battingStrength: TeamStrengthSummary,
        bowlingStrength: TeamStrengthSummary,
        inningsNumber: Int,
        userTeamId: String = "",
        userBattingTactic: String = "NORMAL",
        userBowlingTactic: String = "STANDARD"
    ): InningsResult {
        val batsmenScorecard = battingLineup.take(11).map { BatsmanScorecard(it.name) }.toMutableList()
        val bowlersScorecard = bowlingLineup.filter { it.category == "BOWLER" || it.category == "ALL_ROUNDER" }
            .take(6).map { BowlerScorecard(it.name) }.toMutableList()

        if (bowlersScorecard.isEmpty()) {
            bowlersScorecard.addAll(bowlingLineup.take(5).map { BowlerScorecard(it.name) })
        }

        val commentaryEvents = mutableListOf<String>()
        var runs = 0
        var wickets = 0
        var balls = 0
        
        var strikerIdx = 0
        var nonStrikerIdx = 1

        val oversLimit = 20
        val ballsLimit = oversLimit * 6

        // Weather impact on scoring and bowling swing
        val weatherMultiplier = when (weather) {
            "SUNNY" -> 1.05  // Batsmen paradise, fast outfield (+5% batting boost)
            "OVERCAST" -> 0.93 // Swing and seam movement for bowlers (-7% batting, pacer advantage)
            "RAIN" -> 0.86 // Heavy damp ground, slower outfield (-14% batting, grip for bowlers)
            "DEW" -> 1.08 // Wet ball favors batsmen, hard for spinners to grip (+8% batting boost)
            "WINDY" -> 0.95 // Crosswinds create significant drift for spinners and deceptive flight (-5% batting)
            else -> 1.0
        }

        var currentPartnershipRuns = 0
        var currentPartnershipBalls = 0
        var lastPartnershipMilestone = 0

        while (balls < ballsLimit && wickets < 10) {
            if (target != null && runs >= target) {
                break
            }

            val overNum = balls / 6
            val ballInOver = (balls % 6) + 1
            val isPowerplay = overNum < 6
            val isDeathOvers = overNum >= 16

            val bowlerIndex = overNum % bowlersScorecard.size
            val currentBowler = bowlersScorecard[bowlerIndex]
            val bowlerPlayer = bowlingLineup.find { it.name == currentBowler.name }
            val strikerPlayer = battingLineup.getOrNull(strikerIdx) ?: break
            val nonStrikerPlayer = battingLineup.getOrNull(nonStrikerIdx) ?: strikerPlayer

            val sScore = batsmenScorecard[strikerIdx]
            val bScore = bowlersScorecard[bowlerIndex]

            // Calculate effective batsman skill (with form, morale, and fatigue penalties)
            val baseBatting = strikerPlayer.batting.coerceIn(10, 100)
            val batFormBonus = ((strikerPlayer.form - 50) * 0.2).toInt()
            val batMoraleBonus = ((strikerPlayer.morale - 50) * 0.1).toInt()
            val batFatigue = if (sScore.balls > 25 && strikerPlayer.fitness < 75) ((sScore.balls - 25) / 10) * 2 else 0
            val effBat = (baseBatting + batFormBonus + batMoraleBonus - batFatigue + battingStrength.captaincyBoost).coerceIn(15, 100)

            // Calculate effective bowler skill
            val baseBowling = (bowlerPlayer?.bowling ?: 60).coerceIn(10, 100)
            val bowlFormBonus = (((bowlerPlayer?.form ?: 50) - 50) * 0.2).toInt()
            val bowlFatigue = if (bScore.overs >= 3.0 && (bowlerPlayer?.fitness ?: 80) < 75) 3 else 0
            val effBowl = (baseBowling + bowlFormBonus - bowlFatigue + bowlingStrength.captaincyBoost).coerceIn(15, 100)

            val skillDiff = effBat - effBowl
            val phaseMultiplier = when {
                isPowerplay -> 1.08 // Field restrictions encourage aggressive shots
                isDeathOvers -> 1.15 // All-out attack
                else -> 0.95 // Steady middle overs rotation
            }

            val isUserBatting = (battingTeam.teamId == userTeamId && userTeamId.isNotEmpty())
            val isUserBowling = (bowlingTeam.teamId == userTeamId && userTeamId.isNotEmpty())
            val batTactic = if (isUserBatting) userBattingTactic else "NORMAL"
            val bowlTactic = if (isUserBowling) userBowlingTactic else "STANDARD"

            val baseRunChance = (0.58 + (skillDiff * 0.0035)) * phaseMultiplier * weatherMultiplier
            val rand = Random.nextDouble()
            val ballRuns: Int
            var wicketFallen = false

            if (rand > baseRunChance) {
                // Bowler wins ball -> Dot ball or Wicket opportunity
                val baseWicketChance = if (isDeathOvers) 0.14 else if (isPowerplay) 0.08 else 0.06
                var adjustedWicketChance = baseWicketChance + ((effBowl - effBat) * 0.0015).coerceIn(-0.02, 0.06)

                // Apply tactics to wicket chance
                if (batTactic == "AGGRESSIVE") adjustedWicketChance *= 1.30
                else if (batTactic == "DEFENSIVE") adjustedWicketChance *= 0.65

                if (bowlTactic == "ALL_OUT_ATTACK") adjustedWicketChance *= 1.30
                else if (bowlTactic == "CONTAINMENT") adjustedWicketChance *= 0.70

                if (Random.nextDouble() < adjustedWicketChance) {
                    // Check if it's a catching opportunity where fielding rating matters (~65% of wickets)
                    if (Random.nextDouble() < 0.65) {
                        val teamFielding = bowlingStrength.fielding
                        if (Random.nextInt(1, 100) > (teamFielding + 18)) {
                            // DROPPED CATCH DUE TO FIELDING RATING!
                            ballRuns = if (Random.nextBoolean()) 1 else 2
                            val fielderName = bowlingLineup.randomOrNull()?.name ?: "Fielder"
                            commentaryEvents.add("⚠️ OVER ${overNum+1}.$ballInOver: DROPPED! $fielderName drops a tough chance off ${strikerPlayer.name}! They scramble for $ballRuns.")
                        } else {
                            wicketFallen = true
                            ballRuns = 0
                        }
                    } else {
                        wicketFallen = true
                        ballRuns = 0
                    }
                } else {
                    // Dot ball or 1 single
                    ballRuns = if (Random.nextDouble() < 0.3) 1 else 0
                }
            } else {
                // Batsman wins ball -> Scoring runs
                val runsRand = Random.nextDouble()
                var boundaryBoost = if (isDeathOvers) 0.12 else if (isPowerplay) 0.08 else 0.0

                // Apply tactics to boundary scoring
                if (batTactic == "AGGRESSIVE") boundaryBoost += 0.15
                else if (batTactic == "DEFENSIVE") boundaryBoost -= 0.08

                if (bowlTactic == "ALL_OUT_ATTACK") boundaryBoost += 0.12
                else if (bowlTactic == "CONTAINMENT") boundaryBoost -= 0.12

                ballRuns = when {
                    runsRand < (0.16 + boundaryBoost) -> 4
                    runsRand < (0.24 + boundaryBoost) -> 6
                    runsRand < 0.62 -> 1
                    runsRand < 0.88 -> 2
                    else -> 0
                }
            }

            // Update match counts
            balls++
            runs += ballRuns
            currentPartnershipRuns += ballRuns
            currentPartnershipBalls++

            val oldRuns = sScore.runs
            val newRuns = oldRuns + ballRuns
            val newBalls = sScore.balls + 1
            val newFours = sScore.fours + if (ballRuns == 4) 1 else 0
            val newSixes = sScore.sixes + if (ballRuns == 6) 1 else 0
            val newSR = if (newBalls > 0) (newRuns.toDouble() / newBalls) * 100.0 else 0.0

            batsmenScorecard[strikerIdx] = sScore.copy(
                runs = newRuns,
                balls = newBalls,
                fours = newFours,
                sixes = newSixes,
                strikeRate = Math.round(newSR * 10.0) / 10.0
            )

            val oldBowlRuns = bScore.runs
            val newBowlRuns = oldBowlRuns + ballRuns
            val newOvers = bScore.overs + 0.16667
            bowlersScorecard[bowlerIndex] = bScore.copy(
                runs = newBowlRuns,
                overs = newOvers
            )

            // Special Commentary & Milestones
            if (ballRuns == 6) {
                if (batTactic == "AGGRESSIVE" && Random.nextDouble() < 0.6) {
                    commentaryEvents.add("🚀 AGGRESSIVE TACTIC: ${strikerPlayer.name} takes the aerial route with high risk, smashing a massive SIX off ${bowlerPlayer?.name ?: "the bowler"}!")
                } else if (Random.nextDouble() < 0.5) {
                    commentaryEvents.add("🔥 OVER ${overNum+1}.$ballInOver: SIX! ${strikerPlayer.name} launches ${bowlerPlayer?.name ?: "the bowler"} deep into the stands!")
                }
            } else if (ballRuns == 4) {
                if (batTactic == "AGGRESSIVE" && Random.nextDouble() < 0.5) {
                    commentaryEvents.add("⚡ AGGRESSIVE TACTIC: Fearless boundary! ${strikerPlayer.name} pierces the gap with brutal power!")
                } else if (Random.nextDouble() < 0.4) {
                    commentaryEvents.add("⚡ OVER ${overNum+1}.$ballInOver: FOUR! Cracking drive by ${strikerPlayer.name} piercing the gap!")
                }
            } else if (ballRuns == 0 && bowlTactic == "CONTAINMENT" && Random.nextDouble() < 0.15) {
                commentaryEvents.add("🎯 CONTAINMENT TACTIC: Pinpoint yorker by ${bowlerPlayer?.name ?: "the bowler"} restricts ${strikerPlayer.name} to a dot ball!")
            } else if ((ballRuns == 1 || ballRuns == 2) && batTactic == "DEFENSIVE" && Random.nextDouble() < 0.15) {
                commentaryEvents.add("🛡️ DEFENSIVE TACTIC: Solid anchor play by ${strikerPlayer.name}, softly rotating the strike with zero risk.")
            }

            // Individual Batting Milestones
            if (oldRuns < 50 && newRuns >= 50) {
                commentaryEvents.add("🏏 OVER ${overNum+1}.$ballInOver: HALF-CENTURY! ${strikerPlayer.name} reaches a brilliant 50 off $newBalls balls!")
            } else if (oldRuns < 100 && newRuns >= 100) {
                commentaryEvents.add("💯 OVER ${overNum+1}.$ballInOver: CENTURY! Sensational 100 by ${strikerPlayer.name}! The stadium erupts!")
            }

            // Partnership Milestones
            if (currentPartnershipRuns >= lastPartnershipMilestone + 50) {
                lastPartnershipMilestone += 50
                commentaryEvents.add("🤝 OVER ${overNum+1}.$ballInOver: ${lastPartnershipMilestone}-run partnership between ${strikerPlayer.name} and ${nonStrikerPlayer.name}!")
            }

            if (wicketFallen) {
                wickets++
                val disBowler = bowlerPlayer?.name ?: "Bowler"
                val disType = listOf("c. Fielder b. $disBowler", "b. $disBowler", "lbw b. $disBowler").random()
                batsmenScorecard[strikerIdx] = batsmenScorecard[strikerIdx].copy(
                    dismissal = disType
                )
                
                val newWkt = bScore.wickets + 1
                bowlersScorecard[bowlerIndex] = bowlersScorecard[bowlerIndex].copy(
                    wickets = newWkt
                )

                if (bowlTactic == "ALL_OUT_ATTACK" && Random.nextDouble() < 0.5) {
                    commentaryEvents.add("💥 ALL-OUT ATTACK: Aggressive field placement pays dividends! WICKET! ${strikerPlayer.name} departs for $oldRuns ($newBalls b)! $disType. Total: $runs/$wickets")
                } else {
                    commentaryEvents.add("🔴 OVER ${overNum+1}.$ballInOver: WICKET! ${strikerPlayer.name} departs for $oldRuns ($newBalls b)! $disType. Total: $runs/$wickets")
                }

                if (newWkt == 3) {
                    commentaryEvents.add("🎯 OVER ${overNum+1}.$ballInOver: 3-WICKET HAUL for ${bowlerPlayer?.name ?: "the bowler"}!")
                } else if (newWkt == 5) {
                    commentaryEvents.add("🌟 OVER ${overNum+1}.$ballInOver: 5-WICKET HAUL! Outstanding bowling display by ${bowlerPlayer?.name ?: "the bowler"}!")
                }

                // New batsman comes in, reset partnership
                currentPartnershipRuns = 0
                currentPartnershipBalls = 0
                lastPartnershipMilestone = 0
                strikerIdx = maxOf(strikerIdx, nonStrikerIdx) + 1
            } else {
                // Rotate strike on odd runs
                if (ballRuns % 2 != 0) {
                    val temp = strikerIdx
                    strikerIdx = nonStrikerIdx
                    nonStrikerIdx = temp
                }
            }

            // End of over: rotate strike
            if (balls % 6 == 0) {
                val temp = strikerIdx
                strikerIdx = nonStrikerIdx
                nonStrikerIdx = temp
            }
        }

        // Format overs and calculate economies
        val finalOvers = (balls / 6) + (balls % 6) * 0.1
        val finalBowlers = bowlersScorecard.map { b ->
            val oversInt = b.overs.toInt()
            val ballsRemaining = ((b.overs - oversInt) * 6).roundToNearestInt()
            val oversFormatted = oversInt + (ballsRemaining * 0.1)
            val economy = if (b.overs > 0) (b.runs / (oversInt + (ballsRemaining / 6.0))) else 0.0
            val economyRounded = Math.round(economy * 100.0) / 100.0
            b.copy(overs = oversFormatted, economy = economyRounded)
        }

        return InningsResult(
            totalRuns = runs,
            totalWickets = wickets,
            totalOvers = finalOvers,
            batsmanScores = batsmenScorecard,
            bowlerScores = finalBowlers,
            inningsCommentary = commentaryEvents
        )
    }

    private fun getWeatherImpactText(weather: String): String {
        return when (weather) {
            "SUNNY" -> "Fast outfield, great conditions for aggressive batting (+5% batting boost)."
            "OVERCAST" -> "Swing and seam movement expected for seam bowlers (-7% batting, pacer advantage)."
            "RAIN" -> "Damp outfield will make scoring boundaries tougher (-14% batting, grip for bowlers)."
            "DEW" -> "Heavy evening dew makes gripping the ball difficult for spinners (+8% batting advantage)."
            "WINDY" -> "Crosswinds creating significant drift for spinners and deceptive flight (-5% batting)."
            else -> "Balanced pitch for both bat and ball."
        }
    }

    private fun Double.roundToNearestInt(): Int {
        return (this + 0.5).toInt()
    }
}

