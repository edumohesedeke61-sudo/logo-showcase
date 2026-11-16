import type { ReactNode } from "react";
import {
  AlertTriangle,
  Bell,
  Camera,
  Check,
  ChevronRight,
  Clock,
  FileCheck2,
  KeyRound,
  Lock,
  MapPin,
  Megaphone,
  Mic,
  MicOff,
  Navigation,
  Package,
  Phone,
  PhoneOff,
  ShieldAlert,
  Upload,
  Volume2,
} from "lucide-react";
import logo from "@/assets/lightgrab-logo.webp.asset.json";
import { cn } from "@/lib/utils";
import { AppBar, BottomNav, MiniMap, PhoneFrame } from "./PhoneFrame";

/* Small building blocks that mirror :core:designsystem components. */

function BigAction({ children, tone = "forest", icon }: { children: ReactNode; tone?: "forest" | "leaf" | "navy" | "outline" | "danger"; icon?: ReactNode }) {
  const tones = {
    forest: "bg-forest text-forest-foreground",
    leaf: "bg-leaf text-leaf-foreground",
    navy: "bg-navy text-navy-foreground",
    outline: "border-2 border-navy text-navy bg-paper",
    danger: "bg-destructive text-destructive-foreground",
  };
  return (
    <div className={cn("flex h-14 items-center justify-center gap-2 rounded-xl text-base font-bold", tones[tone])}>
      {icon}
      {children}
    </div>
  );
}

function Field({ label, value, placeholder }: { label: string; value?: string; placeholder?: string }) {
  return (
    <div>
      <p className="mb-1 text-xs font-semibold">{label}</p>
      <div className={cn("flex h-12 items-center rounded-lg border-2 border-input bg-paper px-3 text-sm", !value && "text-muted-foreground")}>
        {value ?? placeholder}
      </div>
    </div>
  );
}

function Chip({ children, tone = "leaf" }: { children: ReactNode; tone?: "leaf" | "navy" | "warning" | "muted" }) {
  const tones = {
    leaf: "bg-leaf text-leaf-foreground",
    navy: "bg-navy text-navy-foreground",
    warning: "bg-warning text-warning-foreground",
    muted: "bg-muted text-muted-foreground",
  };
  return <span className={cn("inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-bold", tones[tone])}>{children}</span>;
}

/* ---------- Screens ---------- */

function SignIn() {
  return (
    <PhoneFrame>
      <div className="flex flex-1 flex-col px-6 pt-8">
        <div className="mx-auto w-40 rounded-xl bg-paper p-2 shadow-card">
          <img src={logo.url} alt="" className="w-full" />
        </div>
        <h3 className="mt-6 text-center text-2xl font-extrabold">Courier sign in</h3>
        <p className="mt-1 text-center text-sm text-muted-foreground">Approved LightGrab couriers only</p>
        <div className="mt-6 space-y-3">
          <Field label="Phone or email" placeholder="080… or you@email.com" />
          <Field label="Password" value="••••••••••" />
        </div>
        <p className="mt-2 text-right text-xs font-semibold text-primary">Forgot password?</p>
        <div className="mt-5">
          <BigAction>Sign in</BigAction>
        </div>
        <div className="mt-auto mb-4 rounded-xl border border-dashed border-navy/30 p-3 text-center text-xs">
          Not a courier yet? <span className="font-bold text-primary">Apply to deliver</span>
        </div>
      </div>
    </PhoneFrame>
  );
}

function ApplyDocuments() {
  const docs = [
    { label: "ID card — front", done: true },
    { label: "ID card — back", done: true },
    { label: "Selfie holding ID", done: false },
    { label: "Vehicle papers", done: false },
  ];
  return (
    <PhoneFrame>
      <AppBar title="Apply to deliver" subtitle="Step 4 of 5 · Documents" />
      <div className="h-1.5 bg-muted">
        <div className="h-full w-4/5 bg-leaf-stripe" />
      </div>
      <div className="flex flex-1 flex-col gap-2.5 px-5 py-4">
        <p className="text-sm text-muted-foreground">Clear photos in good light. Files are stored privately and only seen by LightGrab staff.</p>
        {docs.map((d) => (
          <div key={d.label} className="flex items-center gap-3 rounded-xl border border-border bg-paper p-3">
            <span className={cn("grid size-9 place-items-center rounded-lg", d.done ? "bg-leaf text-leaf-foreground" : "bg-muted text-navy")}>
              {d.done ? <FileCheck2 className="size-5" /> : <Camera className="size-5" />}
            </span>
            <span className="flex-1 text-sm font-semibold">{d.label}</span>
            {d.done ? <Chip>Added</Chip> : <Upload className="size-4 text-muted-foreground" />}
          </div>
        ))}
        <div className="mt-auto">
          <BigAction tone="navy">Continue</BigAction>
        </div>
      </div>
    </PhoneFrame>
  );
}

