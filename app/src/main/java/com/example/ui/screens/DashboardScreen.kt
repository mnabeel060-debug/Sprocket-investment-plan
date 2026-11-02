package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentStatus
import com.example.data.model.UserInvestmentEntity
import com.example.ui.components.CountdownTimerWidget
import com.example.ui.components.MarketTickerBar
import com.example.ui.components.MetricCard
import com.example.ui.components.PortfolioGrowthChart
import com.example.ui.components.SimulationDisclaimerBanner
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ForexViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ForexViewModel,
    onNavigateToPackages: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val investments by viewModel.userInvestments.collectAsState()
    val deposits by viewModel.userDeposits.collectAsState()
    val withdrawals by viewModel.userWithdrawals.collectAsState()
    val tickers by viewModel.marketTickers.collectAsState()

    val pendingDepositsCount = deposits.count { it.status == com.example.data.model.RequestStatus.PENDING }
    val pendingWithdrawalsCount = withdrawals.count { it.status == com.example.data.model.RequestStatus.PENDING }
    val activeInvestments = investments.filter { it.status == InvestmentStatus.ACTIVE }
    val completedInvestments = investments.filter { it.status == InvestmentStatus.COMPLETED }

    val user = currentUser ?: return

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Profile banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${user.fullName}",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Investor ID: ",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = user.investorId,
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElectricBlue.copy(alpha = 0.2f))
                        .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VERIFIED INVESTOR",
                        color = ElectricBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            SimulationDisclaimerBanner()
        }

        item {
            MarketTickerBar(tickers = tickers)
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToPackages,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_buy_package_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invest Packages", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onNavigateToWithdraw,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_withdraw_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Withdraw", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Primary Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Available Balance",
                    value = "$${String.format(Locale.US, "%.2f", user.virtualBalance)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = AccentCyan,
                    modifier = Modifier.weight(1f),
                    highlightColor = AccentCyan,
                    subtitle = "Liquid Virtual Funds"
                )
                MetricCard(
                    title = "Total Invested",
                    value = "$${String.format(Locale.US, "%.2f", user.totalInvested)}",
                    icon = Icons.Default.AccountBalance,
                    iconTint = ElectricBlue,
                    modifier = Modifier.weight(1f),
                    subtitle = "${activeInvestments.size} Active Packages"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Actual Profit Earned",
                    value = "+$${String.format(Locale.US, "%.2f", user.totalProfitEarned)}",
                    icon = Icons.Default.ShowChart,
                    iconTint = ProfitGreen,
                    modifier = Modifier.weight(1f),
                    highlightColor = ProfitGreen,
                    subtitle = "Trading Yield Credited"
                )
                MetricCard(
                    title = "Completed Cycles",
                    value = "${completedInvestments.size}",
                    icon = Icons.Default.CheckCircle,
                    iconTint = StatusCompleted,
                    modifier = Modifier.weight(1f),
                    subtitle = "7-Day Cycles Finished"
                )
            }
        }

        // Pending Status Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Pending Requests", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Deposits: $pendingDepositsCount", color = if (pendingDepositsCount > 0) AccentGold else TextMuted, fontSize = 12.sp)
                        Text(text = "Withdrawals: $pendingWithdrawalsCount", color = if (pendingWithdrawalsCount > 0) AccentGold else TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }

        // Equity Chart
        item {
            PortfolioGrowthChart()
        }

        // Active Investments Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Investments (${activeInvestments.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "7-Day Cycle",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (activeInvestments.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalAtm,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No active investments yet", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "Choose from 5 packages ($50 - $1,000) to activate trading simulation.", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToPackages,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Explore Packages")
                        }
                    }
                }
            }
        } else {
            items(activeInvestments) { inv ->
                InvestmentItemCard(inv)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InvestmentItemCard(inv: UserInvestmentEntity) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val startDateStr = dateFormat.format(Date(inv.startDate))
    val maturityDateStr = dateFormat.format(Date(inv.maturityDate))
    val isProfit = inv.actualProfitOrLoss >= 0

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("investment_card_${inv.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = inv.packageName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Invested: $${inv.investedAmount.toInt()}",
                        color = AccentCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "ACTIVE", color = ElectricBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Start Date", color = TextSecondary, fontSize = 11.sp)
                    Text(text = startDateStr, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text(text = "Maturity Date", color = TextSecondary, fontSize = 11.sp)
                    Text(text = maturityDateStr, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Actual P/L", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${if (isProfit) "+" else ""}$${String.format(Locale.US, "%.2f", inv.actualProfitOrLoss)}",
                        color = if (isProfit) ProfitGreen else LossRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            CountdownTimerWidget(maturityDateMillis = inv.maturityDate)
        }
    }
}
