package ng.lightgrab.courier.feature.auth

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ng.lightgrab.courier.R
import ng.lightgrab.courier.core.designsystem.BigActionButton
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTextField
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.OutlineActionButton
import ng.lightgrab.courier.core.designsystem.Tone
import ng.lightgrab.courier.core.model.AccessState
import ng.lightgrab.courier.core.model.ApplicationStatus
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.data.CourierRepository

@Composable
private fun Logo() {
    Surface(shape = RoundedCornerShape(20.dp), color = Brand.White, shadowElevation = 2.dp) {
        Image(painterResource(R.drawable.lightgrab_logo), contentDescription = "LightGrab", modifier = Modifier.padding(14.dp).width(150.dp))
    }
}

@Composable
fun SignInScreen(repo: CourierRepository, onForgot: () -> Unit, onApply: () -> Unit) {
    val scope = rememberCoroutineScope()
    var id by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo()
        Spacer(Modifier.height(24.dp))
        Text("Courier sign in", style = MaterialTheme.typography.headlineMedium)
        Text("Approved LightGrab couriers only", color = Brand.Muted)
        if (repo.isDemo) {
            Spacer(Modifier.height(8.dp))
            Text("Demo: enter any phone or email and password.", style = MaterialTheme.typography.bodySmall, color = Brand.Muted)
        }
        Spacer(Modifier.height(24.dp))
        LgTextField("Phone or email", id, { id = it }, placeholder = "080… or you@email.com", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        LgTextField("Password", pw, { pw = it }, password = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onForgot) { Text("Forgot password?", color = Brand.Forest) }
        }
        ErrorNote(error)
        Spacer(Modifier.height(12.dp))
        BigActionButton("Sign in", loading = busy, enabled = id.isNotBlank() && pw.isNotBlank(), onClick = {
            busy = true; error = null
            scope.launch {
                runCatching { repo.signIn(id, pw) }.onFailure { error = it.userMessage() }
                busy = false
            }
        })
        Spacer(Modifier.height(28.dp))
        Surface(shape = RoundedCornerShape(14.dp), color = Brand.White.copy(alpha = 0.6f), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Not a courier yet?", style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = onApply) { Text("Apply to deliver with LightGrab", color = Brand.Forest, style = MaterialTheme.typography.titleSmall) }
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(repo: CourierRepository, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        LgTopBar("Reset password", onBack = onBack)
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (sent) {
                StatusHero(Icons.Filled.MarkEmailRead, "Check your email", "If an account exists for $email, we've sent a link. Open it on this phone to set a new password.")
                BigActionButton("Back to sign in", onClick = onBack, tone = Tone.Navy)
            } else {
                Text("Enter the email on your courier account. We'll send you a reset link.", color = Brand.Muted)
                LgTextField("Email", email, { email = it }, keyboardType = KeyboardType.Email)
                ErrorNote(error)
                BigActionButton("Send reset link", loading = busy, enabled = email.contains("@"), onClick = {
                    busy = true; error = null
                    scope.launch {
                        runCatching { repo.sendPasswordReset(email) }.onSuccess { sent = true }.onFailure { error = it.userMessage() }
                        busy = false
                    }
                })
            }
        }
    }
}

@Composable
fun SetNewPasswordScreen(repo: CourierRepository, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var pw by remember { mutableStateOf("") }
    var pw2 by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize()) {
        LgTopBar("Set a new password")
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LgTextField("New password", pw, { pw = it }, password = true, supporting = "At least 8 characters")
            LgTextField("Repeat new password", pw2, { pw2 = it }, password = true)
            ErrorNote(error)
            BigActionButton("Save password", loading = busy, enabled = pw.length >= 8 && pw == pw2, onClick = {
                busy = true; error = null
                scope.launch {
                    runCatching { repo.updatePassword(pw) }.onSuccess { onDone() }.onFailure { error = it.userMessage() }
                    busy = false
                }
            })
        }
    }
}

@Composable
fun StatusHero(icon: ImageVector, title: String, body: String, dark: Boolean = false, iconBg: androidx.compose.ui.graphics.Color = Brand.Leaf) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(88.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (iconBg == Brand.Danger) Brand.White else Brand.Navy, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, color = if (dark) Brand.White else Brand.Navy)
        Spacer(Modifier.height(8.dp))
        Text(body, textAlign = TextAlign.Center, color = if (dark) Brand.White.copy(alpha = 0.8f) else Brand.Muted)
    }
}

@Composable
fun PendingScreen(state: AccessState.InReview, onRefresh: () -> Unit, onSignOut: () -> Unit) {
    val (title, body) = when (state.status) {
        ApplicationStatus.NEEDS_INFO -> "More information needed" to (state.note ?: "LightGrab staff need more information. Please contact LightGrab Office.")
        ApplicationStatus.REJECTED -> "Application not approved" to (state.note ?: "Your application was not approved. Contact LightGrab Office if you have questions.")
        else -> "Application under review" to "We'll notify you as soon as LightGrab staff approve your application. Deliveries stay locked until then."
    }
    Column(Modifier.fillMaxSize().background(Brand.Navy).verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        StatusHero(
            if (state.status == ApplicationStatus.REJECTED) Icons.Filled.ErrorOutline else Icons.Filled.HourglassTop,
            title, body, dark = true,
            iconBg = if (state.status == ApplicationStatus.REJECTED) Brand.Danger else Brand.Leaf,
        )
        Spacer(Modifier.height(28.dp))
        val steps = listOf("Submitted" to true, "Documents checked" to (state.status != ApplicationStatus.PENDING), "Approved" to false)
        steps.forEach { (label, done) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(Brand.White.copy(alpha = 0.1f)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(if (done) Brand.Leaf else Brand.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    if (done) Icon(Icons.Filled.Check, null, tint = Brand.Navy, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(label, color = Brand.White, style = MaterialTheme.typography.titleSmall)
            }
        }
        Spacer(Modifier.height(24.dp))
        BigActionButton("Check status", onClick = onRefresh, tone = Tone.Leaf)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out", color = Brand.White) }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, null, tint = Brand.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("No access to orders before approval", color = Brand.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SuspendedScreen(reason: String?, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brand.Navy).padding(24.dp)) {
        Spacer(Modifier.height(48.dp))
        StatusHero(
            Icons.Filled.ShieldMoon, "Account suspended",
            (reason?.let { "$it\n\n" } ?: "") + "Your access to deliveries is paused. Location sharing and calls have stopped. Contact LightGrab Office for help.",
            dark = true, iconBg = Brand.Danger,
        )
        Spacer(Modifier.weight(1f))
        BigActionButton("Sign out", onClick = onSignOut, tone = Tone.Leaf)
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        StatusHero(Icons.Filled.ErrorOutline, "Couldn't load your account", message, iconBg = Brand.Warning)
        Spacer(Modifier.height(24.dp))
        BigActionButton("Try again", onClick = onRetry)
        Spacer(Modifier.height(8.dp))
        OutlineActionButton("Sign out", onClick = onSignOut)
    }
}
