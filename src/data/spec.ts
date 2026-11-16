/** LightGrab Courier technical specification content. Rendered by the spec pages. */

export const SPEC_VERSION = "v0.1 · Architecture & screen structure";

export const platformRules = {
  required: [
    "Native Android application written in Kotlin",
    "UI built with Jetpack Compose + Material 3",
    "Built and signed in Android Studio / Gradle",
    "Shares the existing LightGrab backend with Office and Admin",
  ],
  prohibited: [
    "Progressive Web App (PWA)",
    "WebView wrapper around a website",
    "Browser app presented as native",
    "Website converted into an APK",
  ],
};

export const techStack: { area: string; choice: string; note: string }[] = [
  { area: "Language", choice: "Kotlin 2.x (K2 compiler)", note: "Coroutines + Flow for all async work" },
  { area: "UI", choice: "Jetpack Compose, Material 3", note: "Compose BOM pinned in version catalog" },
  { area: "Navigation", choice: "Navigation Compose (type-safe routes)", note: "Single-activity app" },
  { area: "DI", choice: "Hilt", note: "One module per core library" },
  { area: "Backend client", choice: "supabase-kt (Auth, Postgrest, Realtime, Storage, Functions)", note: "Ktor OkHttp engine" },
  { area: "Serialization", choice: "kotlinx.serialization", note: "DTOs generated from the shared schema" },
  { area: "Local cache", choice: "Room + DataStore", note: "Assigned orders readable offline; tokens in encrypted storage" },
  { area: "Background work", choice: "WorkManager", note: "Retries queued status updates when signal returns" },
  { area: "Location", choice: "Fused Location Provider", note: "Foreground service only during an active delivery" },
  { area: "Maps", choice: "MapProvider interface", note: "Google Maps Compose or MapLibre behind one contract" },
  { area: "Calling", choice: "LiveKit Android SDK", note: "Self-managed ConnectionService for system call UI" },
  { area: "Push", choice: "Firebase Cloud Messaging", note: "Data messages; app renders notifications" },
  { area: "Camera", choice: "CameraX + Photo Picker", note: "ID documents and proof of delivery" },
  { area: "Images", choice: "Coil 3", note: "Signed Storage URLs only" },
];

export type KotlinModule = { name: string; layer: "app" | "core" | "feature"; purpose: string; exposes: string[] };

