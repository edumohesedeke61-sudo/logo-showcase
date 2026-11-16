import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowRight, Ban, Check, Cpu, Database, GitBranch, Smartphone } from "lucide-react";
import courier from "@/assets/courier-oron.jpg";
import { Button } from "@/components/ui/button";
import { Panel, Section } from "@/components/spec/Section";
import { SCREENS } from "@/components/screens/CourierScreens";
import { platformRules, SPEC_VERSION } from "@/data/spec";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "LightGrab Courier — Native Android app specification" },
      { name: "description", content: "Architecture, screens, workflows and backend contracts for the LightGrab Courier Android app in Oron, Akwa Ibom." },
      { property: "og:title", content: "LightGrab Courier — Native Android app specification" },
      { property: "og:description", content: "Architecture, screens, workflows and backend contracts for the LightGrab Courier Android app." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: Overview,
});

const PILLARS = [
  { icon: Smartphone, title: "Screen structure", body: "Every courier screen, designed for outdoor readability and big touch targets.", to: "/screens" },
  { icon: GitBranch, title: "Workflows", body: "Registration and approval, delivery status steps, in-app calling, notifications.", to: "/workflows" },
  { icon: Cpu, title: "Android build", body: "Kotlin modules, libraries, permissions, build flavours and APK generation.", to: "/architecture" },
  { icon: Database, title: "Shared backend", body: "Tables, permissions, endpoints and realtime events shared with Office and Admin.", to: "/backend" },
] as const;

function Overview() {
  const featured = SCREENS.filter((s) => ["dashboard", "order", "call"].includes(s.id));
  return (
    <>
      <section className="relative overflow-hidden bg-brand text-navy-foreground">
        <img src={courier} alt="LightGrab courier on a motorcycle in Oron" width={1536} height={1024} className="absolute inset-0 h-full w-full object-cover object-[70%_30%]" />
        <div className="absolute inset-0 bg-gradient-to-r from-navy via-navy/85 to-navy/20" />
        <div className="relative mx-auto grid max-w-7xl gap-10 px-4 py-20 sm:px-6 lg:grid-cols-[1.1fr_0.9fr] lg:py-28">
          <div className="animate-speed-in">
            <span className="inline-flex items-center gap-2 rounded-full bg-leaf px-3 py-1 text-xs font-bold text-leaf-foreground">
              {SPEC_VERSION}
            </span>
            <h1 className="mt-6 text-5xl font-black leading-[0.95] font-wide sm:text-7xl">
              LightGrab
              <br />
              <span className="text-leaf">Courier</span>
            </h1>
            <p className="mt-6 max-w-xl text-lg text-navy-foreground/85">
              The native Android app approved couriers use to receive assigned orders, find customers and complete deliveries — starting in Oron, Akwa Ibom State.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Button asChild variant="leaf" size="xl">
                <Link to="/screens">
                  See the screens <ArrowRight />
                </Link>
              </Button>
              <Button asChild variant="cream" size="xl">
                <Link to="/architecture">Android build plan</Link>
              </Button>
            </div>
          </div>
          <div className="grid gap-3 self-end sm:grid-cols-2">
            <div className="rounded-xl bg-navy-foreground/10 p-5 backdrop-blur">
              <p className="flex items-center gap-2 text-sm font-bold text-leaf"><Check className="size-4" /> The app is</p>
              <ul className="mt-3 space-y-2 text-sm">
                {platformRules.required.map((r) => <li key={r}>{r}</li>)}
              </ul>
            </div>
            <div className="rounded-xl bg-navy-foreground/10 p-5 backdrop-blur">
              <p className="flex items-center gap-2 text-sm font-bold text-warning"><Ban className="size-4" /> The app is never</p>
              <ul className="mt-3 space-y-2 text-sm">
                {platformRules.prohibited.map((r) => <li key={r}>{r}</li>)}
              </ul>
            </div>
          </div>
        </div>
      </section>

      <Section title="What this specification covers" kicker="Start here">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {PILLARS.map((p) => (
            <Link key={p.to} to={p.to} className="group">
              <Panel className="h-full transition-transform group-hover:-translate-y-1">
                <span className="grid size-11 place-items-center rounded-lg bg-navy text-leaf">
                  <p.icon className="size-5" />
                </span>
                <h3 className="mt-4 text-lg font-extrabold">{p.title}</h3>
                <p className="mt-1 text-sm text-muted-foreground">{p.body}</p>
                <p className="mt-4 flex items-center gap-1 text-sm font-bold text-primary">
                  Open <ArrowRight className="size-4 transition-transform group-hover:translate-x-1" />
                </p>
              </Panel>
            </Link>
          ))}
        </div>
      </Section>

      <Section title="Built for the road" kicker="Design direction">
        <div className="grid items-start gap-10 lg:grid-cols-[0.8fr_1.2fr]">
          <div className="space-y-4 text-muted-foreground">
            <p className="text-lg text-foreground">High-contrast navy on cream, forest green for the next action, leaf green for confirmed states.</p>
            <ul className="space-y-2 text-sm">
              <li>• Primary actions are 56dp tall and full width — easy to hit with gloves.</li>
              <li>• Important status changes use swipe or confirm sheets, never a single tap.</li>
              <li>• Text sizes start at 14sp; key info (order number, status) at 18sp+.</li>
              <li>• Dark screens only for focused moments: calls, approvals, suspension.</li>
            </ul>
            <div className="flex gap-2 pt-2">
              {["bg-navy", "bg-forest", "bg-leaf", "bg-cream border border-border", "bg-paper border border-border"].map((c) => (
                <span key={c} className={`h-10 w-10 rounded-lg ${c}`} />
              ))}
            </div>
          </div>
          <div className="flex gap-6 overflow-x-auto pb-6">
            {featured.map((s) => (
              <div key={s.id}>{s.render()}</div>
            ))}
          </div>
        </div>
      </Section>
    </>
  );
}
