import { createFileRoute } from "@tanstack/react-router";
import { ShieldCheck } from "lucide-react";
import { Code, PageIntro, Panel, Section } from "@/components/spec/Section";
import { endpoints, realtimeEvents, rlsRules, tables } from "@/data/spec";
import { DELIVERY_STATUSES } from "@/lib/delivery-status";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/backend")({
  head: () => ({
    meta: [
      { title: "Shared backend — LightGrab Courier" },
      { name: "description", content: "Tables, permissions, endpoints and realtime events LightGrab Courier shares with LightGrab Office and Admin." },
      { property: "og:title", content: "Shared backend — LightGrab Courier" },
      { property: "og:description", content: "Backend contracts shared by LightGrab Courier, Office and Admin." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: BackendPage,
});

const KIND_TONE = {
  RPC: "bg-forest text-forest-foreground",
  "Edge function": "bg-navy text-navy-foreground",
  View: "bg-leaf text-leaf-foreground",
} as const;

function BackendPage() {
  return (
    <>
      <PageIntro eyebrow="Shared backend" title="One database for Courier, Office and Admin">
        Courier adds a small set of tables and server endpoints to the existing LightGrab backend. No second database. Office assigns, Admin approves, Courier delivers — all on the same records.
      </PageIntro>

      <Section title="Tables & relationships" kicker="Data">
        <div className="mb-4 flex flex-wrap items-center gap-2 text-sm">
          <span className="font-semibold">delivery_status enum:</span>
          {DELIVERY_STATUSES.map((s) => <Code key={s}>{s}</Code>)}
        </div>
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {tables.map((t) => (
            <Panel key={t.name}>
              <div className="flex items-center justify-between gap-2">
                <p className="font-mono font-bold">{t.name}</p>
                <span className={cn("rounded-full px-2 py-0.5 text-[10px] font-bold uppercase", t.owner === "courier" ? "bg-leaf text-leaf-foreground" : "bg-muted text-muted-foreground")}>
                  {t.owner === "courier" ? "New for courier" : "Shared"}
                </span>
              </div>
              <p className="mt-1 text-sm text-muted-foreground">{t.purpose}</p>
              <ul className="mt-3 space-y-0.5 font-mono text-[11px] text-foreground/80">
                {t.columns.map((c) => <li key={c}>{c}</li>)}
              </ul>
              <p className="mt-3 border-t border-border pt-2 text-xs"><span className="font-bold">Courier access:</span> {t.courierAccess}</p>
            </Panel>
          ))}
        </div>
      </Section>

      <Section title="Endpoints" kicker="Server">
        <div className="overflow-x-auto rounded-xl border border-border bg-card">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="bg-navy text-navy-foreground">
              <tr>{["Type", "Name", "Input", "Rule"].map((h) => <th key={h} className="px-4 py-3 font-semibold">{h}</th>)}</tr>
            </thead>
            <tbody>
              {endpoints.map((e) => (
                <tr key={e.name} className="border-t border-border align-top">
                  <td className="px-4 py-3"><span className={cn("whitespace-nowrap rounded-full px-2 py-0.5 text-[11px] font-bold", KIND_TONE[e.kind])}>{e.kind}</span></td>
                  <td className="px-4 py-3 font-mono text-xs font-bold">{e.name}</td>
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{e.input}</td>
                  <td className="px-4 py-3">{e.rule}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Section>

      <Section title="Realtime events" kicker="Live">
        <div className="grid gap-3 md:grid-cols-2">
          {realtimeEvents.map((r) => (
            <Panel key={r.channel}>
              <p className="font-mono text-xs font-bold">{r.channel}</p>
              <p className="mt-2 text-sm"><span className="text-muted-foreground">From</span> {r.source} <span className="text-muted-foreground">→</span> {r.consumer}</p>
              <p className="mt-1 text-sm text-muted-foreground">{r.effect}</p>
            </Panel>
          ))}
        </div>
      </Section>

      <Section title="Security & permissions" kicker="Access">
        <div className="rounded-xl bg-navy p-6 text-navy-foreground">
          <ul className="grid gap-3 md:grid-cols-2">
            {rlsRules.map((r) => (
              <li key={r} className="flex gap-3 text-sm">
                <ShieldCheck className="mt-0.5 size-5 shrink-0 text-leaf" />
                {r}
              </li>
            ))}
          </ul>
        </div>
      </Section>
    </>
  );
}
