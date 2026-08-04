package com.codecraft.subvault.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSubscriptionClick: (Long) -> Unit = {},
    onEditSubscription: (Long) -> Unit = {},
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val titleFocusRequester = remember { FocusRequester() }

    // Robust focus/keyboard clearing for all Android versions
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Clear immediately
                focusManager.clearFocus()
                keyboardController?.hide()
                
                // Also clear with a slight delay and steal focus to title
                scope.launch {
                    delay(300) // Slightly longer for very slow devices
                    try {
                        titleFocusRequester.requestFocus()
                    } catch (_: Exception) {
                        focusManager.clearFocus()
                    }
                    keyboardController?.hide()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedSubscriptionForDetail by remember { mutableStateOf<Subscription?>(null) }

    DashboardContent(
        summary = summary,
        subscriptions = subscriptions,
        defaultCurrency = defaultCurrency,
        searchQuery = searchQuery,
        selectedCategory = selectedCategory,
        selectedStatus = selectedStatus,
        categories = categories,
        titleFocusRequester = titleFocusRequester,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onStatusSelect = viewModel::onStatusSelect,
        onSubscriptionClick = { id -> 
            onSubscriptionClick(id) // Use the parameter if provided
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
    searchQuery: String,
    selectedCategory: String,
    selectedStatus: String,
    categories: List<String>,
    titleFocusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onStatusSelect: (String) -> Unit,
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
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .focusRequester(titleFocusRequester)
                    .focusable()
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                placeholder = { Text("Search subscriptions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    val statuses = listOf("All", "Active", "Expired")
                    items(statuses) { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { onStatusSelect(status) },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelect(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }

        if (searchQuery.isEmpty() && selectedCategory == "All" && selectedStatus == "All") {
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
        }

        val activeSubscriptions = subscriptions.filter { it.isActive }
        val inactiveSubscriptions = subscriptions.filter { !it.isActive }

        if (activeSubscriptions.isNotEmpty()) {
            item {
                Text(
                    text = if (searchQuery.isEmpty() && selectedCategory == "All" && selectedStatus == "All") 
                        "Active Subscriptions" 
                    else "Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
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
                    text = if (searchQuery.isEmpty() && selectedCategory == "All" && selectedStatus == "All") 
                        "Expired / Inactive" 
                    else "Inactive Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
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
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isEmpty()) 
                            "No subscriptions added yet. Tap + to start tracking!" 
                        else 
                            "No subscriptions match your search."
                    )
                }
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
            searchQuery = "",
            selectedCategory = "All",
            selectedStatus = "All",
            categories = listOf("All", "Entertainment", "Food"),
            titleFocusRequester = remember { FocusRequester() },
            onSearchQueryChange = {},
            onCategorySelect = {},
            onStatusSelect = {},
            onSubscriptionClick = {},
            onEditSubscription = {},
            onDeleteSubscription = {}
        )
    }
}
