import type { ReactNode } from "react";
import { Battery, Signal, Wifi } from "lucide-react";
import { cn } from "@/lib/utils";

/** Android device frame used to present native screen designs. */
export function PhoneFrame({ children, dark = false, className }: { children: ReactNode; dark?: boolean; className?: string }) {
  return (
    <div className={cn("relative w-[300px] shrink-0 rounded-[2.6rem] bg-device p-2.5 shadow-device", className)}>
      <div className={cn("relative flex h-[610px] flex-col overflow-hidden rounded-[2.1rem]", dark ? "bg-navy text-navy-foreground" : "bg-cream text-navy")}>
        <div className="flex items-center justify-between px-6 pt-3 pb-1 text-[11px] font-semibold">
          <span>13:50</span>
          <span className="absolute left-1/2 top-2.5 h-4 w-4 -translate-x-1/2 rounded-full bg-device" />
          <span className="flex items-center gap-1">
            <Signal className="size-3" />
            <Wifi className="size-3" />
            <Battery className="size-3.5" />
          </span>
        </div>
        <div className="relative flex min-h-0 flex-1 flex-col">{children}</div>
        <div className="flex justify-center pb-2 pt-1">
          <span className={cn("h-1 w-24 rounded-full", dark ? "bg-navy-foreground/50" : "bg-navy/40")} />
        </div>
      </div>
    </div>
  );
}

export function AppBar({ title, subtitle, dark = false, trailing }: { title: string; subtitle?: string; dark?: boolean; trailing?: ReactNode }) {
  return (
    <div className={cn("flex items-center justify-between px-5 py-3", dark ? "" : "border-b border-border bg-paper")}>
      <div>
        <p className="font-display text-lg font-extrabold leading-tight">{title}</p>
        {subtitle && <p className={cn("text-xs", dark ? "text-navy-foreground/70" : "text-muted-foreground")}>{subtitle}</p>}
      </div>
      {trailing}
    </div>
  );
}

export function BottomNav({ active }: { active: "home" | "orders" | "alerts" | "me" }) {
  const items = [
    { id: "home", label: "Home" },
    { id: "orders", label: "Orders" },
    { id: "alerts", label: "Alerts" },
    { id: "me", label: "Account" },
  ] as const;
  return (
    <div className="mt-auto grid grid-cols-4 border-t border-border bg-paper px-2 py-2 text-center text-[11px] font-semibold">
      {items.map((i) => (
        <span key={i.id} className={cn("mx-1 rounded-full py-1.5", i.id === active ? "bg-leaf text-leaf-foreground" : "text-muted-foreground")}>
          {i.label}
        </span>
      ))}
    </div>
  );
}

export function MiniMap({ className, showCourier = true }: { className?: string; showCourier?: boolean }) {
  return (
    <div className={cn("relative overflow-hidden rounded-xl map-grid", className)}>
      <svg viewBox="0 0 260 140" className="absolute inset-0 h-full w-full" preserveAspectRatio="none" aria-hidden>
        {showCourier && (
          <path d="M40 115 C 80 110, 90 70, 130 72 S 190 40, 205 38" fill="none" className="stroke-forest" strokeWidth="5" strokeLinecap="round" strokeDasharray="1 10" />
        )}
      </svg>
      {showCourier && (
        <span className="absolute left-[13%] top-[78%] -translate-x-1/2 -translate-y-1/2">
          <span className="absolute inset-0 rounded-full bg-forest animate-pulse-ring" />
          <span className="relative block h-3.5 w-3.5 rounded-full border-2 border-paper bg-forest" />
        </span>
      )}
      <span className="absolute left-[79%] top-[24%] -translate-x-1/2 -translate-y-full">
        <svg width="26" height="32" viewBox="0 0 26 32" aria-hidden>
          <path d="M13 0C5.8 0 0 5.6 0 12.6 0 22 13 32 13 32s13-10 13-19.4C26 5.6 20.2 0 13 0z" className="fill-navy" />
          <circle cx="13" cy="12.5" r="5" className="fill-leaf" />
        </svg>
      </span>
      <span className="absolute bottom-1.5 right-2 rounded bg-paper/85 px-1.5 py-0.5 text-[9px] font-medium text-muted-foreground">
        Map provider tiles
      </span>
    </div>
  );
}