export const kotlinModules: KotlinModule[] = [
  { name: ":app", layer: "app", purpose: "Entry point, NavHost, Hilt graph, FCM service, app-level session gate.", exposes: ["MainActivity", "LightGrabApp", "CourierMessagingService"] },
  { name: ":core:designsystem", layer: "core", purpose: "Brand theme, typography and outdoor-readable components.", exposes: ["LightGrabTheme", "BigActionButton", "SwipeToConfirm", "StatusChip"] },
  { name: ":core:model", layer: "core", purpose: "Pure Kotlin domain models shared by every module.", exposes: ["Order", "Assignment", "DeliveryStatus", "CourierProfile"] },
  { name: ":core:network", layer: "core", purpose: "Configured Supabase client, auth session refresh, error mapping.", exposes: ["SupabaseProvider", "ApiResult"] },
  { name: ":core:data", layer: "core", purpose: "Repositories, Room cache, offline status-update queue.", exposes: ["AssignmentRepository", "CourierRepository", "NotificationRepository"] },
  { name: ":core:location", layer: "core", purpose: "Permission flow, location stream, active-delivery foreground service.", exposes: ["LocationClient", "DeliveryTrackingService"] },
  { name: ":core:maps", layer: "core", purpose: "Provider-agnostic map contract. Swap implementation by build config.", exposes: ["MapProvider", "MiniMap()", "RouteRequest"] },
  { name: ":core:maps-google", layer: "core", purpose: "Google Maps Compose + Directions implementation of MapProvider.", exposes: ["GoogleMapProvider"] },
  { name: ":core:maps-maplibre", layer: "core", purpose: "MapLibre + OSRM/Valhalla implementation of MapProvider.", exposes: ["MapLibreProvider"] },
  { name: ":core:calling", layer: "core", purpose: "VoIP contract, LiveKit implementation, Telecom ConnectionService.", exposes: ["CallClient", "CallState", "LiveKitCallClient"] },
  { name: ":core:notifications", layer: "core", purpose: "Notification channels, FCM token registration, deep links.", exposes: ["NotificationChannels", "DeviceTokenRegistrar"] },
  { name: ":core:security", layer: "core", purpose: "Encrypted token storage, FLAG_SECURE on sensitive screens, audit helpers.", exposes: ["SecureStore", "SecureScreen()"] },
  { name: ":feature:auth", layer: "feature", purpose: "Sign in, password recovery, suspended / pending states, sign out.", exposes: ["SignInScreen", "ResetPasswordScreen"] },
  { name: ":feature:onboarding", layer: "feature", purpose: "Courier application form, document capture, application status.", exposes: ["ApplyFlow", "ApplicationStatusScreen"] },
  { name: ":feature:dashboard", layer: "feature", purpose: "Availability toggle, today's assignments, counts, earnings card (flagged).", exposes: ["DashboardScreen"] },
  { name: ":feature:orders", layer: "feature", purpose: "Assigned list, order detail with mini-map, history.", exposes: ["OrdersScreen", "OrderDetailScreen", "HistoryScreen"] },
  { name: ":feature:delivery", layer: "feature", purpose: "Status stepper, confirmations, handover code, issue reporting.", exposes: ["DeliveryFlow", "HandoverScreen", "ReportIssueSheet"] },
  { name: ":feature:call", layer: "feature", purpose: "In-app call screen bound to one assignment's call session.", exposes: ["CallScreen"] },
  { name: ":feature:notifications", layer: "feature", purpose: "Inbox for assignments, changes, instructions, announcements.", exposes: ["NotificationsScreen"] },
  { name: ":feature:profile", layer: "feature", purpose: "Courier profile, vehicle, documents status, settings, sign out.", exposes: ["ProfileScreen"] },
];

export const permissions: { name: string; when: string; why: string }[] = [
  { name: "INTERNET, ACCESS_NETWORK_STATE", when: "Install", why: "Backend, realtime and call media" },
  { name: "ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION", when: "First time an order is opened", why: "Show courier on mini-map, record arrival" },
  { name: "FOREGROUND_SERVICE, FOREGROUND_SERVICE_LOCATION", when: "Courier taps Start trip", why: "Location updates while travelling, with visible notification" },
  { name: "POST_NOTIFICATIONS", when: "After first sign in (Android 13+)", why: "New assignments and instructions" },
  { name: "RECORD_AUDIO, MODIFY_AUDIO_SETTINGS", when: "First call attempt", why: "In-app VoIP call with customer" },
  { name: "FOREGROUND_SERVICE_PHONE_CALL, MANAGE_OWN_CALLS", when: "Install", why: "Keep call alive with screen off; system call UI" },
  { name: "BLUETOOTH_CONNECT", when: "When a headset is detected (Android 12+)", why: "Route call audio to helmet / earpiece" },
  { name: "CAMERA", when: "Document upload or proof of delivery", why: "Capture ID, vehicle papers, handover photo" },
  { name: "WAKE_LOCK", when: "Install", why: "Keep call and tracking service running" },
];

export const notRequested = [
  "ACCESS_BACKGROUND_LOCATION — tracking only runs in a visible foreground service",
  "CALL_PHONE / READ_PHONE_STATE — calls never use the SIM dialer",
  "READ_CONTACTS, READ_EXTERNAL_STORAGE — Photo Picker needs no storage permission",
];

