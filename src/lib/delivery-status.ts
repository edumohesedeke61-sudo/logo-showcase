/**
 * Delivery workflow state machine for LightGrab Courier.
 * This file is the single source of truth mirrored by the backend RPC
 * `courier_update_delivery_status` and the Kotlin `DeliveryStatus` enum.
 */

export const DELIVERY_STATUSES = [
  "assigned",
  "accepted",
  "en_route",
  "arrived",
  "handover",
  "completed",
  "issue_reported",
] as const;

export type DeliveryStatus = (typeof DELIVERY_STATUSES)[number];

export const STATUS_LABELS: Record<DeliveryStatus, string> = {
  assigned: "Assignment received",
  accepted: "Assignment accepted",
  en_route: "Travelling to destination",
  arrived: "Arrived at destination",
  handover: "Delivery confirmation",
  completed: "Delivery completed",
  issue_reported: "Delivery issue reported",
};

/** Transitions a courier may perform from their own device. */
const COURIER_TRANSITIONS: Record<DeliveryStatus, readonly DeliveryStatus[]> = {
  assigned: ["accepted"],
  accepted: ["en_route", "issue_reported"],
  en_route: ["arrived", "issue_reported"],
  arrived: ["handover", "issue_reported"],
  handover: ["completed", "issue_reported"],
  completed: [],
  // Only Office staff can resolve an issue and move the delivery on.
  issue_reported: [],
};

/** Status changes that need an explicit confirm step (dialog or swipe) in the app. */
const CONFIRM_REQUIRED: readonly DeliveryStatus[] = [
  "accepted",
  "arrived",
  "completed",
  "issue_reported",
];

/** Fields a courier is never allowed to write, whatever the status. */
export const COURIER_FORBIDDEN_FIELDS = [
  "payment_status",
  "amount_paid",
  "delivery_fee",
  "courier_id",
  "assigned_by",
] as const;

export function nextCourierStatuses(from: DeliveryStatus): readonly DeliveryStatus[] {
  return COURIER_TRANSITIONS[from];
}

export function canCourierTransition(from: DeliveryStatus, to: DeliveryStatus): boolean {
  return COURIER_TRANSITIONS[from].includes(to);
}

export function requiresConfirmation(to: DeliveryStatus): boolean {
  return CONFIRM_REQUIRED.includes(to);
}

export function isTerminalForCourier(status: DeliveryStatus): boolean {
  return COURIER_TRANSITIONS[status].length === 0;
}

export function courierMayWriteField(field: string): boolean {
  return !(COURIER_FORBIDDEN_FIELDS as readonly string[]).includes(field);
}
