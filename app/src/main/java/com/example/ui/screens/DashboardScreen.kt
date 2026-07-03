package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.zIndex
import kotlin.random.Random
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: GameViewModel) {
    var activeTab by remember { mutableStateOf("HOME") }
    val team = viewModel.activeTeam ?: return
    val saveToast by viewModel.saveToastMessage.collectAsState()

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFEF7FF), Color(0xFFF3EDF7))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(android.graphics.Color.parseColor(team.logoBgColor)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(team.logoText, color = Color(android.graphics.Color.parseColor(team.logoTextColor)), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(team.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                            Text(team.slogan, fontSize = 10.sp, color = Color(0xFF49454F))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.currentScreen = Screen.SlotSelection }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit to save", tint = Color(0xFF6750A4))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF3EDF7))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                contentColor = Color(0xFF49454F)
            ) {
                val items = listOf(
                    Triple("HOME", Icons.Default.Home, "HOME"),
                    Triple("SCHEDULE", Icons.Default.DateRange, "SCHEDULE"),
                    Triple("SQUAD", Icons.Default.AccountBox, "SQUAD"),
                    Triple("AUCTION", Icons.Default.ShoppingCart, "AUCTION"),
                    Triple("FINANCES", Icons.Default.Star, "FINANCES"),
                    Triple("MORE", Icons.Default.Menu, "MORE")
                )
                items.forEach { (label, icon, tab) ->
                    val isSelected = activeTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            indicatorColor = Color(0xFFE8DEF8),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F)
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFFFEF7FF)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(gradient)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = saveToast != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    modifier = Modifier.fillMaxWidth().zIndex(100f)
                ) {
                    saveToast?.let { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Saved", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (activeTab) {
                        "HOME" -> HomeTab(viewModel, onNavigateToSchedule = { activeTab = "SCHEDULE" }, onNavigateToSquad = { activeTab = "SQUAD" })
                        "SCHEDULE" -> ScheduleScreen(viewModel)
                        "SQUAD" -> SquadScreen(viewModel)
                        "AUCTION" -> AuctionScreen(viewModel)
                        "FINANCES" -> FinancesScreen(viewModel)
                        "MORE" -> MoreTabsHub(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTab(viewModel: GameViewModel, onNavigateToSchedule: () -> Unit = {}, onNavigateToSquad: () -> Unit = {}) {
    val team = viewModel.activeTeam ?: return
    val schedule = viewModel.matches
    val pointsTable = viewModel.pointsTable

    // Find the current active MatchDay match for the user
    val currentDay = viewModel.matches.firstOrNull { it.teamAId == team.teamId || it.teamBId == team.teamId }?.let {
        // Simple fallback
        viewModel.matches.filter { (it.teamAId == team.teamId || it.teamBId == team.teamId) && !it.isPlayed }.minByOrNull { it.matchDay }?.matchDay ?: 1
    } ?: 1

    val nextUserMatch = schedule.firstOrNull { 
        (it.teamAId == team.teamId || it.teamBId == team.teamId) && it.matchDay == currentDay
    }

    val scrollState = rememberScrollState()

    val statusText by viewModel.progressStore.statusText.collectAsState()
    val seasonProgress by viewModel.progressStore.seasonProgressPercentage.collectAsState()
    val isSeasonOver = statusText.contains("Completed") || statusText.contains("Game Over")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        // Central Store & Season Status Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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
                    Text("💾 LOCAL PERSISTENCE STORE (ROOM SQLITE)", fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 11.sp)
                    Text(statusText, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { seasonProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF6750A4),
                    trackColor = Color(0xFFEADDFF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-persisting Budget, Roster (${viewModel.activePlayers.size} players) & Match Results across sessions", fontSize = 10.sp, color = Color(0xFF49454F))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.saveFranchiseProgress() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp).testTag("save_progress_button")
                        ) {
                            Text("💾 SAVE SNAPSHOT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        if (isSeasonOver) {
                            Button(
                                onClick = { viewModel.advanceToNextSeason() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("next_season_button")
                            ) {
                                Text("🏆 NEXT SEASON", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Visual Header Stats Row (Budget, Roster, Fan Count, Fan Happiness)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderMetricCard(label = "Budget (Balance)", valStr = "₹" + viewModel.formatCurrency(team.balance), color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(4.dp))
            HeaderMetricCard(label = "Current Roster", valStr = "${viewModel.activePlayers.size} Players", color = Color(0xFF006C4C), modifier = Modifier.weight(0.9f))
            Spacer(modifier = Modifier.width(4.dp))
            HeaderMetricCard(label = "Total Fan Count", valStr = "${String.format("%.1f", team.followers / 1000000.0)} M", color = Color(0xFF6750A4), modifier = Modifier.weight(0.9f))
            Spacer(modifier = Modifier.width(4.dp))
            HeaderMetricCard(label = "Fan Happiness", valStr = "${team.fanHappiness}%", color = Color(0xFF2E7D32), modifier = Modifier.weight(0.9f))
        }

        WinLossAndFanGrowthChart(matches = viewModel.matches, teamId = team.teamId, currentFollowers = team.followers)

        Row(modifier = Modifier.fillMaxWidth()) {
            // Next Match & Simulator Card (Left column)
            Column(modifier = Modifier.weight(1.2f).padding(end = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("NEXT CRICKET MATCH", fontSize = 12.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                    Text(
                        text = "📅 SCHEDULE ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4),
                        modifier = Modifier.clickable { onNavigateToSchedule() }.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                
                if (nextUserMatch != null) {
                    val oppId = if (nextUserMatch.teamAId == team.teamId) nextUserMatch.teamBId else nextUserMatch.teamAId
                    val opponent = viewModel.allTeams.find { it.teamId == oppId }
                    val isUserHome = nextUserMatch.teamAId == team.teamId
                    val homeTeamObj = if (isUserHome) team else opponent
                    val awayTeamObj = if (isUserHome) opponent else team
                    val venueName = if (nextUserMatch.isPlayoff) "Narendra Modi Stadium, Ahmedabad (Neutral)" else "${homeTeamObj?.stadiumName ?: "Home Stadium"}, ${homeTeamObj?.city ?: ""}"
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("next_match_card"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (nextUserMatch.isPlayoff) "🏆 PLAYOFF • ${nextUserMatch.playoffType}" else "Match Day $currentDay", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6750A4))
                                Text("Weather: ${nextUserMatch.weather}", fontSize = 11.sp, color = Color(0xFF49454F))
                            }
                            Text("📍 Venue: $venueName", fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 2.dp))
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Home Team
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(homeTeamObj?.name?.split(" ")?.firstOrNull() ?: "Home", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 13.sp)
                                    Text("HOME", fontSize = 10.sp, color = Color(0xFF49454F))
                                }

                                Text("vs", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 14.sp)

                                // Away Team
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(awayTeamObj?.name?.split(" ")?.firstOrNull() ?: "Away", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 13.sp)
                                    Text("AWAY", fontSize = 10.sp, color = Color(0xFF49454F))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { 
                                        // Auto simulate all other match AI matches for this day
                                        viewModel.simulateRemainingMatchesForDay(currentDay)
                                        // Prepare user match
                                        viewModel.prepareMatchDay(nextUserMatch)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("play_match_button")
                                ) {
                                    Text("▶️ PLAY MATCH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        viewModel.simulateScheduledRound(currentDay)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.weight(1f).height(36.dp).testTag("quick_sim_match_button")
                                ) {
                                    Text("⚡ QUICK SIM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = { onNavigateToSchedule() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6750A4)),
                                border = BorderStroke(1.dp, Color(0xFF6750A4)),
                                modifier = Modifier.fillMaxWidth().height(32.dp).padding(top = 6.dp).testTag("view_full_schedule_button")
                            ) {
                                Text("📅 VIEW FULL SEASON FIXTURES", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Congratulations! Season matches complete. Check final standings or reset via Admin panel.", color = Color(0xFF1D1B20))
                            Button(
                                onClick = { onNavigateToSchedule() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text("📅 VIEW SEASON RESULTS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Objectives Section
                Text("🎯 DAILY OBJECTIVES (MISSION REWARDS)", fontSize = 12.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                viewModel.dailyObjectives.forEach { obj ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = if (obj.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)),
                        border = BorderStroke(1.dp, if (obj.isCompleted) Color(0xFF2E7D32) else Color(0xFFFFA000))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(obj.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D1B20))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (obj.isCompleted) {
                                        Text("✔ COMPLETED", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                                    } else {
                                        Text("⏳ IN PROGRESS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    }
                                }
                                Text(obj.description, fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 2.dp))
                                Text("Reward: ${obj.rewardText}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF303F9F), modifier = Modifier.padding(top = 2.dp))
                            }
                            if (obj.isCompleted && !obj.isClaimed) {
                                Button(
                                    onClick = { viewModel.claimObjectiveReward(obj.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("CLAIM REWARD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (obj.isClaimed) {
                                Text("CLAIMED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Franchise News Feed
                Text("DAILY NEWS & TRANSFERS", fontSize = 12.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                if (viewModel.news.isEmpty()) {
                    Text("No news bulletins generated yet.", color = Color(0xFF49454F), fontSize = 11.sp)
                } else {
                    viewModel.news.take(2).forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                            border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(0.5f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(item.title, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 11.sp)
                                Text(item.content, fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            // Points Table (Right Column)
            Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                Text("LEAGUE STANDINGS", fontSize = 12.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Team", fontWeight = FontWeight.Bold, color = Color(0xFF49454F), fontSize = 11.sp, modifier = Modifier.weight(2f))
                            Text("W", fontWeight = FontWeight.Bold, color = Color(0xFF49454F), fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                            Text("Pts", fontWeight = FontWeight.Bold, color = Color(0xFF49454F), fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                        }
                        pointsTable.take(8).forEachIndexed { idx, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx+1}. ${entry.teamName.split(" ").firstOrNull() ?: ""}",
                                    color = if (entry.teamId == team.teamId) Color(0xFF6750A4) else Color(0xFF1D1B20),
                                    fontSize = 10.sp,
                                    fontWeight = if (entry.teamId == team.teamId) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(2f)
                                )
                                Text(entry.won.toString(), color = Color(0xFF1D1B20), fontSize = 10.sp, modifier = Modifier.weight(0.5f))
                                Text(entry.points.toString(), color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onNavigateToSchedule,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxWidth().height(22.dp)
                        ) {
                            Text("VIEW FULL STANDINGS ➡", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Current Roster Snapshot Card (for quick dashboard overview of squad)
        val roster = viewModel.activePlayers
        val topRosterPlayers = roster.sortedByDescending { (it.batting + it.bowling) / 2 }.take(4)
        Text("CURRENT ROSTER HIGHLIGHTS (${roster.size} PLAYERS)", fontSize = 12.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dashboard_roster_snapshot"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFCAC4D0))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avgOvr = if (roster.isNotEmpty()) roster.map { (it.batting + it.bowling) / 2 }.average().toInt() else 0
                    val totalVal = roster.sumOf { it.salary }
                    Text("Avg Squad OVR: $avgOvr", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF6750A4))
                    Text("Total Squad Value: ₹${viewModel.formatCurrency(totalVal)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2E7D32))
                }
                Spacer(modifier = Modifier.height(8.dp))
                topRosterPlayers.forEach { p ->
                    val ovr = (p.batting + p.bowling) / 2
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1D1B20))
                            Text("${p.category.replace("_", " ")} • Age ${p.age}", fontSize = 10.sp, color = Color(0xFF49454F))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Color(0xFFEADDFF), shape = RoundedCornerShape(4.dp)) {
                                Text("OVR $ovr", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text("₹${viewModel.formatCurrency(p.salary)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF3EDF7), thickness = 1.dp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { onNavigateToSquad() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("view_full_roster_button")
                ) {
                    Text("👑 VIEW COMPLETE ROSTER & LINEUP (${roster.size} PLAYERS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun HeaderMetricCard(label: String, valStr: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(0.4f))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label.uppercase(), fontSize = 10.sp, color = Color(0xFF21005D), fontWeight = FontWeight.Medium)
            Text(valStr, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun MoreTabsHub(viewModel: GameViewModel) {
    var subTab by remember { mutableStateOf("STADIUM") } // STADIUM, SPONSORS, STORE, TRANSFERS, ACHIEVEMENTS, ADMIN

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Horizontal selector of More subsystems
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("STADIUM", "SPONSORS", "STORE", "TRANSFERS", "TROPHIES", "ADMIN").forEach { tab ->
                val isSelected = subTab == tab
                Button(
                    onClick = { subTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFEADDFF) else Color(0xFFF3EDF7)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).weight(1f).padding(horizontal = 1.dp)
                ) {
                    Text(tab, color = if (isSelected) Color(0xFF21005D) else Color(0xFF49454F), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        when (subTab) {
            "STADIUM" -> StadiumSubTab(viewModel)
            "SPONSORS" -> SponsorsSubTab(viewModel)
            "STORE" -> StoreSubTab(viewModel)
            "TRANSFERS" -> TransfersSubTab(viewModel)
            "TROPHIES" -> TrophiesSubTab(viewModel)
            "ADMIN" -> AdminSubTab(viewModel)
        }
    }
}

@Composable
fun StadiumSubTab(viewModel: GameViewModel) {
    val team = viewModel.activeTeam ?: return
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Text("STADIUM INFRASTRUCTURE", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 15.sp)
        Text("Upgrade facilities to boost audience capacity, matchday revenues, and team health bonuses.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 12.dp))

        val growthBoost = (team.stadiumCapacity / 1000) * 2
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
            border = BorderStroke(1.dp, Color(0xFF3F51B5))
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Growth", tint = Color(0xFF3F51B5), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("🚀 FAN COUNT GROWTH RATE BOOSTER: +$growthBoost% PER MATCH", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF1A237E))
                    Text("Expanding stadium capacity directly multiplies your follower acquisition rates after every match!", fontSize = 10.sp, color = Color(0xFF303F9F))
                }
            }
        }

        // Capacity Box
        StadiumUpgradeItem(
            label = "Stadium Seating Capacity",
            currentVal = "${team.stadiumCapacity} Seats",
            cost = 300000000L, // 30 Cr
            level = (team.stadiumCapacity / 20000).coerceIn(1, 5),
            maxLevel = 5,
            onUpgrade = { viewModel.upgradeStadium("CAPACITY", 300000000L) },
            viewModel = viewModel
        )

        val facilityUpgrades = listOf(
            Triple("VIP Corporate Stands", team.stadiumVipStands, "VIP"),
            Triple("Premium Parking Space", team.stadiumParking, "PARKING"),
            Triple("Food & Refreshments Court", team.stadiumFoodCourts, "FOOD"),
            Triple("Floodlight LED Systems", team.stadiumLighting, "LIGHTING"),
            Triple("Training Gym Academy Complex", team.stadiumTrainingGrounds, "TRAINING"),
            Triple("Franchise Hall of Fame Museum", team.stadiumMuseum, "MUSEUM"),
            Triple("Official Merchandise Megastore", team.stadiumMerchShop, "MERCH")
        )

        facilityUpgrades.forEach { (label, level, key) ->
            val upgradeCost = (level + 1) * 20000000L // e.g. 2 Crore, 4 Crore, etc.
            StadiumUpgradeItem(
                label = label,
                currentVal = "Level $level/5",
                cost = if (level >= 5) 0L else upgradeCost,
                level = level,
                maxLevel = 5,
                onUpgrade = { viewModel.upgradeStadium(key, upgradeCost) },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun StadiumUpgradeItem(label: String, currentVal: String, cost: Long, level: Int, maxLevel: Int = 5, onUpgrade: () -> Unit, viewModel: GameViewModel) {
    val targetProgress = (level.toFloat() / maxLevel.toFloat()).coerceIn(0.05f, 1.0f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "stadium_bar"
    )
    val barColor by animateColorAsState(
        targetValue = if (level >= maxLevel) Color(0xFF2E7D32) else Color(0xFF6750A4),
        animationSpec = tween(500),
        label = "stadium_bar_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                    Text("Current status: $currentVal", fontSize = 11.sp, color = Color(0xFF49454F))
                }

                if (cost > 0) {
                    Button(
                        onClick = onUpgrade,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("UPGRADE (₹${viewModel.formatCurrency(cost)})", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("MAX LEVEL", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = barColor,
                    trackColor = Color(0xFFE0E0E0),
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${(animatedProgress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = barColor)
            }
        }
    }
}

@Composable
fun StoreSubTab(viewModel: GameViewModel) {
    val storeItems = listOf(
        Triple("Official Jerseys", 1200L, "JERSEY"),
        Triple("Franchise Caps", 500L, "CAP"),
        Triple("Autographed Cricket Bats", 4000L, "BAT"),
        Triple("Coffee Mugs", 300L, "MUG"),
        Triple("Team Posters", 150L, "POSTER")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text("MERCHANDISE STORE & SALES", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 15.sp)
        Text("Manufacture and sell branded products. Sales rate is based on your follower base and fan happiness.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 12.dp))

        LazyColumn {
            items(storeItems) { (label, basePrice, key) ->
                var sellPrice by remember { mutableStateOf(basePrice) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                        Text("Recommended Base: ₹$basePrice", fontSize = 10.sp, color = Color(0xFF49454F))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (sellPrice > basePrice / 2) sellPrice -= 50 }) {
                            Text("-", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                        }
                        Text("₹$sellPrice", color = Color(0xFF2E7D32), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { sellPrice += 50 }) {
                            Text("+", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { 
                                val amountSold = Random.nextInt(20, 100)
                                viewModel.sellMerchandise(key, sellPrice, amountSold)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("SELL BATCH", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransfersSubTab(viewModel: GameViewModel) {
    val freeAgents = viewModel.allPlayers.filter { it.teamId == null }
    val userSquad = viewModel.activePlayers

    var viewAgentsMode by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TRANSFER MARKET", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 15.sp)
            TextButton(onClick = { viewAgentsMode = !viewAgentsMode }) {
                Text(if (viewAgentsMode) "VIEW MY ROSTER" else "SIGN FREE AGENTS", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (viewAgentsMode) {
            Text("Sign unassigned free agents into your squad.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn {
                items(freeAgents) { player ->
                    val ovrText = if (player.isStatsKnown) "OVR: ${(player.batting+player.bowling)/2}" else "OVR: ??? (Hidden)"
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(player.name, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 13.sp)
                                Text("${player.category} • $ovrText", fontSize = 11.sp, color = if (player.isStatsKnown) Color(0xFF49454F) else Color(0xFFB3261E))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (!player.isStatsKnown) {
                                    Button(
                                        onClick = { viewModel.scoutAuctionPlayer(player, 2500000L) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C4C)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("🔍 SCOUT (₹25L)", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Button(
                                    onClick = { viewModel.buyFreeAgent(player) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("SIGN (₹${viewModel.formatCurrency(player.salary)})", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("Release existing players into free agency.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn {
                items(userSquad) { player ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(player.name, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 13.sp)
                                Text("${player.category} • OVR: ${(player.batting+player.bowling)/2}", fontSize = 11.sp, color = Color(0xFF49454F))
                            }
                            Button(
                                onClick = { viewModel.releasePlayer(player) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.8f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("RELEASE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrophiesSubTab(viewModel: GameViewModel) {
    val achievements = viewModel.achievements

    Column(modifier = Modifier.fillMaxSize()) {
        Text("TROPHY CABINET & PROFILE MILESTONES", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 15.sp)
        Text("Unlock prestigious local achievements as you manage your career franchise.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 12.dp))

        LazyColumn {
            items(achievements) { ach ->
                val isUnlocked = ach.unlockedAt != null
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color(0xFFEADDFF).copy(0.5f) else Color(0xFFF3EDF7)),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isUnlocked) Color(0xFFFFCA28) else Color(0xFFCAC4D0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (isUnlocked) Color.Black else Color.White)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(ach.title, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color(0xFF21005D) else Color(0xFF1D1B20), fontSize = 13.sp)
                            Text(ach.description, fontSize = 11.sp, color = Color(0xFF49454F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSubTab(viewModel: GameViewModel) {
    val players = viewModel.activePlayers

    Column(modifier = Modifier.fillMaxSize()) {
        Text("ADMIN COMMAND PANEL", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 15.sp, modifier = Modifier.padding(bottom = 4.dp))
        Text("System utilities, player editor adjustments, and database tools.", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 12.dp))

        // --- SAVE GAME & RESET DATA CONTROL PANEL ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
            border = BorderStroke(1.dp, Color(0xFF6750A4))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "Storage", tint = Color(0xFF6750A4), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💾 STORAGE & PROGRESS CONTROL PANEL", fontWeight = FontWeight.ExtraBold, color = Color(0xFF21005D), fontSize = 12.sp)
                }
                Text("Manage stored career progress in local SQLite database (Room). All match results, transactions, and facility upgrades are stored locally.", fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                
                Text("Last Status: ${viewModel.lastSavedTimestamp}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(bottom = 8.dp))

                // Auto-Save Toggle Row
                Surface(
                    color = if (viewModel.autoSaveEnabled) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (viewModel.autoSaveEnabled) Color(0xFF2E7D32) else Color(0xFFE65100)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (viewModel.autoSaveEnabled) "⚡ AUTO-SAVE: ENABLED" else "🚫 AUTO-SAVE: DISABLED (MANUAL MODE)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = if (viewModel.autoSaveEnabled) Color(0xFF1B5E20) else Color(0xFFE65100)
                            )
                            Text(
                                text = if (viewModel.autoSaveEnabled) "Automatic localStorage updates after every match or action." else "Automatic database writes disabled. Click Manual Save to persist.",
                                fontSize = 9.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                        Switch(
                            checked = viewModel.autoSaveEnabled,
                            onCheckedChange = { viewModel.autoSaveEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF2E7D32),
                                checkedTrackColor = Color(0xFFC8E6C9),
                                uncheckedThumbColor = Color(0xFFE65100),
                                uncheckedTrackColor = Color(0xFFFFE0B2)
                            )
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.saveGameProgressManually() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("💾 MANUAL SAVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Button(
                        onClick = { viewModel.resetCurrentFranchiseProgress {} },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("🔄 RESET CAREER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Button(
                        onClick = { viewModel.wipeSlotAndExit { viewModel.currentScreen = Screen.SlotSelection } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("🗑️ WIPE & EXIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }

        Text("Roster Editor (Quick Override):", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(players) { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(player.name, color = Color(0xFF1D1B20), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.adminModifyPlayer(player, 99, 99, 99) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("MAX STATS (99)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SponsorsSubTab(viewModel: GameViewModel) {
    val team = viewModel.activeTeam ?: return
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Text("🤝 BRAND SPONSORSHIP MODULE", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 15.sp)
        Text("Accept short-term brand contracts to generate instant extra budget. Payouts scale dynamically with your current fan base (${String.format("%.1f", team.followers / 1000000.0)}M Followers)!", fontSize = 11.sp, color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            border = BorderStroke(1.dp, Color(0xFF2E7D32))
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Sponsor Rate", tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("📊 FAN COUNT MULTIPLIER ACTIVE", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF1B5E20))
                    Text("Current Brand Valuation: ₹${viewModel.formatCurrency((team.followers * 15L).toLong())} per Title Deal", fontSize = 10.sp, color = Color(0xFF2E7D32))
                }
            }
        }

        val shortTermContracts = listOf(
            Triple("Tata Group - Title Sponsor (3 Matches)", 15.0, "Title Partner"),
            Triple("Jio Digital - Streaming Rights Partner", 10.0, "Digital Media"),
            Triple("Dream11 - Official Fantasy Partner", 8.0, "Gaming Sponsor"),
            Triple("Puma Apparel - Kit & Jersey Partner", 7.0, "Merchandise"),
            Triple("Red Bull Energy - Beverage Partner", 5.0, "Nutrition Partner")
        )

        Text("Available Short-Term Brand Contracts:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D1B20), modifier = Modifier.padding(bottom = 6.dp))

        shortTermContracts.forEach { (name, rate, category) ->
            val payout = (team.followers * rate).toLong().coerceAtLeast(5000000L)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D1B20))
                        Text("Category: $category • Rate: ₹$rate/Fan", fontSize = 10.sp, color = Color(0xFF49454F))
                        Text("Instant Payout: ₹${viewModel.formatCurrency(payout)}", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 2.dp))
                    }
                    Button(
                        onClick = { viewModel.acceptShortTermSponsor(name, rate) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("ACCEPT DEAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WinLossAndFanGrowthChart(matches: List<MatchEntity>, teamId: String, currentFollowers: Long) {
    val playedMatches = matches.filter { it.isPlayed && (it.teamAId == teamId || it.teamBId == teamId) }
        .sortedBy { it.matchDay }
        .takeLast(10)

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
        border = BorderStroke(1.dp, Color(0xFF6750A4)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📈 10-MATCH HISTORICAL WIN/LOSS & FAN GROWTH ANALYTICS", fontWeight = FontWeight.ExtraBold, color = Color(0xFF6750A4), fontSize = 11.sp)
                Text("${playedMatches.size} Matches Recorded", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
            }
            Text("Visualizing team form (Green = Win, Red = Loss) against historical follower growth trajectory.", fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.padding(vertical = 4.dp))
            
            Spacer(modifier = Modifier.height(6.dp))
            
            if (playedMatches.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFEADDFF), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("No match history available yet. Simulate matches to generate analytics graph!", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                }
            } else {
                // Native Compose Canvas Graph
                Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val n = playedMatches.size
                        val stepX = if (n > 1) w / (n - 1) else w / 2f
                        
                        // Draw horizontal grid lines
                        val gridColor = Color(0xFFE0E0E0)
                        drawLine(color = gridColor, start = Offset(0f, h * 0.25f), end = Offset(w, h * 0.25f), strokeWidth = 1f)
                        drawLine(color = gridColor, start = Offset(0f, h * 0.5f), end = Offset(w, h * 0.5f), strokeWidth = 1f)
                        drawLine(color = gridColor, start = Offset(0f, h * 0.75f), end = Offset(w, h * 0.75f), strokeWidth = 1f)
                        
                        val minFans = (currentFollowers * 0.6f).toLong()
                        val maxFans = currentFollowers
                        val fanRange = (maxFans - minFans).coerceAtLeast(10000L).toFloat()
                        
                        val path = Path()
                        val points = mutableListOf<Offset>()
                        
                        playedMatches.forEachIndexed { i, m ->
                            val isWin = m.winnerId == teamId
                            val x = if (n > 1) i * stepX else w / 2f
                            
                            val barColor = if (isWin) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            val barHeight = if (isWin) h * 0.4f else h * 0.2f
                            drawRect(
                                color = barColor.copy(alpha = 0.7f),
                                topLeft = Offset(x - 8f, h - barHeight),
                                size = androidx.compose.ui.geometry.Size(16f, barHeight)
                            )
                            
                            val progressFrac = if (n > 1) i.toFloat() / (n - 1) else 1f
                            val estFans = minFans + (fanRange * progressFrac) + (if (isWin) 20000f else -5000f)
                            val normY = 1f - ((estFans - minFans) / fanRange).coerceIn(0.1f, 0.9f)
                            val y = h * normY
                            points.add(Offset(x, y))
                            
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        
                        drawPath(
                            path = path,
                            color = Color(0xFF6750A4),
                            style = Stroke(width = 5f)
                        )
                        
                        points.forEach { pt ->
                            drawCircle(color = Color(0xFF21005D), radius = 6f, center = pt)
                            drawCircle(color = Color.White, radius = 3f, center = pt)
                        }
                    }
                }
                
                // Legend
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF2E7D32), RoundedCornerShape(2.dp)))
                        Text(" Win (+Fans)", fontSize = 10.sp, color = Color(0xFF1D1B20), modifier = Modifier.padding(start = 4.dp, end = 12.dp))
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFD32F2F), RoundedCornerShape(2.dp)))
                        Text(" Loss (-Fans)", fontSize = 10.sp, color = Color(0xFF1D1B20), modifier = Modifier.padding(start = 4.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp, 3.dp).background(Color(0xFF6750A4)))
                        Text(" Fan Growth Trajectory", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}
