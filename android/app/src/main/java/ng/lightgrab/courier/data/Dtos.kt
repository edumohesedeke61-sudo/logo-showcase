package ng.lightgrab.courier.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import ng.lightgrab.courier.core.model.AppNotification
import ng.lightgrab.courier.core.model.AssignedOrder
import ng.lightgrab.courier.core.model.DeliveryStatus

@Serializable
data class CourierRow(
    val id: String,
    @SerialName("account_status") val accountStatus: String,
    val availability: String? = null,
    @SerialName("operating_area") val operatingArea: String? = null,
    @SerialName("vehicle_type") val vehicleType: String? = null,
    @SerialName("suspended_reason") val suspendedReason: String? = null,
)

@Serializable
data class ProfileRow(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
)

@Serializable
data class ApplicationRow(
    val id: String,
    val status: String,
    @SerialName("decision_note") val decisionNote: String? = null,
)

/** Row of the `courier_assigned_orders` / `courier_delivery_history` views. */
@Serializable
data class AssignedOrderRow(
    @SerialName("assignment_id") val assignmentId: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("order_number") val orderNumber: String,
    val status: String,
    @SerialName("customer_first_name") val customerFirstName: String? = null,
    @SerialName("delivery_address") val deliveryAddress: String? = null,
    val landmark: String? = null,
    @SerialName("dest_lat") val destLat: Double? = null,
    @SerialName("dest_lng") val destLng: Double? = null,
    @SerialName("package_summary") val packageSummary: String? = null,
    val instructions: String? = null,
    @SerialName("assigned_at") val assignedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
) {
    fun toModel(): AssignedOrder? {
        val s = DeliveryStatus.fromWire(status) ?: return null
        return AssignedOrder(assignmentId, orderId, orderNumber, s, customerFirstName, deliveryAddress, landmark, destLat, destLng, packageSummary, instructions, assignedAt, completedAt)
    }
}

@Serializable
data class NotificationRow(
    val id: String,
    val kind: String,
    val title: String,
    val body: String = "",
    val payload: JsonObject? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("read_at") val readAt: String? = null,
) {
    fun toModel() = AppNotification(
        id = id,
        kind = kind,
        title = title,
        body = body,
        assignmentId = payload?.get("assignment_id")?.jsonPrimitive?.content,
        createdAt = createdAt,
        read = readAt != null,
    )
}

@Serializable
data class CallSessionResponse(
    @SerialName("call_session_id") val callSessionId: String,
    @SerialName("server_url") val serverUrl: String,
    val token: String,
    @SerialName("customer_first_name") val customerFirstName: String,
    @SerialName("order_number") val orderNumber: String,
)

@Serializable
data class FunctionError(val error: String? = null, val message: String? = null)
