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
> | Secure storage | **Preferences DataStore** for tokens / Android Keystore |
> | Subscriptions | RevenueCat Android SDK + Google Play Billing |
> | Crash reporting | Firebase Crashlytics |
> | Analytics | **Firebase Analytics** |
> | Formatting | **ktfmt via Spotless** |
> | Static analysis | **Detekt + mrmans0n/compose-rules + slackhq/compose-lints** |
> | Beta distribution | **Firebase App Distribution** |
> | Push notifications | Firebase Cloud Messaging (FCM) |
> | Camera | CameraX |

> **Data management pattern:** TBD — owner will demonstrate the canonical pattern before Phase 0 data work begins. All `Data` tasks in this checklist should be held until that pattern is established and then implemented consistently.

---

## Phase 0 — Pre-development Foundations
> Prerequisite gates before feature code is written · **Target: v0.1.0**

- [x] **0.0** `Setup` — Version catalog (`libs.versions.toml`) — pin versions and declare aliases for every library in the stack above: Koin 4, Ktor Client 3 (CIO engine, content-negotiation, auth, logging), kotlinx.serialization, Jetpack Navigation 3, Room + KSP, Preferences DataStore, Coil 3, CameraX, Firebase BOM (Crashlytics, Analytics, App Distribution, FCM), RevenueCat, Material 3 Expressive, Compose BOM; add KSP plugin alias
- [ ] **0.1** `Design` — ~~Receive and review brand artwork~~ **Artwork received and analysed.** Assets confirmed: (a) **App icon** — chef-dad head on radial-gradient green; two variants (with translucent "D" letterform and clean — use clean for production icon); (b) **Logotype** — "Chomp Squad" italic display type on transparent background, layered dark-green shadow → white inline stroke → light-green fill; (c) **Squad illustration** — dad + two daughters on transparent/black background for splash compositing. Remaining action: confirm mascot safe zones, minimum display size, and whether the "D" letterform variant has an intended use case.
- [x] **0.2** `Design` — Define Compose design system — spacing tokens, corner radius (12dp cards, 8dp pills), semantic color mapping using M3 Expressive color roles; select Material Symbols icon set. **Confirmed colour tokens from brand artwork + iOS screenshots:** seed `MaterialTheme` with: `colorPrimary` = `#4AA448` (gradient midpoint of app icon radial gradient `#72C472`→`#2C7A2C`); `colorSecondary` = `#6BBF6B` (logotype fill, on-dark accent); `colorSecondaryContainer` = `#D4EDDA` (tag chips, recipe placeholder tile backgrounds); `colorBackground`/`colorSurface` = `#F2F2F7`; `colorTertiary` = amber (dev/debug UI only — developer badge, Environment row). Brand pink `#E84A8A` (squad illustration accent) noted for future promo/marketing use — not a primary UI colour. Semantic roles: `colorPrimary` = brand/action; `colorError` = destructive (Sign Out, Delete Account); `colorTertiary` = debug-only. **Logotype:** use as an image asset (`logotype.svg` / `logotype.png`) — do not attempt to replicate the layered stroke effect in Compose `Text`. **Body-title layout rule:** Scan, Planner, and Profile root-tab screens use NO `TopAppBar`. Recipe Detail uses floating `IconButton` overlays. Only pushed destinations (Settings, Notifications, etc.) use a standard `TopAppBar` with a back `NavigationIcon`.
- [x] **0.3** `Setup` — Configure Android project baseline — verify min SDK 24, target/compile SDK 36, enable KSP, wire Koin 4 (`KoinApplication` in `Application` class, single `startKoin` call), add `kotlin.serialization` Gradle plugin, confirm Compose compiler via Kotlin 2.x compose plugin
- [x] **0.4** `Setup` — Configure Google Play Developer account — application ID `com.bluelampcreative.chompsquad`, signing config, declare permissions (camera, read media images, internet, post notifications)
- [x] **0.5** `Setup` — Integrate RevenueCat SDK — configure products, entitlements, and developer user with pro-tier override; expose `Purchases` instance via Koin singleton
- [x] **0.6** `Data` — Define canonical `Recipe` data model — all schema fields with `@Serializable` (kotlinx.serialization); Room `@Entity` + `@Dao`; separate API DTO ↔ domain model mapping; register DAO in Koin module *(hold implementation of mapping layer until data management pattern is demonstrated)*
- [x] **0.6b** `Setup` — Code formatting and static analysis — wire three tools into the Gradle build:
  - **ktfmt via Spotless** — add `com.diffplug.spotless` Gradle plugin; configure `kotlin { ktfmt() }` block; `./gradlew spotlessApply` formats all `.kt` files; `./gradlew spotlessCheck` is a non-formatting gate (used in CI). No `.editorconfig` ktfmt style overrides — zero-config is the point.
  - **Detekt** — add `io.gitlab.arturbosch.detekt` plugin; create `config/detekt/detekt.yml`; add `io.nlopez.compose.rules:detekt` dependency for `@Composable` static-analysis rules (unstable parameters, missing `key` in `LazyColumn`, incorrect `remember` usage, etc.); runs as part of `./gradlew check`.
  - **slackhq/compose-lints** — add `com.slack.lint.compose:compose-lint-checks` as a `lintChecks` dependency; extends Android Lint with Compose-aware rules; runs as part of `./gradlew lint`.
  - Add `spotlessCheck` and `detekt` to the `check` task dependency graph so `./gradlew check` enforces all three.
  - *Future hook-up:* Phase 5.3 CI workflow should run `./gradlew spotlessCheck lint detekt` as a pre-merge gate.

