package com.example.simulation

import com.example.data.MatchEntity
import com.example.data.TeamEntity

object SeasonSimulator {

    data class PointsTableEntry(
        val teamId: String,
        val teamName: String,
        val colorHex: String,
        val played: Int = 0,
        val won: Int = 0,
        val lost: Int = 0,
        val points: Int = 0,
        val nrr: Double = 0.0
    )

    /**
     * Generates a Round Robin schedule (14 rounds, home and away) for 8 teams.
     */
    fun generateSchedule(slotId: String, teams: List<TeamEntity>, season: Int = 1): List<MatchEntity> {
        val totalTeams = teams.size
        if (totalTeams != 8) return emptyList()

        val matches = mutableListOf<MatchEntity>()
        val teamList = teams.toMutableList()

        // Round Robin Scheduling (Circle Method)
        for (round in 1..7) {
            for (i in 0 until 4) {
                val home = teamList[i]
                val away = teamList[totalTeams - 1 - i]
                
                // Round 1 to 7
                matches.add(
                    MatchEntity(
                        matchId = "${slotId}_S${season}_R${round}_M${i+1}",
                        slotId = slotId,
                        season = season,
                        matchDay = round,
                        teamAId = home.teamId,
                        teamBId = away.teamId,
                        weather = listOf("SUNNY", "SUNNY", "OVERCAST", "RAIN").random()
                    )
                )

                // Round 8 to 14 (Reverse Fixture)
                matches.add(
                    MatchEntity(
                        matchId = "${slotId}_S${season}_R${round+7}_M${i+1}",
                        slotId = slotId,
                        season = season,
                        matchDay = round + 7,
                        teamAId = away.teamId,
                        teamBId = home.teamId,
                        weather = listOf("SUNNY", "SUNNY", "OVERCAST", "RAIN").random()
                    )
                )
            }
            // Rotate teamList (keep first element fixed)
            val temp = teamList[1]
            for (k in 1 until totalTeams - 1) {
                teamList[k] = teamList[k + 1]
            }
            teamList[totalTeams - 1] = temp
        }

        // Sort by matchDay
        return matches.sortedBy { it.matchDay }
    }

    /**
     * Calculates the points table based on match entities.
     */
    fun calculatePointsTable(teams: List<TeamEntity>, matches: List<MatchEntity>): List<PointsTableEntry> {
        val entries = teams.map { 
            PointsTableEntry(
                teamId = it.teamId,
                teamName = it.name,
                colorHex = it.logoBgColor
            )
        }.associateBy { it.teamId }.toMutableMap()

        val groupStageMatches = matches.filter { !it.isPlayoff && it.isPlayed }

        for (match in groupStageMatches) {
            val entryA = entries[match.teamAId] ?: continue
            val entryB = entries[match.teamBId] ?: continue

            // Parse scores to estimate NRR
            val runsA = parseRuns(match.teamAScore)
            val oversA = parseOvers(match.teamAScore)
            val runsB = parseRuns(match.teamBScore)
            val oversB = parseOvers(match.teamBScore)

            val winner = match.winnerId

            val isWinnerA = winner == match.teamAId
            val isWinnerB = winner == match.teamBId

            entries[match.teamAId] = entryA.copy(
                played = entryA.played + 1,
                won = entryA.won + if (isWinnerA) 1 else 0,
                lost = entryA.lost + if (isWinnerB) 1 else 0,
                points = entryA.points + if (isWinnerA) 2 else 0,
                // Simplified NRR approximation: run rate scored - run rate conceded
                nrr = entryA.nrr + calculateNrrDelta(runsA, oversA, runsB, oversB)
            )

            entries[match.teamBId] = entryB.copy(
                played = entryB.played + 1,
                won = entryB.won + if (isWinnerB) 1 else 0,
                lost = entryB.lost + if (isWinnerA) 1 else 0,
                points = entryB.points + if (isWinnerB) 2 else 0,
                nrr = entryB.nrr + calculateNrrDelta(runsB, oversB, runsA, oversA)
            )
        }

        return entries.values.sortedWith(
            compareByDescending<PointsTableEntry> { it.points }
                .thenByDescending { it.nrr }
        )
    }

    private fun calculateNrrDelta(runsScored: Int, oversFaced: Double, runsConceded: Int, oversBowled: Double): Double {
        if (oversFaced == 0.0 || oversBowled == 0.0) return 0.0
        val rrScored = runsScored / oversFaced
        val rrConceded = runsConceded / oversBowled
        return (rrScored - rrConceded).coerceIn(-4.0, 4.0) * 0.1 // Scaled to look realistic (e.g. +0.450 NRR)
    }

    private fun parseRuns(scoreText: String): Int {
        // e.g. "182/4 (20.0)" -> 182
        val parts = scoreText.split("/")
        return parts.firstOrNull()?.trim()?.toIntOrNull() ?: 0
    }

    private fun parseOvers(scoreText: String): Double {
        // e.g. "182/4 (19.4)" -> 19.6667 overs
        val regex = "\\(([^)]+)\\)".toRegex()
        val matchResult = regex.find(scoreText)
        val oversStr = matchResult?.groups?.get(1)?.value ?: "20.0"
        val oversDouble = oversStr.toDoubleOrNull() ?: 20.0
        val oversInt = oversDouble.toInt()
        val balls = ((oversDouble - oversInt) * 10).toInt()
        return oversInt + (balls / 6.0)
    }
}
