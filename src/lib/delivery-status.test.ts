import { describe, expect, it } from "vitest";
import {
  canCourierTransition,
  courierMayWriteField,
  isTerminalForCourier,
  requiresConfirmation,
} from "./delivery-status";

describe("courier delivery workflow", () => {
  it("follows the order: received, accepted, travelling, arrived, confirmation, completed", () => {
    expect(canCourierTransition("assigned", "accepted")).toBe(true);
    expect(canCourierTransition("accepted", "en_route")).toBe(true);
    expect(canCourierTransition("en_route", "arrived")).toBe(true);
    expect(canCourierTransition("arrived", "handover")).toBe(true);
    expect(canCourierTransition("handover", "completed")).toBe(true);
  });

  it("does not let a courier skip delivery confirmation", () => {
    expect(canCourierTransition("arrived", "completed")).toBe(false);
    expect(canCourierTransition("en_route", "completed")).toBe(false);
  });

  it("lets a courier report an issue once the order is accepted", () => {
    expect(canCourierTransition("en_route", "issue_reported")).toBe(true);
    expect(canCourierTransition("assigned", "issue_reported")).toBe(false);
  });

  it("only Office can move an order on after an issue or completion", () => {
    expect(isTerminalForCourier("issue_reported")).toBe(true);
    expect(isTerminalForCourier("completed")).toBe(true);
  });

  it("asks for confirmation before accepting, arriving, completing or reporting an issue", () => {
    expect(requiresConfirmation("accepted")).toBe(true);
    expect(requiresConfirmation("arrived")).toBe(true);
    expect(requiresConfirmation("completed")).toBe(true);
    expect(requiresConfirmation("issue_reported")).toBe(true);
    expect(requiresConfirmation("en_route")).toBe(false);
  });

  it("never lets a courier mark an order as paid", () => {
    expect(courierMayWriteField("payment_status")).toBe(false);
    expect(courierMayWriteField("amount_paid")).toBe(false);
    expect(courierMayWriteField("note")).toBe(true);
  });
});