---

## Phase 1 — Authentication & Account
> User identity, onboarding, and subscription scaffolding · **Target: v0.1.0**

- [x] **1.1** `UI` — Onboarding flow — mascot-driven welcome screens with M3 Expressive motion and brand green + golden yellow palette, value proposition, sign up / sign in CTA; wired into Navigation 3 start destination
- [x] **1.2** `Auth` — Sign in with Google — `POST /v1/auth/google` confirmed in updated schema. Request: `GoogleAuthRequest { id_token: String }` (note: `id_token`, not `identity_token`). Response: `TokenResponse`. Use Credential Manager API to obtain Google ID token; `POST` via Ktor; store token pair in Preferences DataStore (task 1.4).
- [x] **1.3** `Auth` — Email/password auth — registration, login, forgot password, email verification flow via Ktor; all endpoints under `/v1/auth/*`
- [x] **1.4** `Data` — JWT token management — store access + refresh tokens in **Preferences DataStore** (two string keys; encrypted at rest via Android Keystore — no protobuf schema needed). Refresh via `POST /v1/auth/refresh` with `{ refresh_token }` — server performs **token rotation** (new pair issued, old refresh token invalidated). Ktor `BearerTokens` auth plugin handles silent refresh; on 401 attempt one refresh then navigate to Login. Logout via `POST /v1/auth/logout` with `{ refresh_token }` → 204 (adds JTI to server blocklist); clear DataStore on success.
- [x] **1.5** `UI` — User profile screen — body-title layout (`"Profile"` as first content composable, no `TopAppBar`). Data from `GET /v1/users/me` → `UserProfile` schema: `id`, `email`, `display_name`, `avatar_url` (signed URL — Coil handles), `subscription_tier`, `scans_used_this_month`, `scans_remaining` (null = unlimited), `beta_expires_at`, `created_at`. Three stacked content areas: (1) **Identity card** — circular `AsyncImage(avatar_url)` clipped to circle, green camera-badge `IconButton` overlay → `PUT /v1/users/me/avatar` (multipart); bold `display_name`; gray `email` (read-only — `UpdateProfileRequest` only accepts `display_name`); conditional dark-pill `"Developer"` `SuggestionChip` when `subscription_tier == "developer"` or `BuildConfig.DEBUG`. (2) **Stats card** — `Row` + `VerticalDivider`; left: `scans_used_this_month` + label; right: display `∞` in `colorPrimary` when `scans_remaining == null`, else integer count. (3) **Settings rows** — three `ElevatedCard`s with single `ListItem` + chevron: `"Developer Settings"` (amber, `BuildConfig.DEBUG` only), `"Send Feedback"` → `POST /v1/feedback`, `"Settings"`.
- [x] **1.6** `Subscriptions` — Subscription entitlement wiring — RevenueCat entitlement check on launch via Koin-injected `Purchases`; gate pro features; developer tier bypass
- [x] **1.7** `Subscriptions` — Paywall screen — free vs. pro comparison with M3 Expressive layout using brand green + yellow palette, monthly/annual toggle, Google Play Billing purchase flow via RevenueCat; list "Ad-free experience" as a Pro benefit
- [x] **1.8** `UI` — Settings screen — pushed destination; `TopAppBar` with back `NavigationIcon` (only confirmed screen requiring one alongside Profile sub-navigations). `LazyColumn` with five `ElevatedCard` groups, each with a gray section-label header composable above it and `HorizontalDivider`s between rows: **Account** (`ListItem` read-only rows for Email + Display Name with trailing value text in gray; Edit Profile with chevron); **Subscription** (Upgrade to Pro with chevron; Manage Billing & Subscription as `colorPrimary` tinted `ListItem`; Restore Purchases as `colorPrimary` tinted — Android: `BillingClient` acknowledgement); **Preferences** (Notifications with chevron); **Support** (Contact Support, Privacy Policy, Terms of Service — all `colorPrimary` tinted, open external URLs); **Danger Zone** (Sign Out + Delete Account — `colorError` text, no icons, `AlertDialog` confirmation). Footer `ElevatedCard` (non-interactive): `BuildConfig.VERSION_NAME` + `BuildConfig.VERSION_CODE` right-aligned; `Environment` row in `colorTertiary` (amber) for non-production builds only.

