package ng.lightgrab.courier.feature.notifications

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.LoadingScreen
import ng.lightgrab.courier.core.model.AppNotification
import ng.lightgrab.courier.core.util.userMessage

@Composable
fun NotificationsScreen(container: AppContainer, onOpenOrder: (String) -> Unit, onRead: () -> Unit) {
    val repo = container.repository
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<AppNotification>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        runCatching { repo.notifications() }.onSuccess { items = it }.onFailure { error = it.userMessage() }
        runCatching { repo.newNotifications().collect { n -> items = listOf(n) + items.orEmpty() } }
    }
    Column(Modifier.fillMaxSize()) {
        LgTopBar("Alerts", "Newest first")
        val list = items
        when {
            error != null -> ErrorNote(error, Modifier.padding(16.dp))
            list == null -> LoadingScreen()
            list.isEmpty() -> Text("No alerts yet.", color = Brand.Muted, modifier = Modifier.padding(24.dp))
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list, key = { it.id }) { n ->
                    NotificationRow(n) {
                        if (!n.read) {
                            items = items?.map { if (it.id == n.id) it.copy(read = true) else it }
                            onRead()
                            scope.launch { runCatching { repo.markNotificationRead(n.id) } }
                        }
                        n.assignmentId?.let(onOpenOrder)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, onClick: () -> Unit) {
    val (icon, bg) = when (n.kind) {
        "assignment" -> Icons.Filled.Inventory2 to Brand.Leaf
        "assignment_changed", "assignment_withdrawn" -> Icons.Filled.SyncProblem to Brand.Warning
        "customer_update" -> Icons.Filled.Person to Brand.Sand
        "instruction" -> Icons.Filled.SpeakerNotes to Brand.Navy
        else -> Icons.Filled.Campaign to Brand.Sand
    }
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = Brand.White, shadowElevation = 1.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(bg), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (bg == Brand.Navy) Brand.White else Brand.Navy)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(n.title, style = MaterialTheme.typography.titleSmall)
                Text(n.body, color = Brand.Muted, style = MaterialTheme.typography.bodySmall)
            }
            if (!n.read) Box(Modifier.size(10.dp).clip(CircleShape).background(Brand.Forest))
        }
    }
}
