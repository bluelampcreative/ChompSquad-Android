# ChompSquad Android — Implementation Checklist

> Updated March 2026 · Tracks every deliverable from ROADMAP.md
>
> **Tech stack overrides** (supersede the ROADMAP reference table where they differ):
>
> | Concern | Stack |
> |---|---|
> | UI framework | Jetpack Compose |
> | Design system | **Material 3 Expressive** |
> | Navigation | **Jetpack Navigation 3** |
> | Dependency injection | **Koin 4** |
> | Network | **Ktor Client 3** |
> | Serialization | **kotlinx.serialization** |
> | Local database | Room |
> | Image loading | Coil |
> | Async | Kotlin Coroutines + Flow |
> | Secure storage | **DataStore** (Proto DataStore for tokens) / Android Keystore |
> | Subscriptions | RevenueCat Android SDK + Google Play Billing |
> | Crash reporting | Firebase Crashlytics |
> | Analytics | **Firebase Analytics** |
> | Beta distribution | **Firebase App Distribution** |
> | Push notifications | Firebase Cloud Messaging (FCM) |
> | Camera | CameraX |

> **Data management pattern:** TBD — owner will demonstrate the canonical pattern before Phase 0 data work begins. All `Data` tasks in this checklist should be held until that pattern is established and then implemented consistently.

---

## Phase 0 — Pre-development Foundations
> Prerequisite gates before feature code is written · **Target: v0.1.0**

- [ ] **0.0** `Setup` — Version catalog (`libs.versions.toml`) — pin versions and declare aliases for every library in the stack above: Koin 4, Ktor Client 3 (CIO engine, content-negotiation, auth, logging), kotlinx.serialization, Jetpack Navigation 3, Room + KSP, DataStore (Proto), Coil 3, CameraX, Firebase BOM (Crashlytics, Analytics, App Distribution, FCM), RevenueCat, Material 3 Expressive, Compose BOM; add KSP plugin alias
- [ ] **0.1** `Design` — Receive and review brand artwork — finalize color palette, typography choice (Nunito / Fredoka for display), mascot usage guidelines
- [ ] **0.2** `Design` — Define Compose design system — spacing tokens, corner radius (12dp cards, 8dp pills), semantic color mapping using M3 Expressive color roles; select Material Symbols icon set
- [ ] **0.3** `Setup` — Configure Android project baseline — verify min SDK 24, target/compile SDK 36, enable KSP, wire Koin 4 (`KoinApplication` in `Application` class, single `startKoin` call), add `kotlin.serialization` Gradle plugin, confirm Compose compiler via Kotlin 2.x compose plugin
- [ ] **0.4** `Setup` — Configure Google Play Developer account — application ID `com.bluelampcreative.chompsquad`, signing config, declare permissions (camera, read media images, internet, post notifications)
- [ ] **0.5** `Setup` — Integrate RevenueCat SDK — configure products, entitlements, and developer user with pro-tier override; expose `Purchases` instance via Koin singleton
- [ ] **0.6** `Data` — Define canonical `Recipe` data model — all schema fields with `@Serializable` (kotlinx.serialization); Room `@Entity` + `@Dao`; separate API DTO ↔ domain model mapping; register DAO in Koin module *(hold implementation of mapping layer until data management pattern is demonstrated)*

---

## Phase 1 — Authentication & Account
> User identity, onboarding, and subscription scaffolding · **Target: v0.1.0**

- [ ] **1.1** `UI` — Onboarding flow — mascot-driven welcome screens with M3 Expressive motion and brand green + golden yellow palette, value proposition, sign up / sign in CTA; wired into Navigation 3 start destination
- [ ] **1.2** `Auth` — Sign in with Google — Credential Manager API to obtain Google ID token; `POST /v1/auth/google` via Ktor; backend handles new vs. returning users; reachable from both Login and Register screens
- [ ] **1.3** `Auth` — Email/password auth — registration, login, forgot password, email verification flow via Ktor; all endpoints under `/v1/auth/*`
- [ ] **1.4** `Data` — JWT token management — store access + refresh tokens in Proto DataStore (encrypted at rest via Android Keystore); Ktor `BearerTokens` auth plugin reads/writes the DataStore for silent refresh; session expiry triggers navigation to Login
- [ ] **1.5** `UI` — User profile screen — display name, email, account tier badge (free / pro in golden yellow pill), monthly scan counter; data sourced from `GET /v1/users/me` via Ktor
- [ ] **1.6** `Subscriptions` — Subscription entitlement wiring — RevenueCat entitlement check on launch via Koin-injected `Purchases`; gate pro features; developer tier bypass
- [ ] **1.7** `Subscriptions` — Paywall screen — free vs. pro comparison with M3 Expressive layout using brand green + yellow palette, monthly/annual toggle, Google Play Billing purchase flow via RevenueCat; list "Ad-free experience" as a Pro benefit
- [ ] **1.8** `UI` — Settings screen — account management, sign out, delete account, restore purchases; destructive actions use M3 Expressive error color role