---

## Phase 2 — Recipe Scanner
> Core AI scan flow — the primary value proposition · **Target: v0.1.0**

- [x] **2.1** `UI` — Camera capture screen — single and multi-image capture (up to 5 pages) via CameraX; Photo Picker API for library selection; M3 Expressive FAB for shutter in brand green
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

- [ ] **3.1** `UI` — Catalog / All Recipes screen — `GET /v1/recipes` params: `page` (int, default 1), `page_size` (1–100, default 20), `tag` (string, optional), `search` (string, optional) → `RecipeListResponse { items: RecipeListItem[], total, page, page_size }`. `RecipeListItem` fields: `id` (UUID), `origin_type` ("scanned"|"manual"|"social_save"), `title`, `tags: string[]`, `hero_image_url` (signed URL | null), `total_time` (minutes | null), `created_at`. List row: `AsyncImage(hero_image_url)` with placeholder green tile (Coil); if URL returns 403, refresh via `POST /v1/images/refresh-url { blob_path }`. Grid/list toggle, tag `FilterChip` row, `SearchBar` all drive query params — debounce search input before firing. Results cached in Room; follow data management pattern.
- [ ] **3.2** `UI` — Recipe detail screen — `GET /v1/recipes/{recipe_id}` → `RecipeSchema`. Confirmed fields: `id`, `origin_type`, `title`, `yield_amount` (string|null), `yield_unit` (string|null), `prep_time` (min|null), `cook_time` (min|null), `total_time` (min|null), `source` (attribution string|null), `tags: string[]`, `images: RecipeImageSchema[]` (max 5, each has `id`, `blob_path`, `url` signed, `position`), `ingredients: IngredientSchema[]` (each: `id`, `position`, `quantity`|null, `unit`|null, `name`, `prep_note`|null), `steps: StepSchema[]` (each: `id`, `position`, `instruction`). **Q5 resolved — `images` is an array → `HorizontalPager` confirmed.** Pushed destination; **no `TopAppBar`**. `Box` with floating `IconButton` overlays in white rounded containers: back `<` top-left; ❤️ Favorite (pending D4 clarification) + 🔖 Add to Cookbook (pending D3) + ⋯ `DropdownMenu` top-right. `LazyColumn`: `headlineLarge` title → `HorizontalPager(images)` with `AsyncImage` (edge-to-edge, handle 403 → `POST /v1/images/refresh-url`) + `HorizontalPagerIndicator` → `source` attribution row (link icon + text, gray) → star rating row (pending D2 for `personal_rating`) → 3-column stats `ElevatedCard`s (cook_time / total_time / yield_amount+yield_unit with green icons) → **Tags** `FlowRow` of `SuggestionChip`s → **Ingredients** card (bullet + `name` bold + `prep_note` gray + `quantity`+`unit` right-aligned, `HorizontalDivider` between rows) → **Steps** numbered `instruction` rows. Sub-lists are non-lazy columns inside the outer `LazyColumn`.
- [ ] **3.3** `UI` — Search — `SearchBar` (M3 Expressive) with full-text search across title, ingredients, tags; queries Room locally, falls back to server
- [ ] **3.4** `UI` — Filter & tag browsing — `FilterChip` rows in M3 Expressive style using brand green tint bg + green dark text; cuisine / meal type / dietary tags
- [ ] **3.5** `UI` — Edit saved recipe — modal bottom sheet from detail toolbar menu; `PATCH /v1/recipes/{id}` via Ktor; updates Room on success *(follow data management pattern)*
- [ ] **3.6** `Data` — Delete recipe — `AlertDialog` confirmation; `DELETE /v1/recipes/{id}` via Ktor; remove from Room after server confirms; error surfaced inline *(follow data management pattern)*
- [ ] **3.7** `UI` — Empty states — distinct Composables for zero-recipe state vs. no search/filter results; CTA deep-links to Scan tab via Navigation 3
- [ ] **3.8** `UI` — Bottom navigation — 4-tab `NavigationBar` (M3 Expressive); tabs: Catalog, Scan, Planner, Profile; wired to Navigation 3 `NavController`; brand green active indicator
- [ ] **3.9** `Assets` — App icon & launch screen — **Adaptive icon:** foreground layer = clean chef-dad head asset (no "D" overlay); background layer = `#4AA448` solid (or best-effort radial gradient via `<gradient>` drawable if tooling supports it; test on Pixel launcher). Generate all density buckets (`mdpi`/`hdpi`/`xhdpi`/`xxhdpi`/`xxxhdpi`) + `anydpi-v26` adaptive XML with `ic_launcher_foreground.xml` + `ic_launcher_background.xml`. **Splash screen:** Android 12+ `SplashScreen` API — set `windowSplashScreenBackground = #4AA448`; `windowSplashScreenAnimatedIcon` = squad illustration (dad + daughters) composited with logotype image below. For API < 31 devices: dedicated `SplashActivity` with full-bleed green `ConstraintLayout`, centred `ImageView` (squad illustration), `ImageView` (logotype), auto-finish after 1.5 s (or on data ready). **Debug vs release icon:** `ic_launcher` (production, clean chef-dad head) used in release builds; `ic_launcher_debug` (chef-dad head with translucent "D" letterform overlay) used in debug builds. Wire via `app/src/debug/res/` override — place the debug adaptive icon XML + drawable in `src/debug/` so Gradle automatically swaps it without any `BuildConfig` branching in code.

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
- [ ] **4.8** `Subscriptions` — RevenueCat end-to-end validation — the subscription flow has been implemented with a debug bypass (always grants Pro) and has not yet been exercised against a real RevenueCat project. Before shipping, this must be validated in a real environment: (1) add `revenuecat.api.key.android` to `local.properties` pointing at the RevenueCat **sandbox** project; (2) confirm that Play Store products (monthly + annual) are created in Google Play Console and linked in the RevenueCat dashboard with matching entitlement IDs (`"pro"`); (3) add a Google Play sandbox tester account and verify the purchase sheet launches and completes; (4) verify `entitlementStatus` updates to `hasPro = true` after purchase and persists across app restarts; (5) verify Restore Purchases correctly reinstates entitlement for the test account; (6) verify the RevenueCat → backend webhook (`POST /v1/webhooks/revenuecat`) fires and the backend reflects the subscription tier change on `GET /v1/users/me`; (7) test the cancellation / expiry path so `hasPro` returns to `false`.

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

