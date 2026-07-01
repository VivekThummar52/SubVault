package com.codecraft.subvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.domain.model.SpendingSummary
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.model.SubscriptionRenewal
import com.codecraft.subvault.domain.util.CurrencyUtils
import com.codecraft.subvault.ui.components.ConfirmationDialog
import com.codecraft.subvault.ui.components.SubscriptionItem
import com.codecraft.subvault.ui.components.SummaryCard
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.ui.viewmodel.DashboardViewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSubscriptionClick: (Long) -> Unit = {},
    onEditSubscription: (Long) -> Unit = {},
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()

    var selectedSubscriptionForDetail by remember { mutableStateOf<Subscription?>(null) }

    DashboardContent(
        summary = summary,
        subscriptions = subscriptions,
        defaultCurrency = defaultCurrency,
        onSubscriptionClick = { id -> 
            selectedSubscriptionForDetail = subscriptions.find { it.id == id }
        },
        onEditSubscription = onEditSubscription,
        onDeleteSubscription = viewModel::deleteSubscription
    )

    selectedSubscriptionForDetail?.let { subscription ->
        com.codecraft.subvault.ui.components.SubscriptionDetailDialog(
            subscription = subscription,
            onDismiss = { selectedSubscriptionForDetail = null },
            onEdit = { onEditSubscription(subscription.id) }
        )
    }
}

@Composable
fun DashboardContent(
    summary: SpendingSummary?,
    subscriptions: List<Subscription>,
    defaultCurrency: String,
    onSubscriptionClick: (Long) -> Unit,
    onEditSubscription: (Long) -> Unit,
    onDeleteSubscription: (Subscription) -> Unit
) {
    var subscriptionToDelete by remember { mutableStateOf<Subscription?>(null) }
    var expandedSubscriptionId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp), // Definitively avoid bottom nav overlap
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SummaryCard(
                totalMonthlySpend = summary?.totalMonthlySpend ?: 0.0,
                subscriptionCount = summary?.activeCount ?: 0,
                currency = defaultCurrency
            )
        }

        if (summary?.upcomingRenewals?.isNotEmpty() == true) {
            item {
                Text(
                    text = "Upcoming (Next 7 Days)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(summary.upcomingRenewals) { renewal ->
                UpcomingRenewalItem(renewal)
            }
        }

        val activeSubscriptions = subscriptions.filter { it.isActive }
        val inactiveSubscriptions = subscriptions.filter { !it.isActive }

        if (activeSubscriptions.isNotEmpty()) {
            item {
                Text(
                    text = "Active Subscriptions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(activeSubscriptions, key = { it.id }) { subscription ->
                SubscriptionItem(
                    subscription = subscription,
                    isExpanded = expandedSubscriptionId == subscription.id,
                    onExpand = { expandedSubscriptionId = subscription.id },
                    onCollapse = { if (expandedSubscriptionId == subscription.id) expandedSubscriptionId = null },
                    onEdit = { onEditSubscription(subscription.id) },
                    onDelete = { subscriptionToDelete = subscription },
                    onClick = { onSubscriptionClick(subscription.id) }
                )
            }
        }

        if (inactiveSubscriptions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Expired / Inactive",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            items(inactiveSubscriptions, key = { it.id }) { subscription ->
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) {
                    SubscriptionItem(
                        subscription = subscription,
                        isExpanded = expandedSubscriptionId == subscription.id,
                        onExpand = { expandedSubscriptionId = subscription.id },
                        onCollapse = { if (expandedSubscriptionId == subscription.id) expandedSubscriptionId = null },
                        onEdit = { onEditSubscription(subscription.id) },
                        onDelete = { subscriptionToDelete = subscription },
                        onClick = { onSubscriptionClick(subscription.id) }
                    )
                }
            }
        }

        if (subscriptions.isEmpty()) {
            item {
                Text(
                    text = "No subscriptions added yet. Tap + to start tracking!",
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }

    ConfirmationDialog(
        show = subscriptionToDelete != null,
        onDismiss = { subscriptionToDelete = null },
        onConfirm = {
            subscriptionToDelete?.let { onDeleteSubscription(it) }
        },
        title = "Delete Subscription",
        message = "Are you sure you want to delete ${subscriptionToDelete?.name}? This action cannot be undone.",
        confirmButtonText = "Delete"
    )
}

@Composable
fun UpcomingRenewalItem(renewal: SubscriptionRenewal) {
    val symbol = CurrencyUtils.getSymbol(renewal.currency)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = renewal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(renewal.nextRenewalDate)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = symbol + String.format(Locale.getDefault(), "%.2f", renewal.amount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    SubVaultTheme {
        DashboardContent(
            summary = SpendingSummary(
                totalMonthlySpend = 159.33,
                activeCount = 2,
                upcomingRenewals = listOf(
                    SubscriptionRenewal(1, "Netflix", System.currentTimeMillis() + 86400000, 51.0, "USD")
                )
            ),
            subscriptions = listOf(
                Subscription(1, "Netflix", "Entertainment", 51.0, "USD", "Monthly", System.currentTimeMillis(), paymentMethod = "Card"),
                Subscription(2, "Domino's", "Food", 25.0, "USD", "Weekly", System.currentTimeMillis(), paymentMethod = "UPI")
            ),
            defaultCurrency = "USD",
            onSubscriptionClick = {},
            onEditSubscription = {},
            onDeleteSubscription = {}
        )
    }
}
