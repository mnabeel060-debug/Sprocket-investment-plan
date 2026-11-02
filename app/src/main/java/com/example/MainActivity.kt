package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.local.ForexDatabase
import com.example.data.model.UserRole
import com.example.data.repository.ForexRepository
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PackagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ForexTheme
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ForexViewModel
import com.example.ui.viewmodel.ForexViewModelFactory

enum class AppNavDestination(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    PACKAGES("Packages", Icons.Default.AddCard),
    WITHDRAW("Withdraw", Icons.Default.Payments),
    TRANSACTIONS("Ledger", Icons.Default.ReceiptLong),
    ADMIN("Admin Desk", Icons.Default.AdminPanelSettings),
    PROFILE("Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ForexViewModel by viewModels {
        val db = ForexDatabase.getDatabase(applicationContext, lifecycleScope)
        val repo = ForexRepository(db.forexDao())
        ForexViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ForexTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: ForexViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(AppNavDestination.DASHBOARD) }

    val unreadNotifications = notifications.count { !it.isRead }

    LaunchedEffect(operationMessage) {
        operationMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = { currentScreen = AppNavDestination.DASHBOARD }
        )
    } else {
        val user = currentUser!!

        BackHandler(enabled = currentScreen != AppNavDestination.DASHBOARD) {
            currentScreen = AppNavDestination.DASHBOARD
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground),
            containerColor = NavyBackground,
            contentColor = TextPrimary,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Forex Pro Simulator",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (user.role == UserRole.ADMIN) "Administrator Desk" else "Demo Simulator",
                                    color = if (user.role == UserRole.ADMIN) AccentGold else ProfitGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { currentScreen = AppNavDestination.PROFILE },
                            modifier = Modifier.testTag("top_bar_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge(containerColor = AccentGold, contentColor = Color.Black) {
                                            Text("$unreadNotifications", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavySurface,
                        titleContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = NavySurface,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    AppNavDestination.values().forEach { destination ->
                        // Show admin tab distinctly
                        val isSelected = currentScreen == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title,
                                    tint = if (isSelected) {
                                        if (destination == AppNavDestination.ADMIN) AccentGold else AccentCyan
                                    } else {
                                        TextSecondary
                                    }
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentCyan,
                                selectedTextColor = AccentCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = NavySurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppNavDestination.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToPackages = { currentScreen = AppNavDestination.PACKAGES },
                        onNavigateToWithdraw = { currentScreen = AppNavDestination.WITHDRAW }
                    )
                    AppNavDestination.PACKAGES -> PackagesScreen(
                        viewModel = viewModel
                    )
                    AppNavDestination.WITHDRAW -> WithdrawScreen(
                        viewModel = viewModel
                    )
                    AppNavDestination.TRANSACTIONS -> TransactionsScreen(
                        viewModel = viewModel
                    )
                    AppNavDestination.ADMIN -> AdminScreen(
                        viewModel = viewModel
                    )
                    AppNavDestination.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToAuth = { currentScreen = AppNavDestination.DASHBOARD }
                    )
                }
            }
        }
    }
}
