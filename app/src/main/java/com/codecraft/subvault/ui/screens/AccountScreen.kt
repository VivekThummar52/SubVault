package com.codecraft.subvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codecraft.subvault.ui.theme.SubVaultTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val dashboardSummary by viewModel.dashboardSummary.collectAsState()
    AccountContent(lastUpdated = dashboardSummary?.lastRatesUpdate)
}

@Composable
fun AccountContent(lastUpdated: Long? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Manage your SubVault account and preferences.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Currency Update Status
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Currency Rates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val dateStr = lastUpdated?.let {
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(it))
                    } ?: "Never"
                    Text("Last Updated: $dateStr", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Future placeholder for login/sync/profile details
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cloud Sync Status: Offline", fontWeight = FontWeight.SemiBold)
                    Text("Future Phase: Google Sign-in and data backup", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountPreview() {
    SubVaultTheme {
        AccountContent()
    }
}
