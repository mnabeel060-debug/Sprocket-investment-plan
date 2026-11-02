package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepositRequestEntity
import com.example.data.model.RequestStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WithdrawalRequestEntity
import com.example.ui.components.MetricCard
import com.example.ui.components.SimulationDisclaimerBanner
import com.example.ui.components.StatusBadge
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
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusProcessing
import com.example.ui.theme.StatusRejected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ForexViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    viewModel: ForexViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()
    val allInvestments by viewModel.allInvestments.collectAsState()
    val settings by viewModel.settingsMap.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Deposits", "Profit Engine", "Withdrawals", "Investors", "Settings")

    val pendingDeposits = allDeposits.filter { it.status == RequestStatus.PENDING }
    val pendingWithdrawals = allWithdrawals.filter { it.status == RequestStatus.PENDING || it.status == RequestStatus.APPROVED || it.status == RequestStatus.PROCESSING }

    val totalDepositsVolume = allDeposits.filter { it.status == RequestStatus.APPROVED }.sumOf { it.amount }
    val totalActiveCapital = allInvestments.filter { it.status == com.example.data.model.InvestmentStatus.ACTIVE }.sumOf { it.investedAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp)
            .testTag("admin_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = AccentGold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Admin Control Desk",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Super Administrator Access",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            SimulationDisclaimerBanner()
        }

        // Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavySurface,
                contentColor = AccentCyan,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    val badgeCount = when (index) {
                        1 -> pendingDeposits.size
                        3 -> pendingWithdrawals.size
                        else -> 0
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                                if (badgeCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AccentGold)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = "$badgeCount", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> { // Overview
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Registered Investors",
                            value = "${allUsers.size}",
                            icon = Icons.Default.Group,
                            iconTint = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Active Investments",
                            value = "$${totalActiveCapital.toInt()}",
                            icon = Icons.Default.AccountBalance,
                            iconTint = AccentCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Approved Deposits",
                            value = "$${totalDepositsVolume.toInt()}",
                            icon = Icons.Default.Payments,
                            iconTint = ProfitGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Pending Approvals",
                            value = "${pendingDeposits.size}",
                            icon = Icons.Default.ShowChart,
                            iconTint = AccentGold,
                            modifier = Modifier.weight(1f),
                            highlightColor = if (pendingDeposits.isNotEmpty()) AccentGold else null
                        )
                    }
                }
            }
            1 -> { // Deposits Management
                if (allDeposits.isEmpty()) {
                    item {
                        EmptyCard(text = "No deposit requests submitted yet")
                    }
                } else {
                    items(allDeposits) { dep ->
                        AdminDepositCard(
                            deposit = dep,
                            onApprove = { viewModel.adminApproveDeposit(dep.id) },
                            onReject = { viewModel.adminRejectDeposit(dep.id) }
                        )
                    }
                }
            }
            2 -> { // Profit & Loss Distribution Engine
                item {
                    AdminProfitDistributionSection(viewModel)
                }
            }
            3 -> { // Withdrawals Management
                if (allWithdrawals.isEmpty()) {
                    item {
                        EmptyCard(text = "No withdrawal requests found")
                    }
                } else {
                    items(allWithdrawals) { wth ->
                        AdminWithdrawalCard(
                            withdrawal = wth,
                            onUpdateStatus = { newStatus -> viewModel.adminUpdateWithdrawalStatus(wth.id, newStatus) }
                        )
                    }
                }
            }
            4 -> { // Investors List
                items(allUsers) { usr ->
                    AdminUserCard(usr)
                }
            }
            5 -> { // Settings
                item {
                    AdminSettingsSection(settings, onSave = { k, v -> viewModel.adminSaveSetting(k, v) })
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AdminDepositCard(
    deposit: DepositRequestEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.US)
    val dateStr = dateFormat.format(Date(deposit.createdAt))

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (deposit.status == RequestStatus.PENDING) AccentGold.copy(alpha = 0.5f) else NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_deposit_${deposit.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = deposit.investorName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${deposit.investorId} • ${deposit.packageName}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${deposit.amount.toInt()}",
                        color = ProfitGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    StatusBadge(status = deposit.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurfaceVariant)
                    .padding(10.dp)
            ) {
                Text(text = "Sender Mobile: ${deposit.senderNumber}", color = TextPrimary, fontSize = 12.sp)
                Text(text = "Transaction ID: ${deposit.transactionId}", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (deposit.receiptNote.isNotBlank()) {
                    Text(text = "Screenshot Note: ${deposit.receiptNote}", color = TextSecondary, fontSize = 11.sp)
                }
                Text(text = "Submitted: $dateStr", color = TextMuted, fontSize = 10.sp)
            }

            if (deposit.status == RequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_deposit_${deposit.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LossRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_deposit_${deposit.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Credit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProfitDistributionSection(viewModel: ForexViewModel) {
    var percentInput by remember { mutableStateOf("14.5") }
    var sessionNote by remember { mutableStateOf("London Session GBP/USD Breakout Trade") }
    val allInvestments by viewModel.allInvestments.collectAsState()
    val activeCount = allInvestments.count { it.status == com.example.data.model.InvestmentStatus.ACTIVE }

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_profit_distribution_section")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = ProfitGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Profit & Loss Distribution Engine", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Distribute verified trading performance proportionally to all active investments", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurfaceVariant)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Active Investments in Cycle: $activeCount\nTarget profit ratio is 20% over 7 days. Enter actual verified session return (positive for profit, negative for drawdown).",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            OutlinedTextField(
                value = percentInput,
                onValueChange = { percentInput = it },
                label = { Text("Session Return (%)") },
                placeholder = { Text("e.g. 15.0 or -5.5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_percent_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = NavyBorder
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = sessionNote,
                onValueChange = { sessionNote = it },
                label = { Text("Trading Session Reference / Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_session_note_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = NavyBorder
                ),
                singleLine = true
            )

            val parsedPercent = percentInput.toDoubleOrNull() ?: 0.0

            Button(
                onClick = {
                    viewModel.adminDistributeProfitLoss(parsedPercent, sessionNote)
                },
                enabled = activeCount > 0 && percentInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("execute_profit_distribution_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (parsedPercent >= 0) ProfitGreen else LossRed,
                    contentColor = if (parsedPercent >= 0) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Distribute ${if (parsedPercent >= 0) "+$parsedPercent%" else "$parsedPercent%"} to $activeCount Investments",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdminWithdrawalCard(
    withdrawal: WithdrawalRequestEntity,
    onUpdateStatus: (RequestStatus) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_withdrawal_${withdrawal.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = withdrawal.investorName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "ID: ${withdrawal.investorId} • ${withdrawal.paymentMethod}", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "$${String.format(Locale.US, "%.2f", withdrawal.amount)}", color = AccentCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    StatusBadge(status = withdrawal.status)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Holder: ${withdrawal.accountHolder} | Account: ${withdrawal.accountNumber}", color = TextPrimary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (withdrawal.status == RequestStatus.PENDING) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(RequestStatus.REJECTED) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LossRed)
                    ) {
                        Text("Reject & Refund", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onUpdateStatus(RequestStatus.APPROVED) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                    ) {
                        Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (withdrawal.status == RequestStatus.APPROVED || withdrawal.status == RequestStatus.PROCESSING) {
                    Button(
                        onClick = { onUpdateStatus(RequestStatus.PAID) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted)
                    ) {
                        Text("Mark as Dispatched & Paid", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(user: UserEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = user.fullName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "ID: ${user.investorId} • ${user.role}", color = AccentCyan, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NavySurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Balance: $${String.format(Locale.US, "%.2f", user.virtualBalance)}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Email: ${user.email} | Mobile: ${user.mobileNumber}", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun AdminSettingsSection(
    settings: Map<String, String>,
    onSave: (key: String, value: String) -> Unit
) {
    var holder by remember(settings) { mutableStateOf(settings["easypaisa_holder"] ?: "Forex Trading Desk") }
    var number by remember(settings) { mutableStateOf(settings["easypaisa_number"] ?: "03135445331") }
    var bankHolder by remember(settings) { mutableStateOf(settings["bank_holder"] ?: "Forex Trading Management Ltd") }
    var bankIban by remember(settings) { mutableStateOf(settings["bank_account"] ?: "PK89BAHL0001928374829102") }

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_settings_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = AccentCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Receiving Account Configuration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Text(
                text = "These details are displayed to all investors in the 'Buy Package' payment sheet.",
                color = TextSecondary,
                fontSize = 11.sp
            )

            OutlinedTextField(
                value = holder,
                onValueChange = { holder = it },
                label = { Text("Easypaisa Account Holder Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_easypaisa_holder_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = NavyBorder
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Easypaisa Account / Mobile Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_easypaisa_number_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = NavyBorder
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = bankHolder,
                onValueChange = { bankHolder = it },
                label = { Text("Bank Account Holder") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = NavyBorder
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = bankIban,
                onValueChange = { bankIban = it },
                label = { Text("Bank IBAN / Account Number") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = NavyBorder
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    onSave("easypaisa_holder", holder)
                    onSave("easypaisa_number", number)
                    onSave("bank_holder", bankHolder)
                    onSave("bank_account", bankIban)
                    Toast.makeText(context, "Payment details saved successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_admin_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Payment Details", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyCard(text: String) {
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
            Text(text = text, color = TextSecondary, fontSize = 13.sp)
        }
    }
}
