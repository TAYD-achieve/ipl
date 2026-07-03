package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MatchEntity
import com.example.data.TeamEntity
import com.example.viewmodel.GameViewModel

@Composable
fun ScheduleScreen(viewModel: GameViewModel) {
    // 1. Reactive State from State Management Provider (GameProgressStore)
    val allMatchesFromStore by viewModel.progressStore.allMatches.collectAsState()
    val currentSeason by viewModel.progressStore.currentSeason.collectAsState()
    val team = viewModel.activeTeam
    val allTeams = viewModel.allTeams

    // Fallback if store matches are still initializing
    val matchesList = if (allMatchesFromStore.isNotEmpty()) allMatchesFromStore else viewModel.matches
    val seasonMatches = matchesList.filter { it.season == currentSeason }.ifEmpty { matchesList }

    // User fixtures and statistics
    val userFixtures = seasonMatches.filter { it.teamAId == team?.teamId || it.teamBId == team?.teamId }.sortedBy { it.matchDay }
    val playedCount = userFixtures.count { it.isPlayed }
    val wonCount = userFixtures.count { it.winnerId == team?.teamId }
    val lostCount = playedCount - wonCount
    val nextUserMatch = userFixtures.firstOrNull { !it.isPlayed }

    var activeTab by remember { mutableStateOf(0) } // 0: My Fixtures, 1: All Rounds, 2: Playoffs Bracket
    var fixtureFilter by remember { mutableStateOf("ALL") } // ALL, UPCOMING, COMPLETED, HOME, AWAY
    var selectedRound by remember { mutableStateOf(nextUserMatch?.matchDay ?: 1) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // --- 1. HEADER BANNER: SEASON SCHEDULE & RECORD ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
            border = BorderStroke(1.dp, Color(0xFF6750A4)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📅 IPL SEASON SCHEDULE & FIXTURES",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6750A4),
                        fontSize = 11.sp
                    )
                    Text(
                        "${team?.name ?: "My Team"} • Season $currentSeason",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScheduleStatItem(
                        title = "League Progress",
                        value = "$playedCount / ${userFixtures.size} M",
                        subtitle = if (playedCount == userFixtures.size) "🏁 League Stage Complete" else "Matchday ${nextUserMatch?.matchDay ?: playedCount + 1}",
                        color = Color(0xFF6750A4),
                        modifier = Modifier.weight(1f)
                    )
                    ScheduleStatItem(
                        title = "Season Record",
                        value = "${wonCount}W - ${lostCount}L",
                        subtitle = "Win Rate: ${if (playedCount > 0) (wonCount * 100 / playedCount) else 0}%",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    ScheduleStatItem(
                        title = "Next Opponent",
                        value = nextUserMatch?.let { m ->
                            val oppId = if (m.teamAId == team?.teamId) m.teamBId else m.teamAId
                            allTeams.find { it.teamId == oppId }?.name?.split(" ")?.firstOrNull() ?: "AI"
                        } ?: "None",
                        subtitle = nextUserMatch?.let { m -> if (m.teamAId == team?.teamId) "📍 Home Venue" else "📍 Away Venue" } ?: "Season Ended",
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 2. MAIN TABS ---
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF6750A4),
            indicator = {},
            divider = {},
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            val tabTitles = listOf("MY FIXTURES (${userFixtures.size})", "ALL ROUNDS (${seasonMatches.size})", "PLAYOFFS BRACKET", "LEAGUE STANDINGS")
            tabTitles.forEachIndexed { index, title ->
                val isSelected = activeTab == index
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clickable { activeTab = index },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF6750A4) else Color(0xFFEADDFF),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0))
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.White else Color(0xFF21005D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- SIMULATION ENGINE CONTROLS ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.simulateUpToNextUserMatch() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Text("⚡ SIM TO NEXT MATCH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.simulateScheduledRound(selectedRound) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Text("⚡ SIM ROUND $selectedRound", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- 3. TAB CONTENT ---
        when (activeTab) {
            0 -> MyFixturesTab(
                userFixtures = userFixtures,
                nextUserMatch = nextUserMatch,
                team = team,
                allTeams = allTeams,
                fixtureFilter = fixtureFilter,
                onFilterChange = { fixtureFilter = it },
                onPlayMatch = { match ->
                    viewModel.simulateRemainingMatchesForDay(match.matchDay)
                    viewModel.prepareMatchDay(match)
                },
                onQuickSimMatch = { match ->
                    viewModel.simulateScheduledGame(match)
                }
            )
            1 -> AllRoundsTab(
                seasonMatches = seasonMatches,
                selectedRound = selectedRound,
                onRoundChange = { selectedRound = it },
                userTeamId = team?.teamId,
                allTeams = allTeams,
                onSimulateGame = { match ->
                    viewModel.simulateScheduledGame(match)
                },
                onSimulateRound = { round ->
                    viewModel.simulateScheduledRound(round)
                }
            )
            2 -> PlayoffsBracketTab(
                seasonMatches = seasonMatches,
                allTeams = allTeams,
                userTeamId = team?.teamId,
                pointsTable = viewModel.pointsTable,
                onPlayMatch = { match ->
                    viewModel.prepareMatchDay(match)
                }
            )
            3 -> LeagueStandingsTab(
                pointsTable = viewModel.pointsTable,
                userTeamId = team?.teamId,
                onSimulateUpToNext = { viewModel.simulateUpToNextUserMatch() }
            )
        }
    }
}

@Composable
fun ScheduleStatItem(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, fontSize = 9.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = color, modifier = Modifier.padding(vertical = 2.dp))
            Text(subtitle, fontSize = 8.sp, color = Color(0xFF1D1B20), maxLines = 1)
        }
    }
}

