package ng.lightgrab.courier.feature.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.LoadingScreen
import ng.lightgrab.courier.core.model.AssignedOrder
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.feature.dashboard.OrderCard

@Composable
fun HistoryScreen(container: AppContainer) {
    var items by remember { mutableStateOf<List<AssignedOrder>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        runCatching { container.repository.history() }.onSuccess { items = it }.onFailure { error = it.userMessage() }
    }
    Column(Modifier.fillMaxSize()) {
        LgTopBar("Delivery history", "Completed and reported deliveries")
        val list = items
        when {
            error != null -> ErrorNote(error, Modifier.padding(16.dp))
            list == null -> LoadingScreen()
            list.isEmpty() -> Text("No finished deliveries yet.", color = Brand.Muted, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(24.dp))
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Customer details are not kept for finished deliveries; the card shows order number and status.
                items(list, key = { it.assignmentId }) { OrderCard(it) {} }
            }
        }
    }
}
