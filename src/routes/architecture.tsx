import { createFileRoute } from "@tanstack/react-router";
import { Ban, Terminal } from "lucide-react";
import { Code, PageIntro, Panel, Section } from "@/components/spec/Section";
import { buildCommands, buildConfig, kotlinModules, notRequested, permissions, techStack, type KotlinModule } from "@/data/spec";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/architecture")({
  head: () => ({
    meta: [
      { title: "Android build — LightGrab Courier" },
      { name: "description", content: "Kotlin modules, libraries, Android permissions, build configuration and APK generation for LightGrab Courier." },
      { property: "og:title", content: "Android build — LightGrab Courier" },
      { property: "og:description", content: "Kotlin + Jetpack Compose architecture and build plan for LightGrab Courier." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: ArchitecturePage,
});

const LAYERS: { id: KotlinModule["layer"]; title: string; tone: string }[] = [
  { id: "app", title: "App shell", tone: "bg-navy text-navy-foreground" },
  { id: "feature", title: "Feature modules", tone: "bg-forest text-forest-foreground" },
  { id: "core", title: "Core libraries", tone: "bg-leaf text-leaf-foreground" },
];

function ArchitecturePage() {
  return (
    <>
      <PageIntro eyebrow="Native Android" title="Kotlin architecture & build plan">
        A multi-module Android Studio project. Features depend on core libraries, never on each other; map and calling providers sit behind interfaces so they can be swapped.
      </PageIntro>

      <Section title="Technology" kicker="Stack">
        <div className="overflow-x-auto rounded-xl border border-border bg-card">
          <table className="w-full min-w-[640px] text-left text-sm">
            <tbody>
              {techStack.map((t) => (
                <tr key={t.area} className="border-b border-border last:border-0">
                  <td className="w-40 px-4 py-3 font-bold">{t.area}</td>
                  <td className="px-4 py-3">{t.choice}</td>
                  <td className="px-4 py-3 text-muted-foreground">{t.note}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Section>

      <Section title="Gradle modules" kicker="Structure">
        <div className="space-y-6">
          {LAYERS.map((layer) => (
            <div key={layer.id}>
              <p className={cn("inline-block rounded-md px-3 py-1 text-xs font-bold uppercase tracking-wider", layer.tone)}>{layer.title}</p>
              <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {kotlinModules.filter((m) => m.layer === layer.id).map((m) => (
                  <Panel key={m.name}>
                    <p className="font-mono text-sm font-bold">{m.name}</p>
                    <p className="mt-1 text-sm text-muted-foreground">{m.purpose}</p>
                    <div className="mt-3 flex flex-wrap gap-1">
                      {m.exposes.map((e) => <Code key={e}>{e}</Code>)}
                    </div>
                  </Panel>
                ))}
              </div>
            </div>
          ))}
        </div>
        <Panel className="mt-6">
          <p className="font-bold">Swappable providers</p>
          <pre className="mt-3 overflow-x-auto rounded-lg bg-navy p-4 font-mono text-xs leading-relaxed text-navy-foreground">{`interface MapProvider {
    @Composable fun MiniMap(destination: LatLng, courier: LatLng?, route: Route?)
    suspend fun route(from: LatLng, to: LatLng): Result<Route>   // provider data only
    fun navigationIntent(to: LatLng): Intent                       // hand-off to turn-by-turn
}

interface CallClient {
    val state: StateFlow<CallState>   // Idle, Requesting, Ringing, Connected, Reconnecting, Ended(reason), Failed(error)
    suspend fun start(assignmentId: AssignmentId)
    fun setMuted(muted: Boolean); fun setSpeaker(on: Boolean); suspend fun hangUp()
}`}</pre>
          <p className="mt-3 text-sm text-muted-foreground">
            Selected at build time with <Code>MAP_PROVIDER</Code> — switching providers changes one Hilt binding, not the screens.
          </p>
        </Panel>
      </Section>

      <Section title="Android permissions" kicker="Manifest">
        <div className="overflow-x-auto rounded-xl border border-border bg-card">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead className="bg-navy text-navy-foreground">
              <tr>{["Permission", "Asked when", "Why"].map((h) => <th key={h} className="px-4 py-3 font-semibold">{h}</th>)}</tr>
            </thead>
            <tbody>
              {permissions.map((p) => (
                <tr key={p.name} className="border-t border-border">
                  <td className="px-4 py-3 font-mono text-xs font-semibold">{p.name}</td>
                  <td className="px-4 py-3">{p.when}</td>
                  <td className="px-4 py-3 text-muted-foreground">{p.why}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <Panel className="mt-4">
          <p className="flex items-center gap-2 font-bold"><Ban className="size-4 text-destructive" /> Deliberately not requested</p>
          <ul className="mt-2 space-y-1 text-sm text-muted-foreground">
            {notRequested.map((n) => <li key={n}>• {n}</li>)}
          </ul>
        </Panel>
      </Section>

      <Section title="Build configuration & APK" kicker="Ship">
        <div className="grid gap-6 lg:grid-cols-2">
          <div className="overflow-hidden rounded-xl border border-border bg-card">
            {buildConfig.map((b) => (
              <div key={b.key} className="grid gap-1 border-b border-border px-4 py-3 text-sm last:border-0 sm:grid-cols-[170px_1fr]">
                <p className="font-mono text-xs font-bold">{b.key}</p>
                <p className="text-muted-foreground">{b.value}</p>
              </div>
            ))}
          </div>
          <div className="rounded-xl bg-navy p-5 text-navy-foreground">
            <p className="flex items-center gap-2 font-bold text-leaf"><Terminal className="size-4" /> Gradle commands</p>
            <div className="mt-4 space-y-4">
              {buildCommands.map((c) => (
                <div key={c.cmd}>
                  <p className="text-xs text-navy-foreground/70">{c.label}</p>
                  <p className="mt-1 rounded-md bg-navy-foreground/10 px-3 py-2 font-mono text-sm">{c.cmd}</p>
                </div>
              ))}
            </div>
            <p className="mt-6 border-t border-navy-foreground/20 pt-4 text-sm text-navy-foreground/80">
              Release readiness: unit tests for the status rules, UI tests on every screen, and a signed build installed on a real low-end Android device on Oron mobile data before any APK is called working.
            </p>
          </div>
        </div>
      </Section>
    </>
  );
}
