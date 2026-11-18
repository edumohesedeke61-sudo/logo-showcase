package ng.lightgrab.courier.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ng.lightgrab.courier.core.model.DeliveryStatus
import kotlin.math.roundToInt

enum class Tone { Forest, Leaf, Navy, Danger }

private fun Tone.colors(): Pair<Color, Color> = when (this) {
    Tone.Forest -> Brand.Forest to Brand.White
    Tone.Leaf -> Brand.Leaf to Brand.Navy
    Tone.Navy -> Brand.Navy to Brand.White
    Tone.Danger -> Brand.Danger to Brand.White
}

/** 60dp tall, full-width primary action — easy to hit with a glove or while stationary on a bike. */
@Composable
fun BigActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Forest,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val (bg, fg) = tone.colors()
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = fg, strokeWidth = 3.dp)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun OutlineActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Brand.Navy),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Brand.Navy, containerColor = Brand.White),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Deliberate swipe used as the confirmation for important status changes. */
@Composable
fun SwipeToConfirm(label: String, onConfirmed: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val density = LocalDensity.current
    val knobPx = with(density) { 60.dp.toPx() }
    val padPx = with(density) { 6.dp.toPx() }
    var width by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableFloatStateOf(0f) }
    val max = (width - knobPx - padPx * 2).coerceAtLeast(0f)
    Box(
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) Brand.Forest else Brand.Muted)
            .onSizeChanged { width = it.width.toFloat() },
    ) {
        Text(
            label,
            modifier = Modifier.align(Alignment.Center).padding(start = 64.dp, end = 12.dp),
            color = Brand.White,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
        Box(
            Modifier
                .offset { IntOffset((offset + padPx).roundToInt(), padPx.roundToInt()) }
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brand.Leaf)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enabled,
                    state = rememberDraggableState { delta -> offset = (offset + delta).coerceIn(0f, max) },
                    onDragStopped = {
                        if (max > 0f && offset >= max * 0.85f) onConfirmed()
                        offset = 0f
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Swipe to confirm", tint = Brand.Navy, modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = if (danger) Brand.Danger else Brand.Forest),
                modifier = Modifier.height(52.dp),
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.height(52.dp)) { Text("Not yet", color = Brand.Navy) } },
        containerColor = Brand.White,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LgTopBar(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Brand.White.copy(alpha = 0.75f))
            }
        },
        navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Brand.Navy,
            titleContentColor = Brand.White,
            navigationIconContentColor = Brand.White,
            actionIconContentColor = Brand.White,
        ),
    )
}

@Composable
fun StatusChip(status: DeliveryStatus) {
    val (bg, fg) = when (status) {
        DeliveryStatus.ASSIGNED -> Brand.Warning to Brand.Navy
        DeliveryStatus.COMPLETED -> Brand.Forest to Brand.White
        DeliveryStatus.ISSUE_REPORTED -> Brand.Danger to Brand.White
        DeliveryStatus.EN_ROUTE -> Brand.Navy to Brand.White
        else -> Brand.Leaf to Brand.Navy
    }
    Pill(status.label, bg, fg)
}

@Composable
fun Pill(text: String, bg: Color, fg: Color) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, color = fg, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, accent: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Brand.White,
        shadowElevation = 1.dp,
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            if (accent != null) Box(Modifier.width(5.dp).fillMaxHeight().background(accent))
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        }
    }
}

@Composable
fun DemoBanner() {
    Row(
        Modifier.fillMaxWidth().background(Brand.Warning).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Science, contentDescription = null, tint = Brand.Navy, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Demo mode — sample data only, not connected to LightGrab",
            color = Brand.Navy,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun ErrorNote(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brand.Danger.copy(alpha = 0.1f))
            .border(1.dp, Brand.Danger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) { Text(message, color = Brand.Danger, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun LoadingScreen(label: String = "Loading…") {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = Brand.Forest)
        Spacer(Modifier.height(16.dp))
        Text(label, color = Brand.Muted)
    }
}
