package ng.lightgrab.courier.feature.delivery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.BigActionButton
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ConfirmDialog
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.PhotoInput
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.feature.auth.StatusHero
import ng.lightgrab.courier.feature.orders.SecureScreen
import ng.lightgrab.courier.location.DeliveryTrackingService

/** Delivery confirmation: the customer's 4-digit code is checked on the server, never on the phone. */
@Composable
fun HandoverScreen(container: AppContainer, assignmentId: String, onBack: () -> Unit, onDone: () -> Unit) {
    SecureScreen()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<ByteArray?>(null) }
    var confirm by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        LgTopBar("Delivery confirmation", onBack = if (done) null else onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (done) {
                StatusHero(Icons.Filled.CheckCircle, "Delivery completed", "LightGrab Office has been notified.")
                BigActionButton("Back to my orders", onClick = onDone)
            } else {
                Surface(shape = RoundedCornerShape(14.dp), color = Brand.Navy) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Key, null, tint = Brand.Leaf)
                        Spacer(Modifier.width(12.dp))
                        Text("Ask the customer for their 4-digit delivery code", color = Brand.White, style = MaterialTheme.typography.titleSmall)
                    }
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { v -> code = v.filter(Char::isDigit).take(4); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("• • • •", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 32.sp) },
                    textStyle = TextStyle(fontSize = 36.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, letterSpacing = 12.sp, color = Brand.Navy),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brand.Forest, focusedContainerColor = Brand.White, unfocusedContainerColor = Brand.White),
                )
                PhotoInput("Handover photo (optional)", hasPhoto = photo != null, onPhoto = { photo = it })
                Text("Payment is recorded by LightGrab Office. You can't mark an order as paid.", color = Brand.Muted, style = MaterialTheme.typography.bodySmall)
                ErrorNote(error)
            }
        }
        if (!done) {
            Column(Modifier.padding(20.dp)) {
                BigActionButton("Complete delivery", enabled = code.length == 4, loading = busy, onClick = { confirm = true })
            }
        }
    }

    if (confirm) {
        ConfirmDialog(
            title = "Complete this delivery?",
            message = "Only confirm once the package is in the customer's hands.",
            confirmLabel = "Yes, delivered",
            onDismiss = { confirm = false },
            onConfirm = {
                confirm = false; busy = true; error = null
                scope.launch {
                    runCatching { container.repository.confirmHandover(assignmentId, code, photo) }
                        .onSuccess { DeliveryTrackingService.stop(context); done = true }
                        .onFailure { error = it.userMessage() }
                    busy = false
                }
            },
        )
    }
}
