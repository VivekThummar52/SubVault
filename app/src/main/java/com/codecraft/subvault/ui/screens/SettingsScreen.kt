package com.codecraft.subvault.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.ui.theme.AppTheme
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.ui.viewmodel.ThemeViewModel
import com.codecraft.subvault.ui.viewmodel.DashboardViewModel
import java.util.Currency
import java.util.Locale

@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val currentTheme by themeViewModel.theme.collectAsState()
    val defaultCurrency by dashboardViewModel.defaultCurrency.collectAsState()

    SettingsContent(
        currentTheme = currentTheme,
        defaultCurrency = defaultCurrency,
        onThemeSelected = { themeViewModel.setTheme(it) },
        onCurrencySelected = { dashboardViewModel.setDefaultCurrency(it) }
    )
}

@Composable
fun SettingsContent(
    currentTheme: AppTheme,
    defaultCurrency: String,
    onThemeSelected: (AppTheme) -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsSection(title = "Appearance") {
            val supportedThemes = remember {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    AppTheme.entries
                } else {
                    AppTheme.entries.filter { it != AppTheme.SYSTEM }
                }
            }
            supportedThemes.forEach { theme ->
                ThemeOption(
                    theme = theme,
                    isSelected = currentTheme == theme,
                    onClick = { onThemeSelected(theme) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "Currency") {
            DefaultCurrencySelector(
                selectedCurrency = defaultCurrency,
                onCurrencySelected = onCurrencySelected
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Subscriptions,
                title = "Manage Play Store Subscriptions",
                subtitle = "Open Google Play to manage external subscriptions",
                onClick = { openPlayStoreSubscriptions(context) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "App") {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notification Settings",
                subtitle = "Manage how you receive renewal reminders",
                onClick = { /* TODO */ }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About SubVault",
                subtitle = "Version 1.0.0",
                onClick = { /* TODO */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultCurrencySelector(selectedCurrency: String, onCurrencySelected: (String) -> Unit) {
    val commonCurrencies = listOf("USD", "EUR", "GBP", "JPY", "INR", "CAD", "AUD", "BRL")
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        OutlinedTextField(
            value = selectedCurrency,
            onValueChange = {},
            readOnly = true,
            label = { Text("Default Currency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            commonCurrencies.forEach { code ->
                DropdownMenuItem(
                    text = { Text("$code (${Currency.getInstance(code).getSymbol(Locale.US)})") },
                    onClick = { onCurrencySelected(code); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun openPlayStoreSubscriptions(context: Context) {
    val uri = Uri.parse("https://play.google.com/store/account/subscriptions")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    SubVaultTheme {
        SettingsContent(
            currentTheme = AppTheme.SYSTEM,
            defaultCurrency = "USD",
            onThemeSelected = {},
            onCurrencySelected = {}
        )
    }
}
