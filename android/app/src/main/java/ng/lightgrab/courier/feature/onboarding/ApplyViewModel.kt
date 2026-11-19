package ng.lightgrab.courier.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ng.lightgrab.courier.core.model.ApplicationDraft
import ng.lightgrab.courier.core.model.DocKind
import ng.lightgrab.courier.core.model.IdType
import ng.lightgrab.courier.core.model.VehicleType
import ng.lightgrab.courier.core.util.NigerianPhone
import ng.lightgrab.courier.core.util.userMessage
import ng.lightgrab.courier.data.CourierRepository

data class ApplyUiState(
    val step: Int = 0,
    val draft: ApplicationDraft = ApplicationDraft(),
    val docs: Map<DocKind, ByteArray> = emptyMap(),
    val submitting: Boolean = false,
    val error: String? = null,
)

@Serializable
private data class SavedDraft(
    val fullName: String = "", val phone: String = "", val email: String = "", val residentialAddress: String = "",
    val operatingArea: String = "", val idType: String = "", val idNumber: String = "", val vehicleType: String = "", val vehiclePlate: String = "",
)

/** Holds the application in progress. Text fields are saved on the phone so a lost signal doesn't lose progress. */
class ApplyViewModel(private val repo: CourierRepository, context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("courier_application_draft", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(ApplyUiState(draft = restore()))
    val state: StateFlow<ApplyUiState> = _state.asStateFlow()

    val stepTitles = listOf("Personal details", "Identity", "Vehicle", "Documents", "Review & submit")

    fun update(transform: (ApplicationDraft) -> ApplicationDraft) {
        _state.update { it.copy(draft = transform(it.draft), error = null) }
        save(_state.value.draft)
    }

    fun setDoc(kind: DocKind, bytes: ByteArray) = _state.update { it.copy(docs = it.docs + (kind to bytes), error = null) }

    fun stepValid(step: Int = _state.value.step): Boolean {
        val d = _state.value.draft
        return when (step) {
            0 -> d.fullName.trim().length >= 3 && NigerianPhone.toE164(d.phone) != null && d.residentialAddress.isNotBlank() && d.operatingArea.isNotBlank()
            1 -> d.idNumber.trim().length >= 5
            2 -> !d.vehicleType.needsPapers || d.vehiclePlate.isNotBlank()
            3 -> d.requiredDocs().all { it in _state.value.docs }
            else -> true
        }
    }

    fun next() = _state.update { if (stepValid()) it.copy(step = (it.step + 1).coerceAtMost(4)) else it.copy(error = "Please complete this step first.") }
    fun back(): Boolean {
        if (_state.value.step == 0) return false
        _state.update { it.copy(step = it.step - 1, error = null) }
        return true
    }

    fun submit(onDone: () -> Unit) {
        val s = _state.value
        if (!(0..3).all { stepValid(it) }) { _state.update { it.copy(error = "Some steps are incomplete.") }; return }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            runCatching { repo.submitApplication(s.draft, s.docs.filterKeys { it in s.draft.requiredDocs() }) }
                .onSuccess { prefs.edit().clear().apply(); _state.update { it.copy(submitting = false) }; onDone() }
                .onFailure { e -> _state.update { it.copy(submitting = false, error = e.userMessage()) } }
        }
    }

    private fun save(d: ApplicationDraft) {
        val saved = SavedDraft(d.fullName, d.phone, d.email, d.residentialAddress, d.operatingArea, d.idType.wire, d.idNumber, d.vehicleType.wire, d.vehiclePlate)
        prefs.edit().putString("draft", json.encodeToString(SavedDraft.serializer(), saved)).apply()
    }

    private fun restore(): ApplicationDraft {
        val raw = prefs.getString("draft", null) ?: return ApplicationDraft()
        val s = runCatching { json.decodeFromString(SavedDraft.serializer(), raw) }.getOrNull() ?: return ApplicationDraft()
        return ApplicationDraft(
            fullName = s.fullName, phone = s.phone, email = s.email, residentialAddress = s.residentialAddress, operatingArea = s.operatingArea,
            idType = IdType.entries.firstOrNull { it.wire == s.idType } ?: IdType.NIN, idNumber = s.idNumber,
            vehicleType = VehicleType.entries.firstOrNull { it.wire == s.vehicleType } ?: VehicleType.MOTORCYCLE, vehiclePlate = s.vehiclePlate,
        )
    }
}
