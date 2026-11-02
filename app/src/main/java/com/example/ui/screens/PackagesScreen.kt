package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentPackageEntity
import com.example.data.model.PaymentMethod
import com.example.ui.components.CopyableAccountCard
import com.example.ui.components.SimulationDisclaimerBanner
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ForexViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    viewModel: ForexViewModel,
    modifier: Modifier = Modifier
) {
    val packages by viewModel.packages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.settingsMap.collectAsState()

    var selectedPackageForPurchase by remember { mutableStateOf<InvestmentPackageEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val easypaisaHolder = settings["easypaisa_holder"] ?: "Forex Trading Desk"
    val easypaisaNumber = settings["easypaisa_number"] ?: "03135445331"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp)
            .testTag("packages_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Forex Investment Packages",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fixed 7-day algorithmic simulation cycles with target return tracking",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        item {
            SimulationDisclaimerBanner()
        }

        item {
            // Quick Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Min Investment", color = TextSecondary, fontSize = 11.sp)
                        Text("$50", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cycle Duration", color = TextSecondary, fontSize = 11.sp)
                        Text("7 Days", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Target Profit", color = TextSecondary, fontSize = 11.sp)
                        Text("20% (Non-Guaranteed)", color = ProfitGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        items(packages) { pkg ->
            PackageCard(
                pkg = pkg,
                userBalance = currentUser?.virtualBalance ?: 0.0,
                onBuyClick = { selectedPackageForPurchase = pkg }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Purchase / Deposit Bottom Sheet
    selectedPackageForPurchase?.let { pkg ->
        ModalBottomSheet(
            onDismissRequest = { selectedPackageForPurchase = null },
            sheetState = sheetState,
            containerColor = NavySurface,
            contentColor = TextPrimary
        ) {
            BuyPackageSheetContent(
                pkg = pkg,
                userBalance = currentUser?.virtualBalance ?: 0.0,
                easypaisaHolder = easypaisaHolder,
                easypaisaNumber = easypaisaNumber,
                onActivateFromBalance = {
                    viewModel.activatePackage(pkg)
                    selectedPackageForPurchase = null
                },
                onSubmitDeposit = { senderNum, txnId, note ->
                    viewModel.submitDeposit(
                        amount = pkg.minInvestment,
                        pkgName = "${pkg.name} ($${pkg.minInvestment.toInt()})",
                        method = PaymentMethod.EASYPAISA,
                        senderNumber = senderNum,
                        txnId = txnId,
                        receiptNote = note
                    )
                    selectedPackageForPurchase = null
                },
                onClose = { selectedPackageForPurchase = null }
            )
        }
    }
}

@Composable
fun PackageCard(
    pkg: InvestmentPackageEntity,
    userBalance: Double,
    onBuyClick: () -> Unit
) {
    val isVip = pkg.id == "vip"
    val isPremium = pkg.id == "premium"

    val borderColor = when {
        isVip -> AccentGold
        isPremium -> AccentCyan
        else -> NavyBorder
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = if (isVip || isPremium) 0.8f else 1f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("package_card_${pkg.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(
                                if (isVip) AccentGold.copy(alpha = 0.2f)
                                else if (isPremium) AccentCyan.copy(alpha = 0.2f)
                                else ElectricBlue.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVip) Icons.Default.Star else Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (isVip) AccentGold else if (isPremium) AccentCyan else ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = pkg.name,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "7-Day Trading Term",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${pkg.minInvestment.toInt()}",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Target: 20%",
                        color = ProfitGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = pkg.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LockClock, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Duration: 7 Days", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Text("Variable Return", color = TextSecondary, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBuyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("buy_package_button_${pkg.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVip) AccentGold else ElectricBlue,
                    contentColor = if (isVip) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Buy ${pkg.name} Package ($${pkg.minInvestment.toInt()})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BuyPackageSheetContent(
    pkg: InvestmentPackageEntity,
    userBalance: Double,
    easypaisaHolder: String,
    easypaisaNumber: String,
    onActivateFromBalance: () -> Unit,
    onSubmitDeposit: (senderNumber: String, txnId: String, note: String) -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (userBalance >= pkg.minInvestment) 0 else 1) }
    var senderNumber by remember { mutableStateOf("") }
    var txnId by remember { mutableStateOf("") }
    var screenshotNote by remember { mutableStateOf("") }

    val hasSufficientBalance = userBalance >= pkg.minInvestment

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("buy_package_sheet_content")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Purchase ${pkg.name} Package",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Required: $${pkg.minInvestment.toInt()} | Available Balance: $${String.format(Locale.US, "%.2f", userBalance)}",
                    color = AccentCyan,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NavySurfaceVariant,
            contentColor = AccentCyan
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Virtual Balance", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Easypaisa Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Pay from existing balance
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Instant Balance Activation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasSufficientBalance)
                            "You have sufficient virtual funds. $${pkg.minInvestment.toInt()} will be deducted from your liquid balance and invested in the ${pkg.name} 7-day cycle."
                        else
                            "Insufficient balance ($${String.format(Locale.US, "%.2f", userBalance)}). Please use the Easypaisa Deposit tab to submit a deposit request.",
                        color = if (hasSufficientBalance) TextSecondary else AccentGold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onActivateFromBalance,
                enabled = hasSufficientBalance,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activate_balance_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm Activation ($${pkg.minInvestment.toInt()})", fontWeight = FontWeight.Bold)
            }
        } else {
            // Easypaisa Deposit details
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Official Account Card
                CopyableAccountCard(
                    title = "OFFICIAL EASYPAISA ACCOUNT",
                    holderName = easypaisaHolder,
                    accountNumber = easypaisaNumber
                )

                Text(
                    text = "Payment Instructions:\n1. Transfer $${pkg.minInvestment.toInt()} to the official Easypaisa number above.\n2. Enter your Sender Number and Transaction ID below.\n3. Request goes to Admin for manual verification.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = senderNumber,
                    onValueChange = { senderNumber = it },
                    label = { Text("Your Sender Mobile Number") },
                    placeholder = { Text("03xx-xxxxxxx") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sender_number_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = NavyBorder
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = txnId,
                    onValueChange = { txnId = it },
                    label = { Text("Transaction ID / Reference") },
                    placeholder = { Text("e.g. 98402847291") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("txn_id_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = NavyBorder
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = screenshotNote,
                    onValueChange = { screenshotNote = it },
                    label = { Text("Payment Screenshot Note / Slip ID (Optional)") },
                    placeholder = { Text("Slip verified, screenshot ref #...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("screenshot_note_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = NavyBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onSubmitDeposit(senderNumber, txnId, screenshotNote) },
                    enabled = txnId.isNotBlank() && senderNumber.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_deposit_request_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Submit Deposit for Admin Approval", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
