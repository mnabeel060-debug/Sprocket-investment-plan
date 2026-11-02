package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
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
fun TransactionsScreen(
    viewModel: ForexViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.userTransactions.collectAsState()
    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredList = if (selectedFilter == null) {
        transactions
    } else {
        transactions.filter { it.type == selectedFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp)
            .testTag("transactions_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Transaction Audit Ledger",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Complete record of deposits, investments, verified trading returns, and withdrawals",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        item {
            SimulationDisclaimerBanner()
        }

        // Horizontal filter chips
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    label = "All (${transactions.size})",
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
                FilterChip(
                    label = "Deposits",
                    selected = selectedFilter == TransactionType.DEPOSIT,
                    onClick = { selectedFilter = TransactionType.DEPOSIT }
                )
                FilterChip(
                    label = "Investments",
                    selected = selectedFilter == TransactionType.INVESTMENT,
                    onClick = { selectedFilter = TransactionType.INVESTMENT }
                )
                FilterChip(
                    label = "Profits",
                    selected = selectedFilter == TransactionType.PROFIT,
                    onClick = { selectedFilter = TransactionType.PROFIT }
                )
                FilterChip(
                    label = "Losses",
                    selected = selectedFilter == TransactionType.LOSS,
                    onClick = { selectedFilter = TransactionType.LOSS }
                )
                FilterChip(
                    label = "Withdrawals",
                    selected = selectedFilter == TransactionType.WITHDRAWAL,
                    onClick = { selectedFilter = TransactionType.WITHDRAWAL }
                )
                FilterChip(
                    label = "Refunds",
                    selected = selectedFilter == TransactionType.REFUND,
                    onClick = { selectedFilter = TransactionType.REFUND }
                )
            }
        }

        if (filteredList.isEmpty()) {
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
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No transactions found in this category", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredList) { txn ->
                TransactionCard(txn)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) ElectricBlue else NavySurface)
            .border(1.dp, if (selected) ElectricBlue else NavyBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TransactionCard(txn: TransactionEntity) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
    val dateStr = dateFormat.format(Date(txn.timestamp))

    val (icon: ImageVector, iconTint: Color, prefix: String, amountColor: Color) = when (txn.type) {
        TransactionType.DEPOSIT -> Tuple4(Icons.Default.ArrowDownward, ProfitGreen, "+$", ProfitGreen)
        TransactionType.INVESTMENT -> Tuple4(Icons.Default.ReceiptLong, ElectricBlue, "-$", ElectricBlue)
        TransactionType.PROFIT -> Tuple4(Icons.Default.TrendingUp, ProfitGreen, "+$", ProfitGreen)
        TransactionType.LOSS -> Tuple4(Icons.Default.TrendingDown, LossRed, "-$", LossRed)
        TransactionType.WITHDRAWAL -> Tuple4(Icons.Default.ArrowUpward, AccentGold, "-$", AccentGold)
        TransactionType.REFUND -> Tuple4(Icons.Default.RotateLeft, StatusCompleted, "+$", StatusCompleted)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${txn.referenceNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = txn.description,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ref: ${txn.referenceNumber}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(txn.referenceNumber))
                            Toast.makeText(context, "Copied Ref", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(12.dp))
                    }
                }
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix${String.format(Locale.US, "%.2f", txn.amount)}",
                    color = amountColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = txn.type.name,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