function Pending() {
  return (
    <PhoneFrame dark>
      <div className="flex flex-1 flex-col items-center px-6 pt-14 text-center">
        <span className="grid size-20 place-items-center rounded-full bg-leaf text-leaf-foreground">
          <Clock className="size-10" />
        </span>
        <h3 className="mt-6 text-2xl font-extrabold">Application under review</h3>
        <p className="mt-2 text-sm text-navy-foreground/75">
          We'll notify you as soon as LightGrab staff approve your application. Deliveries stay locked until then.
        </p>
        <div className="mt-6 w-full space-y-2 text-left text-sm">
          {[
            ["Submitted", true],
            ["Documents checked", false],
            ["Approved", false],
          ].map(([l, d]) => (
            <div key={l as string} className="flex items-center gap-3 rounded-lg bg-navy-foreground/10 px-3 py-2.5">
              <span className={cn("grid size-6 place-items-center rounded-full", d ? "bg-leaf text-leaf-foreground" : "border border-navy-foreground/40")}>
                {d ? <Check className="size-4" /> : null}
              </span>
              {l as string}
            </div>
          ))}
        </div>
        <div className="mt-auto mb-4 flex w-full items-center justify-center gap-2 text-xs text-navy-foreground/70">
          <Lock className="size-3.5" /> No access to orders before approval
        </div>
      </div>
    </PhoneFrame>
  );
}

function Dashboard() {
  return (
    <PhoneFrame>
      <AppBar title="Good afternoon" subtitle="Courier · Oron" trailing={<Bell className="size-5" />} />
      <div className="flex flex-1 flex-col gap-3 overflow-hidden px-4 py-4">
        <div className="flex items-center justify-between rounded-2xl bg-navy p-4 text-navy-foreground">
          <div>
            <p className="text-xs text-navy-foreground/70">Availability</p>
            <p className="text-lg font-extrabold">Online</p>
          </div>
          <span className="flex h-8 w-14 items-center justify-end rounded-full bg-leaf p-1">
            <span className="size-6 rounded-full bg-navy" />
          </span>
        </div>
        <div className="grid grid-cols-3 gap-2 text-center">
          {[
            ["Assigned", "—"],
            ["Active", "—"],
            ["Done today", "—"],
          ].map(([l, v]) => (
            <div key={l} className="rounded-xl bg-paper p-2.5 shadow-card">
              <p className="font-display text-2xl font-extrabold">{v}</p>
              <p className="text-[10px] font-semibold text-muted-foreground">{l}</p>
            </div>
          ))}
        </div>
        <p className="mt-1 text-xs font-bold uppercase tracking-wider text-muted-foreground">Assigned to you</p>
        {[
          ["LG-0000", "Accepted", "leaf"],
          ["LG-0000", "New", "warning"],
        ].map(([n, s, t], i) => (
          <div key={i} className="flex items-center gap-3 rounded-xl bg-paper p-3 shadow-card">
            <span className="grid size-10 place-items-center rounded-lg bg-muted">
              <Package className="size-5" />
            </span>
            <div className="flex-1">
              <p className="font-mono text-sm font-bold">{n}</p>
              <p className="text-xs text-muted-foreground">Delivery address from order</p>
            </div>
            <Chip tone={t as "leaf" | "warning"}>{s}</Chip>
          </div>
        ))}
        <p className="text-center text-[10px] text-muted-foreground">Counts and orders load from the backend</p>
      </div>
      <BottomNav active="home" />
    </PhoneFrame>
  );
}