export const buildConfig: { key: string; value: string }[] = [
  { key: "applicationId", value: "ng.lightgrab.courier" },
  { key: "minSdk", value: "26 (Android 8.0)" },
  { key: "targetSdk / compileSdk", value: "Latest level required by Google Play at release time" },
  { key: "Flavors", value: "dev · staging · prod (separate backend URLs and app ids suffixes)" },
  { key: "BuildConfig fields", value: "SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY, LIVEKIT_URL, MAP_PROVIDER, MAPS_API_KEY" },
  { key: "Secrets source", value: "local.properties locally, CI secrets in pipelines — never committed" },
  { key: "Minify", value: "R8 full mode with keep rules for kotlinx.serialization, supabase-kt, LiveKit" },
  { key: "Signing", value: "Upload key in CI keystore; Play App Signing for production" },
];

export const buildCommands = [
  { label: "Debug APK (device testing)", cmd: "./gradlew :app:assembleDevDebug" },
  { label: "Release APK (sideload / internal)", cmd: "./gradlew :app:assembleProdRelease" },
  { label: "Play Store bundle", cmd: "./gradlew :app:bundleProdRelease" },
  { label: "Unit + UI tests", cmd: "./gradlew testDevDebugUnitTest connectedDevDebugAndroidTest" },
];

export type TableSpec = { name: string; owner: "shared" | "courier"; purpose: string; columns: string[]; courierAccess: string };

export const tables: TableSpec[] = [
  { name: "profiles", owner: "shared", purpose: "One row per signed-in person (staff, courier, customer).", columns: ["id uuid → auth.users", "full_name", "phone", "email", "avatar_path", "created_at"], courierAccess: "Read / update own row" },
  { name: "user_roles", owner: "shared", purpose: "Roles kept apart from profiles to prevent privilege escalation.", columns: ["user_id → auth.users", "role app_role (admin | office | courier)"], courierAccess: "Read own roles via has_role()" },
  { name: "courier_applications", owner: "courier", purpose: "Public applications awaiting Admin review.", columns: ["id", "user_id → auth.users", "full_name", "phone", "email", "residential_address", "operating_area", "id_type", "id_number (restricted)", "vehicle_type", "vehicle_plate", "status (pending | needs_info | approved | rejected)", "reviewed_by", "reviewed_at", "decision_note"], courierAccess: "Insert own; read own status; no update after submit" },
  { name: "courier_documents", owner: "courier", purpose: "Metadata for files in the private courier-documents bucket.", columns: ["id", "application_id →", "kind (id_front | id_back | selfie | vehicle_papers | licence)", "storage_path", "uploaded_at"], courierAccess: "Insert / read own; files via signed URL" },
  { name: "couriers", owner: "shared", purpose: "Approved couriers. Created by Admin on approval.", columns: ["id → auth.users", "application_id →", "account_status (active | suspended | deactivated)", "availability (offline | online | on_delivery)", "operating_area", "vehicle_type", "suspended_reason"], courierAccess: "Read own; availability via RPC only" },
  { name: "orders", owner: "shared", purpose: "Existing LightGrab orders (owned by Office).", columns: ["id", "order_number unique", "customer_id →", "delivery_address", "dest_lat", "dest_lng", "package_summary", "instructions", "payment_status (read-only to couriers)"], courierAccess: "No direct access — via courier_assigned_orders view" },
  { name: "delivery_assignments", owner: "shared", purpose: "Office assigns an order to a courier. Many per courier.", columns: ["id", "order_id →", "courier_id →", "assigned_by →", "status delivery_status", "assigned_at", "accepted_at", "arrived_at", "completed_at", "handover_code_hash"], courierAccess: "Read own rows; status via RPC only" },
  { name: "delivery_events", owner: "shared", purpose: "Append-only audit of every status change.", columns: ["id", "assignment_id →", "actor_id", "from_status", "to_status", "lat", "lng", "note", "created_at"], courierAccess: "Read own; written by RPC" },
  { name: "delivery_issues", owner: "shared", purpose: "Problems reported by couriers for Office follow-up.", columns: ["id", "assignment_id →", "kind", "description", "photo_path", "resolution", "resolved_by"], courierAccess: "Insert / read own" },
  { name: "courier_locations", owner: "shared", purpose: "Location pings during an active delivery only.", columns: ["courier_id", "assignment_id", "lat", "lng", "accuracy_m", "recorded_at"], courierAccess: "Insert own while assignment active" },
  { name: "call_sessions", owner: "shared", purpose: "One VoIP room per assignment call attempt.", columns: ["id", "assignment_id →", "courier_id", "customer_id", "room_name", "status (ringing | connected | ended | failed)", "started_at", "ended_at", "end_reason"], courierAccess: "Read own; created by edge function" },
  { name: "notifications", owner: "shared", purpose: "Per-user inbox mirrored to push.", columns: ["id", "user_id", "kind", "title", "body", "payload jsonb", "read_at"], courierAccess: "Read own; mark read" },
  { name: "device_tokens", owner: "shared", purpose: "FCM tokens per device.", columns: ["user_id", "token", "platform", "updated_at"], courierAccess: "Upsert / delete own" },
  { name: "announcements", owner: "shared", purpose: "Operational announcements from Office/Admin.", columns: ["id", "title", "body", "audience (all_couriers | area)", "published_at"], courierAccess: "Read published" },
  { name: "courier_earnings", owner: "shared", purpose: "Optional fee ledger, shown only when enabled.", columns: ["id", "courier_id", "assignment_id", "amount", "status", "created_at"], courierAccess: "Read own (feature-flagged)" },
  { name: "audit_log", owner: "shared", purpose: "Security-relevant actions across all apps.", columns: ["id", "actor_id", "action", "entity", "entity_id", "meta jsonb", "created_at"], courierAccess: "None (written server-side)" },
];

