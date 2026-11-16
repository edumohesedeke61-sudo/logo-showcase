import { createFileRoute } from "@tanstack/react-router";
import { AlertTriangle, ArrowDown, Check, Lock } from "lucide-react";
import { PageIntro, Panel, Section } from "@/components/spec/Section";
import { callFlow, callSafeguards, notificationMatrix, registrationFlow } from "@/data/spec";
import { nextCourierStatuses, requiresConfirmation, STATUS_LABELS, type DeliveryStatus } from "@/lib/delivery-status";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/workflows")({
  head: () => ({
    meta: [
      { title: "Workflows — LightGrab Courier" },
      { name: "description", content: "Courier registration and approval, delivery status steps, in-app customer calling and notifications for LightGrab Courier." },
      { property: "og:title", content: "Workflows — LightGrab Courier" },
      { property: "og:description", content: "Registration, delivery workflow, calling and notifications for LightGrab Courier." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: WorkflowsPage,
});

const HAPPY_PATH: DeliveryStatus[] = ["assigned", "accepted", "en_route", "arrived", "handover", "completed"];

function WorkflowsPage() {
  return (
    <>
      <PageIntro eyebrow="Workflows" title="How couriers join, deliver and call">
        The rules here are shared by the Android app, the backend and the Office and Admin products.
      </PageIntro>

      <Section id="registration" title="Registration & approval" kicker="01">
        <ol className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {registrationFlow.map((s, i) => (
            <li key={s.step}>
              <Panel className={cn("h-full", i === 6 && "border-navy bg-navy text-navy-foreground")}>
                <p className={cn("font-mono text-xs", i === 6 ? "text-leaf" : "text-primary")}>Step {i + 1}</p>
                <p className="mt-1 font-bold">{s.step}</p>
                <p className={cn("mt-1 text-sm", i === 6 ? "text-navy-foreground/75" : "text-muted-foreground")}>{s.detail}</p>
              </Panel>
            </li>
          ))}
        </ol>
        <p className="mt-4 flex items-center gap-2 text-sm font-semibold">
          <Lock className="size-4 text-primary" /> Applicants never reach delivery screens or data until Admin approves them.
        </p>
      </Section>

      <Section id="delivery" title="Delivery status workflow" kicker="02">
        <div className="grid gap-8 lg:grid-cols-[1fr_1fr]">
          <div className="space-y-1">
            {HAPPY_PATH.map((s, i) => (
              <div key={s}>
                <div className="flex items-center gap-4 rounded-xl bg-card p-4 shadow-card">
                  <span className={cn("grid size-10 shrink-0 place-items-center rounded-full font-display font-extrabold", s === "completed" ? "bg-leaf text-leaf-foreground" : "bg-navy text-navy-foreground")}>
                    {s === "completed" ? <Check className="size-5" /> : i + 1}
                  </span>
                  <div className="flex-1">
                    <p className="font-bold">{STATUS_LABELS[s]}</p>
                    <p className="font-mono text-xs text-muted-foreground">{s}</p>
                  </div>
                  {requiresConfirmation(s) && (
                    <span className="rounded-full bg-warning px-2.5 py-1 text-[11px] font-bold text-warning-foreground">Confirm step</span>
                  )}
                </div>
                {i < HAPPY_PATH.length - 1 && <ArrowDown className="mx-auto my-1 size-4 text-muted-foreground" />}
              </div>
            ))}
          </div>
          <div className="space-y-4">
            <Panel className="border-destructive/40">
              <p className="flex items-center gap-2 font-bold text-destructive">
                <AlertTriangle className="size-4" /> {STATUS_LABELS.issue_reported}
              </p>
              <p className="mt-2 text-sm text-muted-foreground">
                Can be reported from: {(["accepted", "en_route", "arrived", "handover"] as DeliveryStatus[]).filter((s) => nextCourierStatuses(s).includes("issue_reported")).map((s) => STATUS_LABELS[s]).join(", ")}.
                Only Office can resolve it and move the delivery on.
              </p>
            </Panel>
            <Panel>
              <p className="font-bold">Rules enforced on the server</p>
              <ul className="mt-2 space-y-1.5 text-sm text-muted-foreground">
                <li>• Couriers can't skip steps — e.g. arrived → completed is rejected.</li>
                <li>• Completing requires the customer's delivery code.</li>
                <li>• Each change stores who, when and where in the audit trail.</li>
                <li>• Couriers can never mark an order paid or edit fees.</li>
                <li>• Changes made offline are queued and sent when signal returns.</li>
              </ul>
            </Panel>
          </div>
        </div>
      </Section>

      <Section id="calling" title="In-app customer calling" kicker="03">
        <div className="grid gap-8 lg:grid-cols-[1.3fr_0.7fr]">
          <ol className="relative space-y-3 border-l-2 border-leaf pl-6">
            {callFlow.map((c, i) => (
              <li key={i} className="relative">
                <span className="absolute -left-[33px] top-1 size-4 rounded-full border-2 border-leaf bg-background" />
                <p className="text-xs font-bold uppercase tracking-wider text-primary">{c.actor}</p>
                <p>{c.action}</p>
              </li>
            ))}
          </ol>
          <Panel className="h-fit bg-navy text-navy-foreground">
            <p className="font-bold text-leaf">Never the wrong customer</p>
            <ul className="mt-3 space-y-2 text-sm text-navy-foreground/85">
              {callSafeguards.map((s) => <li key={s}>• {s}</li>)}
            </ul>
          </Panel>
        </div>
      </Section>

      <Section id="notifications" title="Notifications" kicker="04">
        <div className="overflow-x-auto rounded-xl border border-border bg-card">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="bg-navy text-navy-foreground">
              <tr>
                {["Event", "Android channel", "Priority", "Opens"].map((h) => <th key={h} className="px-4 py-3 font-semibold">{h}</th>)}
              </tr>
            </thead>
            <tbody>
              {notificationMatrix.map((n) => (
                <tr key={n.event} className="border-t border-border">
                  <td className="px-4 py-3 font-semibold">{n.event}</td>
                  <td className="px-4 py-3 font-mono text-xs">{n.channel}</td>
                  <td className="px-4 py-3">{n.priority}</td>
                  <td className="px-4 py-3 font-mono text-xs">{n.deepLink}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Section>
    </>
  );
}