function OrderDetail() {
  return (
    <PhoneFrame>
      <AppBar title="LG-0000" subtitle="Travelling to destination" trailing={<Chip tone="navy">En route</Chip>} />
      <div className="flex flex-1 flex-col gap-3 overflow-hidden px-4 py-3">
        <MiniMap className="h-36" />
        <div className="rounded-xl bg-paper p-3 shadow-card">
          <div className="flex items-start gap-2">
            <MapPin className="mt-0.5 size-4 shrink-0 text-primary" />
            <div>
              <p className="text-sm font-bold">Delivery address</p>
              <p className="text-xs text-muted-foreground">Street, landmark and pinned coordinates from the order</p>
            </div>
          </div>
          <div className="mt-2 border-t border-border pt-2 text-xs">
            <span className="font-semibold">Customer:</span> first name only
          </div>
        </div>
        <div className="rounded-xl border-l-4 border-warning bg-paper p-3 text-xs shadow-card">
          <p className="font-bold">Delivery instructions</p>
          <p className="text-muted-foreground">Shown exactly as entered by Office</p>
        </div>
        <div className="grid grid-cols-2 gap-2">
          <BigAction tone="outline" icon={<Phone className="size-5" />}>Call</BigAction>
          <BigAction tone="navy" icon={<Navigation className="size-5" />}>Navigate</BigAction>
        </div>
        <div className="mt-auto">
          <div className="relative flex h-14 items-center rounded-xl bg-forest px-1.5 text-forest-foreground">
            <span className="grid h-11 w-14 place-items-center rounded-lg bg-leaf text-leaf-foreground">
              <ChevronRight className="size-6" />
            </span>
            <span className="flex-1 pr-14 text-center text-sm font-bold">Swipe — I've arrived</span>
          </div>
        </div>
      </div>
    </PhoneFrame>
  );
}

function Call() {
  return (
    <PhoneFrame dark>
      <div className="flex flex-1 flex-col items-center px-6 pt-10 text-center">
        <Chip tone="leaf">In-app call · LG-0000</Chip>
        <div className="relative mt-10 grid size-28 place-items-center">
          <span className="absolute inset-0 rounded-full bg-leaf/40 animate-pulse-ring" />
          <span className="relative grid size-24 place-items-center rounded-full bg-navy-foreground/15 font-display text-3xl font-extrabold">
            C
          </span>
        </div>
        <h3 className="mt-6 text-2xl font-extrabold">Customer first name</h3>
        <p className="mt-1 text-sm text-navy-foreground/70">Ringing… connecting securely</p>
        <p className="mt-4 rounded-lg bg-navy-foreground/10 px-3 py-2 text-xs text-navy-foreground/80">
          You're calling the customer for order LG-0000 only
        </p>
        <div className="mt-auto mb-8 flex w-full items-center justify-around">
          <span className="grid size-14 place-items-center rounded-full bg-navy-foreground/15"><MicOff className="size-6" /></span>
          <span className="grid size-16 place-items-center rounded-full bg-destructive text-destructive-foreground"><PhoneOff className="size-7" /></span>
          <span className="grid size-14 place-items-center rounded-full bg-navy-foreground/15"><Volume2 className="size-6" /></span>
        </div>
      </div>
    </PhoneFrame>
  );
}

function ConfirmArrival() {
  return (
    <PhoneFrame>
      <AppBar title="LG-0000" subtitle="Travelling to destination" />
      <div className="relative flex flex-1 flex-col px-4 py-3">
        <MiniMap className="h-40 opacity-50" />
        <div className="absolute inset-0 bg-navy/50" />
        <div className="absolute inset-x-0 bottom-0 rounded-t-3xl bg-paper p-5 shadow-device">
          <div className="mx-auto mb-4 h-1 w-10 rounded-full bg-border" />
          <h3 className="text-xl font-extrabold">Confirm you've arrived?</h3>
          <p className="mt-1 text-sm text-muted-foreground">Office and the customer will see you're at the address. Your location is recorded with this change.</p>
          <div className="mt-5 space-y-2">
            <BigAction icon={<Check className="size-5" />}>Yes, I've arrived</BigAction>
            <BigAction tone="outline">Not yet</BigAction>
          </div>
        </div>
      </div>
    </PhoneFrame>
  );
}

