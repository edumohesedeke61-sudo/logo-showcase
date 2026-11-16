import { Link } from "@tanstack/react-router";
import logo from "@/assets/lightgrab-logo.webp.asset.json";

const NAV = [
  { to: "/", label: "Overview" },
  { to: "/screens", label: "Screens" },
  { to: "/workflows", label: "Workflows" },
  { to: "/architecture", label: "Android build" },
  { to: "/backend", label: "Backend" },
] as const;

export function SiteHeader() {
  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background/90 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center gap-6 px-4 py-3 sm:px-6">
        <Link to="/" className="flex shrink-0 items-center gap-2.5">
          <span className="grid h-10 w-14 place-items-center rounded-md bg-paper p-1 shadow-card">
            <img src={logo.url} alt="LightGrab logo" className="h-full w-full object-contain" />
          </span>
          <span className="leading-none">
            <span className="block font-display text-lg font-extrabold font-wide">LightGrab</span>
            <span className="block text-[11px] font-semibold uppercase tracking-[0.18em] text-primary">
              Courier
            </span>
          </span>
        </Link>
        <nav className="-mx-2 flex flex-1 gap-1 overflow-x-auto px-2 text-sm font-medium">
          {NAV.map((item) => (
            <Link
              key={item.to}
              to={item.to}
              activeOptions={{ exact: true }}
              className="whitespace-nowrap rounded-md px-3 py-2 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground data-[status=active]:bg-navy data-[status=active]:text-navy-foreground"
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </div>
    </header>
  );
}

export function SiteFooter() {
  return (
    <footer className="mt-24 border-t border-border">
      <div className="mx-auto flex max-w-7xl flex-col gap-2 px-4 py-8 text-sm text-muted-foreground sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <p>LightGrab Courier — product &amp; technical specification. Oron, Akwa Ibom State.</p>
        <p>No Android APK has been built or tested from this specification yet.</p>
      </div>
    </footer>
  );
}
