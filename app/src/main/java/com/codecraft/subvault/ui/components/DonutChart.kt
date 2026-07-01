package com.codecraft.subvault.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codecraft.subvault.domain.util.CurrencyUtils
import com.codecraft.subvault.ui.viewmodel.CategoryData
import java.util.*

@Composable
fun DonutChart(
    data: List<CategoryData>,
    modifier: Modifier = Modifier,
    thickness: Dp = 16.dp,
    currency: String = "USD"
) {
    val totalAmount = data.sumOf { it.totalAmount }
    val symbol = CurrencyUtils.getSymbol(currency)
    
    var hasAnimated by rememberSaveable(data.hashCode()) { mutableStateOf(false) }
    val animationProgress = remember { Animatable(if (hasAnimated) 1f else 0f) }

    LaunchedEffect(data) {
        if (!hasAnimated) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
            hasAnimated = true
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .padding(thickness / 2)
        ) {
            var startAngle = -90f
            data.forEach { category ->
                val sweepAngle = category.percentage * 360f * animationProgress.value
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = symbol + String.format(Locale.getDefault(), "%.2f", totalAmount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
