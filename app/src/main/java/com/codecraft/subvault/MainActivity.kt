package com.codecraft.subvault

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codecraft.subvault.ui.Screen
import com.codecraft.subvault.ui.screens.AccountScreen
import com.codecraft.subvault.ui.screens.AddSubscriptionScreen
import com.codecraft.subvault.ui.screens.AnalyticsScreen
import com.codecraft.subvault.ui.screens.DashboardScreen
import com.codecraft.subvault.ui.screens.EditSubscriptionScreen
import com.codecraft.subvault.ui.screens.SettingsScreen
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.worker.RenewalReminderWorker
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, you can perform notification-related tasks here if needed
            Log.d(TAG, "is notification permission granted: $isGranted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestNotificationPermission()
        scheduleRenewalReminders()

        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val currentTheme by themeViewModel.theme.collectAsState()
            
            SubVaultTheme(appTheme = currentTheme) {
                AppNavigation()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleRenewalReminders() {
        val workRequest = PeriodicWorkRequestBuilder<RenewalReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "renewal_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Globally clear focus and hide keyboard on any navigation change
    LaunchedEffect(currentDestination) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Dashboard.route),
        BottomNavItem("Analytics", Icons.Default.BarChart, Screen.Analytics.route),
        BottomNavItem("Add", Icons.Default.Add, Screen.AddSubscription.route),
        BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings.route),
        BottomNavItem("Account", Icons.Default.Person, Screen.Account.route)
    )

    val selectedTab = items.indexOfFirst { item -> 
        currentDestination?.hierarchy?.any { it.route == item.route } == true 
    }.let { if (it == -1) 0 else it }

    Scaffold(
        bottomBar = {
            CustomBottomNavigation(
                items = items,
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    val route = items[index].route
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding).padding(bottom = 8.dp)
        ) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    onSubscriptionClick = { _ -> },
                    onEditSubscription = { id -> 
                        navController.navigate(Screen.EditSubscription.createRoute(id))
                    }
                )
            }
            composable(
                route = Screen.EditSubscription.route,
                arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
            ) { 
                EditSubscriptionScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
            composable(Screen.Settings.route) { 
                SettingsScreen()
            }
            composable(Screen.Account.route) { 
                AccountScreen()
            }
            composable(Screen.AddSubscription.route) { 
                AddSubscriptionScreen(onNavigateBack = { navController.popBackStack() }) 
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(
    items: List<BottomNavItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val animatedX by animateFloatAsState(
        targetValue = selectedTab.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "humpX"
    )

    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(90.dp) // Increased from 80dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            val barHeight = 68.dp.toPx() // Increased from 64dp
            val width = size.width
            val height = size.height
            val itemWidth = width / items.size
            val centerX = itemWidth * animatedX + itemWidth / 2
            
            val path = Path().apply {
                val curveWidth = 60.dp.toPx() // Slightly wider
                val curveHeight = 22.dp.toPx() // Slightly deeper curve
                
                moveTo(0f, height)
                lineTo(0f, height - barHeight)
                
                lineTo(centerX - curveWidth, height - barHeight)
                
                cubicTo(
                    centerX - curveWidth / 1.5f, height - barHeight,
                    centerX - curveWidth / 2f, height - barHeight - curveHeight,
                    centerX, height - barHeight - curveHeight
                )
                
                cubicTo(
                    centerX + curveWidth / 2f, height - barHeight - curveHeight,
                    centerX + curveWidth / 1.5f, height - barHeight,
                    centerX + curveWidth, height - barHeight
                )
                
                lineTo(width, height - barHeight)
                lineTo(width, height)
                close()
            }
            
            drawPath(path, color = backgroundColor)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(68.dp), // Match barHeight
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                val animatedOffset by animateFloatAsState(
                    targetValue = if (isSelected) -16f else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "iconOffset"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center // Center content in the box
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(y = animatedOffset.dp)
                    ) {
                        if (index == 2) {
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 56.dp else 48.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    item.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isSelected) 32.dp else 28.dp)
                                )
                            }
                            // Reduced spacer to give more top clearance
                            Spacer(modifier = Modifier.height(10.dp))
                        } else {
                            val contentColor = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@Preview(showBackground = true)
@Composable
fun CustomBottomNavigationPreview() {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Dashboard.route),
        BottomNavItem("Analytics", Icons.Default.BarChart, Screen.Analytics.route),
        BottomNavItem("Add", Icons.Default.Add, Screen.AddSubscription.route),
        BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings.route),
        BottomNavItem("Account", Icons.Default.Person, Screen.Account.route)
    )
    SubVaultTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            CustomBottomNavigation(
                items = items,
                selectedTab = 2,
                onTabSelected = {}
            )
        }
    }
}
