package ng.lightgrab.courier.feature.call

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.calling.CallState
import ng.lightgrab.courier.core.designsystem.BigActionButton
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.OutlineActionButton
import ng.lightgrab.courier.core.designsystem.Pill
import ng.lightgrab.courier.core.designsystem.Tone
import ng.lightgrab.courier.core.model.CallSessionInfo
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.feature.auth.StatusHero

/** An unanswered call is ended after this long. */
const val RING_TIMEOUT_MS = 45_000L

/**
 * In-app VoIP call bound to ONE assignment. The server creates the call session for
 * that assignment's customer only, so the courier can't reach the wrong customer.
 */
@Composable
fun CallScreen(container: AppContainer, assignmentId: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val calls = container.calls
    val state by calls.state.collectAsStateWithLifecycle()
    val muted by calls.muted.collectAsStateWithLifecycle()
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var micDenied by remember { mutableStateOf(false) }
    var session by remember { mutableStateOf<CallSessionInfo?>(null) }
    var preparing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        micGranted = ok; micDenied = !ok
    }

    DisposableEffect(Unit) { calls.reset(); onDispose { scope.launch { calls.hangUp() } } }

    fun hangUp() = scope.launch {
        calls.hangUp()
        session?.let { s -> runCatching { container.repository.endCallSession(s.sessionId, "courier_hangup") } }
        onClose()
    }
    BackHandler { hangUp() }

    // Ring timeout: give up if the customer hasn't joined.
    LaunchedEffect(state) {
        if (state == CallState.Ringing) {
            delay(RING_TIMEOUT_MS)
            if (calls.state.value == CallState.Ringing) {
                calls.markFailed("No answer from the customer.")
                session?.let { s -> runCatching { container.repository.endCallSession(s.sessionId, "no_answer") } }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Brand.Navy).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            !micGranted -> {
                Spacer(Modifier.height(40.dp))
                StatusHero(
                    Icons.Filled.Mic,
                    if (micDenied) "Microphone is off" else "Allow microphone for in-app calls",
                    if (micDenied) "Calls can't work without the microphone. Turn it on in Settings, or report an issue if you can't reach the customer."
                    else "Calls go through LightGrab, not your SIM. Your number stays private and so does the customer's.",
                    dark = true,
                )
                Spacer(Modifier.weight(1f))
                if (micDenied) BigActionButton("Open Settings", tone = Tone.Leaf, onClick = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
                }) else BigActionButton("Continue", tone = Tone.Leaf, onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) })
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Not now", color = Brand.White) }
            }
            session == null -> {
                Spacer(Modifier.height(40.dp))
                StatusHero(Icons.Filled.Mic, "Call the customer", "We'll connect you securely to the customer on this order.", dark = true)
                if (error != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(error!!, color = Brand.Warning, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.weight(1f))
                BigActionButton("Start call", tone = Tone.Leaf, loading = preparing, onClick = {
                    preparing = true; error = null
                    scope.launch {
                        runCatching { container.repository.createCallSession(assignmentId) }
                            .onSuccess { s -> session = s; calls.connect(s) }
                            .onFailure { error = it.userMessage() }
                        preparing = false
                    }
                })
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Back", color = Brand.White) }
            }
            else -> {
                val s = session!!
                Pill("In-app call · ${s.orderNumber}", Brand.Leaf, Brand.Navy)
                Spacer(Modifier.height(40.dp))
                Box(Modifier.size(112.dp).clip(CircleShape).background(Brand.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text(s.customerFirstName.take(1).uppercase(), color = Brand.White, fontSize = 44.sp, style = MaterialTheme.typography.headlineLarge)
                }
                Spacer(Modifier.height(20.dp))
                Text(s.customerFirstName, color = Brand.White, style = MaterialTheme.typography.headlineMedium)
                Text(
                    when (val st = state) {
                        CallState.Idle, CallState.Connecting -> "Connecting securely…"
                        CallState.Ringing -> "Ringing…"
                        CallState.Connected -> "Connected"
                        CallState.Reconnecting -> "Reconnecting…"
                        is CallState.Failed -> st.reason
                        is CallState.Ended -> st.reason
                    },
                    color = Brand.White.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(16.dp))
                Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Brand.White.copy(alpha = 0.1f)).padding(12.dp)) {
                    Text("You're calling the customer for order ${s.orderNumber} only", color = Brand.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.weight(1f))
                if (state is CallState.Failed || state is CallState.Ended) {
                    BigActionButton("Try again", tone = Tone.Leaf, onClick = { session = null; calls.reset() })
                    Spacer(Modifier.height(8.dp))
                    OutlineActionButton("Close", onClick = onClose)
                } else {
                    Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        FilledIconButton(
                            onClick = { scope.launch { calls.setMuted(!muted) } },
                            modifier = Modifier.size(64.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (muted) Brand.Leaf else Brand.White.copy(alpha = 0.15f)),
                        ) { Icon(if (muted) Icons.Filled.MicOff else Icons.Filled.Mic, if (muted) "Unmute" else "Mute", tint = if (muted) Brand.Navy else Brand.White) }
                        FilledIconButton(
                            onClick = { hangUp() },
                            modifier = Modifier.size(76.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Brand.Danger),
                        ) { Icon(Icons.Filled.CallEnd, "End call", tint = Brand.White, modifier = Modifier.size(34.dp)) }
                    }
                }
            }
        }
    }
}