---

## Phase 2 — Recipe Scanner
> Core AI scan flow — the primary value proposition · **Target: v0.1.0**

- [ ] **2.1** `UI` — Camera capture screen — single and multi-image capture (up to 5 pages) via CameraX; Photo Picker API for library selection; M3 Expressive FAB for shutter in brand green
- [ ] **2.2** `Data` — Image pre-processing — resize to 2048 px, JPEG 0.78 quality, 2 MB hard cap; CPU-bound work on `Dispatchers.Default`; pure Kotlin / Android Bitmap — no networking dependency
- [ ] **2.3** `UI` — Scan submission & loading state — multipart `POST` to backend via Ktor `submitFormWithBinaryData`; per-page loading message; 45 s socket timeout configured on `HttpClient`; 1 auto-retry on 504 via Ktor `HttpRequestRetry` plugin
- [ ] **2.4** `UI` — Scan result review screen — extracted recipe rendered in canonical schema; field-level inline editing before saving; M3 Expressive card / text-field styles
- [ ] **2.5** `UI` — Ingredient list editor — add, remove, reorder (drag handle via `LazyColumn` reorder); quantity / unit / prep-note fields; 48 dp min row height
- [ ] **2.6** `UI` — Steps editor — add, remove, reorder numbered instruction steps with drag-to-reorder; same reorder pattern as 2.5
- [ ] **2.7** `Data` — Save recipe — `POST /v1/recipes` via Ktor; write-through to local Room database; haptic + M3 Expressive spring animation confirmation *(follow data management pattern)*
- [ ] **2.8** `Subscriptions` — Scan cap enforcement — client-side check via RevenueCat entitlement + 402/403 server response; remaining scan indicator in UI; upgrade prompt routes to Paywall (1.7)
- [ ] **2.9** `UI` — Manual recipe entry — fallback flow to create a recipe from scratch without scanning; reuses editors from 2.4 – 2.6

---

## Phase 3 — Cookbook & Collection
> Browse, search, and view the personal recipe library · **Target: v0.1.0**

- [ ] **3.1** `UI` — Cookbook home screen — recipe grid/list toggle; hero photo via Coil; tag filter chips; full two-way API sync with pagination (`GET /v1/recipes?page=…`) via Ktor; results cached in Room *(follow data management pattern)*
- [ ] **3.2** `UI` — Recipe detail screen — full recipe display; large title; M3 Expressive typography hierarchy; all canonical fields rendered
- [ ] **3.3** `UI` — Search — `SearchBar` (M3 Expressive) with full-text search across title, ingredients, tags; queries Room locally, falls back to server
- [ ] **3.4** `UI` — Filter & tag browsing — `FilterChip` rows in M3 Expressive style using brand green tint bg + green dark text; cuisine / meal type / dietary tags
- [ ] **3.5** `UI` — Edit saved recipe — modal bottom sheet from detail toolbar menu; `PATCH /v1/recipes/{id}` via Ktor; updates Room on success *(follow data management pattern)*
- [ ] **3.6** `Data` — Delete recipe — `AlertDialog` confirmation; `DELETE /v1/recipes/{id}` via Ktor; remove from Room after server confirms; error surfaced inline *(follow data management pattern)*
- [ ] **3.7** `UI` — Empty states — distinct Composables for zero-recipe state vs. no search/filter results; CTA deep-links to Scan tab via Navigation 3
- [ ] **3.8** `UI` — Bottom navigation — 4-tab `NavigationBar` (M3 Expressive); tabs: Catalog, Scan, Planner, Profile; wired to Navigation 3 `NavController`; brand green active indicator
- [ ] **3.9** `Assets` — App icon & launch screen — adaptive icon from final artwork; all density buckets; mascot on brand green `SplashScreen` API screen

---

## Phase 4 — Polish & Pre-release Hardening
> Refinement, hero photo AI, and pre-launch hardening · **Target: v0.1.0 (4.3–4.7) · v1.0.0 (4.1–4.2)**

