package com.codecraft.subvault.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codecraft.subvault.domain.util.CurrencyUtils
import com.codecraft.subvault.domain.util.DateUtils
import com.codecraft.subvault.ui.theme.SubVaultTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StepIndicator(
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf("Details", "Billing", "Payment", "Review")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, title ->
            val isActive = index <= currentPage
            val isCurrent = index == currentPage

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left Line
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 2.dp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Right Line
                    if (index < steps.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 2.dp,
                            color = if (index < currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun Step1Identity(name: String, category: String, onNameChange: (String) -> Unit, onCategoryChange: (String) -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val headerCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F9FE)
    val iconSurfaceBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE8EAF6)
    val accentColor = MaterialTheme.colorScheme.primary
    val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
    val infoCardBg = if (isDark) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF0F4FF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = headerCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    color = iconSurfaceBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Let's add your subscription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tell us what you're subscribing to and we'll handle the rest.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subscription Name Input
        Text(
            "Subscription Name",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("e.g. Netflix, Spotify, Dropbox", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { onNameChange("") }) {
                        Icon(Icons.Default.Cancel, contentDescription = "Clear", tint = Color.LightGray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Category Selection
        Text(
            "Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val categories = listOf(
            CategoryItem("Entertainment", Icons.Default.Movie, Color(0xFF3F51B5)),
            CategoryItem("Utility", Icons.Default.FlashOn, Color(0xFF2196F3)),
            CategoryItem("Software", Icons.Default.Computer, Color(0xFF00C853)),
            CategoryItem("Food", Icons.Default.Restaurant, Color(0xFFFF7043)),
            CategoryItem("Other", Icons.Default.GridView, Color(0xFF78909C))
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // First Row: 3 items
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.take(3).forEach { item ->
                    CategoryCard(
                        item = item,
                        isSelected = category == item.name,
                        onClick = { onCategoryChange(item.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // Second Row: 2 items
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.drop(3).forEach { item ->
                    CategoryCard(
                        item = item,
                        isSelected = category == item.name,
                        onClick = { onCategoryChange(item.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f)) // Placeholder for grid alignment
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = infoCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = accentColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Good to know",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "You can always change these details later.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.DarkGray
                    )
                }
            }
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun CategoryCard(item: CategoryItem, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF0F4FF)
    val unselectedBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val accentColor = MaterialTheme.colorScheme.primary
    val unselectedBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFF5F5F5)

    OutlinedCard(
        modifier = modifier
            .height(100.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) selectedBg else unselectedBg
        ),
        border = if (isSelected) BorderStroke(1.dp, accentColor) else BorderStroke(1.dp, unselectedBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else item.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) accentColor else if (isDark) MaterialTheme.colorScheme.onSurface else Color.DarkGray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Pricing(
    amount: String,
    currency: String,
    cycle: String,
    defaultCurrency: String,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onCycleChange: (String) -> Unit
) {
    val symbol = CurrencyUtils.getSymbol(currency)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val headerCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F9FE)
    val iconSurfaceBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE8EAF6)
    val accentColor = MaterialTheme.colorScheme.primary
    val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
    val infoCardBg = if (isDark) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF0F4FF)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = headerCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    color = iconSurfaceBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                        // Small overlay icon (coin)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(16.dp)
                                .background(if (isDark) MaterialTheme.colorScheme.tertiary else Color(0xFFFFB74D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(symbol, color = if (isDark) MaterialTheme.colorScheme.onTertiary else Color.White, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Set up billing details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Add the price, billing cycle and currency for this subscription.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Price and Currency Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price Input
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Price",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onAmountChange(it) },
                    placeholder = { Text("0.00", color = Color.LightGray) },
                    leadingIcon = { Text(symbol, color = subTextColor, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE0E0E0)
                    )
                )
                Text(
                    "Enter the subscription amount",
                    style = MaterialTheme.typography.labelSmall,
                    color = subTextColor,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            // Currency Selector
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Currency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                CurrencyDropdown(currency, onCurrencyChange, Modifier.fillMaxWidth())
                Text(
                    "Default: $defaultCurrency (can be changed later from settings)",
                    style = MaterialTheme.typography.labelSmall,
                    color = subTextColor,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Billing Cycle Selection
        Text(
            "Billing Cycle",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val cycles = listOf(
            BillingCycleItem("Weekly", "Every 7 days", Icons.Default.CalendarToday, Color(0xFF4CAF50)),
            BillingCycleItem("Monthly", "Every month", Icons.Default.CalendarMonth, Color(0xFFFF9800)),
            BillingCycleItem("Yearly", "Every year", Icons.Default.Event, Color(0xFFE91E63))
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // First Row: 3 items
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                cycles.forEach { item ->
                    BillingCycleCard(
                        item = item,
                        isSelected = cycle == item.name,
                        onClick = { onCycleChange(item.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // Second Row: Lifetime item
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val lifetime = BillingCycleItem("Lifetime", "One-time payment", Icons.Default.AllInclusive, Color(0xFF00BCD4))
                BillingCycleCard(
                    item = lifetime,
                    isSelected = cycle == lifetime.name,
                    onClick = { onCycleChange(lifetime.name) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(2f)) // Placeholder for alignment
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = infoCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = accentColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Good to know",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "You can change the price, currency or billing cycle anytime from subscription settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.DarkGray
                    )
                }
            }
        }
    }
}

data class BillingCycleItem(val name: String, val subtext: String, val icon: ImageVector, val color: Color)

@Composable
fun BillingCycleCard(item: BillingCycleItem, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF0F4FF)
    val unselectedBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val accentColor = MaterialTheme.colorScheme.primary
    val unselectedBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFF5F5F5)

    OutlinedCard(
        modifier = modifier
            .height(110.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) selectedBg else unselectedBg
        ),
        border = if (isSelected) BorderStroke(1.dp, accentColor) else BorderStroke(1.dp, unselectedBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else item.color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) accentColor else if (isDark) MaterialTheme.colorScheme.onSurface else Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
            Text(
                item.subtext,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
                textAlign = TextAlign.Center
            )
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val headerCardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF8F9FE)
    val iconSurfaceBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE8EAF6)
    val accentColor = MaterialTheme.colorScheme.primary
    val subTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
    val infoCardBg = if (isDark) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF0F4FF)

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val isLifetime = cycle.lowercase() == "lifetime"

    // If cycle is lifetime, we shouldn't have an end date
    LaunchedEffect(isLifetime) {
        if (isLifetime) {
            onHasEndDateChange(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = headerCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    color = iconSurfaceBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                        // Small overlay icon (check)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(16.dp)
                                .background(if (isDark) MaterialTheme.colorScheme.tertiary else Color(0xFF4CAF50), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = if (isDark) MaterialTheme.colorScheme.onTertiary else Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Almost there!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Add the start date, payment method and optionally an end date (if applicable).",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dates Section
        Text(
            text = "Dates",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
            border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Start Date
                Text("Start Date", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(renewalDate)),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = accentColor) },
                    trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subTextColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE0E0E0)
                    ),
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subscription has an end date", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = hasEndDate,
                            onCheckedChange = onHasEndDateChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, 
                                checkedTrackColor = accentColor
                            )
                        )
                    }

                    if (hasEndDate) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("End Date (Optional)", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = endDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) } ?: "Select date",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = accentColor) },
                            trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = subTextColor) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE0E0E0)
                            ),
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            color = infoCardBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "We'll use the end date to stop renewals and mark this subscription as inactive.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Payment Method Section
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
            border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Method", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                Spacer(modifier = Modifier.height(4.dp))
                
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
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = accentColor) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE0E0E0)
                        )
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color(0xFFF8F9FE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = subTextColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your payment details are not stored. We only save the payment method type.",
                            style = MaterialTheme.typography.bodySmall,
                            color = subTextColor
                        )
                    }
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
fun Step4Review(
    name: String,
    amount: String,
    currency: String,
    cycle: String,
    category: String,
    renewalDate: Long,
    endDate: Long?,
    paymentMethod: String,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = CurrencyUtils.getSymbol(currency)
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val initials = CurrencyUtils.getProductShortName(name)
    
    val isLifetime = cycle.lowercase() == "lifetime"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text("Review Subscription", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Please review the details before saving", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { onEditClick(0) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ReviewRow(Icons.Default.Payments, "Total Price", symbol + String.format(Locale.getDefault(), "%.2f", amount.toDoubleOrNull() ?: 0.0))
                ReviewRow(Icons.Default.EventRepeat, "Billing Cycle", cycle)
                ReviewRow(Icons.Default.CalendarToday, "Starts On", sdf.format(Date(renewalDate)))
                
                if (isLifetime) {
                    ReviewRow(Icons.Default.HourglassEmpty, "Ends On", "Never Expires")
                } else if (endDate != null) {
                    ReviewRow(Icons.Default.HourglassEmpty, "Ends On", sdf.format(Date(endDate)))
                }

                ReviewRow(Icons.Default.CreditCard, "Payment Method", paymentMethod)
                ReviewRow(Icons.Default.CurrencyExchange, "Currency", currency)

                if (!isLifetime) {
                    val nextRenewal = DateUtils.calculateNextRenewal(renewalDate, cycle)
                    val daysUntil = ((nextRenewal - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                    ReviewRow(
                        icon = Icons.Default.History,
                        label = "Next Renewal",
                        value = sdf.format(Date(nextRenewal)),
                        subValue = "in $daysUntil days"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("You're all set!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Review your details before finalizing.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewRow(icon: ImageVector, label: String, value: String, subValue: String? = null) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF5F5F5),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            if (subValue != null) {
                Text(subValue, style = MaterialTheme.typography.labelMedium, color = if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(selectedCurrency: String, onCurrencyChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val commonCurrencies = listOf("USD", "EUR", "GBP", "JPY", "INR", "CAD", "AUD", "BRL")
    var expanded by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accentColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE0E0E0)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedCurrency,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = borderColor
            )
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

// Previews
@Preview(showBackground = true)
@Composable
fun PreviewStepIndicator() {
    SubVaultTheme {
        StepIndicator(currentPage = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStep1() {
    SubVaultTheme {
        Step1Identity(
            name = "Netflix",
            category = "Entertainment",
            onNameChange = {},
            onCategoryChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStep2() {
    SubVaultTheme {
        Step2Pricing(
            amount = "15.99",
            currency = "USD",
            cycle = "Monthly",
            defaultCurrency = "USD",
            onAmountChange = {},
            onCurrencyChange = {},
            onCycleChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStep3() {
    SubVaultTheme {
        Step3Details(
            renewalDate = System.currentTimeMillis(),
            endDate = null,
            hasEndDate = false,
            paymentMethod = "Credit Card",
            cycle = "Monthly",
            onRenewalDateChange = {},
            onEndDateChange = {},
            onHasEndDateChange = {},
            onPaymentMethodChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStep4() {
    SubVaultTheme {
        Step4Review(
            name = "Netflix",
            amount = "15.99",
            currency = "USD",
            cycle = "Monthly",
            category = "Entertainment",
            renewalDate = System.currentTimeMillis(),
            endDate = null,
            paymentMethod = "Credit Card",
            onEditClick = {}
        )
    }
}