function Handover() {
  return (
    <PhoneFrame>
      <AppBar title="Delivery confirmation" subtitle="LG-0000" />
      <div className="flex flex-1 flex-col gap-4 px-5 py-5">
        <div className="flex items-center gap-3 rounded-xl bg-navy p-3 text-navy-foreground">
          <KeyRound className="size-6 text-leaf" />
          <p className="text-sm">Ask the customer for their 4-digit delivery code</p>
        </div>
        <div className="flex justify-center gap-2.5">
          {["", "", "", ""].map((_, i) => (
            <span key={i} className={cn("grid h-14 w-12 place-items-center rounded-lg border-2 bg-paper font-mono text-2xl font-bold", i === 0 ? "border-forest" : "border-input")}>
              {i === 0 ? "|" : ""}
            </span>
          ))}
        </div>
        <div className="flex items-center gap-3 rounded-xl border border-dashed border-navy/30 bg-paper p-3 text-sm">
          <Camera className="size-5" /> Add handover photo (optional)
        </div>
        <p className="text-xs text-muted-foreground">Payment is recorded by Office. You can't mark an order as paid.</p>
        <div className="mt-auto space-y-2">
          <BigAction>Complete delivery</BigAction>
          <p className="flex items-center justify-center gap-1 text-xs font-semibold text-destructive">
            <AlertTriangle className="size-3.5" /> Report a problem
          </p>
        </div>
      </div>
    </PhoneFrame>
  );
}

function ReportIssue() {
  const reasons = ["Customer not reachable", "Wrong or unclear address", "Customer refused package", "Package damaged", "Vehicle / safety problem", "Other"];
  return (
    <PhoneFrame>
      <AppBar title="Report an issue" subtitle="LG-0000 · Office will follow up" />
      <div className="flex flex-1 flex-col gap-2 px-5 py-4">
        {reasons.map((r, i) => (
          <div key={r} className={cn("flex items-center gap-3 rounded-xl border-2 bg-paper px-3 py-3 text-sm font-semibold", i === 0 ? "border-forest" : "border-transparent")}>
            <span className={cn("size-5 rounded-full border-2", i === 0 ? "border-[6px] border-forest" : "border-input")} />
            {r}
          </div>
        ))}
        <Field label="Details" placeholder="What happened?" />
        <div className="mt-auto">
          <BigAction tone="danger" icon={<AlertTriangle className="size-5" />}>Send report</BigAction>
        </div>
      </div>
    </PhoneFrame>
  );
}

function Notifications() {
  const items = [
    { icon: Package, title: "New assignment", body: "Order assigned to you by Office", tone: "bg-leaf text-leaf-foreground" },
    { icon: AlertTriangle, title: "Assignment changed", body: "Delivery address updated", tone: "bg-warning text-warning-foreground" },
    { icon: MapPin, title: "Delivery instruction", body: "New note from Office", tone: "bg-navy text-navy-foreground" },
    { icon: Megaphone, title: "Announcement", body: "Operational update for all couriers", tone: "bg-muted text-navy" },
  ];
  return (
    <PhoneFrame>
      <AppBar title="Alerts" subtitle="Newest first" />
      <div className="flex flex-1 flex-col gap-2 px-4 py-3">
        {items.map((n) => (
          <div key={n.title} className="flex gap-3 rounded-xl bg-paper p-3 shadow-card">
            <span className={cn("grid size-10 shrink-0 place-items-center rounded-lg", n.tone)}>
              <n.icon className="size-5" />
            </span>
            <div>
              <p className="text-sm font-bold">{n.title}</p>
              <p className="text-xs text-muted-foreground">{n.body}</p>
            </div>
          </div>
        ))}
      </div>
      <BottomNav active="alerts" />
    </PhoneFrame>
  );
}

function Suspended() {
  return (
    <PhoneFrame dark>
      <div className="flex flex-1 flex-col items-center px-6 pt-16 text-center">
        <span className="grid size-20 place-items-center rounded-full bg-destructive text-destructive-foreground">
          <ShieldAlert className="size-10" />
        </span>
        <h3 className="mt-6 text-2xl font-extrabold">Account suspended</h3>
        <p className="mt-2 text-sm text-navy-foreground/75">
          Your access to deliveries is paused. Location tracking and calls have stopped. Contact LightGrab Office for help.
        </p>
        <div className="mt-auto mb-6 w-full space-y-2">
          <BigAction tone="leaf">Contact Office</BigAction>
          <p className="text-xs text-navy-foreground/70">Sign out</p>
        </div>
      </div>
    </PhoneFrame>
  );
}

