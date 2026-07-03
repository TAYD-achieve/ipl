package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceEntity
import com.example.viewmodel.GameViewModel

@Composable
fun FinancesScreen(viewModel: GameViewModel) {
    val financesList = viewModel.finances
    var activeCategoryTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Ledgers

    // Calculate income vs expense aggregates
    val totalIncome = financesList.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = financesList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val profitAndLoss = totalIncome - totalExpense

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("FRANCHISE FINANCES", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))

        // Balance Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFCAC4D0))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Account Balance", fontSize = 12.sp, color = Color(0xFF49454F))
                    val teamBal = viewModel.activeTeam?.balance ?: 0L
                    Text("₹" + viewModel.formatCurrency(teamBal), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Net Season Profit/Loss", fontSize = 11.sp, color = Color(0xFF49454F))
                    val pnlColor = if (profitAndLoss >= 0) Color(0xFF2E7D32) else Color(0xFFBA1A1A)
                    val pnlSign = if (profitAndLoss >= 0) "+" else ""
                    Text(
                        text = "$pnlSign₹${viewModel.formatCurrency(profitAndLoss)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = activeCategoryTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF1D1B20),
            indicator = {}
        ) {
            Tab(selected = activeCategoryTab == 0, onClick = { activeCategoryTab = 0 }) {
                Text("CHARTS & METRICS", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp), color = if (activeCategoryTab == 0) Color(0xFF6750A4) else Color(0xFF49454F))
            }
            Tab(selected = activeCategoryTab == 1, onClick = { activeCategoryTab = 1 }) {
                Text("TRANSACTION LEDGER", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp), color = if (activeCategoryTab == 1) Color(0xFF6750A4) else Color(0xFF49454F))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeCategoryTab == 0) {
            // Dashboard with custom charts
            Row(modifier = Modifier.fillMaxSize()) {
                // Charts block (Left half)
                Column(modifier = Modifier.weight(1.3f).padding(end = 8.dp)) {
                    Text("Season Revenue vs Expenses", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20), modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        FinancialBarChart(income = totalIncome, expense = totalExpense)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Financial Summary Indicators", fontSize = 11.sp, color = Color(0xFF49454F))
                    Text("• Revenue is driven by ticket bookings, jerseys, cap sales, and signed contracts.", fontSize = 11.sp, color = Color(0xFF1D1B20), modifier = Modifier.padding(top = 4.dp))
                    Text("• Major costs include drafting bids, training academy facilities, and coach paychecks.", fontSize = 11.sp, color = Color(0xFF1D1B20), modifier = Modifier.padding(top = 2.dp))
                }

                // Breakdowns block (Right half)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, Color(0xFFCAC4D0)), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("INCOME STREAMS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                    val categories = listOf("TICKETS", "SPONSORS", "MERCHANDISE", "PRIZE_MONEY")
                    categories.forEach { cat ->
                        val amt = financesList.filter { it.type == "INCOME" && it.category == cat }.sumOf { it.amount }
                        Text("$cat: ₹${viewModel.formatCurrency(amt)}", fontSize = 11.sp, color = Color(0xFF1D1B20), modifier = Modifier.padding(vertical = 3.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFCAC4D0))

                    Text("EXPENSES BREAKDOWN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFBA1A1A))
                    val expCats = listOf("TRANSFERS", "SALARIES", "MAINTENANCE", "TRAINING")
                    expCats.forEach { cat ->
                        val amt = financesList.filter { it.type == "EXPENSE" && it.category == cat }.sumOf { it.amount }
                        Text("$cat: ₹${viewModel.formatCurrency(amt)}", fontSize = 11.sp, color = Color(0xFF1D1B20), modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }
        } else {
            // LEDGER VIEW (List of transactions)
            if (financesList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions logged yet.", color = Color(0xFF49454F))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(financesList) { finance ->
                        TransactionRow(finance = finance, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(finance: FinanceEntity, viewModel: GameViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(0.6f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = finance.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "Season ${finance.season} • Match Day ${finance.matchDay}",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F)
                )
            }

            val sign = if (finance.type == "INCOME") "+" else "-"
            val color = if (finance.type == "INCOME") Color(0xFF2E7D32) else Color(0xFFBA1A1A)
            Text(
                text = "$sign₹${viewModel.formatCurrency(finance.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
        }
    }
}

@Composable
fun FinancialBarChart(income: Long, expense: Long) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val total = (income + expense).toDouble()
        if (total == 0.0) return@Canvas

        val incomePercentage = income.toDouble() / total
        val expensePercentage = expense.toDouble() / total

        val spacing = 20.dp.toPx()
        val graphHeight = size.height - spacing * 2
        val barWidth = 40.dp.toPx()

        val maxPercentage = maxOf(incomePercentage, expensePercentage).coerceAtLeast(0.1)

        // Draw Income Bar
        val incHeight = (incomePercentage / maxPercentage) * graphHeight
        drawRoundRect(
            color = Color(0xFF2E7D32),
            topLeft = Offset(size.width * 0.25f - barWidth / 2f, size.height - spacing - incHeight.toFloat()),
            size = Size(barWidth, incHeight.toFloat()),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Draw Expense Bar
        val expHeight = (expensePercentage / maxPercentage) * graphHeight
        drawRoundRect(
            color = Color(0xFFBA1A1A),
            topLeft = Offset(size.width * 0.75f - barWidth / 2f, size.height - spacing - expHeight.toFloat()),
            size = Size(barWidth, expHeight.toFloat()),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Draw Labels
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#1D1B20")
                textSize = 11.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText("INCOME", size.width * 0.25f, size.height - 4.dp.toPx(), paint)
            drawText("EXPENSES", size.width * 0.75f, size.height - 4.dp.toPx(), paint)
        }
    }
}
