package com.codecraft.subvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.domain.model.PriceChangeLog
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.util.CurrencyUtils
import com.codecraft.subvault.ui.components.*
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.ui.viewmodel.EditSubscriptionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditSubscriptionScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: EditSubscriptionViewModel = hiltViewModel()
) {
    val subscription by viewModel.subscription.collectAsState()
    val priceHistory by viewModel.priceHistory.collectAsState()
    
    subscription?.let { sub ->
        EditSubscriptionContent(
            subscription = sub,
            priceHistory = priceHistory,
            onNavigateBack = onNavigateBack,
            onUpdateSubscription = { name, amount, currency, cycle, category, renewal, end, method ->
                viewModel.updateSubscription(name, amount, currency, cycle, category, renewal, end, method)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubscriptionContent(
    subscription: Subscription,
    priceHistory: List<PriceChangeLog>,
    onNavigateBack: () -> Unit,
    onUpdateSubscription: (String, Double, String, String, String, Long, Long?, String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(subscription.name) }
    var category by remember { mutableStateOf(subscription.category) }
    var amount by remember { mutableStateOf(subscription.amount.toString()) }
    var currency by remember { mutableStateOf(subscription.currency) }
    var cycle by remember { mutableStateOf(subscription.cycle) }
    var renewalDate by remember { mutableLongStateOf(subscription.renewalDate) }
    var endDate by remember { mutableStateOf<Long?>(subscription.endDate) }
    var hasEndDate by remember { mutableStateOf(subscription.endDate != null) }
    var paymentMethod by remember { mutableStateOf(subscription.paymentMethod) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Subscription") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1) / 4f },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> Step1Identity(name, category, onNameChange = { name = it }, onCategoryChange = { category = it })
                    1 -> Step2Pricing(
                        amount = amount,
                        currency = currency,
                        cycle = cycle,
                        onAmountChange = { amount = it },
                        onCurrencyChange = { currency = it },
                        onCycleChange = { cycle = it }
                    )
                    2 -> Step3Details(
                        renewalDate = renewalDate,
                        endDate = endDate,
                        hasEndDate = hasEndDate,
                        paymentMethod = paymentMethod,
                        cycle = cycle,
                        onRenewalDateChange = { renewalDate = it },
                        onEndDateChange = { endDate = it },
                        onHasEndDateChange = { hasEndDate = it },
                        onPaymentMethodChange = { paymentMethod = it }
                    )
                    3 -> Column(modifier = Modifier.fillMaxSize()) {
                        Step4Review(name, amount, currency, cycle, category, renewalDate, endDate, paymentMethod)
                        
                        if (priceHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Price History", 
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(priceHistory) { log ->
                                    PriceHistoryRow(log, currency)
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Text("Back")
                    }
                } else {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 3) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onUpdateSubscription(
                                name,
                                amount.toDoubleOrNull() ?: 0.0,
                                currency,
                                cycle,
                                category,
                                renewalDate,
                                if (hasEndDate) endDate else null,
                                paymentMethod
                            )
                            onNavigateBack()
                        }
                    },
                    enabled = when(pagerState.currentPage) {
                        0 -> name.isNotBlank()
                        1 -> amount.isNotBlank()
                        else -> true
                    }
                ) {
                    Text(if (pagerState.currentPage == 3) "Update" else "Next")
                }
            }
        }
    }
}

@Composable
fun PriceHistoryRow(log: PriceChangeLog, currencyCode: String) {
    val symbol = CurrencyUtils.getSymbol(currencyCode)
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = sdf.format(Date(log.date)), style = MaterialTheme.typography.bodySmall)
        Text(
            text = symbol + String.format(Locale.getDefault(), "%.2f", log.oldPrice) + " -> " + symbol + String.format(Locale.getDefault(), "%.2f", log.newPrice),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditSubscriptionPreview() {
    SubVaultTheme {
        EditSubscriptionContent(
            subscription = Subscription(
                id = 1, 
                name = "Netflix", 
                category = "Entertainment", 
                amount = 51.0, 
                currency = "USD", 
                cycle = "Monthly", 
                renewalDate = System.currentTimeMillis(), 
                paymentMethod = "Card"
            ),
            priceHistory = listOf(
                PriceChangeLog(1, 1, 45.0, 51.0, System.currentTimeMillis() - 2592000000L)
            ),
            onNavigateBack = {},
            onUpdateSubscription = { _, _, _, _, _, _, _, _ -> }
        )
    }
}
