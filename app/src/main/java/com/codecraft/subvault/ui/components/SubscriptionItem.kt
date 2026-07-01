package com.codecraft.subvault.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.util.CurrencyUtils
import com.codecraft.subvault.domain.util.DateUtils
import com.codecraft.subvault.ui.theme.SubVaultTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class DragValue {
    Start,
    End
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionItem(
    subscription: Subscription,
    isExpanded: Boolean = false,
    onExpand: () -> Unit = {},
    onCollapse: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    val gap = 8.dp
    val actionsWidth = 120.dp
    val totalRevealWidth = actionsWidth + gap
    val totalRevealWidthPx = with(density) { totalRevealWidth.toPx() }

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    
    val state = remember(totalRevealWidthPx) {
        AnchoredDraggableState(
            initialValue = if (isExpanded) DragValue.End else DragValue.Start,
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            state.animateTo(DragValue.End)
        } else {
            state.animateTo(DragValue.Start)
        }
    }

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == DragValue.End) {
            onExpand()
        } else {
            onCollapse()
        }
    }

    LaunchedEffect(totalRevealWidthPx) {
        val anchors = DraggableAnchors {
            DragValue.Start at 0f
            DragValue.End at -totalRevealWidthPx
        }
        state.updateAnchors(anchors)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionsWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                scope.launch { state.animateTo(DragValue.Start) }
                onEdit()
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = {
                scope.launch { state.animateTo(DragValue.Start) }
                onDelete()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = state.offset.takeIf { !it.isNaN() }?.roundToInt() ?: 0,
                        y = 0
                    )
                }
                .anchoredDraggable(state, Orientation.Horizontal),
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subscription.iconUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(subscription.iconUrl)
                            .crossfade(enable = true)
                            .build(),
                        contentDescription = subscription.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    val initials = CurrencyUtils.getProductShortName(subscription.name)
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = subscription.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = subscription.cycle,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val symbol = CurrencyUtils.getSymbol(subscription.currency)
                    Text(
                        text = symbol + String.format(Locale.getDefault(), "%.2f", subscription.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val nextRenewal = if (subscription.isActive && subscription.cycle.lowercase() != "lifetime") {
                        DateUtils.calculateNextRenewal(subscription.renewalDate, subscription.cycle)
                    } else {
                        null
                    }

                    val dateLabel = when {
                        !subscription.isActive -> "Ended on"
                        subscription.cycle.lowercase() == "lifetime" -> "Started on"
                        else -> "Renews on"
                    }
                    
                    val displayDate = when {
                        !subscription.isActive -> subscription.endDate ?: subscription.renewalDate
                        subscription.cycle.lowercase() == "lifetime" -> subscription.renewalDate
                        else -> nextRenewal ?: subscription.renewalDate
                    }
                    
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(displayDate),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = {
                    scope.launch {
                        if (state.currentValue == DragValue.Start) {
                            state.animateTo(DragValue.End)
                        } else {
                            state.animateTo(DragValue.Start)
                        }
                    }
                }) {
                    val rotation by animateFloatAsState(
                        if (state.targetValue == DragValue.End) 180f else 0f,
                        label = "Arrow rotation"
                    )
                    Icon(
                        Icons.Default.ChevronRight, 
                        contentDescription = "Reveal Actions",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun SubscriptionItemPreview() {
    SubVaultTheme {
        SubscriptionItem(
            subscription = Subscription(
                id = 1,
                name = "Netflix",
                category = "Entertainment",
                amount = 51.0,
                currency = "USD",
                cycle = "Monthly",
                renewalDate = System.currentTimeMillis(),
                paymentMethod = "Credit Card"
            ),
            onEdit = {},
            onDelete = {},
            onClick = {}
        )
    }
}
