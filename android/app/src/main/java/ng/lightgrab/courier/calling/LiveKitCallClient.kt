package ng.lightgrab.courier.calling

import android.content.Context
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.lightgrab.courier.core.model.CallSessionInfo

/**
 * LiveKit implementation. The token (created server-side by `create-call-session`)
 * only grants access to the one room for this assignment's customer.
 */
class LiveKitCallClient(private val context: Context) : CallClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow<CallState>(CallState.Idle)
    override val state: StateFlow<CallState> = _state.asStateFlow()
    private val _muted = MutableStateFlow(false)
    override val muted: StateFlow<Boolean> = _muted.asStateFlow()

    private var room: Room? = null
    private var events: Job? = null

    override suspend fun connect(session: CallSessionInfo) {
        release()
        _muted.value = false
        _state.value = CallState.Connecting
        val r = LiveKit.create(context.applicationContext)
        room = r
        events = scope.launch {
            r.events.collect { event ->
                when (event) {
                    is RoomEvent.ParticipantConnected -> _state.value = CallState.Connected
                    is RoomEvent.ParticipantDisconnected -> if (r.remoteParticipants.isEmpty()) hangUp("Customer left the call")
                    is RoomEvent.Reconnecting -> _state.value = CallState.Reconnecting
                    is RoomEvent.Reconnected -> _state.value = if (r.remoteParticipants.isEmpty()) CallState.Ringing else CallState.Connected
                    is RoomEvent.Disconnected -> if (_state.value !is CallState.Ended && _state.value !is CallState.Failed) {
                        _state.value = CallState.Ended("Call ended")
                    }
                    is RoomEvent.FailedToConnect -> _state.value = CallState.Failed("Couldn't connect the call. Check your signal.")
                    else -> Unit
                }
            }
        }
        try {
            r.connect(session.serverUrl, session.token)
            r.localParticipant.setMicrophoneEnabled(true)
            _state.value = if (r.remoteParticipants.isEmpty()) CallState.Ringing else CallState.Connected
        } catch (t: Throwable) {
            _state.value = CallState.Failed("Couldn't connect the call. Check your signal.")
            release()
        }
    }

    override suspend fun setMuted(muted: Boolean) {
        room?.localParticipant?.setMicrophoneEnabled(!muted)
        _muted.value = muted
    }

    override suspend fun hangUp(reason: String) {
        if (_state.value !is CallState.Failed) _state.value = CallState.Ended(reason)
        release()
    }

    override fun markFailed(reason: String) {
        _state.value = CallState.Failed(reason)
        release()
    }

    override fun reset() {
        release()
        _state.value = CallState.Idle
    }

    private fun release() {
        events?.cancel(); events = null
        room?.let { runCatching { it.disconnect() }; runCatching { it.release() } }
        room = null
    }
}
