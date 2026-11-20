package ng.lightgrab.courier

import ng.lightgrab.courier.core.model.CourierWriteRules
import ng.lightgrab.courier.core.model.DeliveryStatus
import ng.lightgrab.courier.core.model.DeliveryStatus.*
import ng.lightgrab.courier.core.util.NigerianPhone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryRulesTest {
    @Test fun courierCannotSkipAcceptance() = assertFalse(ASSIGNED.canMoveTo(EN_ROUTE))
    @Test fun courierCannotSkipDeliveryConfirmation() = assertFalse(ARRIVED.canMoveTo(COMPLETED))
    @Test fun handoverCanComplete() = assertTrue(HANDOVER.canMoveTo(COMPLETED))
    @Test fun onlyOfficeMovesOnFromIssue() = assertTrue(ISSUE_REPORTED.courierNext.isEmpty())
    @Test fun completedIsFinal() = assertTrue(COMPLETED.isTerminalForCourier)
    @Test fun issueCanBeReportedOnceAccepted() = assertTrue(listOf(ACCEPTED, EN_ROUTE, ARRIVED, HANDOVER).all { it.canMoveTo(ISSUE_REPORTED) })
    @Test fun importantChangesNeedConfirmation() =
        assertEquals(setOf(ACCEPTED, ARRIVED, COMPLETED, ISSUE_REPORTED), DeliveryStatus.entries.filter { DeliveryStatus.requiresConfirmation(it) }.toSet())
    @Test fun courierCannotWritePaymentStatus() = assertFalse(CourierWriteRules.mayWrite("payment_status"))
    @Test fun wireNamesMatchBackend() =
        assertEquals(listOf("assigned", "accepted", "en_route", "arrived", "handover", "completed", "issue_reported"), DeliveryStatus.entries.map { it.wire })
    @Test fun nigerianPhoneNormalised() = assertEquals("+2348031234567", NigerianPhone.toE164("0803 123 4567"))
    @Test fun invalidPhoneRejected() = assertNull(NigerianPhone.toE164("12345"))
}
