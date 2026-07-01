package com.codecraft.subvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.ui.components.*
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.ui.viewmodel.AddSubscriptionViewModel
import kotlinx.coroutines.launch

@Composable
fun AddSubscriptionScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AddSubscriptionViewModel = hiltViewModel()
) {
    AddSubscriptionContent(
        name = viewModel.name,
        category = viewModel.category,
        amount = viewModel.amount,
        currency = viewModel.currency,
        cycle = viewModel.cycle,
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
                    0 -> Step1Identity(name, category, onNameChange, onCategoryChange)
                    1 -> Step2Pricing(amount, currency, cycle, onAmountChange, onCurrencyChange, onCycleChange)
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
                    3 -> Step4Review(name, amount, currency, cycle, category, renewalDate, endDate, paymentMethod)
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
                            onAddSubscription()
                        }
                    },
                    enabled = when(pagerState.currentPage) {
                        0 -> name.isNotBlank()
                        1 -> amount.isNotBlank()
                        else -> true
                    }
                ) {
                    Text(if (pagerState.currentPage == 3) "Finish" else "Next")
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
