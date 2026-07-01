package com.codecraft.subvault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.codecraft.subvault.domain.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Step1Identity(name: String, category: String, onNameChange: (String) -> Unit, onCategoryChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("What is this subscription for?", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Subscription Name") },
            placeholder = { Text("e.g. Netflix, Spotify") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Category", style = MaterialTheme.typography.labelLarge)
        val categories = listOf("Entertainment", "Utility", "Software", "Food", "Other")
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(cat) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Pricing(amount: String, currency: String, cycle: String, onAmountChange: (String) -> Unit, onCurrencyChange: (String) -> Unit, onCycleChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Billing Details", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onAmountChange(it) },
                label = { Text("Price") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.width(8.dp))
            CurrencyDropdown(currency, onCurrencyChange, Modifier.width(120.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Billing Cycle", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Weekly", "Monthly", "Yearly", "Lifetime").forEach { c ->
                FilterChip(
                    selected = cycle == c,
                    onClick = { onCycleChange(c) },
                    label = { Text(c) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3Details(
    renewalDate: Long,
    endDate: Long?,
    hasEndDate: Boolean,
    paymentMethod: String,
    cycle: String,
    onRenewalDateChange: (Long) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onHasEndDateChange: (Boolean) -> Unit,
    onPaymentMethodChange: (String) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val isLifetime = cycle.lowercase() == "lifetime"

    // If cycle is lifetime, we shouldn't have an end date
    LaunchedEffect(isLifetime) {
        if (isLifetime) {
            onHasEndDateChange(false)
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Dates & Payment", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(renewalDate)),
            onValueChange = {},
            readOnly = true,
            label = { Text("Start Date") },
            trailingIcon = {
                IconButton(onClick = { showStartDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Select Start Date")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                showStartDatePicker = true
                            }
                        }
                    }
                }
        )

        if (!isLifetime) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = hasEndDate, onCheckedChange = onHasEndDateChange)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Subscription has an end date")
            }

            if (hasEndDate) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = endDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) } ?: "Select Date",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date") },
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select End Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showEndDatePicker = true
                                    }
                                }
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Payment Method", style = MaterialTheme.typography.labelLarge)
        val methods = listOf("Credit Card", "Debit Card", "UPI", "Net Banking", "Cash", "Wallet", "Other")
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = paymentMethod,
                onValueChange = {},
                readOnly = true,
                label = { Text("Method") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                methods.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method) },
                        onClick = {
                            onPaymentMethodChange(method)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = renewalDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onRenewalDateChange(it) }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onEndDateChange(it) }
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun Step4Review(name: String, amount: String, currency: String, cycle: String, category: String, renewalDate: Long, endDate: Long?, paymentMethod: String) {
    val symbol = CurrencyUtils.getSymbol(currency)
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Review Subscription", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(category, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Price")
                    Text(text = symbol + String.format(Locale.getDefault(), "%.2f", amount.toDoubleOrNull() ?: 0.0), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Billed")
                    Text(cycle, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Starts")
                    Text(sdf.format(Date(renewalDate)), fontWeight = FontWeight.Bold)
                }
                if (endDate != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ends")
                        Text(sdf.format(Date(endDate)), fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment")
                    Text(paymentMethod, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(selectedCurrency: String, onCurrencyChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val commonCurrencies = listOf("USD", "EUR", "GBP", "JPY", "INR", "CAD", "AUD", "BRL")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedCurrency,
            onValueChange = {},
            readOnly = true,
            label = { Text("Currency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            commonCurrencies.forEach { code ->
                val currency = Currency.getInstance(code)
                DropdownMenuItem(
                    text = { Text("${currency.currencyCode} (${currency.getSymbol(Locale.US)})") },
                    onClick = { onCurrencyChange(code); expanded = false }
                )
            }
        }
    }
}
