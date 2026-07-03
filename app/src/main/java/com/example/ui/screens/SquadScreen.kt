package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.example.data.PlayerEntity
import com.example.viewmodel.GameViewModel

@Composable
fun SquadScreen(viewModel: GameViewModel) {
    // 1. Reactive State from State Management Provider (GameProgressStore)
    val franchiseTeam by viewModel.progressStore.activeFranchiseTeam.collectAsState()
    val storeRoster by viewModel.progressStore.franchiseRoster.collectAsState()
    val remainingBudget by viewModel.progressStore.franchiseBalance.collectAsState()
    val currentSeason by viewModel.progressStore.currentSeason.collectAsState()

    // Fallback if store roster is initializing
    val roster = if (storeRoster.isNotEmpty()) storeRoster else viewModel.activePlayers
    var selectedPlayer by remember { mutableStateOf<PlayerEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    // 2. Team Strength & Composition Metrics Calculation
    val rosterCount = roster.size
    val avgOvr = if (roster.isNotEmpty()) roster.map { (it.batting + it.bowling) / 2 }.average().toInt() else 0
    val topBatsmen = roster.sortedByDescending { it.batting }.take(6)
    val batStrength = if (topBatsmen.isNotEmpty()) topBatsmen.map { it.batting }.average().toInt() else 0
    val topBowlers = roster.sortedByDescending { it.bowling }.take(5)
    val bowlStrength = if (topBowlers.isNotEmpty()) topBowlers.map { it.bowling }.average().toInt() else 0
    val avgAge = if (roster.isNotEmpty()) roster.map { it.age }.average().toInt() else 0

    val batCount = roster.count { it.category == "BATSMAN" }
    val bowlCount = roster.count { it.category == "BOWLER" }
    val arCount = roster.count { it.category == "ALL_ROUNDER" }
    val wkCount = roster.count { it.category == "WICKET_KEEPER" }

    val filteredRoster = if (selectedFilter == "ALL") roster else roster.filter { it.category == selectedFilter }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // --- HEADER BANNER: TEAM DASHBOARD & STRENGTH METRICS ---
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
                        "🏆 FRANCHISE ROSTER & TEAM DASHBOARD",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6750A4),
                        fontSize = 11.sp
                    )
                    Text(
                        "${franchiseTeam?.name ?: "My Franchise"} • Season $currentSeason",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Metric Grid Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Card 1: Overall Strength
                    TeamMetricItem(
                        title = "Team Strength",
                        value = "$avgOvr / 100",
                        subtitle = if (avgOvr >= 80) "🌟 Title Favorite" else if (avgOvr >= 70) "⚡ Playoff Contender" else "🌱 Developing",
                        color = Color(0xFF6750A4),
                        modifier = Modifier.weight(1f)
                    )
                    // Card 2: Remaining Budget
                    TeamMetricItem(
                        title = "Remaining Budget",
                        value = "₹" + viewModel.formatCurrency(remainingBudget),
                        subtitle = "Salary Cap Available",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    // Card 3: Roster Breakdown
                    TeamMetricItem(
                        title = "Squad Roster",
                        value = "$rosterCount / 25",
                        subtitle = "BAT:$batCount • BOWL:$bowlCount • AR:$arCount • WK:$wkCount",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1.2f)
                    )
                    // Card 4: Attack Ratings
                    TeamMetricItem(
                        title = "Unit Ratings",
                        value = "BAT:$batStrength | BOWL:$bowlStrength",
                        subtitle = "Avg Squad Age: $avgAge Yrs",
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = BorderStroke(1.dp, Color(0xFFFFA000))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🔥 TEAM MORALE: ${viewModel.teamMoraleScore}%", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFFE65100))
                            Text("High morale boosts OVR in match simulations! Training intensity reduces morale.", fontSize = 10.sp, color = Color(0xFFBF360C))
                        }
                        Button(
                            onClick = { viewModel.conductSquadRetreat(10000000L) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("🧘 SQUAD RETREAT (₹1.0 Cr)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- FILTER CHIPS FOR ROSTER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "BATSMAN", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER").forEach { cat ->
                    val isSel = selectedFilter == cat
                    Surface(
                        modifier = Modifier.clickable { selectedFilter = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) Color(0xFF6750A4) else Color(0xFFEADDFF),
                        border = BorderStroke(1.dp, if (isSel) Color(0xFF6750A4) else Color(0xFFCAC4D0))
                    ) {
                        Text(
                            text = if (cat == "ALL") "ALL ($rosterCount)" else cat.replace("_", " "),
                            color = if (isSel) Color.White else Color(0xFF21005D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // --- MAIN CONTENT: ROSTER LIST & PLAYER TRAINING DETAIL ---
        if (roster.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your franchise roster is currently empty!", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Visit the IPL Auction Room to sign top international cricket stars and build your dream team.", color = Color(0xFF49454F), fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Roster List (Left column)
                LazyColumn(modifier = Modifier.weight(1.2f).padding(end = 8.dp)) {
                    items(filteredRoster) { player ->
                        PlayerListRow(
                            player = player,
                            isSelected = (selectedPlayer?.playerId ?: roster.firstOrNull()?.playerId) == player.playerId,
                            onSelect = { selectedPlayer = player }
                        )
                    }
                }

                // Selected Player Details & Academy Training (Right Column)
                Column(modifier = Modifier.weight(1.5f)) {
                    val p = selectedPlayer ?: roster.firstOrNull()
                    if (p != null) {
                        PlayerDetailCard(player = p, viewModel = viewModel)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a player to view attributes and train.", color = Color(0xFF49454F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMetricItem(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
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
fun PlayerListRow(player: PlayerEntity, isSelected: Boolean, onSelect: () -> Unit) {
    val avgOvr = (player.batting + player.bowling) / 2
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onSelect() }
            .testTag("player_row_${player.name.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEADDFF) else Color.White
        ),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF6750A4)) else BorderStroke(1.dp, Color(0xFFCAC4D0))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(player.name, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 13.sp)
                Text("OVR $avgOvr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val contractWarning = if (player.contractDuration <= 1) " ⚠️ ${player.contractDuration}Yr" else " • ${player.contractDuration}Yrs"
                Text(
                    text = player.category.replace("_", " ") + " • Age ${player.age}" + contractWarning,
                    fontSize = 10.sp,
                    color = if (player.contractDuration <= 1) Color(0xFFD32F2F) else Color(0xFF49454F),
                    fontWeight = if (player.contractDuration <= 1) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "BAT: ${player.batting} | BOWL: ${player.bowling}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F)
                )
            }
        }
    }
}

@Composable
fun PlayerDetailCard(player: PlayerEntity, viewModel: GameViewModel) {
    var activeAcademyTab by remember { mutableStateOf(0) } // 0: Stats, 1: Academy Training
    val avgOvr = (player.batting + player.bowling) / 2

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(player.name, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D1B20), fontSize = 16.sp)
                    Text("${player.nationality} • Age: ${player.age} • Category: ${player.category.replace("_", " ")}", fontSize = 11.sp, color = Color(0xFF49454F))
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8DEF8)
                ) {
                    Text("OVR $avgOvr", fontWeight = FontWeight.ExtraBold, color = Color(0xFF21005D), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Text("Salary: ₹${viewModel.formatCurrency(player.salary)} / Season", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
            
            Spacer(modifier = Modifier.height(10.dp))

            // Sub-tabs
            TabRow(
                selectedTabIndex = activeAcademyTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1D1B20),
                indicator = {}
            ) {
                listOf("ATTRIBUTES & STATS", "ACADEMY TRAINING", "CONTRACT & RENEWAL").forEachIndexed { index, title ->
                    Tab(selected = activeAcademyTab == index, onClick = { activeAcademyTab = index }) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(vertical = 6.dp), color = if (activeAcademyTab == index) Color(0xFF6750A4) else Color(0xFF49454F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activeAcademyTab == 0) {
                // STATS TAB
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text("Player Skill Attributes:", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                    StatBar("Batting Rating", player.batting)
                    StatBar("Bowling Rating", player.bowling)
                    StatBar("Fielding & Agility", player.fielding)
                    StatBar("Stamina & Fitness", player.fitness)
                    StatBar("Leadership & Mentality", player.leadership)
                    StatBar("Form & Morale", player.morale)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Career Performance Stats:", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Matches: ${player.matchesPlayed}", fontSize = 11.sp, color = Color(0xFF49454F))
                            Text("Runs Scored: ${player.runs}", fontSize = 11.sp, color = Color(0xFF49454F))
                        }
                        Column {
                            Text("Wickets Taken: ${player.wickets}", fontSize = 11.sp, color = Color(0xFF49454F))
                            Text("Centuries (100s): ${player.centuries}", fontSize = 11.sp, color = Color(0xFF49454F))
                        }
                    }
                }
            } else if (activeAcademyTab == 1) {
                // ACADEMY TRAINING TAB
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    // LONG TERM DEVELOPMENT PROGRAM
                    val currentAssignment = viewModel.academyAssignedPlayers[player.playerId]
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("🎓 LONG-TERM ROSTER DEVELOPMENT", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF1B5E20))
                                Text("${viewModel.academyAssignedPlayers.size}/3 Assigned", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Text("Assigned players steadily improve their stats after every match, rewarding long-term roster management without recurring fees!", fontSize = 10.sp, color = Color(0xFF333333), modifier = Modifier.padding(vertical = 4.dp))
                            
                            if (currentAssignment != null) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Active Track: $currentAssignment", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    Button(
                                        onClick = { viewModel.removeFromAcademy(player) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("UNASSIGN", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("BATTING", "BOWLING", "ALL_ROUND").forEach { track ->
                                        Button(
                                            onClick = { viewModel.assignToAcademy(player, track) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp).weight(1f)
                                        ) {
                                            Text("+ $track", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text("One-Time Intensive Drills (Immediate Rating Boost):", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))

                    val trainingOptions = listOf(
                        Triple("Batting Masterclass", "BATTING", 5000000L), // 50L
                        Triple("Bowling Drill & Pace", "BOWLING", 5000000L),
                        Triple("Agility & Fielding Drills", "FIELDING", 3000000L),
                        Triple("Cardio & Fitness Conditioning", "FITNESS", 2000000L),
                        Triple("Captaincy & Tactical Briefing", "CAPTAINCY", 4000000L),
                        Triple("Mental Counseling & Form", "MENTAL", 1500000L)
                    )

                    LazyColumn {
                        items(trainingOptions) { (label, stat, cost) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 11.sp)
                                    Text("Cost: ₹${viewModel.formatCurrency(cost)}", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                }

                                Button(
                                    onClick = { viewModel.trainPlayer(player, stat, cost) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("TRAIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            } else if (activeAcademyTab == 2) {
                // CONTRACT MANAGEMENT SYSTEM TAB
                val isAging = player.age >= 30
                val hikePercentage = if (player.age >= 34) 35L else if (player.age >= 32) 25L else if (player.age >= 30) 15L else 10L
                val oneYearSalary = player.salary + (player.salary * hikePercentage / 100L)
                val threeYearSalary = player.salary + (player.salary * (hikePercentage + 10L) / 100L)

                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isAging) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, if (isAging) Color(0xFFE65100) else Color(0xFF2E7D32))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isAging) "👴 AGING VETERAN PRO" else "⚡ PRIME ROSTER CONTRACT", fontWeight = FontWeight.ExtraBold, color = if (isAging) Color(0xFFE65100) else Color(0xFF1B5E20), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isAging) "Aging players (30+ yrs) demand higher salary increments for extensions due to veteran experience and leadership prestige (+${hikePercentage}% base demand)."
                                else "Standard contract extension rules apply (+${hikePercentage}% annual salary growth).",
                                fontSize = 10.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    }

                    Text("Current Contract Status:", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining Duration:", fontSize = 11.sp, color = Color(0xFF49454F))
                        Text("${player.contractDuration} Seasons (${if (player.contractDuration <= 1) "⚠️ EXPIRING SOON" else "✔ SECURE"})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (player.contractDuration <= 1) Color(0xFFD32F2F) else Color(0xFF2E7D32))
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Salary:", fontSize = 11.sp, color = Color(0xFF49454F))
                        Text("₹${viewModel.formatCurrency(player.salary)} / Season", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                    }

                    Text("Available Extension Offers:", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))

                    // Offer 1: 1-Year Extension
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                        border = BorderStroke(1.dp, Color(0xFF6750A4))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("1-Season Extension (+1 Yr)", fontWeight = FontWeight.Bold, color = Color(0xFF21005D), fontSize = 11.sp)
                                Text("New Salary: ₹${viewModel.formatCurrency(oneYearSalary)} / season (+${hikePercentage}%)", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.renewPlayerContract(player, 1, oneYearSalary) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("RENEW (+1 YR)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Offer 2: 3-Year Extension
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                        border = BorderStroke(1.dp, Color(0xFF6750A4))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("3-Season Extension (+3 Yrs)", fontWeight = FontWeight.Bold, color = Color(0xFF21005D), fontSize = 11.sp)
                                Text("New Salary: ₹${viewModel.formatCurrency(threeYearSalary)} / season (+${hikePercentage + 10}%)", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.renewPlayerContract(player, 3, threeYearSalary) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("RENEW (+3 YRS)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBar(label: String, value: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "stat_bar_anim"
    )
    val barColor by animateColorAsState(
        targetValue = if (value >= 85) Color(0xFF2E7D32) else if (value >= 70) Color(0xFFF9A825) else Color(0xFFD32F2F),
        animationSpec = tween(500),
        label = "stat_bar_color"
    )
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 10.sp, color = Color(0xFF49454F))
            Text("$value/100", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            color = barColor,
            trackColor = Color(0xFFCAC4D0).copy(0.4f),
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp).clip(RoundedCornerShape(3.dp))
        )
    }
}