export const endpoints: { kind: "RPC" | "Edge function" | "View"; name: string; input: string; rule: string }[] = [
  { kind: "View", name: "courier_assigned_orders", input: "—", rule: "security_invoker view: assignment + order + customer first name and landmark only, filtered to auth.uid()." },
  { kind: "RPC", name: "courier_update_delivery_status", input: "assignment_id, to_status, lat?, lng?, note?", rule: "Checks ownership, account active, allowed transition; writes delivery_events; never touches payment fields." },
  { kind: "RPC", name: "courier_set_availability", input: "availability", rule: "Rejects when suspended; cannot go offline with an in-progress delivery without Office." },
  { kind: "Edge function", name: "submit-courier-application", input: "form fields + document paths", rule: "Validates with schema, rate-limits per phone/IP, sets status = pending." },
  { kind: "Edge function", name: "confirm-handover", input: "assignment_id, code, photo_path?", rule: "Compares code hash, then moves handover → completed and notifies Office." },
  { kind: "Edge function", name: "create-call-session", input: "assignment_id", rule: "Verifies caller owns an active assignment, creates room for that order's customer only, returns a short-lived LiveKit token." },
  { kind: "Edge function", name: "end-call-session", input: "call_session_id, reason", rule: "Closes the room and records end reason." },
  { kind: "Edge function", name: "livekit-webhook", input: "LiveKit signed event", rule: "Verifies signature; syncs participant joined / left into call_sessions." },
  { kind: "Edge function", name: "register-device-token", input: "token, platform", rule: "Upserts FCM token for auth.uid()." },
  { kind: "Edge function", name: "dispatch-notification", input: "internal trigger", rule: "Called by DB triggers; writes notifications row and sends FCM data message." },
];

export const realtimeEvents: { channel: string; source: string; consumer: string; effect: string }[] = [
  { channel: "postgres_changes · delivery_assignments (courier_id = me)", source: "Office assigns / edits", consumer: "Courier app", effect: "Refresh list; banner for new or changed assignment" },
  { channel: "postgres_changes · couriers (id = me)", source: "Admin suspends", consumer: "Courier app", effect: "Immediately show suspended screen and stop tracking" },
  { channel: "postgres_changes · notifications (user_id = me)", source: "dispatch-notification", consumer: "Courier app", effect: "Inbox badge" },
  { channel: "postgres_changes · call_sessions (id = active)", source: "livekit-webhook", consumer: "Courier app", effect: "Ringing → connected → ended states" },
  { channel: "postgres_changes · delivery_events", source: "Courier RPC", consumer: "Office app", effect: "Live status board" },
  { channel: "broadcast · courier-locations:<assignment_id>", source: "Courier app", consumer: "Office app", effect: "Live courier dot during trip (not persisted every ping)" },
];

