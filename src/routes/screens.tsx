import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { PageIntro, Section } from "@/components/spec/Section";
import { SCREENS, type ScreenSpec } from "@/components/screens/CourierScreens";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/screens")({
  head: () => ({
    meta: [
      { title: "Screens — LightGrab Courier" },
      { name: "description", content: "Screen-by-screen design of the LightGrab Courier Android app: sign in, application, dashboard, orders, delivery, calling and alerts." },
      { property: "og:title", content: "Screens — LightGrab Courier" },
      { property: "og:description", content: "Screen-by-screen design of the native LightGrab Courier Android app." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: ScreensPage,
});

const GROUPS = ["All", "Access", "Deliveries", "Calling", "Account"] as const;

const NAV_MAP = [
  { from: "Launch", to: "Session check → Sign in / Under review / Suspended / Dashboard" },
  { from: "Dashboard", to: "Order detail · Alerts · Account" },
  { from: "Order detail", to: "Navigate (map app) · Call · Status confirm · Handover · Report issue" },
  { from: "Push notification", to: "Deep link straight to the related order" },
];

function ScreensPage() {
  const [group, setGroup] = useState<(typeof GROUPS)[number]>("All");
  const shown = group === "All" ? SCREENS : SCREENS.filter((s) => s.group === group);

  return (
    <>
      <PageIntro eyebrow="Screen structure" title="Every screen a courier touches">
        Native Jetpack Compose screens, shown here in a device frame for review. Order numbers and names are placeholders — real values come from the backend.
      </PageIntro>

      <div className="mx-auto max-w-7xl px-4 sm:px-6">
        <div className="flex flex-wrap gap-2">
          {GROUPS.map((g) => (
            <button
              key={g}
              onClick={() => setGroup(g)}
              className={cn(
                "h-10 rounded-full px-4 text-sm font-semibold transition-colors",
                g === group ? "bg-navy text-navy-foreground" : "bg-card text-muted-foreground hover:text-foreground",
              )}
            >
              {g}
            </button>
          ))}
        </div>
      </div>

      <div className="mx-auto grid max-w-7xl gap-x-8 gap-y-14 px-4 py-10 sm:px-6 md:grid-cols-2 xl:grid-cols-3">
        {shown.map((s) => (
          <ScreenCard key={s.id} screen={s} index={SCREENS.indexOf(s) + 1} />
        ))}
      </div>

      <Section title="Navigation map" kicker="How screens connect">
        <div className="overflow-hidden rounded-xl border border-border bg-card">
          {NAV_MAP.map((n) => (
            <div key={n.from} className="grid gap-1 border-b border-border px-5 py-4 last:border-0 sm:grid-cols-[200px_1fr]">
              <p className="font-bold">{n.from}</p>
              <p className="text-muted-foreground">{n.to}</p>
            </div>
          ))}
        </div>
      </Section>
    </>
  );
}

function ScreenCard({ screen, index }: { screen: ScreenSpec; index: number }) {
  return (
    <article className="flex flex-col items-center">
      {screen.render()}
      <div className="mt-6 w-full max-w-[300px]">
        <p className="font-mono text-xs text-primary">
          {String(index).padStart(2, "0")} · {screen.module}
        </p>
        <h3 className="mt-1 text-xl font-extrabold">{screen.title}</h3>
        <ul className="mt-2 space-y-1 text-sm text-muted-foreground">
          {screen.notes.map((n) => (
            <li key={n}>• {n}</li>
          ))}
        </ul>
      </div>
    </article>
  );
}
