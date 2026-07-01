package com.codecraft.subvault.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codecraft.subvault.domain.util.CurrencyUtils
import com.codecraft.subvault.ui.components.DonutChart
import com.codecraft.subvault.ui.theme.SubVaultTheme
import com.codecraft.subvault.ui.viewmodel.AnalyticsState
import com.codecraft.subvault.ui.viewmodel.AnalyticsViewModel
import com.codecraft.subvault.ui.viewmodel.CategoryData
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val trendRange by viewModel.trendRange.collectAsState()

    AnalyticsContent(
        uiState = uiState,
        selectedMonth = selectedMonth,
        selectedYear = selectedYear,
        trendRange = trendRange,
        onMonthSelected = viewModel::selectMonth,
        onYearSelected = viewModel::selectYear,
        onTrendRangeSelected = viewModel::setTrendRange
    )
}

@Composable
fun AnalyticsContent(
    uiState: AnalyticsState,
    selectedMonth: Int,
    selectedYear: Int,
    trendRange: Int,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit,
    onTrendRangeSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Period Selectors
        item {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthSelector(selectedMonth, onMonthSelected = onMonthSelected, modifier = Modifier.weight(1.5f))
                YearSelector(selectedYear, onYearSelected = onYearSelected, modifier = Modifier.weight(1f))
            }
        }

        if (uiState.isYearlyView) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        title = "Total Yearly",
                        amount = uiState.yearlySpending,
                        currency = uiState.defaultCurrency,
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Monthly Avg",
                        amount = uiState.averageMonthlySpending,
                        currency = uiState.defaultCurrency,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (uiState.categoryData.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No active subscriptions found for this period.")
                }
            }
        } else {
            item {
                DonutChart(
                    data = uiState.categoryData,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    currency = uiState.defaultCurrency
                )
            }

            // Spending Trend
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spending Trend", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold
                        )
                        
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = trendRange == 6,
                                onClick = { onTrendRangeSelected(6) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("6M")
                            }
                            SegmentedButton(
                                selected = trendRange == 12,
                                onClick = { onTrendRangeSelected(12) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("12M")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    SimpleLineChart(
                        points = uiState.trendPoints,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Insights Section
            if (uiState.insights.isNotEmpty()) {
                item {
                    Column {
                        Text(text = "Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.insights.forEach { insight ->
                            InsightCard(insight)
                        }
                    }
                }
            }

            item {
                Text(text = "Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            items(uiState.categoryData) { data ->
                CategoryRow(data, uiState.defaultCurrency)
            }
        }
    }
}

@Composable
fun InfoCard(title: String, amount: Double, currency: String, modifier: Modifier = Modifier) {
    val symbol = CurrencyUtils.getSymbol(currency)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = symbol + String.format(Locale.getDefault(), "%.2f", amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SimpleLineChart(points: List<Float>, modifier: Modifier, color: Color) {
    val path = remember(points) { Path() }
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val spacing = size.width / (points.size - 1)
        val maxPoint = points.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val heightFactor = (size.height - 20.dp.toPx()) / maxPoint
        
        path.reset()
        path.moveTo(0f, size.height - (points[0] * heightFactor))
        points.forEachIndexed { index, point ->
            if (index > 0) {
                path.lineTo(index * spacing, size.height - (point * heightFactor))
            }
        }
        drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx()))
    }
}

@Composable
fun InsightCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Text(text = text, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CategoryRow(data: CategoryData, currency: String) {
    val symbol = CurrencyUtils.getSymbol(currency)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(data.color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = data.category, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = symbol + String.format(Locale.getDefault(), "%.2f", data.totalAmount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = "${(data.percentage * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(selectedMonth: Int, onMonthSelected: (Int) -> Unit, modifier: Modifier) {
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "All Months")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = months[selectedMonth],
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEachIndexed { index, name ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onMonthSelected(index); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelector(selectedYear: Int, onYearSelected: (Int) -> Unit, modifier: Modifier) {
    val years = (2020..2030).map { it.toString() }
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedYear.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Year") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { year ->
                DropdownMenuItem(text = { Text(year) }, onClick = { onYearSelected(year.toInt()); expanded = false })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsPreview() {
    SubVaultTheme {
        AnalyticsContent(
            uiState = AnalyticsState(
                categoryData = listOf(
                    CategoryData("Entertainment", 100.0, 0.6f, Color.Blue),
                    CategoryData("Food", 66.66, 0.4f, Color.Green)
                ),
                totalSpending = 166.66,
                insights = listOf("You spend most on Entertainment."),
                trendPoints = listOf(100f, 120f, 150f, 130f, 166f, 166f),
                defaultCurrency = "USD"
            ),
            selectedMonth = 5,
            selectedYear = 2026,
            trendRange = 6,
            onMonthSelected = {},
            onYearSelected = {},
            onTrendRangeSelected = {}
        )
    }
}
