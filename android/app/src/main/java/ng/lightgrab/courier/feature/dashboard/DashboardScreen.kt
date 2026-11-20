package ng.lightgrab.courier.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.StatusChip
import ng.lightgrab.courier.core.model.AssignedOrder
import ng.lightgrab.courier.core.model.Availability
import ng.lightgrab.courier.core.model.CourierProfile
import ng.lightgrab.courier.core.model.DeliveryStatus
import ng.lightgrab.courier.core.util.userMessage
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@Composable
fun DashboardScreen(container: AppContainer, courier: CourierProfile, onOpenOrder: (String) -> Unit) {
    val repo = container.repository
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<AssignedOrder>?>(null) }
    var doneToday by remember { mutableStateOf(0) }
    var availability by remember { mutableStateOf(courier.availability) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        runCatching {
            orders = repo.assignedOrders()
            doneToday = repo.history().count { it.status == DeliveryStatus.COMPLETED && it.completedAt.isToday() }
            error = null
        }.onFailure { error = it.userMessage() }
    }
    LaunchedEffect(Unit) {
        load()
        runCatching { repo.assignmentChanges().collect { load() } }
    }

    val list = orders.orEmpty()
    val assigned = list.count { it.status == DeliveryStatus.ASSIGNED }
    val active = list.count { it.status.isActive }

    Column(Modifier.fillMaxSize()) {
        LgTopBar(greeting(courier.fullName), "Courier · ${courier.operatingArea ?: "Oron"}", actions = {
            IconButton(onClick = { scope.launch { load() } }) { Icon(Icons.Filled.Refresh, "Refresh") }
        })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = Brand.Navy) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Availability", color = Brand.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                            Text(availability.label, color = Brand.White, style = MaterialTheme.typography.headlineSmall)
                        }
                        Switch(
                            checked = availability != Availability.OFFLINE,
                            onCheckedChange = { on ->
                                val target = if (on) Availability.ONLINE else Availability.OFFLINE
                                val previous = availability
                                availability = target
                                scope.launch {
                                    runCatching { repo.setAvailability(target) }.onFailure { availability = previous; error = it.userMessage() }
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Brand.Leaf, checkedThumbColor = Brand.Navy),
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Stat("Assigned", orders?.let { "$assigned" } ?: "—", Modifier.weight(1f))
                    Stat("Active", orders?.let { "$active" } ?: "—", Modifier.weight(1f))
                    Stat("Done today", orders?.let { "$doneToday" } ?: "—", Modifier.weight(1f))
                }
            }
            item { ErrorNote(error) }
            item {
                Text("ASSIGNED TO YOU", style = MaterialTheme.typography.labelMedium, color = Brand.Muted, modifier = Modifier.padding(top = 4.dp))
            }
            if (orders != null && list.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = Brand.White) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Inventory2, null, tint = Brand.Muted, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("No orders assigned right now", style = MaterialTheme.typography.titleMedium)
                            Text("LightGrab Office assigns orders to you. You'll get an alert.", color = Brand.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            items(list, key = { it.assignmentId }) { o -> OrderCard(o) { onOpenOrder(o.assignmentId) } }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = Brand.White, shadowElevation = 1.dp) {
        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Brand.Muted)
        }
    }
}

@Composable
fun OrderCard(o: AssignedOrder, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = Brand.White, shadowElevation = 1.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(Brand.Sand), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Inventory2, null, tint = Brand.Navy)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(o.orderNumber, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                Text(o.deliveryAddress ?: "Address not provided", color = Brand.Muted, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(o.status)
        }
    }
}

private fun greeting(name: String): String {
    val h = java.time.LocalTime.now().hour
    val part = when { h < 12 -> "Good morning"; h < 17 -> "Good afternoon"; else -> "Good evening" }
    return "$part, ${name.substringBefore(' ')}"
}

private fun String?.isToday(): Boolean = this?.let {
    runCatching { OffsetDateTime.parse(it).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate() == LocalDate.now() }
        .recoverCatching { java.time.Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now() }
        .getOrDefault(false)
} ?: false
