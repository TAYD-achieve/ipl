package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaveSlotEntity
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SlotSelectionScreen(viewModel: GameViewModel) {
    val slots by viewModel.saveSlots.collectAsState()
    
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFEF7FF), Color(0xFFF3EDF7))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Firebase Cloud Connection Status Card
            if (viewModel.firebaseSync.isLoggedIn) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                    border = BorderStroke(1.dp, Color(0xFF6750A4))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text("☁️", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text("Cloud Connection Active", fontWeight = FontWeight.Bold, color = Color(0xFF21005D), fontSize = 14.sp)
                                Text(viewModel.firebaseSync.email ?: "", fontSize = 12.sp, color = Color(0xFF49454F))
                            }
                        }
                        TextButton(
                            onClick = {
                                viewModel.firebaseSync.logout()
                                viewModel.firebaseStatusMsg = "Logged out from cloud."
                                viewModel.currentUser = null
                                viewModel.currentScreen = com.example.viewmodel.Screen.Login
                            }
                        ) {
                            Text("DISCONNECT", color = Color(0xFFB3261E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Status message
            if (viewModel.firebaseStatusMsg.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
                    border = BorderStroke(1.dp, Color(0xFFEADDFF))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.firebaseStatusMsg,
                            color = Color(0xFF6750A4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.firebaseStatusMsg = "" }) {
                            Text("DISMISS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                        }
                    }
                }
            }

            Text(
                text = "SELECT SAVE SLOT",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Welcome, ${viewModel.currentUser}! Pick a career profile to manage your franchise.",
                fontSize = 14.sp,
                color = Color(0xFF49454F),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            slots.forEach { slot ->
                SlotItem(
                    slot = slot,
                    viewModel = viewModel,
                    onSelect = { viewModel.selectSaveSlot(slot) },
                    onDelete = { viewModel.deleteSaveSlot(slot) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SlotItem(
    slot: SaveSlotEntity,
    viewModel: GameViewModel,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isCloudEnabled = viewModel.firebaseSync.isLoggedIn

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("slot_card_${slot.slotNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Career Slot #${slot.slotNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    
                    if (slot.isCreated) {
                        Text(
                            text = "Season: ${slot.currentSeason} • Matchday: ${slot.currentMatchDay}",
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        val date = Date(slot.lastPlayedTime)
                        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        Text(
                            text = "Saved: ${format.format(date)}",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = "Empty Save Slot",
                            fontSize = 14.sp,
                            color = Color(0xFF49454F),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (slot.isCreated) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Save", tint = Color.Red.copy(0.8f))
                        }

                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESUME", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF21005D))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CREATE", color = Color(0xFF21005D), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isCloudEnabled) {
                HorizontalDivider(color = Color(0xFFCAC4D0).copy(alpha = 0.4f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF7FF))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cloud Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                    Row {
                        TextButton(
                            onClick = { viewModel.firebaseUploadSlot(slot.slotId) },
                            enabled = slot.isCreated && !viewModel.isFirebaseSyncLoading,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6750A4))
                        ) {
                            Text("BACKUP ☁️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { viewModel.firebaseDownloadSlot(slot.slotId) },
                            enabled = !viewModel.isFirebaseSyncLoading,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6750A4))
                        ) {
                            Text("RESTORE 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Career Profile?", color = Color(0xFF1D1B20)) },
            text = { Text("Are you sure you want to permanently erase all progress in Slot #${slot.slotNumber}? This action is completely irreversible.", color = Color(0xFF49454F)) },
            containerColor = Color(0xFFF3EDF7),
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("ERASE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = Color(0xFF6750A4))
                }
            }
        )
    }
}

