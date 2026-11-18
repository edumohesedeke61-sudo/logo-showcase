package ng.lightgrab.courier.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
import ng.lightgrab.courier.core.util.NigerianPhone
import ng.lightgrab.courier.core.util.UserFacingException

private const val DOCUMENTS_BUCKET = "courier-documents"
private const val DELIVERY_PHOTOS_BUCKET = "delivery-photos"

class SupabaseCourierRepository(private val client: SupabaseClient) : CourierRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override val isDemo = false

    override val session: Flow<SessionState> = client.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> SessionState.Loading
            is SessionStatus.Authenticated -> status.session.user?.id?.let { SessionState.SignedIn(it) } ?: SessionState.Loading
            is SessionStatus.NotAuthenticated -> SessionState.SignedOut
            // Network failure while refreshing: keep the user signed in if we still know who they are.
            is SessionStatus.RefreshFailure -> client.auth.currentUserOrNull()?.id?.let { SessionState.SignedIn(it) } ?: SessionState.SignedOut
        }
    }.distinctUntilChanged()

    private fun uid(): String = client.auth.currentUserOrNull()?.id ?: throw UserFacingException("Please sign in again.")

    // ---------- Auth ----------

    override suspend fun signIn(identifier: String, password: String) {
        val id = identifier.trim()
        if (id.contains("@")) {
            client.auth.signInWith(Email) { email = id; this.password = password }
        } else {
            val e164 = NigerianPhone.toE164(id) ?: throw UserFacingException("Enter a valid Nigerian phone number or an email address.")
            client.auth.signInWith(Phone) { phone = e164; this.password = password }
        }
    }

    override suspend fun signUp(email: String, phone: String, password: String): Boolean {
        val e164 = NigerianPhone.toE164(phone) ?: throw UserFacingException("Enter a valid Nigerian phone number.")
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = buildJsonObject { put("phone", e164); put("signup_source", "courier_app") }
        }
        return client.auth.currentSessionOrNull() != null
    }

    override suspend fun sendPasswordReset(email: String) {
        client.auth.resetPasswordForEmail(email.trim(), redirectUrl = "lightgrab://auth/recovery")
    }

    override suspend fun updatePassword(newPassword: String) {
        client.auth.updateUser { password = newPassword }
    }

    override suspend fun signOut() {
        client.realtime.removeAllChannels()
        client.auth.signOut()
    }

    // ---------- Access ----------

    override suspend fun loadAccess(): AccessState {
        val uid = client.auth.currentUserOrNull()?.id ?: return AccessState.SignedOut
        val courier = client.from("couriers").select { filter { eq("id", uid) } }.decodeList<CourierRow>().firstOrNull()
        if (courier != null) {
            if (courier.accountStatus != "active") return AccessState.Suspended(courier.suspendedReason)
            val profile = client.from("profiles").select { filter { eq("id", uid) } }.decodeList<ProfileRow>().firstOrNull()
            return AccessState.Active(
                CourierProfile(
                    id = uid,
                    fullName = profile?.fullName ?: "Courier",
                    phone = profile?.phone,
                    operatingArea = courier.operatingArea,
                    vehicleType = courier.vehicleType,
                    availability = Availability.fromWire(courier.availability),
                ),
            )
        }
        val application = client.from("courier_applications").select {
            filter { eq("user_id", uid) }
            order("created_at", Order.DESCENDING)
            limit(1)
        }.decodeList<ApplicationRow>().firstOrNull()
        return if (application == null) AccessState.NeedsApplication
        else AccessState.InReview(ApplicationStatus.fromWire(application.status), application.decisionNote)
    }

    override fun accountChanges(): Flow<Unit> = realtimeFlow("account") { channel, uid ->
        merge(
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "couriers"; filter("id", FilterOperator.EQ, uid)
            },
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "courier_applications"; filter("user_id", FilterOperator.EQ, uid)
            },
        ).map { }
    }

    // ---------- Application ----------

    override suspend fun submitApplication(draft: ApplicationDraft, documents: Map<DocKind, ByteArray>) {
        val uid = uid()
        val phone = NigerianPhone.toE164(draft.phone) ?: throw UserFacingException("Enter a valid Nigerian phone number.")
        val paths = documents.mapValues { (kind, bytes) ->
            val path = "$uid/application/${kind.wire}-${System.currentTimeMillis()}.jpg"
            client.storage.from(DOCUMENTS_BUCKET).upload(path, bytes) { upsert = true }
            path
        }
        invoke(
            "submit-courier-application",
            buildJsonObject {
                put("full_name", draft.fullName.trim())
                put("phone", phone)
                put("email", draft.email.trim())
                put("residential_address", draft.residentialAddress.trim())
                put("operating_area", draft.operatingArea)
                put("id_type", draft.idType.wire)
                put("id_number", draft.idNumber.trim())
                put("vehicle_type", draft.vehicleType.wire)
                put("vehicle_plate", draft.vehiclePlate.trim())
                put("documents", buildJsonObject { paths.forEach { (k, v) -> put(k.wire, v) } })
            },
        )
    }

    // ---------- Deliveries ----------

    override suspend fun setAvailability(availability: Availability) {
        client.postgrest.rpc("courier_set_availability", buildJsonObject { put("availability", availability.wire) })
    }

    override suspend fun assignedOrders(): List<AssignedOrder> =
        client.from("courier_assigned_orders").select { order("assigned_at", Order.DESCENDING) }
            .decodeList<AssignedOrderRow>().mapNotNull { it.toModel() }

    override suspend fun history(): List<AssignedOrder> =
        client.from("courier_delivery_history").select {
            order("assigned_at", Order.DESCENDING)
            limit(100)
        }.decodeList<AssignedOrderRow>().mapNotNull { it.toModel() }

    override fun assignmentChanges(): Flow<Unit> = realtimeFlow("assignments") { channel, uid ->
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "delivery_assignments"; filter("courier_id", FilterOperator.EQ, uid)
        }.map { }
    }

    override suspend fun updateStatus(assignmentId: String, to: DeliveryStatus, lat: Double?, lng: Double?, note: String?) {
        client.postgrest.rpc(
            "courier_update_delivery_status",
            buildJsonObject {
                put("assignment_id", assignmentId)
                put("to_status", to.wire)
                put("lat", lat)
                put("lng", lng)
                put("note", note)
            },
        )
    }

    override suspend fun confirmHandover(assignmentId: String, code: String, photo: ByteArray?) {
        val photoPath = photo?.let { uploadDeliveryPhoto(assignmentId, "handover", it) }
        invoke(
            "confirm-handover",
            buildJsonObject {
                put("assignment_id", assignmentId)
                put("code", code)
                put("photo_path", photoPath)
            },
        )
    }

    override suspend fun reportIssue(assignmentId: String, kind: IssueKind, description: String, photo: ByteArray?, lat: Double?, lng: Double?) {
        val photoPath = photo?.let { uploadDeliveryPhoto(assignmentId, "issue", it) }
        client.postgrest.rpc(
            "courier_report_issue",
            buildJsonObject {
                put("assignment_id", assignmentId)
                put("kind", kind.wire)
                put("description", description.trim())
                put("photo_path", photoPath)
                put("lat", lat)
                put("lng", lng)
            },
        )
    }

    override suspend fun recordLocation(assignmentId: String, lat: Double, lng: Double, accuracyM: Float?) {
        client.from("courier_locations").insert(
            buildJsonObject {
                put("courier_id", uid())
                put("assignment_id", assignmentId)
                put("lat", lat)
                put("lng", lng)
                put("accuracy_m", accuracyM)
            },
        )
    }

    private suspend fun uploadDeliveryPhoto(assignmentId: String, prefix: String, bytes: ByteArray): String {
        val path = "${uid()}/$assignmentId/$prefix-${System.currentTimeMillis()}.jpg"
        client.storage.from(DELIVERY_PHOTOS_BUCKET).upload(path, bytes) { upsert = false }
        return path
    }

    // ---------- Notifications ----------

    override suspend fun notifications(): List<AppNotification> =
        client.from("notifications").select {
            filter { eq("user_id", uid()) }
            order("created_at", Order.DESCENDING)
            limit(100)
        }.decodeList<NotificationRow>().map { it.toModel() }

    override fun newNotifications(): Flow<AppNotification> = realtimeFlow("notifications") { channel, uid ->
        channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "notifications"; filter("user_id", FilterOperator.EQ, uid)
        }.map { it.decodeRecord<NotificationRow>().toModel() }
    }

    override suspend fun markNotificationRead(id: String) {
        client.postgrest.rpc("mark_notification_read", buildJsonObject { put("notification_id", id) })
    }

    override suspend fun registerDeviceToken(token: String) {
        invoke("register-device-token", buildJsonObject { put("token", token); put("platform", "android") })
    }

    // ---------- Calling ----------

    override suspend fun createCallSession(assignmentId: String): CallSessionInfo {
        val body = invoke("create-call-session", buildJsonObject { put("assignment_id", assignmentId) }).bodyAsText()
        val r = json.decodeFromString<CallSessionResponse>(body)
        return CallSessionInfo(r.callSessionId, r.serverUrl, r.token, r.customerFirstName, r.orderNumber)
    }

    override suspend fun endCallSession(sessionId: String, reason: String) {
        invoke("end-call-session", buildJsonObject { put("call_session_id", sessionId); put("reason", reason) })
    }

    // ---------- Helpers ----------

    private suspend fun invoke(function: String, body: JsonObject): HttpResponse {
        val response = client.functions.invoke(function = function, body = body)
        if (!response.status.isSuccess()) {
            val text = runCatching { response.bodyAsText() }.getOrDefault("")
            val parsed = runCatching { json.decodeFromString<FunctionError>(text) }.getOrNull()
            throw UserFacingException(parsed?.error ?: parsed?.message ?: "The server refused the request (${response.status.value}).")
        }
        return response
    }

    /** One realtime channel per subscription, always removed when the collector stops. */
    private fun <T> realtimeFlow(name: String, build: (RealtimeChannel, String) -> Flow<T>): Flow<T> = flow {
        val uid = uid()
        val channel = client.channel("$name-$uid-${System.nanoTime()}")
        val changes = build(channel, uid)
        channel.subscribe()
        try {
            emitAll(changes)
        } finally {
            withContext(NonCancellable) { client.realtime.removeChannel(channel) }
        }
    }
}
