package ng.lightgrab.courier.data

import kotlinx.coroutines.flow.Flow
import ng.lightgrab.courier.core.model.AccessState
import ng.lightgrab.courier.core.model.AppNotification
import ng.lightgrab.courier.core.model.ApplicationDraft
import ng.lightgrab.courier.core.model.AssignedOrder
import ng.lightgrab.courier.core.model.Availability
import ng.lightgrab.courier.core.model.CallSessionInfo
import ng.lightgrab.courier.core.model.DeliveryStatus
import ng.lightgrab.courier.core.model.DocKind
import ng.lightgrab.courier.core.model.IssueKind
import ng.lightgrab.courier.core.model.SessionState

/**
 * Everything the app is allowed to do against the shared LightGrab backend.
 * Every write goes through server-side checks (RPC / edge function); the app
 * never writes order, payment or assignment rows directly.
 */
interface CourierRepository {
    val isDemo: Boolean
    val session: Flow<SessionState>

    suspend fun signIn(identifier: String, password: String)
    /** Returns true when the new account is signed in straight away (false = email confirmation needed). */
    suspend fun signUp(email: String, phone: String, password: String): Boolean
    suspend fun sendPasswordReset(email: String)
    suspend fun updatePassword(newPassword: String)
    suspend fun signOut()

    suspend fun loadAccess(): AccessState
    /** Emits whenever the courier's account or application row changes (approval, suspension). */
    fun accountChanges(): Flow<Unit>

    suspend fun submitApplication(draft: ApplicationDraft, documents: Map<DocKind, ByteArray>)

    suspend fun setAvailability(availability: Availability)
    suspend fun assignedOrders(): List<AssignedOrder>
    suspend fun history(): List<AssignedOrder>
    /** Emits whenever Office creates or edits one of this courier's assignments. */
    fun assignmentChanges(): Flow<Unit>

    suspend fun updateStatus(assignmentId: String, to: DeliveryStatus, lat: Double?, lng: Double?, note: String? = null)
    suspend fun confirmHandover(assignmentId: String, code: String, photo: ByteArray?)
    suspend fun reportIssue(assignmentId: String, kind: IssueKind, description: String, photo: ByteArray?, lat: Double?, lng: Double?)
    suspend fun recordLocation(assignmentId: String, lat: Double, lng: Double, accuracyM: Float?)

    suspend fun notifications(): List<AppNotification>
    fun newNotifications(): Flow<AppNotification>
    suspend fun markNotificationRead(id: String)
    suspend fun registerDeviceToken(token: String)

    suspend fun createCallSession(assignmentId: String): CallSessionInfo
    suspend fun endCallSession(sessionId: String, reason: String)
}
