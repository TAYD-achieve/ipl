package com.example

import com.example.data.PlayerEntity
import com.example.data.TeamEntity
import com.example.simulation.MatchEngine
import org.junit.Assert.*
import org.junit.Test

class MatchEngineTest {

    private fun createDummyPlayer(id: String, teamId: String, name: String, cat: String, bat: Int, bowl: Int): PlayerEntity {
        return PlayerEntity(
            playerId = id,
            slotId = "slot1",
            teamId = teamId,
            name = name,
            category = cat,
            batting = bat,
            bowling = bowl,
            fielding = 80,
            fitness = 85,
            experience = 70,
            form = 75,
            leadership = 60,
            morale = 80,
            salary = 1000000L,
            contractDuration = 3,
            age = 26,
            nationality = "India"
        )
    }

    private fun createDummyTeam(id: String, name: String, shortName: String, city: String): TeamEntity {
        return TeamEntity(
            teamId = id,
            slotId = "slot1",
            name = name,
            city = city,
            logoText = shortName,
            logoBgColor = "#000000",
            logoTextColor = "#FFFFFF",
            logoShape = "CIRCLE",
            jerseyColorPrimary = "#FFFF00",
            jerseyColorSecondary = "#0000FF",
            jerseyNumber = "7",
            jerseyPlayerName = "PLAYER",
            jerseyTemplate = 1,
            slogan = "Win",
            stadiumName = "$city Stadium",
            stadiumCapacity = 40000,
            stadiumVipStands = 1,
            stadiumParking = 1,
            stadiumFoodCourts = 1,
            stadiumLighting = 1,
            stadiumTrainingGrounds = 1,
            stadiumMuseum = 1,
            stadiumMerchShop = 1,
            balance = 1000000000L,
            reputation = 75,
            followers = 1000000L,
            fanHappiness = 80,
            isAi = false,
            aiShortName = shortName
        )
    }

    @Test
    fun testTeamStrengthCalculation() {
        val players = listOf(
            createDummyPlayer("1", "team1", "V. Kohli", "BATSMAN", 92, 20),
            createDummyPlayer("2", "team1", "J. Bumrah", "BOWLER", 25, 95)
        )
        val strength = MatchEngine.calculateTeamStrength(players)
        assertTrue("Overall strength should be reasonable", strength.overall in 40..99)
        assertTrue("Batting rating should be calculated", strength.batting > 0)
        assertTrue("Bowling rating should be calculated", strength.bowling > 0)
    }

    @Test
    fun testMatchSimulationRunsSuccessfully() {
        val teamA = createDummyTeam("teamA", "Chennai Super Kings", "CSK", "Chennai")
        val teamB = createDummyTeam("teamB", "Mumbai Indians", "MI", "Mumbai")

        val playersA = (1..11).map { i ->
            createDummyPlayer("a$i", "teamA", "CSK Player $i", if (i <= 6) "BATSMAN" else "BOWLER", if (i <= 6) 80 else 30, if (i > 6) 80 else 20)
        }
        val playersB = (1..11).map { i ->
            createDummyPlayer("b$i", "teamB", "MI Player $i", if (i <= 6) "BATSMAN" else "BOWLER", if (i <= 6) 82 else 25, if (i > 6) 82 else 25)
        }

        val result = MatchEngine.simulateMatch(teamA, playersA, teamB, playersB, "SUNNY")
        assertNotNull("Winner should be determined", result.winnerId)
        assertTrue("Winner should be either Team A or Team B", result.winnerId == "teamA" || result.winnerId == "teamB")
        assertTrue("Commentary should not be empty", result.commentary.isNotEmpty())
        assertNotNull("Man of the Match should be awarded", result.manOfTheMatch)
        assertEquals("Player stats updates should exist for all 22 players", 22, result.playerStatsUpdates.size)
        assertTrue("Score text should contain overs", result.teamAScoreText.contains("("))
        assertTrue("Score text should contain overs", result.teamBScoreText.contains("("))
    }
}
