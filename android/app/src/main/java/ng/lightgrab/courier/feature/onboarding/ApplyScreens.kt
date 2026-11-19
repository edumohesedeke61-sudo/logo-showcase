package ng.lightgrab.courier.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ng.lightgrab.courier.core.designsystem.BigActionButton
import ng.lightgrab.courier.core.designsystem.Brand
import ng.lightgrab.courier.core.designsystem.ErrorNote
import ng.lightgrab.courier.core.designsystem.LgTextField
import ng.lightgrab.courier.core.designsystem.LgTopBar
import ng.lightgrab.courier.core.designsystem.PhotoInput
import ng.lightgrab.courier.core.designsystem.SectionCard
import ng.lightgrab.courier.core.designsystem.Tone
import ng.lightgrab.courier.core.model.IdType
import ng.lightgrab.courier.core.model.OPERATING_AREAS
import ng.lightgrab.courier.core.model.VehicleType
import ng.lightgrab.courier.core.util.NigerianPhone
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.data.CourierRepository
import ng.lightgrab.courier.feature.auth.StatusHero

/** Step 1 for new applicants: create the account the application belongs to. */
@Composable
fun CreateAccountScreen(repo: CourierRepository, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var needsConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        LgTopBar("Apply to deliver", "Step 1 · Create your account", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (needsConfirm) {
                StatusHero(Icons.Filled.MarkEmailRead, "Confirm your email", "We sent a link to $email. Open it, then sign in to continue your application.")
                BigActionButton("Back to sign in", onClick = onBack, tone = Tone.Navy)
            } else {
                Text("Your account stays locked until LightGrab staff approve your application.", color = Brand.Muted)
                LgTextField("Email", email, { email = it }, keyboardType = KeyboardType.Email)
                LgTextField("Phone number", phone, { phone = it }, placeholder = "080…", keyboardType = KeyboardType.Phone)
                LgTextField("Password", pw, { pw = it }, password = true, supporting = "At least 8 characters")
                ErrorNote(error)
                BigActionButton(
                    "Create account", loading = busy,
                    enabled = email.contains("@") && NigerianPhone.toE164(phone) != null && pw.length >= 8,
                    onClick = {
                        busy = true; error = null
                        scope.launch {
                            runCatching { repo.signUp(email, phone, pw) }
                                .onSuccess { signedIn -> if (!signedIn) needsConfirm = true }
                                .onFailure { error = it.userMessage() }
                            busy = false
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApplyScreen(repo: CourierRepository, onSubmitted: () -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val vm: ApplyViewModel = viewModel { ApplyViewModel(repo, context) }
    val s by vm.state.collectAsStateWithLifecycle()
    val d = s.draft
    BackHandler(enabled = s.step > 0) { vm.back() }

    Column(Modifier.fillMaxSize()) {
        LgTopBar(
            "Apply to deliver", "Step ${s.step + 1} of 5 · ${vm.stepTitles[s.step]}",
            onBack = if (s.step > 0) ({ vm.back(); Unit }) else null,
            actions = { if (s.step == 0) TextButton(onClick = onSignOut) { Text("Sign out", color = Brand.White) } },
        )
        LinearProgressIndicator(
            progress = { (s.step + 1) / 5f }, modifier = Modifier.fillMaxWidth().height(6.dp),
            color = Brand.Leaf, trackColor = Brand.Sand,
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (s.step) {
                0 -> {
                    LgTextField("Full name", d.fullName, { v -> vm.update { it.copy(fullName = v) } })
                    LgTextField("Phone number", d.phone, { v -> vm.update { it.copy(phone = v) } }, placeholder = "080…", keyboardType = KeyboardType.Phone)
                    LgTextField("Email (optional)", d.email, { v -> vm.update { it.copy(email = v) } }, keyboardType = KeyboardType.Email)
                    LgTextField("Residential address", d.residentialAddress, { v -> vm.update { it.copy(residentialAddress = v) } }, singleLine = false, minLines = 2)
                    Text("Where will you deliver?", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OPERATING_AREAS.forEach { area ->
                            ChoiceChip(area, d.operatingArea == area) { vm.update { it.copy(operatingArea = area) } }
                        }
                    }
                }
                1 -> {
                    Text("Type of ID", style = MaterialTheme.typography.titleSmall)
                    IdType.entries.forEach { t -> RadioRow(t.label, d.idType == t) { vm.update { it.copy(idType = t) } } }
                    LgTextField("ID number", d.idNumber, { v -> vm.update { it.copy(idNumber = v) } }, supporting = "Only LightGrab staff can see this")
                }
                2 -> {
                    Text("How will you deliver?", style = MaterialTheme.typography.titleSmall)
                    VehicleType.entries.forEach { t -> RadioRow(t.label, d.vehicleType == t) { vm.update { it.copy(vehicleType = t) } } }
                    if (d.vehicleType.needsPapers) {
                        LgTextField("Plate number", d.vehiclePlate, { v -> vm.update { it.copy(vehiclePlate = v.uppercase()) } })
                    }
                }
                3 -> {
                    Text("Clear photos in good light. Files are stored privately and only seen by LightGrab staff.", color = Brand.Muted)
                    d.requiredDocs().forEach { kind ->
                        PhotoInput(kind.label, kind in s.docs, onPhoto = { vm.setDoc(kind, it) })
                    }
                }
                else -> {
                    SectionCard {
                        Text("Check your details", style = MaterialTheme.typography.titleMedium)
                        Review("Name", d.fullName); Review("Phone", d.phone); Review("Email", d.email.ifBlank { "—" })
                        Review("Address", d.residentialAddress); Review("Area", d.operatingArea)
                        Review("ID", "${d.idType.label} · ${d.idNumber}")
                        Review("Vehicle", d.vehicleType.label + if (d.vehicleType.needsPapers) " · ${d.vehiclePlate}" else "")
                        Review("Documents", "${s.docs.size} of ${d.requiredDocs().size} added")
                    }
                    Text("By submitting you confirm these details are true. Your application stays pending until LightGrab staff approve it.", color = Brand.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
            ErrorNote(s.error)
        }
        Box(Modifier.padding(20.dp)) {
            if (s.step < 4) BigActionButton("Continue", onClick = vm::next, tone = Tone.Navy, enabled = vm.stepValid())
            else BigActionButton("Submit application", loading = s.submitting, onClick = { vm.submit(onSubmitted) })
        }
    }
}

@Composable
private fun Review(label: String, value: String) {
    Row { Text("$label: ", style = MaterialTheme.typography.titleSmall); Text(value, color = Brand.Muted) }
}

@Composable
fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(if (selected) Brand.Forest else Brand.White)
            .border(2.dp, if (selected) Brand.Forest else Brand.Sand, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) { Text(label, color = if (selected) Brand.White else Brand.Navy, style = MaterialTheme.typography.titleSmall) }
}

@Composable
fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Brand.White)
            .border(2.dp, if (selected) Brand.Forest else Brand.White, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked, null, tint = if (selected) Brand.Forest else Brand.Muted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleSmall)
    }
}
