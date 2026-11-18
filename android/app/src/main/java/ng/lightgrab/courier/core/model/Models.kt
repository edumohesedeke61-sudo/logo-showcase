package ng.lightgrab.courier.core.model

enum class Availability(val wire: String, val label: String) {
    OFFLINE("offline", "Offline"),
    ONLINE("online", "Online"),
    ON_DELIVERY("on_delivery", "On a delivery");

    companion object {
        fun fromWire(v: String?) = entries.firstOrNull { it.wire == v } ?: OFFLINE
    }
}

enum class ApplicationStatus(val wire: String) {
    PENDING("pending"), NEEDS_INFO("needs_info"), APPROVED("approved"), REJECTED("rejected");

    companion object {
        fun fromWire(v: String?) = entries.firstOrNull { it.wire == v } ?: PENDING
    }
}

data class CourierProfile(
    val id: String,
    val fullName: String,
    val phone: String?,
    val operatingArea: String?,
    val vehicleType: String?,
    val availability: Availability,
)

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val userId: String) : SessionState
}

sealed interface AccessState {
    data object Loading : AccessState
    data object SignedOut : AccessState
    /** Signed in but no application yet. */
    data object NeedsApplication : AccessState
    data class InReview(val status: ApplicationStatus, val note: String?) : AccessState
    data class Suspended(val reason: String?) : AccessState
    data class Active(val courier: CourierProfile) : AccessState
    data class Error(val message: String) : AccessState
}

data class AssignedOrder(
    val assignmentId: String,
    val orderId: String,
    val orderNumber: String,
    val status: DeliveryStatus,
    val customerFirstName: String?,
    val deliveryAddress: String?,
    val landmark: String?,
    val destLat: Double?,
    val destLng: Double?,
    val packageSummary: String?,
    val instructions: String?,
    val assignedAt: String?,
    val completedAt: String?,
) {
    val hasPin: Boolean get() = destLat != null && destLng != null
}

data class AppNotification(
    val id: String,
    val kind: String,
    val title: String,
    val body: String,
    val assignmentId: String?,
    val createdAt: String?,
    val read: Boolean,
)

enum class VehicleType(val wire: String, val label: String, val needsPapers: Boolean) {
    BICYCLE("bicycle", "Bicycle", false),
    MOTORCYCLE("motorcycle", "Motorcycle", true),
    TRICYCLE("tricycle", "Tricycle (Keke)", true),
    CAR("car", "Car", true),
    ON_FOOT("on_foot", "On foot", false),
}

enum class IdType(val wire: String, val label: String) {
    NIN("nin", "National ID (NIN)"),
    VOTERS_CARD("voters_card", "Voter's card"),
    DRIVERS_LICENCE("drivers_licence", "Driver's licence"),
    PASSPORT("passport", "International passport"),
}

enum class DocKind(val wire: String, val label: String) {
    ID_FRONT("id_front", "ID card — front"),
    ID_BACK("id_back", "ID card — back"),
    SELFIE("selfie", "Selfie holding your ID"),
    VEHICLE_PAPERS("vehicle_papers", "Vehicle papers"),
}

data class ApplicationDraft(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val residentialAddress: String = "",
    val operatingArea: String = "",
    val idType: IdType = IdType.NIN,
    val idNumber: String = "",
    val vehicleType: VehicleType = VehicleType.MOTORCYCLE,
    val vehiclePlate: String = "",
) {
    fun requiredDocs(): List<DocKind> = buildList {
        add(DocKind.ID_FRONT); add(DocKind.ID_BACK); add(DocKind.SELFIE)
        if (vehicleType.needsPapers) add(DocKind.VEHICLE_PAPERS)
    }
}

enum class IssueKind(val wire: String, val label: String) {
    CUSTOMER_UNREACHABLE("customer_unreachable", "Customer not reachable"),
    WRONG_ADDRESS("wrong_address", "Wrong or unclear address"),
    REFUSED("refused", "Customer refused package"),
    DAMAGED("damaged", "Package damaged"),
    SAFETY("vehicle_safety", "Vehicle / safety problem"),
    OTHER("other", "Other"),
}

data class CallSessionInfo(
    val sessionId: String,
    val serverUrl: String,
    val token: String,
    val customerFirstName: String,
    val orderNumber: String,
)

/** Operating areas offered in the application form (initial market: Oron). */
val OPERATING_AREAS = listOf("Oron town", "Eyo Abasi", "Uya Oro", "Esin Ufot", "Eyotong", "Other Oron area")
