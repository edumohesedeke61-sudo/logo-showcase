package ng.lightgrab.courier.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ng.lightgrab.courier.LightGrabApp

/** Receives FCM data messages sent by the backend `dispatch-notification` function. */
class CourierMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val repo = (application as LightGrabApp).container.repository
        scope.launch { runCatching { repo.registerDeviceToken(token) } }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val d = message.data
        Notifier.show(
            context = this,
            id = d["notification_id"] ?: message.messageId ?: System.currentTimeMillis().toString(),
            kind = d["kind"] ?: "announcement",
            title = d["title"] ?: message.notification?.title ?: "LightGrab",
            body = d["body"] ?: message.notification?.body ?: "",
            assignmentId = d["assignment_id"],
        )
        if (d["kind"] == "account") (application as LightGrabApp).container.requestAccessRefresh()
    }
}
