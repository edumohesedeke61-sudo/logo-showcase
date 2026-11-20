package ng.lightgrab.courier.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.BuildConfig
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ConfirmDialog
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.OutlineActionButton
import ng.lightgrab.courier.core.designsystem.SectionCard
import ng.lightgrab.courier.core.model.CourierProfile
import ng.lightgrab.courier.core.model.VehicleType

@Composable
fun ProfileScreen(container: AppContainer, courier: CourierProfile, onSignOut: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        LgTopBar("Profile")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard {
                Text(courier.fullName, style = MaterialTheme.typography.headlineSmall)
                courier.phone?.let { Text(it, color = Brand.Muted) }
            }
            SectionCard {
                Text("Operating area", style = MaterialTheme.typography.labelMedium, color = Brand.Muted)
                Text(courier.operatingArea ?: "—", style = MaterialTheme.typography.titleMedium)
                Text("Vehicle", style = MaterialTheme.typography.labelMedium, color = Brand.Muted)
                Text(VehicleType.entries.firstOrNull { it.wire == courier.vehicleType }?.label ?: courier.vehicleType ?: "—", style = MaterialTheme.typography.titleMedium)
            }
            SectionCard {
                Text("To change your details or documents, contact LightGrab Office.", color = Brand.Muted)
            }
            OutlineActionButton("Sign out", onClick = { confirm = true }, icon = Icons.AutoMirrored.Filled.Logout)
            Text(
                "LightGrab Courier ${BuildConfig.VERSION_NAME} · Map: ${container.maps.name} · " + if (container.repository.isDemo) "Demo mode" else "Connected to LightGrab",
                color = Brand.Muted, style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    if (confirm) {
        ConfirmDialog(
            title = "Sign out?",
            message = "Location sharing stops and you won't get new order alerts until you sign in again.",
            confirmLabel = "Sign out",
            onDismiss = { confirm = false },
            onConfirm = { confirm = false; onSignOut() },
        )
    }
}