@Composable
fun MyFixturesTab(
    userFixtures: List<MatchEntity>,
    nextUserMatch: MatchEntity?,
    team: TeamEntity?,
    allTeams: List<TeamEntity>,
    fixtureFilter: String,
    onFilterChange: (String) -> Unit,
    onPlayMatch: (MatchEntity) -> Unit,
    onQuickSimMatch: (MatchEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "UPCOMING", "COMPLETED", "HOME", "AWAY").forEach { filter ->
                val isSel = fixtureFilter == filter
                Surface(
                    modifier = Modifier.clickable { onFilterChange(filter) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) Color(0xFF21005D) else Color(0xFFF3EDF7),
                    border = BorderStroke(1.dp, if (isSel) Color(0xFF21005D) else Color(0xFFCAC4D0))
                ) {
                    Text(
                        text = filter,
                        color = if (isSel) Color.White else Color(0xFF49454F),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        val filteredFixtures = userFixtures.filter { m ->
            val isHome = m.teamAId == team?.teamId
            when (fixtureFilter) {
                "UPCOMING" -> !m.isPlayed
                "COMPLETED" -> m.isPlayed
                "HOME" -> isHome
                "AWAY" -> !isHome
                else -> true
            }
        }

        if (filteredFixtures.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No fixtures match the selected filter.", color = Color(0xFF49454F), fontSize = 12.sp)
            }
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(nextUserMatch) {
                val idx = filteredFixtures.indexOfFirst { it.matchId == nextUserMatch?.matchId }
                if (idx >= 0) {
                    listState.animateScrollToItem(idx)
                }
            }

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(filteredFixtures) { match ->
                    val isHome = match.teamAId == team?.teamId
                    val oppId = if (isHome) match.teamBId else match.teamAId
                    val oppTeam = allTeams.find { it.teamId == oppId }
                    val venueTeam = allTeams.find { it.teamId == match.teamAId }
                    val isNextMatch = match.matchId == nextUserMatch?.matchId

                    FixtureCard(
                        match = match,
                        isHome = isHome,
                        isNextMatch = isNextMatch,
                        oppTeam = oppTeam,
                        venueTeam = venueTeam,
                        userTeamId = team?.teamId,
                        onPlayMatch = { onPlayMatch(match) },
                        onQuickSimMatch = { onQuickSimMatch(match) }
                    )
                }
            }
        }
    }
}