- [ ] **6.1** `Data` — Meal plan data models — `MealPlanResponse` DTO: `id` (UUID), `week_start` (date), `created_at`, `updated_at`, `slots: MealSlotSchema[]`. `MealSlotSchema`: `id` (UUID), `day_offset` (0–6), `meal_type` ("breakfast"|"lunch"|"dinner"|"snack"|"dessert"), `position` (int), `recipe_id` (UUID|null), `recipe_title` (string). `UpsertSlotRequest`: `day_offset` (0–6, required), `meal_type` (enum, required), `position` (default 0), `recipe_id` (UUID|null), `recipe_title` (string, required). **`day_offset` mapping (✅ Q10 resolved): 0 = Sunday, 1 = Monday, …, 6 = Saturday.** `week_start` is always the Sunday that opens the week; calculate as `LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))`. Room `@Entity` for `MealSlot` stores `weekStart: LocalDate`, `dayOffset: Int`, and all slot fields; derive the actual display date as `weekStart.plusDays(dayOffset.toLong())`. `MealType` enum. Note: **API uses `week_start` date string as path param** for GET/PUT/DELETE slots, but **`plan_id` UUID** for shopping list — store both in Room. Register DAOs in Koin module. *(Follow data management pattern.)*
- [ ] **6.2** `Data` — Meal plan repository + API — CRUD via Ktor to `/v1/meal-plans/*`; optimistic local Room write + background server sync; expose as `Flow` from repository *(follow data management pattern)*
- [ ] **6.3** `UI` — Planner screen (tab root) — body-title layout (`"Meal Planner"` + `"This Week"` + date range string, no `TopAppBar`). Top-right shopping cart: `IconButton` in a `Box` overlay navigates to Shopping List (6.5). **Not a grid** — `LazyColumn` with 7 day sections each preceded by a `stickyHeader` composable: day label `Text` + conditional `SuggestionChip("Today")` in `colorPrimary` (compare section date to `LocalDate.now()`) + conditional `IconButton(Icons.Outlined.AddCircleOutline)` right-aligned for empty/future days; today's day label also rendered in `colorPrimary`. Day sections separated by `HorizontalDivider`. Meal slot rows: `ListItem` with `leadingContent` = meal-type `Icon` (moon = Dinner, fork/knife = Snack/other), `overlineContent` = meal-type label, `headlineContent` = recipe name, `trailingContent` = chevron (tappable rows navigate to Recipe Detail). `collectAsStateWithLifecycle` on planner `Flow`; sync-on-resume via `LifecycleEventEffect`.
- [ ] **6.4** `UI` — Recipe picker — `ModalBottomSheet` (M3 Expressive) to assign a recipe to a slot; `SearchBar` + filter chips from cookbook; "Scan New" shortcut navigates via Navigation 3; "Add Manually" shortcut
- [ ] **6.5** `UI / Data` — Shopping list — `POST /v1/meal-plans/{plan_id}/shopping-list` (note: uses `plan_id` UUID, not `week_start`) → `ShoppingListResponse { plan_id, generated_at, items: ShoppingListItem[] }`. `ShoppingListItem`: `id` (UUID), `name`, `category` ("produce"|"dairy"|"meat"|"seafood"|"bakery"|"pantry"|"spices"|"frozen"|"beverages"|"other"|null), `notes` (string|null). **API has no `checked` field — per-item checked state is client-side only**, persisted in a local Room `ShoppingListItemEntity` with a `isChecked: Boolean` column keyed on item `id`. "Regenerate" clears the local snapshot and re-POSTs. *(Follow data management pattern.)*

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

