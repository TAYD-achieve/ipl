package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranchiseCreationScreen(viewModel: GameViewModel) {
    // Basic Details
    var teamName by remember { mutableStateOf("Bangalore Blasters") }
    var homeCity by remember { mutableStateOf("Bengaluru") }
    var teamSlogan by remember { mutableStateOf("Blaze to Glory!") }
    var stadiumName by remember { mutableStateOf("Blasters Arena") }

    // Logo Designer State
    var logoText by remember { mutableStateOf("BB") }
    var logoBgColor by remember { mutableStateOf("#FF9800") } // Amber
    var logoTextColor by remember { mutableStateOf("#FFFFFF") }
    var logoShape by remember { mutableStateOf("SHIELD") } // "CIRCLE", "SQUARE", "SHIELD", "STAR"

    // Jersey Designer State
    var jerseyPrimaryColor by remember { mutableStateOf("#FF9800") }
    var jerseySecondaryColor by remember { mutableStateOf("#1A237E") } // Indigo
    var jerseyNumber by remember { mutableStateOf("18") }
    var jerseyPlayerName by remember { mutableStateOf("KOHLI") }
    var jerseyTemplate by remember { mutableStateOf(1) } // 1: Stripes, 2: Solid, 3: Halves

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FRANCHISE CREATOR", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = Screen.SlotSelection }) {
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // General Info Header
            Text(
                text = "Build Your Cricket Dynasty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Basic Info
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Franchise Identity", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                    
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Franchise Team Name", color = Color(0xFF49454F)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedLabelColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("team_name_input")
                    )

                    OutlinedTextField(
                        value = homeCity,
                        onValueChange = { homeCity = it },
                        label = { Text("Home City", color = Color(0xFF49454F)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedLabelColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = teamSlogan,
                        onValueChange = { teamSlogan = it },
                        label = { Text("Team Slogan", color = Color(0xFF49454F)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedLabelColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = stadiumName,
                        onValueChange = { stadiumName = it },
                        label = { Text("Stadium Name", color = Color(0xFF49454F)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedLabelColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Step 2: Interactive Logo Designer
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("2. Interactive Logo Designer", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 16.sp, modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp))

                    // Realtime Drawn Logo Preview Box
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        LogoCanvas(
                            text = logoText,
                            bgColor = logoBgColor,
                            textColor = logoTextColor,
                            shape = logoShape
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Options
                    OutlinedTextField(
                        value = logoText,
                        onValueChange = { logoText = it.take(3).uppercase() },
                        label = { Text("Logo Text Initials (Max 3)", color = Color(0xFF49454F)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedLabelColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Text("Logo Frame Shape:", color = Color(0xFF1D1B20), modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("CIRCLE", "SHIELD", "SQUARE", "STAR").forEach { s ->
                            val isSelected = logoShape == s
                            Button(
                                onClick = { logoShape = s },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFEADDFF) else Color(0xFFF3EDF7)
                                ),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Text(s, color = if (isSelected) Color(0xFF21005D) else Color(0xFF49454F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Logo Background Color:", color = Color(0xFF1D1B20), modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                    ColorPickerRow(selectedColor = logoBgColor, onColorSelect = { logoBgColor = it })

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Logo Text Color:", color = Color(0xFF1D1B20), modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                    ColorPickerRow(selectedColor = logoTextColor, onColorSelect = { logoTextColor = it })
                }
            }

            // Step 3: Interactive Jersey Designer
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("3. Jersey Designer & Customization", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 16.sp, modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp))

                    // Jersey Preview Drawing
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FRONT", color = Color(0xFF49454F), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                            Box(modifier = Modifier.size(120.dp)) {
                                JerseyCanvas(
                                    primary = jerseyPrimaryColor,
                                    secondary = jerseySecondaryColor,
                                    template = jerseyTemplate,
                                    isBack = false,
                                    number = jerseyNumber,
                                    name = jerseyPlayerName
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BACK", color = Color(0xFF49454F), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                            Box(modifier = Modifier.size(120.dp)) {
                                JerseyCanvas(
                                    primary = jerseyPrimaryColor,
                                    secondary = jerseySecondaryColor,
                                    template = jerseyTemplate,
                                    isBack = true,
                                    number = jerseyNumber,
                                    name = jerseyPlayerName
                                )
                            }
                        }
                    }

                    // Configuration controls
                    OutlinedTextField(
                        value = jerseyPlayerName,
                        onValueChange = { jerseyPlayerName = it.take(12).uppercase() },
                        label = { Text("Player Name on Jersey", color = Color(0xFF49454F)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedLabelColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = jerseyNumber,
                        onValueChange = { jerseyNumber = it.take(2).filter { c -> c.isDigit() } },
                        label = { Text("Jersey Number (00-99)", color = Color(0xFF49454F)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedLabelColor = Color(0xFF6750A4),
                            unfocusedLabelColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Text("Primary Jersey Color:", color = Color(0xFF1D1B20), modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                    ColorPickerRow(selectedColor = jerseyPrimaryColor, onColorSelect = { jerseyPrimaryColor = it })

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Secondary Jersey Color:", color = Color(0xFF1D1B20), modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                    ColorPickerRow(selectedColor = jerseySecondaryColor, onColorSelect = { jerseySecondaryColor = it })

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Jersey Pattern Template:", color = Color(0xFF1D1B20), modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf(1, 2, 3).forEach { t ->
                            val isSelected = jerseyTemplate == t
                            val label = when (t) {
                                1 -> "STRIPES"
                                2 -> "SOLID"
                                else -> "HALVES"
                            }
                            Button(
                                onClick = { jerseyTemplate = t },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFFEADDFF) else Color(0xFFF3EDF7)),
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            ) {
                                Text(label, color = if (isSelected) Color(0xFF21005D) else Color(0xFF49454F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create Franchise Submit Action
            Button(
                onClick = {
                    viewModel.createFranchise(
                        name = teamName,
                        city = homeCity,
                        slogan = teamSlogan,
                        stadium = stadiumName,
                        logoText = logoText,
                        logoBg = logoBgColor,
                        logoTextCol = logoTextColor,
                        logoShape = logoShape,
                        jerseyPrimary = jerseyPrimaryColor,
                        jerseySecondary = jerseySecondaryColor,
                        jerseyNum = jerseyNumber,
                        jerseyPlName = jerseyPlayerName,
                        jerseyTemplate = jerseyTemplate
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(bottom = 12.dp)
                    .testTag("submit_franchise_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LAUNCH FRANCHISE TYCOON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ColorPickerRow(
    selectedColor: String,
    onColorSelect: (String) -> Unit
) {
    val hexColors = listOf("#F44336", "#E91E63", "#9C27B0", "#3F51B5", "#2196F3", "#00BCD4", "#4CAF50", "#FFEB3B", "#FF9800", "#795548", "#FFFFFF", "#212121")
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        hexColors.take(6).forEach { hex ->
            ColorDot(hex = hex, isSelected = selectedColor == hex, onColorSelect = onColorSelect)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        hexColors.drop(6).forEach { hex ->
            ColorDot(hex = hex, isSelected = selectedColor == hex, onColorSelect = onColorSelect)
        }
    }
}

@Composable
fun ColorDot(hex: String, isSelected: Boolean, onColorSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0).copy(0.5f),
                shape = CircleShape
            )
            .clickable { onColorSelect(hex) }
    )
}

@Composable
fun LogoCanvas(text: String, bgColor: String, textColor: String, shape: String) {
    Canvas(modifier = Modifier.size(120.dp)) {
        val parsedBg = Color(android.graphics.Color.parseColor(bgColor))
        val parsedText = Color(android.graphics.Color.parseColor(textColor))

        when (shape) {
            "CIRCLE" -> {
                drawCircle(color = parsedBg, radius = size.minDimension / 2f)
                drawCircle(color = parsedText, radius = size.minDimension / 2f, style = Stroke(width = 4.dp.toPx()))
            }
            "SQUARE" -> {
                drawRect(color = parsedBg)
                drawRect(color = parsedText, style = Stroke(width = 4.dp.toPx()))
            }
            "SHIELD" -> {
                val shieldPath = Path().apply {
                    moveTo(size.width / 2f, 4.dp.toPx())
                    lineTo(size.width - 4.dp.toPx(), 4.dp.toPx())
                    quadraticTo(size.width - 4.dp.toPx(), size.height * 0.6f, size.width / 2f, size.height - 4.dp.toPx())
                    quadraticTo(4.dp.toPx(), size.height * 0.6f, 4.dp.toPx(), 4.dp.toPx())
                    close()
                }
                drawPath(path = shieldPath, color = parsedBg)
                drawPath(path = shieldPath, color = parsedText, style = Stroke(width = 4.dp.toPx()))
            }
            "STAR" -> {
                val starPath = Path().apply {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val spikes = 5
                    val outerRadius = size.minDimension / 2f
                    val innerRadius = size.minDimension / 4f
                    var rot = Math.PI / 2 * 3
                    val step = Math.PI / spikes

                    moveTo(cx, cy - outerRadius)
                    for (i in 0 until spikes) {
                        val x = cx + Math.cos(rot).toFloat() * outerRadius
                        val y = cy + Math.sin(rot).toFloat() * outerRadius
                        lineTo(x, y)
                        rot += step

                        val x2 = cx + Math.cos(rot).toFloat() * innerRadius
                        val y2 = cy + Math.sin(rot).toFloat() * innerRadius
                        lineTo(x2, y2)
                        rot += step
                    }
                    close()
                }
                drawPath(path = starPath, color = parsedBg)
                drawPath(path = starPath, color = parsedText, style = Stroke(width = 3.dp.toPx()))
            }
        }

        // Draw overlay text
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor(textColor)
                textSize = 28.dp.toPx()
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText(text, size.width / 2f, (size.height / 2f) - ((paint.descent() + paint.ascent()) / 2f), paint)
        }
    }
}

@Composable
fun JerseyCanvas(primary: String, secondary: String, template: Int, isBack: Boolean, number: String, name: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val pColor = Color(android.graphics.Color.parseColor(primary))
        val sColor = Color(android.graphics.Color.parseColor(secondary))

        // Draw basic T-shirt outline path
        val jerseyPath = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.1f)
            lineTo(size.width * 0.7f, size.height * 0.1f)
            lineTo(size.width * 0.85f, size.height * 0.25f)
            lineTo(size.width * 0.75f, size.height * 0.35f)
            lineTo(size.width * 0.7f, size.height * 0.3f)
            lineTo(size.width * 0.7f, size.height * 0.9f)
            lineTo(size.width * 0.3f, size.height * 0.9f)
            lineTo(size.width * 0.3f, size.height * 0.3f)
            lineTo(size.width * 0.25f, size.height * 0.35f)
            lineTo(size.width * 0.15f, size.height * 0.25f)
            close()
        }

        drawPath(path = jerseyPath, color = pColor)

        // Draw pattern template overlays
        when (template) {
            1 -> { // Stripes
                for (i in 1..4) {
                    val stripeX = size.width * (0.3f + i * 0.08f)
                    drawLine(
                        color = sColor,
                        start = androidx.compose.ui.geometry.Offset(stripeX, size.height * 0.25f),
                        end = androidx.compose.ui.geometry.Offset(stripeX, size.height * 0.9f),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }
            3 -> { // Halves
                val halfPath = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.1f)
                    lineTo(size.width * 0.7f, size.height * 0.1f)
                    lineTo(size.width * 0.85f, size.height * 0.25f)
                    lineTo(size.width * 0.75f, size.height * 0.35f)
                    lineTo(size.width * 0.7f, size.height * 0.3f)
                    lineTo(size.width * 0.7f, size.height * 0.9f)
                    lineTo(size.width * 0.5f, size.height * 0.9f)
                    close()
                }
                drawPath(path = halfPath, color = sColor)
            }
            else -> { // Solid / Minimal details
                drawPath(path = jerseyPath, color = sColor, style = Stroke(width = 4.dp.toPx()))
            }
        }

        // Draw Neckline
        val neckPath = Path().apply {
            moveTo(size.width * 0.42f, size.height * 0.1f)
            quadraticTo(size.width * 0.5f, size.height * 0.2f, size.width * 0.58f, size.height * 0.1f)
        }
        drawPath(path = neckPath, color = sColor, style = Stroke(width = 5.dp.toPx()))

        // Draw Number / Player Name text overlays
        drawContext.canvas.nativeCanvas.apply {
            if (isBack) {
                // Name on back top
                val namePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor(secondary)
                    textSize = 10.dp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(name, size.width / 2f, size.height * 0.38f, namePaint)

                // Large Number in center
                val numPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor(secondary)
                    textSize = 30.dp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(number, size.width / 2f, size.height * 0.68f, numPaint)
            } else {
                // Front small number on bottom right
                val frontNumPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor(secondary)
                    textSize = 14.dp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(number, size.width * 0.6f, size.height * 0.6f, frontNumPaint)

                // SPONSOR place text
                val sponsorPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor(secondary)
                    textSize = 9.dp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("SPONSOR", size.width / 2f, size.height * 0.45f, sponsorPaint)
            }
        }
    }
}
