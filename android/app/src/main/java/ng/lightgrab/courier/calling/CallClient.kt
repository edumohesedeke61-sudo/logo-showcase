package ng.lightgrab.courier.calling

import kotlinx.coroutines.flow.StateFlow
import ng.lightgrab.courier.core.model.CallSessionInfo

sealed interface CallState {
    data object Idle : CallState
    data object Connecting : CallState
    data object Ringing : CallState
    data object Connected : CallState
    data object Reconnecting : CallState
    data class Failed(val reason: String) : CallState
    data class Ended(val reason: String) : CallState
}

/** VoIP contract. Calls always go through LightGrab, never the SIM dialer. */
interface CallClient {
    val state: StateFlow<CallState>
    val muted: StateFlow<Boolean>
    suspend fun connect(session: CallSessionInfo)
    suspend fun setMuted(muted: Boolean)
    suspend fun hangUp(reason: String = "Call ended")
    fun markFailed(reason: String)
    fun reset()
}