- [x] **8.1** `Backend` — ✅ Confirmed in updated schema. `RecipeSchema` and `RecipeListItem` now include `personal_rating: Int?`, `personal_note: String?`, `is_favorited: Boolean (default false)`. `UpdateRecipeRequest` accepts all three: `personal_rating` (1–5 inclusive), `personal_note`, `is_favorited`. Tasks 8.2–8.5 and the Favorites tab (9.3) are all unblocked.
- [ ] **8.2** `Data` — Android data model — confirmed API field names: `origin_type` ("scanned"|"manual"|"social_save"), `personal_rating: Int?` (1–5), `personal_note: String?`, `is_favorited: Boolean`. Add all four to `Recipe` Room `@Entity` and `@Serializable` DTO (snake_case → camelCase via `@SerialName`); write Room migration; set `originType` at creation: `"scanned"` in scan flow, `"manual"` in manual entry. *(Follow data management pattern.)*
- [ ] **8.3** `UI` — Recipe origin badge — source attribution on cards and detail header: scanned + source → source icon + name; manual / no source → authorship icon + "My Recipe"; muted `labelSmall` treatment below title; fully client-side
- [ ] **8.4** `UI / Data` — Personal star rating + Favorites toggle — (a) **Star rating:** 5-star composable (amber) in recipe detail and small star row on list cards; `personal_rating` range 1–5 (null = unrated); debounced `PATCH /v1/recipes/{id} { personal_rating }` via Ktor. (b) **Favorites toggle:** ❤️ `IconButton` in Recipe Detail floating actions (filled `colorError` when `is_favorited == true`); `PATCH /v1/recipes/{id} { is_favorited: true/false }`; optimistic local update in Room. Favorites tab in Catalog simply shows Room-cached recipes where `isFavorited == true` — no separate API call. *(Follow data management pattern.)*
- [ ] **8.5** `UI / Data` — Personal notes — "My Notes" section in recipe detail; inline `TextField` (M3 Expressive) with keyboard-aware `WindowInsets` layout; `debounce(500ms)` auto-save via `PATCH /v1/recipes/{id}`; character count indicator *(follow data management pattern)*

