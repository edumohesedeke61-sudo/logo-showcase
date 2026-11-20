package ng.lightgrab.courier.feature.delivery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.designsystem.BigActionButton
import ng.lightgrab.courier.core.designsystem.ConfirmDialog
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTextField
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.PhotoInput
import ng.lightgrab.courier.core.designsystem.Tone
import ng.lightgrab.courier.core.model.IssueKind
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.feature.auth.StatusHero
import ng.lightgrab.courier.feature.onboarding.RadioRow
import ng.lightgrab.courier.location.DeliveryTrackingService

@Composable
fun ReportIssueScreen(container: AppContainer, assignmentId: String, onBack: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var kind by remember { mutableStateOf<IssueKind?>(null) }
    var details by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<ByteArray?>(null) }
    var confirm by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        LgTopBar("Report an issue", "Office will follow up", onBack = if (sent) null else onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (sent) {
                StatusHero(Icons.Filled.SupportAgent, "Report sent", "LightGrab Office will contact you and decide the next step for this delivery.")
                BigActionButton("Back to my orders", onClick = onDone, tone = Tone.Navy)
            } else {
                IssueKind.entries.forEach { k -> RadioRow(k.label, kind == k) { kind = k } }
                LgTextField("What happened?", details, { details = it }, singleLine = false, minLines = 3)
                PhotoInput("Photo (optional)", hasPhoto = photo != null, onPhoto = { photo = it })
                ErrorNote(error)
            }
        }
        if (!sent) {
            Column(Modifier.padding(20.dp)) {
                BigActionButton(
                    "Send report", tone = Tone.Danger, icon = Icons.Filled.ReportProblem, loading = busy,
                    enabled = kind != null && (kind != IssueKind.OTHER || details.isNotBlank()),
                    onClick = { confirm = true },
                )
            }
        }
    }
    if (confirm) {
        ConfirmDialog(
            title = "Send this report?",
            message = "The delivery will pause until LightGrab Office follows up.",
            confirmLabel = "Send report",
            danger = true,
            onDismiss = { confirm = false },
            onConfirm = {
                confirm = false; busy = true; error = null
                scope.launch {
                    val fix = container.location.currentFix()
                    runCatching { container.repository.reportIssue(assignmentId, kind!!, details, photo, fix?.lat, fix?.lng) }
                        .onSuccess { DeliveryTrackingService.stop(context); sent = true }
                        .onFailure { error = it.userMessage() }
                    busy = false
                }
            },
        )
    }
}
