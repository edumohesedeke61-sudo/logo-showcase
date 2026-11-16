import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

export function PageIntro({ eyebrow, title, children }: { eyebrow: string; title: string; children?: ReactNode }) {
  return (
    <div className="mx-auto max-w-7xl px-4 pt-14 pb-6 sm:px-6">
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-primary">{eyebrow}</p>
      <h1 className="mt-3 max-w-3xl text-4xl font-extrabold font-wide sm:text-5xl">{title}</h1>
      {children && <div className="mt-4 max-w-2xl text-lg text-muted-foreground">{children}</div>}
    </div>
  );
}

export function Section({ id, title, kicker, children, className }: { id?: string; title: string; kicker?: string; children: ReactNode; className?: string }) {
  return (
    <section id={id} className={cn("mx-auto max-w-7xl scroll-mt-24 px-4 py-10 sm:px-6", className)}>
      <div className="mb-6 flex items-end gap-4">
        <div>
          {kicker && <p className="text-xs font-bold uppercase tracking-[0.2em] text-primary">{kicker}</p>}
          <h2 className="mt-1 text-2xl font-extrabold sm:text-3xl">{title}</h2>
        </div>
        <div className="mb-2 h-1 flex-1 rounded-full bg-leaf-stripe opacity-60" />
      </div>
      {children}
    </section>
  );
}

export function Panel({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn("rounded-xl border border-border bg-card p-5 shadow-card", className)}>{children}</div>;
}

export function Code({ children }: { children: ReactNode }) {
  return <code className="rounded bg-muted px-1.5 py-0.5 font-mono text-[0.85em] text-foreground">{children}</code>;
}
