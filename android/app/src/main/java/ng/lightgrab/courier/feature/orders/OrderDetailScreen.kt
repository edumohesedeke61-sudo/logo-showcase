package ng.lightgrab.courier.feature.orders

import android.Manifest
import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.BigActionButton
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.LoadingScreen
import ng.lightgrab.courier.core.designsystem.OutlineActionButton
import ng.lightgrab.courier.core.designsystem.SectionCard
import ng.lightgrab.courier.core.designsystem.StatusChip
import ng.lightgrab.courier.core.designsystem.SwipeToConfirm
import ng.lightgrab.courier.core.designsystem.Tone
import ng.lightgrab.courier.core.model.AssignedOrder
import ng.lightgrab.courier.core.model.DeliveryStatus
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.location.DeliveryTrackingService
import ng.lightgrab.courier.maps.Destination

/** Keeps customer details out of screenshots and the recent-apps preview. */
@Composable
fun SecureScreen() {
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

@Composable
fun OrderDetailScreen(
    container: AppContainer,
    assignmentId: String,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onHandover: () -> Unit,
    onReportIssue: () -> Unit,
) {
    SecureScreen()
    val repo = container.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var order by remember { mutableStateOf<AssignedOrder?>(null) }
    var missing by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var hasLocation by remember { mutableStateOf(container.location.hasPermission()) }

    suspend fun load() {
        runCatching { repo.assignedOrders().firstOrNull { it.assignmentId == assignmentId } }
            .onSuccess { order = it; missing = it == null }
            .onFailure { error = it.userMessage() }
    }
    LaunchedEffect(assignmentId) {
        load()
        runCatching { repo.assignmentChanges().collect { load() } }
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasLocation = container.location.hasPermission()
    }
    LaunchedEffect(Unit) {
        if (!hasLocation) locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    fun move(to: DeliveryStatus, then: () -> Unit = {}) {
        val o = order ?: return
        if (!o.status.canMoveTo(to)) { error = "That step isn't allowed right now."; return }
        busy = true; error = null
        scope.launch {
            val fix = container.location.currentFix()
            runCatching { repo.updateStatus(o.assignmentId, to, fix?.lat, fix?.lng) }
                .onSuccess {
                    when (to) {
                        DeliveryStatus.EN_ROUTE -> DeliveryTrackingService.start(context, o.assignmentId, o.orderNumber)
                        DeliveryStatus.ARRIVED, DeliveryStatus.COMPLETED, DeliveryStatus.ISSUE_REPORTED -> DeliveryTrackingService.stop(context)
                        else -> Unit
                    }
                    load(); then()
                }
                .onFailure { error = it.userMessage() }
            busy = false
        }
    }

    val o = order
    Column(Modifier.fillMaxSize()) {
        LgTopBar(o?.orderNumber ?: "Order", o?.status?.label, onBack = onBack, actions = { if (o != null) Box(Modifier.padding(end = 12.dp)) { StatusChip(o.status) } })
        when {
            missing -> Column(Modifier.padding(24.dp)) {
                Text("This order is no longer assigned to you.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                BigActionButton("Back", onClick = onBack, tone = Tone.Navy)
            }
            o == null -> if (error != null) Box(Modifier.padding(16.dp)) { ErrorNote(error) } else LoadingScreen()
            else -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (o.hasPin) {
                    container.maps.MiniMap(Destination(o.destLat!!, o.destLng!!, o.orderNumber), showCourier = hasLocation, modifier = Modifier.fillMaxWidth().height(200.dp))
                } else {
                    SectionCard { Text("No pinned location on this order. Use the address and call the customer if unsure.", color = Brand.Muted) }
                }
                SectionCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.LocationOn, null, tint = Brand.Forest)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(o.deliveryAddress ?: "Address not provided", style = MaterialTheme.typography.titleMedium)
                            o.landmark?.let { Text("Landmark: $it", color = Brand.Muted) }
                        }
                    }
                    Text("Customer: ${o.customerFirstName ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                    o.packageSummary?.let { Text("Package: $it", style = MaterialTheme.typography.bodyMedium) }
                }
                o.instructions?.takeIf { it.isNotBlank() }?.let {
                    SectionCard(accent = Brand.Warning) {
                        Text("Delivery instructions", style = MaterialTheme.typography.titleSmall)
                        Text(it)
                    }
                }
                StatusStepper(o.status)
                if (!o.status.isTerminalForCourier) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlineActionButton("Call", onClick = onCall, icon = Icons.Filled.Phone, modifier = Modifier.weight(1f))
                        BigActionButton(
                            "Navigate", tone = Tone.Navy, icon = Icons.Filled.Navigation, modifier = Modifier.weight(1f),
                            enabled = o.hasPin,
                            onClick = { container.maps.navigate(context, Destination(o.destLat!!, o.destLng!!, o.orderNumber)) },
                        )
                    }
                }
                ErrorNote(error)
                when (o.status) {
                    DeliveryStatus.ASSIGNED -> SwipeToConfirm("Swipe to accept order", enabled = !busy, onConfirmed = { move(DeliveryStatus.ACCEPTED) })
                    DeliveryStatus.ACCEPTED -> BigActionButton("Start trip", icon = Icons.Filled.TwoWheeler, loading = busy, onClick = {
                        if (!hasLocation) locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        move(DeliveryStatus.EN_ROUTE)
                    })
                    DeliveryStatus.EN_ROUTE -> SwipeToConfirm("Swipe — I've arrived", enabled = !busy, onConfirmed = { move(DeliveryStatus.ARRIVED) })
                    DeliveryStatus.ARRIVED -> BigActionButton("Hand over package", icon = Icons.Filled.Check, loading = busy, onClick = { move(DeliveryStatus.HANDOVER, onHandover) })
                    DeliveryStatus.HANDOVER -> BigActionButton("Enter delivery code", icon = Icons.Filled.Check, onClick = onHandover)
                    DeliveryStatus.COMPLETED -> SectionCard { Text("Delivered. Nice work.", style = MaterialTheme.typography.titleMedium) }
                    DeliveryStatus.ISSUE_REPORTED -> SectionCard(accent = Brand.Danger) {
                        Text("Issue reported", style = MaterialTheme.typography.titleMedium)
                        Text("LightGrab Office will follow up and move this delivery on.", color = Brand.Muted)
                    }
                }
                if (DeliveryStatus.ISSUE_REPORTED in o.status.courierNext) {
                    TextButton(onClick = onReportIssue, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.ReportProblem, null, tint = Brand.Danger)
                        Spacer(Modifier.width(6.dp))
                        Text("Report a problem", color = Brand.Danger, style = MaterialTheme.typography.titleSmall)
                    }
                }
                Text("Payment is handled by LightGrab Office. Couriers can't mark orders as paid.", color = Brand.Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatusStepper(current: DeliveryStatus) {
    SectionCard {
        Text("Delivery progress", style = MaterialTheme.typography.titleSmall)
        DeliveryStatus.STEPS.forEach { step ->
            val done = current != DeliveryStatus.ISSUE_REPORTED && step.step <= current.step
            val isCurrent = step == current
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Box(
                    Modifier.size(22.dp).clip(CircleShape).background(if (done) Brand.Forest else Brand.Sand),
                    contentAlignment = Alignment.Center,
                ) { if (done) Icon(Icons.Filled.Check, null, tint = Brand.White, modifier = Modifier.size(14.dp)) }
                Spacer(Modifier.width(10.dp))
                Text(
                    step.label,
                    style = if (isCurrent) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                    color = if (done) Brand.Navy else Brand.Muted,
                )
            }
        }
    }
}
