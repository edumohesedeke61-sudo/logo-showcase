package ng.lightgrab.courier.core.model

/**
 * Delivery workflow. Mirrors the shared rules used by the backend RPC
 * `courier_update_delivery_status` and the LightGrab specification.
 */
enum class DeliveryStatus(val wire: String, val label: String) {
    ASSIGNED("assigned", "Assignment received"),
    ACCEPTED("accepted", "Assignment accepted"),
    EN_ROUTE("en_route", "Travelling to destination"),
    ARRIVED("arrived", "Arrived at destination"),
    HANDOVER("handover", "Delivery confirmation"),
    COMPLETED("completed", "Delivery completed"),
    ISSUE_REPORTED("issue_reported", "Delivery issue reported");

    /** Statuses a courier may move to from this one, from their own device. */
    val courierNext: List<DeliveryStatus>
        get() = when (this) {
            ASSIGNED -> listOf(ACCEPTED)
            ACCEPTED -> listOf(EN_ROUTE, ISSUE_REPORTED)
            EN_ROUTE -> listOf(ARRIVED, ISSUE_REPORTED)
            ARRIVED -> listOf(HANDOVER, ISSUE_REPORTED)
            HANDOVER -> listOf(COMPLETED, ISSUE_REPORTED)
            COMPLETED -> emptyList()
            // Only Office staff can resolve an issue and move the delivery on.
            ISSUE_REPORTED -> emptyList()
        }

    fun canMoveTo(to: DeliveryStatus): Boolean = to in courierNext

    val isTerminalForCourier: Boolean get() = courierNext.isEmpty()

    /** In progress: tracking may run and the courier is busy. */
    val isActive: Boolean get() = this == ACCEPTED || this == EN_ROUTE || this == ARRIVED || this == HANDOVER

    /** Step number shown in the stepper (issue is shown separately). */
    val step: Int get() = ordinal.coerceAtMost(COMPLETED.ordinal)

    companion object {
        /** Moving INTO these statuses needs an explicit confirmation (swipe or dialog). */
        val CONFIRM_REQUIRED = setOf(ACCEPTED, ARRIVED, COMPLETED, ISSUE_REPORTED)

        fun requiresConfirmation(to: DeliveryStatus) = to in CONFIRM_REQUIRED

        fun fromWire(value: String?): DeliveryStatus? = entries.firstOrNull { it.wire == value }

        /** The happy-path steps, in order. */
        val STEPS = listOf(ASSIGNED, ACCEPTED, EN_ROUTE, ARRIVED, HANDOVER, COMPLETED)
    }
}

/** Fields a courier is never allowed to write, whatever the status. */
object CourierWriteRules {
    val FORBIDDEN_FIELDS = setOf("payment_status", "amount_paid", "delivery_fee", "courier_id", "assigned_by")
    fun mayWrite(field: String) = field !in FORBIDDEN_FIELDS
}