- [ ] **4.1** `AI / Pro` — Hero photo detection — surface backend-detected food photo as recipe hero image with crop/confirm UI; `PATCH /v1/recipes/{id}` via Ktor *(target v1.0.0)*
- [ ] **4.2** `AI / Pro` — AI-generated hero photo — Pro feature: trigger AI image generation via backend; display result with confirm/regenerate UI *(target v1.0.0)*
- [ ] **4.3** `Polish` — Haptics & micro-interactions — `HapticFeedback` throughout: scan ready, save confirmed, errors, deletions; M3 Expressive spring physics on key state transitions
- [ ] **4.4** `Polish` — Accessibility audit — large font sizes; `contentDescription` on all icon-only elements; 48 dp minimum touch targets; WCAG AA contrast check against M3 Expressive color tokens
- [ ] **4.5** `Polish` — Dark mode audit — all screens verified in dark mode; zero hardcoded colors; all tones sourced from M3 Expressive `MaterialTheme.colorScheme`
- [ ] **4.6** `Data` — Offline handling — graceful degradation; Room reads succeed offline; pending-write queue flushed when connectivity resumes (observe `ConnectivityManager` NetworkCallback) *(follow data management pattern)*
- [ ] **4.7** `Polish` — Error handling audit — all Ktor `ResponseException` and network failures mapped to user-facing messages; M3 Expressive `Snackbar` / `AlertDialog` with actionable copy for auth errors, scan errors, and API failures

---

## Phase 5 — Firebase Beta Distribution (v0.1.0)
> Beta infrastructure — feedback collection, crash visibility, and automated delivery · **Target: v0.1.0**

- [ ] **5.1** `UI / Beta` — In-app feedback tool — accessible under Profile tab; `ModalBottomSheet` (M3 Expressive) with categories (General, Bug Report, Feature Request), free-text comment, optional screenshot attachment; submits to `/v1/feedback`; first-time badge on tab icon + dismissible announcement `Banner`
- [ ] **5.2** `Setup` — Firebase Crashlytics + Analytics — add `google-services.json`; initialize Firebase via BOM; Crashlytics auto-collects crashes; enrich reports with `setUserId`, subscription tier custom key, and breadcrumb `log` calls for network errors and API contract mismatches; log key conversion and scan events to Firebase Analytics
- [ ] **5.3** `Setup` — CI/CD pipeline — GitHub Actions workflow: build release APK/AAB + upload to **Firebase App Distribution** on merge to `develop`; auto-increment `versionCode`; post-release commit bumps `versionName` and pushes back to `develop`; use `w9jds/firebase-action` or `FirebaseExtended/action-hosting-deploy` (App Distribution equivalent)
- [ ] **5.4** `Launch` — Firebase App Distribution beta — configure tester groups in Firebase console; distribute builds to initial beta cohort via App Distribution invite links; establish feedback cadence aligned with 5.1 in-app form

---

## Phase 6 — Meal Planner (v1.0.0)
> Weekly meal planning with recipe assignment and AI-generated shopping lists · **Target: v1.0.0**

- [ ] **6.1** `Data` — Meal plan data models — `MealPlan` + `MealSlot` Room `@Entity` classes; `MealType` enum; `@Serializable` API DTOs; register DAOs in Koin module *(follow data management pattern)*
- [ ] **6.2** `Data` — Meal plan repository + API — CRUD via Ktor to `/v1/meal-plans/*`; optimistic local Room write + background server sync; expose as `Flow` from repository *(follow data management pattern)*
- [ ] **6.3** `UI` — Planner screen (tab root) — weekly grid 7 days × 5 meal slots (Breakfast / Lunch / Dinner / Snack / Dessert); `collectAsStateWithLifecycle` on planner Flow; sync-on-resume via `LifecycleEventEffect`
- [ ] **6.4** `UI` — Recipe picker — `ModalBottomSheet` (M3 Expressive) to assign a recipe to a slot; `SearchBar` + filter chips from cookbook; "Scan New" shortcut navigates via Navigation 3; "Add Manually" shortcut
- [ ] **6.5** `UI / Data` — Shopping list — on-demand `POST /v1/meal-plans/{id}/shopping-list` via Ktor; persisted snapshot in Room; checklist with per-item checked state persisted; "Regenerate" button clears snapshot and re-fetches

---

## Phase 7 — Push Notifications (v1.0.0)
> Local meal-planning and meal-time reminders · **Target: v1.0.0** · Backend dependency: None

