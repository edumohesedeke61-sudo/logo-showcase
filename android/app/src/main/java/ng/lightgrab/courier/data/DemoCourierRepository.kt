package ng.lightgrab.courier.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import ng.lightgrab.courier.core.model.AccessState
import ng.lightgrab.courier.core.model.AppNotification
import ng.lightgrab.courier.core.model.ApplicationDraft
import ng.lightgrab.courier.core.model.ApplicationStatus
import ng.lightgrab.courier.core.model.AssignedOrder
import ng.lightgrab.courier.core.model.Availability
import ng.lightgrab.courier.core.model.CallSessionInfo
import ng.lightgrab.courier.core.model.CourierProfile
import ng.lightgrab.courier.core.model.DeliveryStatus
import ng.lightgrab.courier.core.model.DocKind
import ng.lightgrab.courier.core.model.IssueKind
import ng.lightgrab.courier.core.model.SessionState
import ng.lightgrab.courier.core.util.UserFacingException
import java.time.Instant

/**
 * Used only when no LightGrab backend is configured in the build. All data is
 * sample data held in memory and the app shows a "Demo mode" banner throughout.
 */
class DemoCourierRepository : CourierRepository {
    override val isDemo = true

    private val signedIn = MutableStateFlow(false)
    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    private var access: AccessState = AccessState.SignedOut
    private var availability = Availability.ONLINE

    private val orders = mutableListOf(
        AssignedOrder("demo-a1", "demo-o1", "LG-DEMO-01", DeliveryStatus.ASSIGNED, "Sample customer", "Sample address, Oron Road", "Sample landmark", 4.8240, 8.2300, "1 sample package", "Sample instruction from Office", Instant.now().toString(), null),
        AssignedOrder("demo-a2", "demo-o2", "LG-DEMO-02", DeliveryStatus.ACCEPTED, "Sample customer", "Sample address, Eyo Abasi", null, 4.8102, 8.2459, "2 sample packages", null, Instant.now().toString(), null),
    )
    private val done = mutableListOf<AssignedOrder>()
    private val inbox = mutableListOf(
        AppNotification("n1", "assignment", "New assignment", "LG-DEMO-01 was assigned to you (sample)", "demo-a1", Instant.now().toString(), false),
        AppNotification("n2", "announcement", "Announcement", "Sample operational update for all couriers", null, Instant.now().toString(), true),
    )

    override val session: Flow<SessionState> = signedIn.map { if (it) SessionState.SignedIn("demo-courier") else SessionState.SignedOut }

    private fun profile() = CourierProfile("demo-courier", "Demo Courier", "+2348000000000", "Oron town", "motorcycle", availability)

    override suspend fun signIn(identifier: String, password: String) {
        delay(400)
        if (identifier.isBlank() || password.isBlank()) throw UserFacingException("Enter any phone or email and password to try the demo.")
        access = AccessState.Active(profile()); signedIn.value = true
    }

    override suspend fun signUp(email: String, phone: String, password: String): Boolean {
        delay(400); access = AccessState.NeedsApplication; signedIn.value = true; return true
    }

    override suspend fun sendPasswordReset(email: String) { delay(300) }
    override suspend fun updatePassword(newPassword: String) { delay(300) }
    override suspend fun signOut() { signedIn.value = false; access = AccessState.SignedOut }

    override suspend fun loadAccess(): AccessState { delay(200); return access }
    override fun accountChanges(): Flow<Unit> = emptyFlow()

    override suspend fun submitApplication(draft: ApplicationDraft, documents: Map<DocKind, ByteArray>) {
        delay(800); access = AccessState.InReview(ApplicationStatus.PENDING, null)
    }

    override suspend fun setAvailability(availability: Availability) { this.availability = availability }
    override suspend fun assignedOrders(): List<AssignedOrder> = orders.toList()
    override suspend fun history(): List<AssignedOrder> = done.toList()
    override fun assignmentChanges(): Flow<Unit> = changes

    private fun move(id: String, to: DeliveryStatus) {
        val i = orders.indexOfFirst { it.assignmentId == id }
        if (i < 0) throw UserFacingException("This order is no longer assigned to you.")
        val o = orders[i]
        if (!o.status.canMoveTo(to)) throw UserFacingException("That step isn't allowed from \"${o.status.label}\".")
        val updated = o.copy(status = to, completedAt = if (to == DeliveryStatus.COMPLETED) Instant.now().toString() else null)
        if (to == DeliveryStatus.COMPLETED || to == DeliveryStatus.ISSUE_REPORTED) { orders.removeAt(i); done.add(0, updated) } else orders[i] = updated
        changes.tryEmit(Unit)
    }

    override suspend fun updateStatus(assignmentId: String, to: DeliveryStatus, lat: Double?, lng: Double?, note: String?) { delay(300); move(assignmentId, to) }

    override suspend fun confirmHandover(assignmentId: String, code: String, photo: ByteArray?) {
        delay(400)
        if (code != "1234") throw UserFacingException("That code doesn't match. (Demo code is 1234.)")
        move(assignmentId, DeliveryStatus.COMPLETED)
    }

    override suspend fun reportIssue(assignmentId: String, kind: IssueKind, description: String, photo: ByteArray?, lat: Double?, lng: Double?) {
        delay(400); move(assignmentId, DeliveryStatus.ISSUE_REPORTED)
    }

    override suspend fun recordLocation(assignmentId: String, lat: Double, lng: Double, accuracyM: Float?) {}
    override suspend fun notifications(): List<AppNotification> = inbox.toList()
    override fun newNotifications(): Flow<AppNotification> = emptyFlow()
    override suspend fun markNotificationRead(id: String) {
        val i = inbox.indexOfFirst { it.id == id }; if (i >= 0) inbox[i] = inbox[i].copy(read = true)
    }
    override suspend fun registerDeviceToken(token: String) {}

    override suspend fun createCallSession(assignmentId: String): CallSessionInfo =
        throw UserFacingException("In-app calls need the live LightGrab backend. They are switched off in demo mode.")

    override suspend fun endCallSession(sessionId: String, reason: String) {}
}
