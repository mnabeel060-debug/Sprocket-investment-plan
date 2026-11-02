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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.PaymentMethod
import com.example.data.model.WithdrawalRequestEntity
import com.example.ui.components.SimulationDisclaimerBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ForexViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WithdrawScreen(
    viewModel: ForexViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val withdrawals by viewModel.userWithdrawals.collectAsState()
    val context = LocalContext.current

    val user = currentUser ?: return

    var selectedMethod by remember { mutableStateOf(PaymentMethod.EASYPAISA) }
    var accountHolder by remember { mutableStateOf(user.fullName) }
    var accountNumber by remember { mutableStateOf(user.mobileNumber) }
    var amountText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp)
            .testTag("withdraw_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Withdraw Funds",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Withdraw your available virtual profit or capital to Easypaisa or Bank",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        item {
            SimulationDisclaimerBanner()
        }

        // Balance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Available Balance", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", user.virtualBalance)}",
                            color = AccentCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentCyan)
                    }
                }
            }
        }

        // Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Select Withdrawal Channel", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PaymentMethodTab(
                            title = "Easypaisa",
                            selected = selectedMethod == PaymentMethod.EASYPAISA,
                            onClick = {
                                selectedMethod = PaymentMethod.EASYPAISA
                                accountNumber = user.mobileNumber
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PaymentMethodTab(
                            title = "Bank Transfer",
                            selected = selectedMethod == PaymentMethod.BANK_TRANSFER,
                            onClick = {
                                selectedMethod = PaymentMethod.BANK_TRANSFER
                                accountNumber = ""
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = accountHolder,
                        onValueChange = { accountHolder = it },
                        label = { Text("Account Holder Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_holder_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = NavyBorder
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text(if (selectedMethod == PaymentMethod.EASYPAISA) "Easypaisa Mobile Number" else "Bank Account / IBAN") },
                        placeholder = { Text(if (selectedMethod == PaymentMethod.EASYPAISA) "03xx-xxxxxxx" else "PK00XXXX0000000000000000") },
                        keyboardOptions = KeyboardOptions(keyboardType = if (selectedMethod == PaymentMethod.EASYPAISA) KeyboardType.Phone else KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_number_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = NavyBorder
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Withdrawal Amount ($)") },
                        placeholder = { Text("Min: $10, Max: $${user.virtualBalance.toInt()}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
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
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (amt > user.virtualBalance) {
                                Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.submitWithdrawal(
                                amount = amt,
                                method = selectedMethod,
                                accountHolder = accountHolder,
                                accountNumber = accountNumber
                            )
                            amountText = ""
                        },
                        enabled = amountText.isNotBlank() && accountHolder.isNotBlank() && accountNumber.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_withdrawal_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Request Withdrawal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Withdrawal Requests (${withdrawals.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Statuses: Pending to Paid", color = TextSecondary, fontSize = 11.sp)
            }
        }

        if (withdrawals.isEmpty()) {
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "No withdrawal requests found", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(withdrawals) { req ->
                WithdrawalItemCard(req)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PaymentMethodTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) ElectricBlue.copy(alpha = 0.2f) else NavySurfaceVariant)
            .border(1.dp, if (selected) ElectricBlue else NavyBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (title == "Easypaisa") Icons.Default.Payments else Icons.Default.AccountBalance,
                contentDescription = null,
                tint = if (selected) ElectricBlue else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (selected) TextPrimary else TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun WithdrawalItemCard(req: WithdrawalRequestEntity) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
    val dateStr = dateFormat.format(Date(req.createdAt))

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("withdrawal_item_${req.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", req.amount)}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Via ${req.paymentMethod} • ${req.accountHolder}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                StatusBadge(status = req.status)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Account: ${req.accountNumber}", color = TextSecondary, fontSize = 11.sp)
                Text(text = dateStr, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