- [ ] **7.1** `Setup` — Notification permissions — `POST_NOTIFICATIONS` runtime permission request (Android 13+) on first Settings toggle; handle denied state with deep-link to system Settings via `Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)`
- [ ] **7.2** `Data` — Notification preferences model — `DataStore<Preferences>` backed settings: weekly planning reminder (day-of-week + time); per-meal-type toggles (Breakfast / Lunch / Dinner) each with individual time; expose as `Flow` from a Koin-provided repository
- [ ] **7.3** `UI` — Notifications settings UI — "Notifications" section in Settings screen; weekly planning reminder row; three meal-type rows with `TimePickerDialog` (M3 Expressive); all toggles trigger permission check on first enable
- [ ] **7.4** `Data` — Local notification scheduler — `WorkManager` `PeriodicWorkRequest` for repeating reminders; cancel + reschedule when preferences `Flow` emits; re-evaluate on `ProcessLifecycleOwner` foreground event
- [ ] **7.5** `UI / Data` — Meal plan–aware notification bodies — at trigger time, query Room for `MealSlot` matching meal type; name recipe in notification body if found; fall back to generic copy otherwise

---

## Phase 8 — Personal Notes, Rating & Recipe Origin (v1.0.0)
> Personal annotation layer plus visual source attribution on recipe cards · **Target: v1.0.0**

> Backend already accepts `personal_note`, `personal_rating`, and `origin` — unblock immediately.

- [ ] **8.1** `Backend` — ✅ Already complete
- [ ] **8.2** `Data` — Android data model — add `origin: RecipeOrigin` (`@Serializable` enum), `personalRating: Int?`, `personalNote: String?` to `Recipe` Room entity and API DTO; write Room migration; set `origin` at creation: `.scan` in scan flow, `.manual` in manual entry *(follow data management pattern)*
- [ ] **8.3** `UI` — Recipe origin badge — source attribution on cards and detail header: scanned + source → source icon + name; manual / no source → authorship icon + "My Recipe"; muted `labelSmall` treatment below title; fully client-side
- [ ] **8.4** `UI / Data` — Personal star rating — 5-star `RatingBar`-equivalent composable in recipe detail; tap to set / clear; debounced `PATCH /v1/recipes/{id}` via Ktor; small star row on recipe cards *(follow data management pattern)*
- [ ] **8.5** `UI / Data` — Personal notes — "My Notes" section in recipe detail; inline `TextField` (M3 Expressive) with keyboard-aware `WindowInsets` layout; `debounce(500ms)` auto-save via `PATCH /v1/recipes/{id}`; character count indicator *(follow data management pattern)*

---

## Phase 9 — Cookbooks & Collections (v1.0.0)
> Named recipe collections for personal organisation · **Target: v1.0.0**

> `/v1/cookbooks/*` endpoints are live — unblock immediately.

- [ ] **9.1** `Backend` — ✅ Already complete
- [ ] **9.2** `Data` — Android cookbook data models — `Cookbook` + `CookbookMembership` Room entities; `@Serializable` API DTOs; repository with Ktor client; register DAOs in Koin module; bump Room migration version *(follow data management pattern)*
- [ ] **9.3** `UI` — Catalog view restructure — rename bottom tab "Cookbook" → "Catalog"; add `TabRow` (M3 Expressive): **All Recipes · Cookbooks · Favorites**; Favorites wired to system cookbook; All Recipes retains existing behaviour
- [ ] **9.4** `UI` — Cookbook shelf view — `LazyVerticalGrid` of cookbook cards: title, recipe count, Coil cover image; "New Cookbook" FAB; tap opens cookbook recipe list via Navigation 3
- [ ] **9.5** `UI` — Add to Cookbook — long-press / context menu on recipe card → "Add to Cookbook…" `ModalBottomSheet` with membership checkmarks and inline "New Cookbook" creation; also available in recipe detail toolbar
- [ ] **9.6** `UI` — Cookbook management — rename / delete from detail toolbar; swipe-to-dismiss removes recipe from cookbook; Favorites not deletable; delete `AlertDialog` warns recipes remain in Catalog; cover photo via Photo Picker API

---

## Phase 10 — Play Store Launch (v1.0.0)
> Final hardening and Play Store submission · **Target: v1.0.0**

- [ ] **10.1** `UI` — Drag-to-reorder ingredients and steps — drag handles using `LazyColumn` reorder (Compose reorder library or custom `detectDragGesturesAfterLongPress`); consistent with the editors in 2.5 and 2.6
- [ ] **10.2** `Polish` — Final accessibility + dark mode sign-off — post-beta sweep; confirm all 4.4 / 4.5 items pass for every new Phase 7–9 screen; test with TalkBack and font scale 200%
- [ ] **10.3** `Launch` — Data safety declaration — complete Google Play data safety form; declare: email address, user content (recipes / photos), usage data, device identifiers; include camera and photo library access rationale; note Firebase Analytics and Crashlytics data flows
- [ ] **10.4** `Launch` — Play Store submission — phone + 7" tablet screenshots, feature graphic, short/long descriptions, content rating questionnaire, review notes