export const rlsRules = [
  "Row-level security on every table; anonymous users can read nothing.",
  "Couriers see only rows where courier_id = auth.uid() and their account_status = 'active'.",
  "Status and availability changes go through security-definer RPCs; direct UPDATE on assignments is denied to couriers.",
  "Customer data reaches couriers only through courier_assigned_orders, and only while the assignment is not completed.",
  "courier-documents bucket is private; uploads limited to the applicant's own folder; reads via short-lived signed URLs.",
  "Service-role keys exist only in edge functions — never in the Android app or its build config.",
  "Roles checked with has_role(auth.uid(), 'courier') from the separate user_roles table.",
];

export const notificationMatrix: { event: string; channel: string; priority: string; deepLink: string }[] = [
  { event: "New assignment", channel: "assignments", priority: "High · sound", deepLink: "lightgrab://order/{id}" },
  { event: "Assignment changed / withdrawn", channel: "assignments", priority: "High", deepLink: "lightgrab://order/{id}" },
  { event: "Customer update", channel: "customer_updates", priority: "Default", deepLink: "lightgrab://order/{id}" },
  { event: "Delivery instruction", channel: "instructions", priority: "High", deepLink: "lightgrab://order/{id}#instructions" },
  { event: "Operational announcement", channel: "announcements", priority: "Low", deepLink: "lightgrab://notifications" },
  { event: "Account status change", channel: "account", priority: "High", deepLink: "lightgrab://account" },
];

export const registrationFlow = [
  { step: "Open courier portal", detail: "From the LightGrab courier portal link or the app's Apply button." },
  { step: "Create account", detail: "Phone + email + password. Account has no courier role yet." },
  { step: "Personal details", detail: "Full name, residential address, operating location (Oron areas)." },
  { step: "Identity", detail: "ID type and number, selfie, ID photos — stored privately." },
  { step: "Vehicle", detail: "Bicycle, motorcycle, tricycle, car or on foot; plate and papers where applicable." },
  { step: "Submit", detail: "Application saved as pending. App shows Under review and blocks all delivery screens." },
  { step: "Admin decision", detail: "Admin dashboard approves (creates couriers row + courier role), rejects or requests more info." },
  { step: "Activated", detail: "Push + email notify the applicant. Next sign-in opens the courier dashboard." },
];

export const callFlow = [
  { actor: "Courier", action: "Taps Call on order LG-xxxx" },
  { actor: "App", action: "Checks microphone permission, then calls create-call-session(assignment_id)" },
  { actor: "Edge function", action: "Confirms the courier owns that active assignment; looks up that order's customer" },
  { actor: "Edge function", action: "Creates room call_<assignment_id>_<nonce>, mints a 10-minute courier token, pushes a join invite to the customer" },
  { actor: "App", action: "Joins the room via LiveKit; shows Ringing with the order number and customer first name" },
  { actor: "Customer", action: "Accepts in the LightGrab customer app; joins with their own scoped token" },
  { actor: "Both", action: "Connected. Mute, speaker, Bluetooth and hang up available" },
  { actor: "App / webhook", action: "On hang up, timeout or failure, end-call-session records the reason" },
];

export const callSafeguards = [
  "Room names are bound to one assignment; tokens only grant that room.",
  "The call screen always shows the order number and customer first name before connecting.",
  "Calls are disabled once an assignment is completed, withdrawn or the account is suspended.",
  "No customer phone number is ever shown to the courier.",
  "Timeout after 45 s unanswered; clear retry and Report issue options.",
];
