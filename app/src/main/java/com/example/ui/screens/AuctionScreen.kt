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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerEntity
import com.example.data.isStatsKnown
import com.example.viewmodel.GameViewModel

@Composable
fun AuctionScreen(viewModel: GameViewModel) {
    // Collect from Central State Management Provider (GameProgressStore)
    val franchiseTeam by viewModel.progressStore.activeFranchiseTeam.collectAsState()
    val remainingBudget by viewModel.progressStore.franchiseBalance.collectAsState()
    val franchiseRoster by viewModel.progressStore.franchiseRoster.collectAsState()
    val allPlayersFromStore by viewModel.progressStore.allPlayers.collectAsState()

    // Fallback to viewModel list if store is still initializing
    val allPlayersList = if (allPlayersFromStore.isNotEmpty()) allPlayersFromStore else viewModel.allPlayers
    val unassignedPlayers = allPlayersList.filter { it.teamId == null }
    val activeAuctionPlayer = viewModel.auctionPlayer

    // Category Filter for Draft Pool
    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "BATSMAN", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER")
    val filteredPlayers = if (selectedCategory == "ALL") {
        unassignedPlayers
    } else {
        unassignedPlayers.filter { it.category == selectedCategory }
    }

    val rosterCount = franchiseRoster.size
    val isRosterFull = rosterCount >= 25

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // --- 1. FRANCHISE BUDGET & AUCTION STATUS HEADER ---
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
                        "💰 FRANCHISE AUCTION COMMAND CENTER",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6750A4),
                        fontSize = 11.sp
                    )
                    val teamName = franchiseTeam?.name ?: "My Franchise"
                    Text(teamName, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Remaining Budget", fontSize = 10.sp, color = Color(0xFF49454F))
                        Text(
                            "₹" + viewModel.formatCurrency(remainingBudget),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (remainingBudget > 200000000L) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Squad Roster Slots", fontSize = 10.sp, color = Color(0xFF49454F))
                        Text(
                            "$rosterCount / 25 Players",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRosterFull) Color(0xFFD32F2F) else Color(0xFF1D1B20)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (rosterCount / 25f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF6750A4),
                    trackColor = Color(0xFFEADDFF)
                )
            }
        }

        // --- 2. ACTION ROW & SIMULATION ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("IPL PLAYER DRAFT POOL", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (filteredPlayers.any { !it.isStatsKnown }) {
                    Button(
                        onClick = { viewModel.scoutBatchPlayers(filteredPlayers, 10000000L) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C4C)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp).testTag("scout_batch_button")
                    ) {
                        Text("🔍 SCOUT ALL (₹1 Cr)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (unassignedPlayers.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.simulateRemainingAuctions() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp).testTag("simulate_all_auctions_button")
                    ) {
                        Text("⚡ AUTO-SIMULATE ALL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. CATEGORY FILTER TABS ---
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF6750A4),
            indicator = {},
            divider = {},
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clickable { selectedCategory = cat },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF6750A4) else Color(0xFFEADDFF),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0))
                ) {
                    Text(
                        text = cat.replace("_", " "),
                        color = if (isSelected) Color.White else Color(0xFF21005D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- 4. MAIN AUCTION ROOM LAYOUT ---
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Block: Available Players List with Base Prices
            Column(modifier = Modifier.weight(1.1f).padding(end = 8.dp)) {
                Text(
                    "AVAILABLE (${filteredPlayers.size})",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (filteredPlayers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No available players in this category.", color = Color(0xFF49454F), fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredPlayers) { player ->
                            val isCurrent = activeAuctionPlayer?.playerId == player.playerId
                            val valPrice = if (player.isStatsKnown) viewModel.calculatePlayerPerformanceValuation(player) else 0L
                            val ovrText = if (player.isStatsKnown) "OVR: ${(player.batting + player.bowling) / 2}" else "OVR: ??? (Hidden)"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .testTag("draft_item_${player.name.replace(" ", "_")}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) Color(0xFFEADDFF) else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isCurrent) Color(0xFF6750A4) else Color(0xFFCAC4D0))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(player.name, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 12.sp)
                                        Text("Age ${player.age}", fontSize = 10.sp, color = Color(0xFF49454F))
                                    }
                                    Text(
                                        player.category.replace("_", " ") + " • $ovrText",
                                        fontSize = 10.sp,
                                        color = if (player.isStatsKnown) Color(0xFF49454F) else Color(0xFFB3261E),
                                        fontWeight = if (player.isStatsKnown) FontWeight.Normal else FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Base Price: ₹" + viewModel.formatCurrency(player.salary), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                                            if (player.isStatsKnown) {
                                                Text("Val: ₹${viewModel.formatCurrency(valPrice)}", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                            } else {
                                                Text("Val: ??? (Unscouted)", fontSize = 9.sp, color = Color(0xFFB3261E), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (!player.isStatsKnown) {
                                                Button(
                                                    onClick = { viewModel.scoutAuctionPlayer(player, 2500000L) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C4C)),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(24.dp).testTag("scout_button_${player.name.replace(" ", "_")}")
                                                ) {
                                                    Text("🔍 ₹25L", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Button(
                                                onClick = { viewModel.selectAuctionPlayer(player) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp).testTag("start_auction_${player.name.replace(" ", "_")}")
                                            ) {
                                                Text("AUCTION", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Block: Live Interactive Bidding Room
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activeAuctionPlayer == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a player from the available list to begin bidding.", color = Color(0xFF49454F), fontSize = 12.sp)
                    }
                } else {
                    Text("🔥 LIVE BIDDING ROOM", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4), fontSize = 13.sp)
                    Text(activeAuctionPlayer.name, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D1B20), fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
                    Text(activeAuctionPlayer.category.replace("_", " ") + " • ${activeAuctionPlayer.nationality} (Age: ${activeAuctionPlayer.age})", fontSize = 11.sp, color = Color(0xFF49454F))

                    if (activeAuctionPlayer.isStatsKnown) {
                        val valPrice = viewModel.calculatePlayerPerformanceValuation(activeAuctionPlayer)
                        Text("BAT ${activeAuctionPlayer.batting} • BOWL ${activeAuctionPlayer.bowling} • FIT ${activeAuctionPlayer.fitness}", fontSize = 11.sp, color = Color(0xFF49454F))
                        Text("Base: ₹${viewModel.formatCurrency(activeAuctionPlayer.salary)} • Est. Val: ₹${viewModel.formatCurrency(valPrice)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 2.dp))
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFDAD6),
                            border = BorderStroke(1.dp, Color(0xFFB3261E))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🕵️ HIDDEN STATS (UNSCOUTED)", fontWeight = FontWeight.Bold, color = Color(0xFFB3261E), fontSize = 11.sp)
                                Text("BAT ??? • BOWL ??? • FIT ??? • Est. Val ???", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF410E0B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.scoutAuctionPlayer(activeAuctionPlayer, 2500000L) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C4C)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("scout_live_button")
                                ) {
                                    Text("🔍 SCOUT NOW (₹25L)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Current Bid Price Panel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("CURRENT BID AMOUNT", fontSize = 10.sp, color = Color(0xFF49454F))
                            Text("₹" + viewModel.formatCurrency(viewModel.currentBidPrice), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                            
                            val bidderName = viewModel.allTeams.find { it.teamId == viewModel.highestBidderId }?.name ?: "None"
                            Text("Highest Bidder: $bidderName", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bidding History Log Terminal
                    Text("Bidding War Log:", fontSize = 10.sp, color = Color(0xFF49454F), modifier = Modifier.align(Alignment.Start))
                    val logState = rememberLazyListState()
                    LaunchedEffect(viewModel.auctionLog.size) {
                        if (viewModel.auctionLog.isNotEmpty()) {
                            logState.animateScrollToItem(viewModel.auctionLog.size - 1)
                        }
                    }
                    LazyColumn(
                        state = logState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFFEF7FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        items(viewModel.auctionLog) { log ->
                            Text(
                                log,
                                fontSize = 10.sp,
                                color = if (log.contains("You bid") || log.contains("SOLD")) Color(0xFF2E7D32) else Color(0xFF1D1B20),
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Budget checks for user bid
                    val canAffordNextBid = remainingBudget >= (viewModel.currentBidPrice + 2000000L)
                    if (!canAffordNextBid && !viewModel.auctionEnded) {
                        Text("⚠️ Insufficient remaining budget to raise bid!", fontSize = 10.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    if (isRosterFull && !viewModel.auctionEnded) {
                        Text("⚠️ Squad roster full (25/25)!", fontSize = 10.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    }

                    if (!viewModel.auctionEnded) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { viewModel.skipOrPassAuction() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                                modifier = Modifier.weight(1f).padding(end = 4.dp).testTag("pass_bid_button")
                            ) {
                                Text("PASS / SKIP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { viewModel.placeUserBid() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                enabled = viewModel.userBiddingEnabled && canAffordNextBid && !isRosterFull,
                                modifier = Modifier.weight(1.2f).padding(start = 4.dp).testTag("place_bid_button")
                            ) {
                                Text("RAISE BID", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                val remaining = allPlayersList.filter { it.teamId == null }
                                if (remaining.isNotEmpty()) {
                                    viewModel.selectAuctionPlayer(remaining.random())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                            modifier = Modifier.fillMaxWidth().testTag("next_player_button")
                        ) {
                            Text("NEXT AVAILABLE PLAYER", color = Color(0xFF21005D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