---

## Phase 11 — Social & Community (v2.0.0)
> Transform the personal catalog into a social recipe-sharing platform · **Target: v2.0.0**

> ⚠️ **Data migration required.** All existing recipes must default to `private` before social ships.

- [ ] **11.1** `Data / UI` — Recipe privacy controls — public/private `SegmentedButton` (M3 Expressive) per recipe; all new + existing recipes default to `private`; privacy indicator on list cards and detail header *(follow data management pattern)*
- [ ] **11.2** `Social` — Squads — named groups; creator-only invitations; members share recipes and Cookbooks within the Squad; invite via link or username search; Ktor endpoints under `/v1/squads/*`
- [ ] **11.3** `UI / Social` — Homepage feed — new root tab added to Navigation 3 back stack; `LazyColumn` feed of public recipes and Squad activity; "Trending," "From Your Squads," "New Saves" sections; paging via Paging 3 or manual cursor
- [ ] **11.4** `Social` — Recipe sharing — share recipe to Squad or specific user via Ktor; shared recipes appear in recipient feed and optionally their Catalog
- [ ] **11.5** `Social / Data` — Social save — copy any public recipe to personal Catalog; original author attribution preserved and immutable (`origin = .socialSave`); Ktor `POST /v1/recipes/{id}/save` *(follow data management pattern)*
- [ ] **11.6** `Data / UI` — Social attribution — `@username` displayed on all socially saved/shared recipe cards and detail screens; sourced from API; not editable
- [ ] **11.7** `Data` — Data migration — Room database version bump for social fields (`isPublic`, `sharedWith`, social attribution); all existing recipes and Cookbooks default to `private` on migration *(follow data management pattern)*
- [ ] **11.8** `UI / Social` — Social profile — public profile screen with display name, avatar (Coil), public recipe count and `LazyVerticalGrid`; accessible by any user via Navigation 3 deep link; editable only by owner
- [ ] **11.9** `UI / Social` — FCM push notifications — FCM device token registration via Ktor `POST /v1/users/me/fcm-token`; handle `RemoteMessage` in `FirebaseMessagingService`; surface Squad invites, direct shares, Squad activity; extend Phase 7 notification preferences toggles in Settings
- [ ] **11.10** `Ads` — Ad integration — Google AdMob native ad cards in Catalog list and social feed; UMP SDK for consent; Koin-provided `AdLoader`; Pro tier entitlement check suppresses all ads

---

## Progress Summary

| Phase | Name | Done | Total | Target |
|---|---|---|---|---|
| 0 | Pre-development foundations | 0 | 7 | v0.1.0 |
| 1 | Authentication & account | 0 | 8 | v0.1.0 |
| 2 | Recipe scanner | 0 | 9 | v0.1.0 |
| 3 | Cookbook & collection | 0 | 9 | v0.1.0 |
| 4 | Polish & pre-release hardening | 0 | 7 | v0.1.0 / v1.0.0 |
| 5 | Firebase beta distribution | 0 | 4 | v0.1.0 |
| 6 | Meal planner | 0 | 5 | v1.0.0 |
| 7 | Push notifications | 0 | 5 | v1.0.0 |
| 8 | Personal notes, rating & recipe origin | 0 | 5 | v1.0.0 |
| 9 | Cookbooks & collections | 0 | 6 | v1.0.0 |
| 10 | Play Store launch | 0 | 4 | v1.0.0 |
| 11 | Social & community | 0 | 10 | v2.0.0 |
| | **Total** | **0** | **79** | |

---

## Open Questions / Decisions Needed

| # | Question | Status |
|---|---|---|
| Q1 | Min SDK: ROADMAP says 29 (Android 10), project is currently set to 24. Confirm target. | 🔴 Needs decision |
| Q2 | Data management pattern — owner to demonstrate before any `Data`-tagged work begins. | 🟡 Pending demo |
| Q3 | Navigation 3 is the preferred library but confirm API stability / version to pin in 0.0. | 🟡 Confirm on setup |
| Q4 | Ktor engine: CIO (pure Kotlin, good for most cases) vs. OkHttp engine (better cert pinning). | 🟡 Confirm on setup |
