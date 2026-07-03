package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreens(viewModel: GameViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFEF7FF), Color(0xFFF3EDF7))
    )

    // Config Dialog for Custom Firebase Settings
    if (viewModel.showFirebaseConfigDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showFirebaseConfigDialog = false },
            title = { Text("Cloud Setup Configuration", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "You can connect IPL Tycoon directly to your own Firebase project for cloud saves across multiple PCs or devices.",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.firebaseApiKeyInput,
                        onValueChange = { viewModel.firebaseApiKeyInput = it },
                        label = { Text("Firebase Web API Key") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.firebaseProjectIdInput,
                        onValueChange = { viewModel.firebaseProjectIdInput = it },
                        label = { Text("Firebase Project ID") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.firebaseApiKeyInput = "AIzaSyAs_DEMO_KEY_ipl_tycoon_728f"
                            viewModel.firebaseProjectIdInput = "ipl-tycoon-sync-demo"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEADDFF)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text("Load Default Demo Credentials", color = Color(0xFF21005D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.firebaseSync.apiKey = viewModel.firebaseApiKeyInput
                        viewModel.firebaseSync.projectId = viewModel.firebaseProjectIdInput
                        viewModel.showFirebaseConfigDialog = false
                        viewModel.firebaseStatusMsg = "Cloud configuration updated!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("Save Settings", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showFirebaseConfigDialog = false }) {
                    Text("Cancel", color = Color(0xFF6750A4))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFCAC4D0))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // High Density Header
                Text(
                    text = "IPL TYCOON",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6750A4),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Cricket Franchise Simulator",
                    fontSize = 14.sp,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Error / Success / Cloud Status Feedback
                if (viewModel.loginError.isNotEmpty()) {
                    Text(
                        text = viewModel.loginError,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (viewModel.resetSuccessMsg.isNotEmpty()) {
                    Text(
                        text = viewModel.resetSuccessMsg,
                        color = Color(0xFF2E7D32),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (viewModel.firebaseStatusMsg.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
                        border = BorderStroke(1.dp, Color(0xFFEADDFF))
                    ) {
                        Text(
                            text = viewModel.firebaseStatusMsg,
                            color = Color(0xFF6750A4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Cloud Sync Authentication ONLY
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = viewModel.firebaseEmail,
                    onValueChange = { viewModel.firebaseEmail = it },
                    label = { Text("Cloud Email Address") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0),
                        focusedTextColor = Color(0xFF1D1B20),
                        unfocusedTextColor = Color(0xFF1D1B20),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedLabelColor = Color(0xFF49454F),
                        focusedLeadingIconColor = Color(0xFF6750A4),
                        unfocusedLeadingIconColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = viewModel.firebasePassword,
                    onValueChange = { viewModel.firebasePassword = it },
                    label = { Text("Cloud Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0),
                        focusedTextColor = Color(0xFF1D1B20),
                        unfocusedTextColor = Color(0xFF1D1B20),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedLabelColor = Color(0xFF49454F),
                        focusedLeadingIconColor = Color(0xFF6750A4),
                        unfocusedLeadingIconColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                if (isRegisterMode) {
                    Button(
                        onClick = { viewModel.firebaseSignUp() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        enabled = !viewModel.isFirebaseSyncLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_button")
                    ) {
                        if (viewModel.isFirebaseSyncLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Create Cloud Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = { isRegisterMode = false },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Already have a cloud profile? Log In", color = Color(0xFF6750A4))
                    }
                } else {
                    Button(
                        onClick = { viewModel.firebaseSignIn() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        enabled = !viewModel.isFirebaseSyncLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        if (viewModel.isFirebaseSyncLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign In with Firebase", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { isRegisterMode = true }) {
                            Text("New Cloud Account", color = Color(0xFF6750A4))
                        }
                        TextButton(onClick = { viewModel.showFirebaseConfigDialog = true }) {
                            Text("Cloud Setup Settings", color = Color(0xFF6750A4).copy(alpha = 0.7f))
                        }
                    }
                }

                // Instant Offline Play Button
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEADDFF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.instantOfflinePlay() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                    border = BorderStroke(1.5.dp, Color(0xFF2E7D32)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("offline_play_button")
                ) {
                    Text("🏏 Instant Offline Play (No Login Required)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
