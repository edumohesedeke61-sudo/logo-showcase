package ng.lightgrab.courier.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ng.lightgrab.courier.AppContainer
import ng.lightgrab.courier.core.model.AccessState
import ng.lightgrab.courier.core.model.SessionState
import ng.lightgrab.courier.core.util.userMessage

/** Session gate: decides which part of the app the signed-in person may see. */
class AppViewModel(private val c: AppContainer) : ViewModel() {
    private val _access = MutableStateFlow<AccessState>(AccessState.Loading)
    val access: StateFlow<AccessState> = _access.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            c.repository.session.collectLatest { s ->
                when (s) {
                    SessionState.Loading -> _access.value = AccessState.Loading
                    SessionState.SignedOut -> { c.stopActiveWork(); _access.value = AccessState.SignedOut }
                    is SessionState.SignedIn -> {
                        refresh()
                        // Approval or suspension by Admin takes effect immediately.
                        runCatching { c.repository.accountChanges().collect { refresh() } }
                    }
                }
            }
        }
        viewModelScope.launch { c.accessRefresh.collect { refresh() } }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val next = runCatching { c.repository.loadAccess() }.getOrElse { AccessState.Error(it.userMessage()) }
            if (next is AccessState.Suspended) c.stopActiveWork()
            _access.value = next
        }
    }

    fun signOut() {
        viewModelScope.launch {
            c.stopActiveWork()
            runCatching { c.repository.signOut() }
            _access.value = AccessState.SignedOut
        }
    }
}
