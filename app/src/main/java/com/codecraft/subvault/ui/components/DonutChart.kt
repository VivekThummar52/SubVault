package com.codecraft.subvault.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
    thickness: Dp = 20.dp,
    currency: String = "USD"
) {
    val totalAmount = data.sumOf { it.totalAmount }
    val symbol = CurrencyUtils.getSymbol(currency)

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "donut_animation"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .padding(thickness / 2)
        ) {
            var startAngle = -90f
            data.forEach { category ->
                val sweepAngle = category.percentage * 360f * animationProgress
                if (sweepAngle > 0.5f) { // Only draw if visible
                    drawArc(
                        color = category.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
                    )
                }
                startAngle += category.percentage * 360f * animationProgress
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total Spending",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = symbol + String.format(Locale.getDefault(), "%.2f", totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