function MicPermission() {
  return (
    <PhoneFrame>
      <AppBar title="LG-0000" subtitle="Call customer" />
      <div className="flex flex-1 flex-col items-center px-6 pt-10 text-center">
        <span className="grid size-20 place-items-center rounded-full bg-leaf text-leaf-foreground">
          <Mic className="size-10" />
        </span>
        <h3 className="mt-6 text-xl font-extrabold">Allow microphone for in-app calls</h3>
        <p className="mt-2 text-sm text-muted-foreground">
          Calls go through LightGrab, not your SIM. Your number stays private and so does the customer's.
        </p>
        <div className="mt-auto mb-4 w-full space-y-2">
          <BigAction>Continue</BigAction>
          <BigAction tone="outline">Not now</BigAction>
        </div>
      </div>
    </PhoneFrame>
  );
}

export type ScreenSpec = {
  id: string;
  group: "Access" | "Deliveries" | "Calling" | "Account";
  title: string;
  module: string;
  notes: string[];
  render: () => ReactNode;
};

export const SCREENS: ScreenSpec[] = [
  { id: "sign-in", group: "Access", title: "Sign in", module: ":feature:auth", render: SignIn, notes: ["Shared LightGrab auth; courier role required after sign-in.", "Password recovery via email deep link back into the app.", "Session tokens kept in encrypted storage; refresh handled by supabase-kt."] },
  { id: "apply", group: "Access", title: "Courier application", module: ":feature:onboarding", render: ApplyDocuments, notes: ["Five steps: account, personal, identity, vehicle, documents.", "CameraX capture or Photo Picker; uploads to private storage.", "Draft saved locally so a lost signal doesn't lose progress."] },
  { id: "pending", group: "Access", title: "Application under review", module: ":feature:onboarding", render: Pending, notes: ["Shown for pending, needs_info and rejected statuses.", "No delivery screens or data are reachable from here.", "Realtime + push flip the app to the dashboard on approval."] },
  { id: "dashboard", group: "Deliveries", title: "Courier dashboard", module: ":feature:dashboard", render: Dashboard, notes: ["Availability toggle writes through courier_set_availability.", "Counts and list come from courier_assigned_orders.", "Earnings card only when the fees feature flag is on."] },
  { id: "order", group: "Deliveries", title: "Order detail & mini-map", module: ":feature:orders", render: OrderDetail, notes: ["Each order has its own mini-map with the pinned destination.", "Navigate hands off to the map provider's turn-by-turn app.", "No distance or ETA shown unless returned by the map provider."] },
  { id: "confirm", group: "Deliveries", title: "Confirm status change", module: ":feature:delivery", render: ConfirmArrival, notes: ["Accept, arrive, complete and issue need explicit confirmation.", "Location and timestamp attached to the change.", "Queued offline and retried by WorkManager."] },
  { id: "handover", group: "Deliveries", title: "Delivery confirmation", module: ":feature:delivery", render: Handover, notes: ["Customer's delivery code verified server-side by confirm-handover.", "Optional proof photo.", "No payment controls for couriers."] },
  { id: "issue", group: "Deliveries", title: "Report an issue", module: ":feature:delivery", render: ReportIssue, notes: ["Moves the delivery to issue_reported.", "Only Office can resolve and move it on.", "Optional photo attached to delivery_issues."] },
  { id: "mic", group: "Calling", title: "Microphone permission", module: ":feature:call", render: MicPermission, notes: ["Explains why before the system prompt.", "Denied state offers Settings and Report issue.", "Never falls back to the SIM dialer."] },
  { id: "call", group: "Calling", title: "In-app call", module: ":feature:call", render: Call, notes: ["One call session per assignment, created server-side.", "Order number and customer first name shown before connecting.", "Ringing, connected, reconnecting, failed and ended states."] },
  { id: "alerts", group: "Account", title: "Notifications", module: ":feature:notifications", render: Notifications, notes: ["Assignments, changes, customer updates, instructions, announcements.", "Each item deep-links to the related order.", "Separate Android channels so couriers can tune sound."] },
  { id: "suspended", group: "Account", title: "Account suspended", module: ":feature:auth", render: Suspended, notes: ["Triggered instantly by realtime change on the couriers row.", "Stops tracking service and ends any call.", "Cached customer data is wiped from the device."] },
];
