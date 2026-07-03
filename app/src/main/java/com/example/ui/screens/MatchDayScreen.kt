package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDayScreen(viewModel: GameViewModel) {
    val match = viewModel.currentMatchPlaying ?: return
    val team = viewModel.activeTeam ?: return
    val oppId = if (match.teamAId == team.teamId) match.teamBId else match.teamAId
    val opponent = viewModel.allTeams.find { it.teamId == oppId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MATCHDAY SIMULATOR", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = Screen.Dashboard }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6750A4))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3EDF7))
            )
        },
        containerColor = Color(0xFFFEF7FF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Head-to-head match cards
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Live Match • Day ${match.matchDay} • Season 1", fontSize = 12.sp, color = Color(0xFF49454F))
                    Text("Venue: ${team.stadiumName} • Weather: ${match.weather}", fontSize = 11.sp, color = Color(0xFF49454F))
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(team.name, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 16.sp)
                            if (viewModel.matchInningsProgress > 0) {
                                val score = if (match.teamAId == team.teamId) match.teamAScore else match.teamBScore
                                Text(score, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D1B20))
                            }
                        }

                        Text("VS", fontWeight = FontWeight.ExtraBold, color = Color(0xFF49454F), fontSize = 18.sp)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(opponent?.name ?: "Opponent", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D1B20), fontSize = 16.sp)
                            if (viewModel.matchInningsProgress > 0) {
                                val score = if (match.teamAId == opponent?.teamId) match.teamAScore else match.teamBScore
                                Text(score, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D1B20))
                            }
                        }
                    }
                }
            }

            if (viewModel.matchInningsProgress == 0) {
                // Weather Forecast & Tactical Lineup Advisor Card
                val w = match.weather
                val (weatherIcon, weatherDesc, lineupAdvice, bgCol, borderCol) = when (w) {
                    "SUNNY" -> arrayOf("☀️ SUNNY (+5% Batting Boost)", "Fast outfield & batsmen paradise.", "STRATEGY: Select aggressive batsmen & powerplay boundary hitters.", Color(0xFFFFF9C4), Color(0xFFFBC02D))
                    "OVERCAST" -> arrayOf("☁️ OVERCAST (-7% Batting, +15% Swing)", "Heavy atmospheric swing & seam movement.", "STRATEGY: Play extra fast bowlers & solid top-order anchors.", Color(0xFFECEFF1), Color(0xFF78909C))
                    "RAIN" -> arrayOf("🌧️ RAIN / DAMP (-14% Batting, +10% Grip)", "Wet outfield makes boundary hitting tough.", "STRATEGY: Play disciplined wicket-to-wicket bowlers & anchors.", Color(0xFFE1F5FE), Color(0xFF039BE5))
                    "DEW" -> arrayOf("💧 EVENING DEW (+8% Batting Advantage)", "Wet ball slips out of spinners' hands.", "STRATEGY: Avoid extra spinners; favor fast pacers & big hitters.", Color(0xFFE0F7FA), Color(0xFF00ACC1))
                    "WINDY" -> arrayOf("💨 STRONG WIND (-5% Batting, +12% Drift)", "Crosswinds create drift for spinners.", "STRATEGY: Pick mystery spinners & slow left-arm bowlers.", Color(0xFFF1F8E9), Color(0xFF7CB342))
                    else -> arrayOf("🌤️ BALANCED CONDITIONS", "Standard pitch behavior.", "STRATEGY: Play your standard balanced Playing XI.", Color(0xFFF3EDF7), Color(0xFF6750A4))
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = bgCol as Color),
                    border = BorderStroke(1.dp, borderCol as Color)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("WEATHER FORECAST: $weatherIcon", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D1B20), fontSize = 11.sp)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, borderCol),
                                modifier = Modifier.clickable { viewModel.randomizeCurrentMatchWeather() }
                            ) {
                                Text("🎲 RANDOMIZE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                        Text("$weatherDesc $lineupAdvice", fontSize = 10.sp, color = Color(0xFF37474F), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 3.dp))
                    }
                }

                // TOSS / SQUAD XI VIEW
                Text("YOUR PLAYING XI FOR TODAY:", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(viewModel.matchXI) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color(0xFFCAC4D0).copy(0.6f)), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(player.name, color = Color(0xFF1D1B20), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(player.category.replace("_", " "), color = Color(0xFF49454F), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                    border = BorderStroke(1.dp, Color(0xFF6750A4))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🎯 MATCHDAY TACTICS & STRATEGY", fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Batting Strategy:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1D1B20))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("NORMAL" to "Balanced", "AGGRESSIVE" to "Attack (+25% 4s/6s)", "DEFENSIVE" to "Anchor (-45% Wkts)").forEach { (valKey, label) ->
                                val isSelected = viewModel.userBattingTactic == valKey
                                Button(
                                    onClick = { viewModel.userBattingTactic = valKey },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF6750A4) else Color(0xFFEADDFF),
                                        contentColor = if (isSelected) Color.White else Color(0xFF21005D)
                                    ),
                                    contentPadding = PaddingValues(2.dp),
                                    modifier = Modifier.weight(1f).height(30.dp).testTag("bat_tactic_$valKey")
                                ) {
                                    Text(label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Bowling Strategy:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1D1B20))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("STANDARD" to "Standard", "ALL_OUT_ATTACK" to "Attack (+30% Wkts)", "CONTAINMENT" to "Contain (-30% Runs)").forEach { (valKey, label) ->
                                val isSelected = viewModel.userBowlingTactic == valKey
                                Button(
                                    onClick = { viewModel.userBowlingTactic = valKey },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
                                        contentColor = if (isSelected) Color.White else Color(0xFF1B5E20)
                                    ),
                                    contentPadding = PaddingValues(2.dp),
                                    modifier = Modifier.weight(1f).height(30.dp).testTag("bowl_tactic_$valKey")
                                ) {
                                    Text(label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.playSelectedMatch() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(top = 12.dp)
                        .testTag("simulate_match_now_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START MATCH SIMULATION", color = Color.White, fontWeight = FontWeight.Bold)
                }

            } else {
                // COMMENTARY / TICKER AND RESULT VIEW
                val summary = viewModel.matchResultSummary
                MatchMomentumAndHealthSection(
                    summary = summary,
                    teamA = team,
                    opponent = opponent,
                    match = match
                )
                if (summary != null) {
                    // Man of the Match Card
                    summary.manOfTheMatch?.let { mvp ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            border = BorderStroke(1.dp, Color(0xFFFFB300)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌟", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                Column {
                                    Text("MAN OF THE MATCH: $mvp", fontWeight = FontWeight.Bold, color = Color(0xFFB78103), fontSize = 13.sp)
                                    Text(summary.manOfTheMatchStats ?: "", fontSize = 12.sp, color = Color(0xFF5D4037))
                                }
                            }
                        }
                    }

                    // Top Performers Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        summary.topBatsmanName?.let { topBat ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, Color(0xFF81C784)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🏏 Best Batter", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text(topBat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                    Text(summary.topBatsmanStats ?: "", fontSize = 10.sp, color = Color(0xFF388E3C))
                                }
                            }
                        }
                        summary.topBowlerName?.let { topBowl ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                border = BorderStroke(1.dp, Color(0xFF64B5F6)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🎯 Best Bowler", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                    Text(topBowl, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                                    Text(summary.topBowlerStats ?: "", fontSize = 10.sp, color = Color(0xFF1976D2))
                                }
                            }
                        }
                    }
                }

                Text("LIVE BALL-BY-BALL COMMENTARY", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp).align(Alignment.Start))

                val commentaryScrollState = rememberLazyListState()
                LaunchedEffect(viewModel.matchCommentary.size) {
                    if (viewModel.matchCommentary.isNotEmpty()) {
                        commentaryScrollState.animateScrollToItem(viewModel.matchCommentary.size - 1)
                    }
                }

                LazyColumn(
                    state = commentaryScrollState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    items(viewModel.matchCommentary) { event ->
                        val textColor = when {
                            event.contains("WICKET!") || event.contains("depart") -> Color(0xFFB3261E)
                            event.contains("SIX!") || event.contains("CENTURY!") -> Color(0xFFE65100)
                            event.contains("FOUR!") -> Color(0xFF1565C0)
                            event.contains("HALF-CENTURY!") -> Color(0xFF2E7D32)
                            event.contains("MATCH CONCLUDED:") || event.contains("won by") -> Color(0xFF2E7D32)
                            event.contains("DROPPED!") -> Color(0xFFB78103)
                            else -> Color(0xFF1D1B20)
                        }
                        val isBold = event.contains("WICKET!") || event.contains("SIX!") || event.contains("FOUR!") || event.contains("CENTURY!") || event.contains("MATCH CONCLUDED:")
                        Text(
                            text = event,
                            fontSize = 12.sp,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }

                viewModel.matchResultSummary?.injuredPlayerName?.let { inj ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD9)),
                        border = BorderStroke(1.dp, Color(0xFFFFB4AB))
                    ) {
                        Text(
                            text = "⚠️ INJURY REPORT: $inj got injured during the match and will require recovery for ${viewModel.matchResultSummary?.injuryDuration} matches.",
                            color = Color(0xFF410002),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.currentScreen = Screen.Dashboard },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("conclude_match_button")
                ) {
                    Text("RETURN TO DASHBOARD", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MatchMomentumAndHealthSection(
    summary: com.example.simulation.MatchEngine.MatchSimulationResult?,
    teamA: com.example.data.TeamEntity,
    opponent: com.example.data.TeamEntity?,
    match: com.example.data.MatchEntity
) {
    val oppName = opponent?.name ?: "Opponent"
    val scoreA = if (match.teamAId == teamA.teamId) match.teamAScore else match.teamBScore
    val scoreB = if (match.teamAId == opponent?.teamId) match.teamAScore else match.teamBScore

    val wktA = remember(scoreA) { parseWicketsFromScore(scoreA) }
    val wktB = remember(scoreB) { parseWicketsFromScore(scoreB) }
    val healthA = ((10 - wktA) / 10f).coerceIn(0f, 1f)
    val healthB = ((10 - wktB) / 10f).coerceIn(0f, 1f)

    val strA = summary?.teamAStrengthRating?.toFloat() ?: 75f
    val strB = summary?.teamBStrengthRating?.toFloat() ?: 75f
    val runsA = remember(scoreA) { parseRunsFromScore(scoreA) }
    val runsB = remember(scoreB) { parseRunsFromScore(scoreB) }
    
    val momentumA = remember(strA, strB, healthA, healthB, runsA, runsB) {
        val scoreDiff = (runsA - runsB) * 0.25f
        val rawA = ((strA * 0.6f) + (healthA * 45f) + scoreDiff).coerceIn(10f, 160f)
        val rawB = ((strB * 0.6f) + (healthB * 45f) - scoreDiff).coerceIn(10f, 160f)
        (rawA / (rawA + rawB)).coerceIn(0.15f, 0.85f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = BorderStroke(1.dp, Color(0xFF6750A4).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("⚡ MATCH MOMENTUM & TEAM HEALTH", fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 12.sp)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (momentumA >= 0.5f) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, if (momentumA >= 0.5f) Color(0xFF2E7D32) else Color(0xFFE65100))
                ) {
                    Text(
                        text = if (momentumA >= 0.5f) "🔥 ADVANTAGE: ${teamA.name.take(12)}" else "🔥 ADVANTAGE: ${oppName.take(12)}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (momentumA >= 0.5f) Color(0xFF1B5E20) else Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text("Real-time visual progress bars tracking batting health (wickets intact) and match dominance.", fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))

            // Bar 1: Match Momentum / Win Dominance
            Text("Dominance Index: ${teamA.name} (${(momentumA * 100).toInt()}%) vs $oppName (${((1 - momentumA) * 100).toInt()}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
            Spacer(modifier = Modifier.height(3.dp))
            CustomProgressBar(
                progress = momentumA,
                fillColor = if (momentumA >= 0.5f) Color(0xFF6750A4) else Color(0xFFFF7043),
                trackColor = Color(0xFFFFE0B2),
                height = 10
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bar 2: Team A Batting Health
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🏏 ${teamA.name} Batting Health:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Text("${10 - wktA}/10 Wickets (${(healthA * 100).toInt()}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (healthA >= 0.5f) Color(0xFF2E7D32) else Color(0xFFD32F2F))
            }
            Spacer(modifier = Modifier.height(3.dp))
            CustomProgressBar(
                progress = healthA,
                fillColor = if (healthA >= 0.6f) Color(0xFF4CAF50) else if (healthA >= 0.3f) Color(0xFFFF9800) else Color(0xFFF44336),
                trackColor = Color(0xFFE8F5E9),
                height = 8
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bar 3: Opponent Batting Health
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🎯 $oppName Batting Health:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Text("${10 - wktB}/10 Wickets (${(healthB * 100).toInt()}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (healthB >= 0.5f) Color(0xFF1565C0) else Color(0xFFD32F2F))
            }
            Spacer(modifier = Modifier.height(3.dp))
            CustomProgressBar(
                progress = healthB,
                fillColor = if (healthB >= 0.6f) Color(0xFF2196F3) else if (healthB >= 0.3f) Color(0xFFFF9800) else Color(0xFFF44336),
                trackColor = Color(0xFFE3F2FD),
                height = 8
            )
        }
    }
}

@Composable
fun CustomProgressBar(
    progress: Float,
    fillColor: Color,
    trackColor: Color,
    height: Int
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(trackColor, RoundedCornerShape(height.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .fillMaxHeight()
                .background(fillColor, RoundedCornerShape(height.dp))
        )
    }
}

private fun parseWicketsFromScore(scoreText: String?): Int {
    if (scoreText == null) return 0
    val parts = scoreText.split("/")
    if (parts.size >= 2) {
        return parts[1].takeWhile { it.isDigit() }.toIntOrNull() ?: 0
    }
    return 0
}

private fun parseRunsFromScore(scoreText: String?): Int {
    if (scoreText == null) return 0
    val parts = scoreText.split("/")
    return parts.firstOrNull()?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
}