---

## Phase 9 — Cookbooks & Collections (v1.0.0)
> Named recipe collections for personal organisation · **Target: v1.0.0**

> `/v1/cookbooks/*` endpoints are live — unblock immediately.

- [x] **9.1** `Backend` — ✅ Fully confirmed. Cookbook endpoints: `GET /v1/cookbooks`; `POST /v1/cookbooks` (`{ name }`) → 201; `PATCH /v1/cookbooks/{id}` (`{ name }`) → rename only; `DELETE /v1/cookbooks/{id}` → 204; `PUT /v1/cookbooks/{id}/recipes` (`{ recipe_id }`) → add recipe; `DELETE /v1/cookbooks/{id}/recipes/{recipe_id}` → 204; `PUT /v1/cookbooks/{id}/cover-image` (multipart `file`) → upload/replace cover; `DELETE /v1/cookbooks/{id}/cover-image` → remove cover. `CookbookResponse`: `{ id, name, cover_image_url: String?, recipe_ids: UUID[], created_at, updated_at }`. Q12 ✅ fully resolved. **Favorites is `is_favorited` on Recipe** — resolved by 8.4.
- [ ] **9.2** `Data` — Android cookbook data models — `CookbookResponse` DTO: `{ id: UUID, name: String, cover_image_url: String?, recipe_ids: List<UUID>, created_at, updated_at }`. Room `@Entity`: `CookbookEntity(id, name, coverImageUrl, createdAt, updatedAt)` + `CookbookRecipeCrossRef(cookbookId, recipeId)` join table. `@Serializable` DTOs with `@SerialName` for snake_case fields. Repository exposes `Flow<List<Cookbook>>` (Koin-provided). Register DAOs in Koin module; bump Room migration version. *(Follow data management pattern.)*
- [ ] **9.3** `UI` — Catalog view restructure — rename bottom tab "Cookbook" → "Catalog"; add `SingleChoiceSegmentedButtonRow` (M3 Expressive): **All Recipes · Cookbooks · Favorites**. Favorites tab is a client-side filtered view of the Room recipe cache (`WHERE is_favorited = 1`) — no extra API call. All Recipes retains existing behaviour. Context-sensitive trailing button: grid-toggle icon on All Recipes + Favorites tabs; green `+` `IconButton` on Cookbooks tab.
- [ ] **9.4** `UI` — Cookbook shelf view — `LazyVerticalGrid` of cookbook cards: title, recipe count, Coil cover image; "New Cookbook" FAB; tap opens cookbook recipe list via Navigation 3
- [ ] **9.5** `UI` — Add to Cookbook — 🔖 icon in Recipe Detail floating actions + long-press context menu on recipe cards → "Add to Cookbook…" `ModalBottomSheet`. Sheet shows cookbook list from Room (names + membership state derived from `recipe_ids`). Tap to toggle: add = `PUT /v1/cookbooks/{id}/recipes { recipe_id }`; remove = `DELETE /v1/cookbooks/{id}/recipes/{recipe_id}`. Inline "New Cookbook" row at bottom of sheet → `POST /v1/cookbooks { name }` → auto-adds recipe. All Ktor calls via Koin-injected repository; optimistic Room update.
- [ ] **9.6** `UI` — Cookbook management — Rename: `PATCH /v1/cookbooks/{id} { name }` (1–100 chars, name-only — cover is a separate endpoint); Delete: `DELETE /v1/cookbooks/{id}` → 204, then purge from Room; swipe-to-dismiss on recipe row = `DELETE /v1/cookbooks/{id}/recipes/{recipe_id}`; delete `AlertDialog` warns recipes remain in Catalog. **Cover photo (✅ Q12 fully resolved):** `cover_image_url: String?` in `CookbookResponse`. Show brand green `MenuBook` placeholder when `null`. Tap cover → Photo Picker → compress → `PUT /v1/cookbooks/{id}/cover-image` (multipart `file`) → response is updated `CookbookResponse`; update Room. Remove cover: `DELETE /v1/cookbooks/{id}/cover-image` → updated `CookbookResponse`. Display with `AsyncImage` (Coil) using `placeholder` + `error` fallback to placeholder vector.

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
| Q1 | ~~Min SDK 24 vs 29?~~ | ✅ Resolved — **minSdk = 29** (Android 10). Updated in `app/build.gradle.kts`. |
| Q2 | Data management pattern — owner to demonstrate before any `Data`-tagged work begins. | 🟡 Pending demo |
| Q3 | ~~Navigation 3 version to pin?~~ | ✅ Resolved — **Navigation 3 confirmed**. Pinned at `1.0.0-alpha04` in `libs.versions.toml`. Verify latest at developer.android.com/jetpack/androidx/releases/navigation3 before first navigation work. |
| Q4 | ~~Ktor engine: CIO vs OkHttp?~~ | ✅ Resolved — **CIO engine** (`ktor-client-cio`). Pinned in `libs.versions.toml`. |
| Q5 | Recipe Detail: does `GET /v1/recipes/{id}` return a `photos[]` array or single `heroImageUrl`? | ✅ Resolved — `images: RecipeImageSchema[]` array confirmed; `HorizontalPager` is correct. |
| Q6 | ~~`POST /v1/auth/google` missing~~ | ✅ Resolved — `POST /v1/auth/google` with `GoogleAuthRequest { id_token: String }` confirmed in updated schema |
| Q7 | ~~`personal_rating` / `personal_note` absent~~ | ✅ Resolved — both fields plus `is_favorited` confirmed in `RecipeSchema`, `RecipeListItem`, and `UpdateRecipeRequest` |
| Q8 | ~~`/v1/cookbooks/*` absent~~ | ✅ Resolved — full cookbook CRUD confirmed in updated schema |
| Q9 | ~~Favorites mechanism unknown~~ | ✅ Resolved — `is_favorited: Boolean` field on Recipe; Favorites tab is a client-side Room filter (`WHERE is_favorited = 1`); toggled via `PATCH /v1/recipes/{id}` |
| Q10 | ~~`day_offset` 0 = Monday or Sunday?~~ | ✅ Resolved — **0 = Sunday, 6 = Saturday**. `week_start` is always the preceding Sunday. Android: `LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))`. Display date = `weekStart.plusDays(dayOffset.toLong())`. |
| Q11 | **Signed image URL TTL** — how long are `RecipeImageSchema.url` signed URLs valid? Determines Coil refresh strategy (on 403 error only vs. proactive on each launch). | 🟡 Pending — owner to confirm with backend |
| Q12 | ~~Cookbook cover mechanism?~~ | ✅ Fully resolved — `cover_image_url: String?` in `CookbookResponse`. Dedicated endpoints: `PUT /v1/cookbooks/{id}/cover-image` (multipart upload) and `DELETE /v1/cookbooks/{id}/cover-image`. `RenameCookbookRequest` stays name-only. Android: brand green `MenuBook` placeholder when `null`; Coil `AsyncImage` with placeholder + error fallback. |