@Composable
fun FixtureCard(
    match: MatchEntity,
    isHome: Boolean,
    isNextMatch: Boolean,
    oppTeam: TeamEntity?,
    venueTeam: TeamEntity?,
    userTeamId: String?,
    onPlayMatch: () -> Unit,
    onQuickSimMatch: () -> Unit
) {
    val venueString = if (match.isPlayoff) {
        "📍 Narendra Modi Stadium, Ahmedabad (Neutral Playoff Venue)"
    } else {
        "📍 ${venueTeam?.stadiumName ?: "International Stadium"}, ${venueTeam?.city ?: ""}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("fixture_card_round_${match.matchDay}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isNextMatch) Color(0xFFFEF7FF) else if (match.isPlayed) Color(0xFFF3EDF7).copy(alpha = 0.7f) else Color.White
        ),
        border = if (isNextMatch) BorderStroke(2.dp, Color(0xFF6750A4)) else BorderStroke(1.dp, Color(0xFFCAC4D0)),
        elevation = if (isNextMatch) CardDefaults.cardElevation(4.dp) else CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Round & Weather & Home/Away Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val roundText = if (match.isPlayoff) "🏆 PLAYOFF • ${match.playoffType}" else "ROUND ${match.matchDay}"
                    Text(roundText, fontWeight = FontWeight.ExtraBold, color = if (isNextMatch) Color(0xFF6750A4) else Color(0xFF1D1B20), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isHome) Color(0xFFE8DEF8) else Color(0xFFFFD8E4)
                    ) {
                        Text(
                            text = if (isHome) "HOME" else "AWAY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHome) Color(0xFF21005D) else Color(0xFF31111D),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                val weatherIcon = when (match.weather) {
                    "SUNNY" -> "☀️ Sunny"
                    "RAIN" -> "🌧️ Rain Forecast"
                    else -> "☁️ Overcast"
                }
                Text(weatherIcon, fontSize = 10.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Matchup Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(
                        text = "vs ${oppTeam?.name ?: "Opponent Team"}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D1B20),
                        fontSize = 15.sp
                    )
                    Text(
                        text = venueString,
                        fontSize = 11.sp,
                        color = Color(0xFF49454F),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Status or Score Right Column
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    if (match.isPlayed) {
                        val isWinner = match.winnerId == userTeamId
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isWinner) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (isWinner) "🎉 WON MATCH" else "❌ LOST MATCH",
                                color = if (isWinner) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val myScore = if (isHome) match.teamAScore else match.teamBScore
                        val oppScore = if (isHome) match.teamBScore else match.teamAScore
                        Text("Us: ${myScore.ifEmpty { "DNP" }}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        Text("Opp: ${oppScore.ifEmpty { "DNP" }}", fontSize = 10.sp, color = Color(0xFF49454F))
                    } else if (isNextMatch) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { onPlayMatch() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp).testTag("play_fixture_${match.matchDay}")
                            ) {
                                Text("PLAY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onQuickSimMatch() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp).testTag("sim_fixture_${match.matchDay}")
                            ) {
                                Text("⚡ SIM", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onQuickSimMatch() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("sim_future_fixture_${match.matchDay}")
                        ) {
                            Text("⚡ SIM GAME", color = Color(0xFF21005D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AllRoundsTab(
    seasonMatches: List<MatchEntity>,
    selectedRound: Int,
    onRoundChange: (Int) -> Unit,
    userTeamId: String?,
    allTeams: List<TeamEntity>,
    onSimulateGame: (MatchEntity) -> Unit,
    onSimulateRound: (Int) -> Unit
) {
    val totalRounds = 15 // 1..14 League, 15 Playoffs
    Column(modifier = Modifier.fillMaxSize()) {
        Text("SELECT LEAGUE MATCHDAY / ROUND:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 6.dp))
        
        ScrollableTabRow(
            selectedTabIndex = (selectedRound - 1).coerceIn(0, totalRounds - 1),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF6750A4),
            indicator = {},
            divider = {},
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            for (round in 1..totalRounds) {
                val isSel = selectedRound == round
                val label = if (round == 15) "PLAYOFFS" else "ROUND $round"
                Surface(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clickable { onRoundChange(round) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) Color(0xFF6750A4) else Color(0xFFEADDFF),
                    border = BorderStroke(1.dp, if (isSel) Color(0xFF6750A4) else Color(0xFFCAC4D0))
                ) {
                    Text(
                        text = label,
                        color = if (isSel) Color.White else Color(0xFF21005D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        val roundMatches = seasonMatches.filter { it.matchDay == selectedRound }
        val unplayedCount = roundMatches.count { !it.isPlayed }
        if (unplayedCount > 0) {
            Button(
                onClick = { onSimulateRound(selectedRound) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).height(34.dp)
            ) {
                Text("⚡ SIMULATE ALL $unplayedCount SCHEDULED GAMES IN ROUND $selectedRound", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (roundMatches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No fixtures scheduled for this round.", color = Color(0xFF49454F), fontSize = 12.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(roundMatches) { match ->
                    val teamA = allTeams.find { it.teamId == match.teamAId }
                    val teamB = allTeams.find { it.teamId == match.teamBId }
                    val isUserInvolved = match.teamAId == userTeamId || match.teamBId == userTeamId
                    val venueString = if (match.isPlayoff) {
                        "📍 Narendra Modi Stadium, Ahmedabad (Neutral)"
                    } else {
                        "📍 ${teamA?.stadiumName ?: "Home Stadium"}, ${teamA?.city ?: ""}"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUserInvolved) Color(0xFFFEF7FF) else Color.White
                        ),
                        border = if (isUserInvolved) BorderStroke(1.5.dp, Color(0xFF6750A4)) else BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    if (match.isPlayoff) "🏆 PLAYOFF • ${match.playoffType}" else "ROUND ${match.matchDay} FIXTURE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4)
                                )
                                Text("Weather: ${match.weather}", fontSize = 10.sp, color = Color(0xFF49454F))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        "${teamA?.name ?: "Team A"} (HOME)",
                                        fontWeight = if (match.teamAId == userTeamId) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (match.teamAId == userTeamId) Color(0xFF6750A4) else Color(0xFF1D1B20),
                                        fontSize = 13.sp
                                    )
                                    Text("vs", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(vertical = 1.dp))
                                    Text(
                                        "${teamB?.name ?: "Team B"} (AWAY)",
                                        fontWeight = if (match.teamBId == userTeamId) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (match.teamBId == userTeamId) Color(0xFF6750A4) else Color(0xFF1D1B20),
                                        fontSize = 13.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                    if (match.isPlayed) {
                                        Text("${match.teamAScore}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                                        Text("${match.teamBScore}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                                        val winnerTeam = allTeams.find { it.teamId == match.winnerId }
                                        Text("Winner: ${winnerTeam?.name?.split(" ")?.firstOrNull() ?: "Tied"}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 2.dp))
                                    } else {
                                        Button(
                                            onClick = { onSimulateGame(match) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("⚡ SIM GAME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(venueString, fontSize = 10.sp, color = Color(0xFF49454F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayoffsBracketTab(
    seasonMatches: List<MatchEntity>,
    allTeams: List<TeamEntity>,
    userTeamId: String?,
    pointsTable: List<com.example.simulation.SeasonSimulator.PointsTableEntry>,
    onPlayMatch: (MatchEntity) -> Unit
) {
    val playoffs = seasonMatches.filter { it.isPlayoff || it.matchDay >= 15 }
    val q1 = playoffs.find { it.playoffType == "Q1" }
    val elim = playoffs.find { it.playoffType == "EL" }
    val q2 = playoffs.find { it.playoffType == "Q2" }
    val finalMatch = playoffs.find { it.playoffType == "FI" }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                border = BorderStroke(1.dp, Color(0xFF6750A4))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🏆 IPL CHAMPIONSHIP PLAYOFF BRACKET", fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 13.sp)
                    Text("Top 4 teams after Round 14 qualify. Q1 Winner advances directly to Final. Eliminator & Q2 determine the second finalist.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        if (playoffs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏳ PLAYOFF FIXTURES NOT YET DETERMINED", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Complete League Stage (Rounds 1 - 14) to lock in top 4 playoff qualifiers.", color = Color(0xFF49454F), fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }

                Text("CURRENT TOP 4 STANDINGS PREVIEW:", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                pointsTable.take(4).forEachIndexed { idx, entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = if (entry.teamId == userTeamId) Color(0xFFFEF7FF) else Color.White),
                        border = BorderStroke(1.dp, if (entry.teamId == userTeamId) Color(0xFF6750A4) else Color(0xFFCAC4D0))
                    ) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("#${idx+1} ${entry.teamName}", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 13.sp)
                            Text("${entry.points} Pts (${entry.won}W - ${entry.lost}L)", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            item {
                Text("STAGE 1: QUALIFIER 1 & ELIMINATOR", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                if (q1 != null) PlayoffMatchCard("QUALIFIER 1 (Top 1 vs Top 2)", q1, allTeams, userTeamId, onPlayMatch)
                if (elim != null) PlayoffMatchCard("ELIMINATOR (Top 3 vs Top 4)", elim, allTeams, userTeamId, onPlayMatch)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("STAGE 2: QUALIFIER 2", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                if (q2 != null) {
                    PlayoffMatchCard("QUALIFIER 2 (Q1 Loser vs EL Winner)", q2, allTeams, userTeamId, onPlayMatch)
                } else {
                    Text("Waiting for Q1 & Eliminator results...", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("STAGE 3: GRAND CHAMPIONSHIP FINAL", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                if (finalMatch != null) {
                    PlayoffMatchCard("🌟 GRAND FINAL", finalMatch, allTeams, userTeamId, onPlayMatch)
                } else {
                    Text("Waiting for Q1 & Q2 winners...", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }
}

@Composable
fun PlayoffMatchCard(
    title: String,
    match: MatchEntity,
    allTeams: List<TeamEntity>,
    userTeamId: String?,
    onPlayMatch: (MatchEntity) -> Unit
) {
    val teamA = allTeams.find { it.teamId == match.teamAId }
    val teamB = allTeams.find { it.teamId == match.teamBId }
    val isUserInvolved = match.teamAId == userTeamId || match.teamBId == userTeamId

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUserInvolved) Color(0xFFFEF7FF) else Color.White),
        border = BorderStroke(1.5.dp, if (isUserInvolved) Color(0xFF6750A4) else Color(0xFFCAC4D0)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 11.sp)
                Text("📍 Narendra Modi Stadium", fontSize = 10.sp, color = Color(0xFF49454F))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text("${teamA?.name ?: "TBD"}", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 14.sp)
                    Text("vs", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(vertical = 1.dp))
                    Text("${teamB?.name ?: "TBD"}", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    if (match.isPlayed) {
                        Text("${match.teamAScore}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        Text("${match.teamBScore}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        val winnerTeam = allTeams.find { it.teamId == match.winnerId }
                        Text("Winner: ${winnerTeam?.name?.split(" ")?.firstOrNull() ?: "Tied"}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 2.dp))
                    } else if (isUserInvolved) {
                        Button(
                            onClick = { onPlayMatch(match) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("PLAY PLAYOFF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("⏳ SCHEDULED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                    }
                }
            }
        }
    }
}

@Composable
fun LeagueStandingsTab(
    pointsTable: List<com.example.simulation.SeasonSimulator.PointsTableEntry>,
    userTeamId: String?,
    onSimulateUpToNext: () -> Unit
) {
    val userEntry = pointsTable.find { it.teamId == userTeamId }
    val userRank = pointsTable.indexOf(userEntry) + 1

    Column(modifier = Modifier.fillMaxSize()) {
        // Explanatory Banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
            border = BorderStroke(1.dp, Color(0xFF6750A4))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("📊 OFFICIAL IPL LEAGUE STANDINGS (8 FRANCHISES)", fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 13.sp)
                Text("Ranks your franchise against 7 simulated AI-controlled IPL franchises based on points calculated from match simulation results. Top 4 qualify for Playoffs.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 4.dp))
                
                if (userEntry != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (userRank <= 4) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (userRank <= 4) Color(0xFF2E7D32) else Color(0xFFE65100)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("YOUR FRANCHISE: #${userRank} ${userEntry.teamName}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                                Text("${if (userRank <= 4) "✔ Currently in Playoff Qualification Zone!" else "⚠ Outside Playoff Zone - win games to climb!"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (userRank <= 4) Color(0xFF2E7D32) else Color(0xFFE65100))
                            }
                            Text("${userEntry.points} Pts (NRR: ${String.format("%.3f", userEntry.nrr)})", fontWeight = FontWeight.ExtraBold, color = Color(0xFF21005D), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Table Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("# Franchise", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2.2f))
                Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                Text("W", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                Text("L", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                Text("NRR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.9f))
                Text("Pts", color = Color(0xFF4CAF50), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
            }
        }

        // Standings List
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            itemsIndexed(pointsTable) { idx, entry ->
                val rank = idx + 1
                val isUser = entry.teamId == userTeamId
                val isPlayoffZone = rank <= 4

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) Color(0xFFEADDFF) else if (isPlayoffZone) Color(0xFFFAFAFA) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isUser) 2.dp else 1.dp,
                        color = if (isUser) Color(0xFF6750A4) else if (isPlayoffZone) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFFE0E0E0)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2.2f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isPlayoffZone) Color(0xFF2E7D32) else Color(0xFF616161),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$rank", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${entry.teamName}${if (isUser) " ⭐ YOU" else ""}",
                                    color = if (isUser) Color(0xFF21005D) else Color(0xFF1D1B20),
                                    fontWeight = if (isUser) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                if (rank == 4) {
                                    Text("--- Playoff Qualification Cutoff ---", fontSize = 8.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("${entry.played}", color = Color(0xFF1D1B20), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                        Text("${entry.won}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                        Text("${entry.lost}", color = Color(0xFFC62828), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                        Text(String.format("%.3f", entry.nrr), color = if (entry.nrr >= 0) Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.weight(0.9f))
                        Text("${entry.points}", color = Color(0xFF1B5E20), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSimulateUpToNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Text("⚡ SIMULATE AI FIXTURES & UPDATE STANDINGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
