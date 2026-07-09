package com.codecraft.subvault.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.domain.util.DateUtils
import com.codecraft.subvault.ui.components.*
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.ui.viewmodel.AddSubscriptionViewModel
import kotlinx.coroutines.launch

@Composable
fun AddSubscriptionScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddSubscriptionViewModel = hiltViewModel()
) {
    val defaultCurrency by viewModel.defaultCurrency.collectAsState()
    
    AddSubscriptionContent(
        name = viewModel.name,
        category = viewModel.category,
        amount = viewModel.amount,
        currency = viewModel.currency,
        cycle = viewModel.cycle,
        defaultCurrency = defaultCurrency,
        renewalDate = viewModel.renewalDate,
        endDate = viewModel.endDate,
        hasEndDate = viewModel.hasEndDate,
        paymentMethod = viewModel.paymentMethod,
        onNameChange = { viewModel.name = it },
        onCategoryChange = { viewModel.category = it },
        onAmountChange = { viewModel.amount = it },
        onCurrencyChange = { viewModel.currency = it },
        onCycleChange = { viewModel.cycle = it },
        onRenewalDateChange = { viewModel.renewalDate = it },
        onEndDateChange = { viewModel.endDate = it },
        onHasEndDateChange = { viewModel.hasEndDate = it },
        onPaymentMethodChange = { viewModel.paymentMethod = it },
        onNavigateBack = onNavigateBack,
        onAddSubscription = {
            viewModel.addSubscription()
            onNavigateBack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionContent(
    name: String,
    category: String,
    amount: String,
    currency: String,
    cycle: String,
    defaultCurrency: String,
    renewalDate: Long,
    endDate: Long?,
    hasEndDate: Boolean,
    paymentMethod: String,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onCycleChange: (String) -> Unit,
    onRenewalDateChange: (Long) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onHasEndDateChange: (Boolean) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onAddSubscription: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var showWarningDialog by remember { mutableStateOf(false) }
    
    val isDark = isSystemInDarkTheme()
    val accentColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF3F51B5)

    ConfirmationDialog(
        show = showWarningDialog,
        onDismiss = { showWarningDialog = false },
        onConfirm = {
            showWarningDialog = false
            onAddSubscription()
        },
        title = "Early Expiration",
        message = "The selected end date is before the first renewal. This subscription will expire without any renewal. Continue?",
        confirmButtonText = "Continue"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Subscription") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                windowInsets = WindowInsets(0) // Remove default top bar insets/padding
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding()) // Only apply top padding from Scaffold
                .fillMaxSize()
        ) {
            StepIndicator(currentPage = pagerState.currentPage)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> Step1Identity(name, category, onNameChange, onCategoryChange)
                    1 -> Step2Pricing(
                        amount = amount,
                        currency = currency,
                        cycle = cycle,
                        defaultCurrency = defaultCurrency,
                        onAmountChange = onAmountChange,
                        onCurrencyChange = onCurrencyChange,
                        onCycleChange = onCycleChange
                    )
                    2 -> Step3Details(
                        renewalDate = renewalDate, 
                        endDate = endDate, 
                        hasEndDate = hasEndDate, 
                        paymentMethod = paymentMethod,
                        cycle = cycle,
                        onRenewalDateChange = onRenewalDateChange,
                        onEndDateChange = onEndDateChange,
                        onHasEndDateChange = onHasEndDateChange,
                        onPaymentMethodChange = onPaymentMethodChange
                    )
                    3 -> Step4Review(
                        name = name, 
                        amount = amount, 
                        currency = currency, 
                        cycle = cycle, 
                        category = category, 
                        renewalDate = renewalDate, 
                        endDate = endDate, 
                        paymentMethod = paymentMethod,
                        onEditClick = { targetPage ->
                            scope.launch { pagerState.animateScrollToPage(targetPage) }
                        },
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Text("Back", color = accentColor, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel", color = accentColor, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 3) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            val firstRenewal = DateUtils.getFirstRenewalDate(renewalDate, cycle)
                            val currentEndDate = endDate
                            if (hasEndDate && currentEndDate != null && currentEndDate < firstRenewal) {
                                showWarningDialog = true
                            } else {
                                onAddSubscription()
                            }
                        }
                    },
                    enabled = when(pagerState.currentPage) {
                        0 -> name.isNotBlank()
                        1 -> amount.isNotBlank()
                        else -> true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = if (pagerState.currentPage == 3) PaddingValues(horizontal = 24.dp, vertical = 12.dp) else ButtonDefaults.ContentPadding
                ) {
                    if (pagerState.currentPage == 3) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish & Save", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Next", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddSubscriptionPreview() {
    SubVaultTheme {
        AddSubscriptionContent(
            name = "",
            category = "Entertainment",
            amount = "",
            currency = "USD",
            cycle = "Monthly",
            defaultCurrency = "USD",
            renewalDate = System.currentTimeMillis(),
            endDate = null,
            hasEndDate = false,
            paymentMethod = "Credit Card",
            onNameChange = {},
            onCategoryChange = {},
            onAmountChange = {},
            onCurrencyChange = {},
            onCycleChange = {},
            onRenewalDateChange = {},
            onEndDateChange = {},
            onHasEndDateChange = {},
            onPaymentMethodChange = {},
            onNavigateBack = {},
            onAddSubscription = {}
        )
    }
}
