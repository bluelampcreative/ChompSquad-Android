# ChompSquad Android — Roadmap

> Updated March 2026 · Confidential · Android parity target

---

## Project Statement

**ChompSquad** is a native Android application that allows users to photograph any physical or on-screen recipe — from handwritten index cards to magazine clippings to websites — and automatically convert it into a structured, consistent format stored in a personal recipe collection. The app leverages a multimodal AI pipeline to extract, normalize, and enrich recipe content with minimal user effort, making it the definitive home for a user's growing recipe library.

As the user base grows, ChompSquad evolves into a social cooking community where users share recipes, form Squads with friends and family, and discover new dishes through a curated public feed — all while preserving the provenance and ownership of every recipe in the system.

---

## Android Technology Stack (Reference)

| Concern | Recommended Technology |
|---|---|
| UI framework | Jetpack Compose |
| Navigation | Navigation Compose |
| Local database | Room (SQLite) |
| Network | Retrofit + OkHttp |
| Image loading | Coil |
| Async | Kotlin Coroutines + Flow |
| Secure storage | Preferences DataStore / Android Keystore |
| Subscriptions | RevenueCat Android SDK + Google Play Billing |
| Crash reporting | Firebase Crashlytics |
| Push notifications | Firebase Cloud Messaging (FCM) |
| Camera | CameraX |
| Dependency injection | Hilt |
| Social auth | Google Sign-In (replaces Sign in with Apple) |
| Beta distribution | Google Play Internal Testing track |

> **Backend:** All features share the same backend as iOS — `https://api.chompsquad.app` (`/v1/` prefix, JWT auth). See `API_INTEGRATION.md` in the iOS repo for the full API contract.

---

## API & Auth Notes

- **Base URL:** `https://api.chompsquad.app/v1/`
- **Auth:** JWT bearer tokens with rotation — access token + refresh token; `POST /v1/auth/refresh` issues a new token pair and invalidates the old refresh token. `POST /v1/auth/logout` adds the refresh token JTI to a blocklist.
- **Social auth:** `POST /v1/auth/apple` (`identity_token`) and `POST /v1/auth/google` (`id_token`) both confirmed in schema. ✅ D1 resolved.
- **Subscription tiers:** `subscription_tier` string in `UserProfile` — values expected: `free` | `beta` | `pro` | `developer`. `scans_remaining: null` = unlimited (pro/developer); integer = remaining count for free tier.
- **RevenueCat:** manages Play Store subscription state; backend notified via `POST /v1/webhooks/revenuecat`.
- **Signed image URLs:** all `url` fields in `RecipeImageSchema` are signed (time-limited). Refresh via `POST /v1/images/refresh-url` with `blob_path`. Android must handle URL expiry in Coil (catch 403, refresh, retry).
- **Account deletion:** `DELETE /v1/auth/account` — soft-delete that anonymises PII; no request body required.

### Confirmed Endpoint Surface (v0.1.0 schema)

| Group | Endpoints |
|---|---|
| Health | `GET /health/live`, `GET /health/ready` |
| Auth | `POST /v1/auth/register`, `/login`, `/refresh`, `/apple`, `/google`, `/logout`; `DELETE /v1/auth/account` |
| Users | `GET/PATCH /v1/users/me`; `PUT/DELETE /v1/users/me/avatar` |
| Scan | `POST /v1/scan` (multipart, 1–5 images, 1 scan credit) |
| Recipes | `GET/POST /v1/recipes`; `GET/PATCH/DELETE /v1/recipes/{id}` |
| Recipe Images | `POST /v1/recipes/{id}/images`; `DELETE /v1/recipes/{id}/images/{img_id}`; `PATCH /v1/recipes/{id}/images/order` |
| Images | `POST /v1/images/upload`; `POST /v1/images/refresh-url` |
| Meal Plans | `GET /v1/meal-plans/{week_start}`; `PUT /v1/meal-plans/{week_start}/slots`; `DELETE /v1/meal-plans/{week_start}/slots/{slot_id}`; `POST /v1/meal-plans/{plan_id}/shopping-list` |
| Cookbooks | `GET/POST /v1/cookbooks`; `PATCH/DELETE /v1/cookbooks/{id}`; `PUT /v1/cookbooks/{id}/recipes`; `DELETE /v1/cookbooks/{id}/recipes/{recipe_id}`; `PUT/DELETE /v1/cookbooks/{id}/cover-image` |
| Feedback | `POST /v1/feedback` |

### ⚠️ API Discrepancies vs Roadmap Assumptions

| # | Discrepancy | Impact |
|---|---|---|
| D1 | ~~`POST /v1/auth/google` absent~~ | ✅ **Resolved** — endpoint confirmed in updated schema (`GoogleAuthRequest { id_token }`) |
| D2 | ~~`personal_rating` + `personal_note` absent~~ | ✅ **Resolved** — both fields now in `RecipeSchema`, `RecipeListItem`, and `UpdateRecipeRequest` (`personal_rating` 1–5 int\|null, `personal_note` string\|null) |
| D3 | ~~`/v1/cookbooks/*` absent~~ | ✅ **Resolved** — full cookbook CRUD confirmed in updated schema |
| D4 | ~~No favorites mechanism~~ | ✅ **Resolved** — `is_favorited: boolean` field on `RecipeSchema` and `UpdateRecipeRequest`. **Note: Favorites is NOT a system cookbook** — it is a direct `PATCH /v1/recipes/{id} { is_favorited: true/false }` toggle. The Favorites tab in the Catalog simply filters `RecipeListItem` by `is_favorited == true`. |
| D5 | Shopping list items have no `checked` field | ⚠️ **Still open** — 6.5 per-item checked state must be client-side only (Room) |

---

## Phase 0 — Pre-development Foundations
> Prerequisite gates before any code is written · **Target: v0.1.0 Play Internal Testing**

- [ ] **0.1** `Design` — ~~Receive and review brand artwork~~ **Artwork received and analysed.** Confirmed assets: (a) app icon — bearded chef-dad head on radial-gradient green background, two versions (with translucent "D" letterform overlay and clean); (b) logotype — "Chomp Squad" italic display type, layered stroke effect (dark green outer shadow → white inline stroke → light-medium green fill), transparent background; (c) squad illustration — bearded dad (green shirt, chef hat, wooden spoon) + two daughters (pink shirts, cooking utensils), transparent/black background for compositing onto splash. Finalize mascot usage guidelines (safe zones, minimum sizes, splash compositing rules).
- [x] **0.2** `Design` — Define Compose design system — spacing tokens, corner radius (12dp cards, 8dp pills), semantic color mapping, Material Icons icon set. **Confirmed colour palette from brand artwork + iOS screenshots:** app icon background uses a radial gradient (`#72C472` centre → `#2C7A2C` outer edge); adopt the midpoint `~#4AA448` as `colorPrimary`. Logotype fill: `~#6BBF6B` (light-medium green — suitable for `colorSecondary` or on-dark accent); logotype shadow: `~#2B5A2B` (dark green). Light green surface `~#D4EDDA` (tag chips, recipe placeholder tiles). Background `~#F2F2F7`. Brand secondary: hot pink `~#E84A8A` (kids' shirts in squad illustration — reserve for future accent/promo use, not primary UI). Semantic roles: `colorPrimary` = brand green, `colorError` = destructive actions, `colorTertiary` = dev/debug amber. **Body-title layout rule:** Scan, Planner, and Profile root screens carry no top app bar — the screen title is the first element in the scrollable content body. Only pushed destinations (e.g. Settings) use a standard top app bar with back navigation.
- [x] **0.3** `Setup` — Set up Android project — Jetpack Compose, min SDK 29 (Android 10), folder structure, Gradle dependency management
- [x] **0.4** `Setup` — Configure Google Play Developer account — application ID, signing config, permissions (camera, photo library, internet, notifications)
- [x] **0.5** `Setup` — Integrate RevenueCat SDK — configure products, entitlements, and developer user with pro-tier override
- [x] **0.6** `Data` — Define canonical recipe data model in Kotlin — all schema fields, serialization (kotlinx.serialization or Gson), Room entity + DAO
- [x] **0.6b** `Setup` — Introduce code formatting and static analysis — **ktfmt** (via Spotless Gradle plugin) for zero-config Kotlin formatting; **Detekt** with `mrmans0n/compose-rules` for static analysis including Compose-specific checks; **slackhq/compose-lints** for Android Lint Compose rules; wire all three into `./gradlew check`; add a `./gradlew spotlessCheck` gate to the CI workflow (Phase 5.3)

---

## Phase 1 — Authentication & Account
> User identity, onboarding, and subscription scaffolding · **Target: v0.1.0 Play Internal Testing**

- [x] **1.1** `UI` — Onboarding flow — mascot-driven welcome screens with brand green + golden yellow palette, value proposition, sign up / sign in CTA
- [x] **1.2** `Auth` — Sign in with Google — `POST /v1/auth/google` confirmed in API schema. Request body: `{ id_token: string }` (note: `id_token`, not `identity_token`). Response: `TokenResponse`. Use Credential Manager API to obtain the Google ID token; send to backend; backend handles new vs. returning users.
- [x] **1.3** `Auth` — Email/password auth — registration, login, forgot password, email verification flow
- [ ] **1.4** `Data` — JWT token management — secure storage in Preferences DataStore (two string keys, encrypted via Android Keystore — no protobuf plugin required), refresh logic, session expiry handling
- [ ] **1.5** `UI` — User profile screen — body-title layout ("Profile" bold, no top app bar). Three stacked content areas: (1) identity card — circular avatar with green camera-badge overlay for photo editing, bold display name, gray email, conditional dark pill `Developer` badge (debug / developer-tier builds only); (2) stats card — two-column split (`Scans This Month` | `Scans Remaining`) with a vertical divider; remaining count shows as `∞` in primary green for pro/developer tier, integer for free tier; (3) settings-row cards — `Developer Settings` (amber icon + text, debug builds only), `Send Feedback`, `Settings` — each a tappable full-width row with trailing chevron
- [ ] **1.6** `Subscriptions` — Subscription entitlement wiring — RevenueCat entitlement check on launch, gate pro features, developer tier bypass
- [ ] **1.7** `Subscriptions` — Paywall screen — free vs. pro comparison using green + yellow palette, monthly/annual toggle, Google Play Billing purchase flow via RevenueCat *(ensure "Ad-free experience" is listed as a Pro benefit even before ads ship)*
- [ ] **1.8** `UI` — Settings screen — pushed destination with standard top app bar + back navigation. Five grouped sections rendered as rounded cards on a gray background: **Account** (Email read-only, Display Name read-only, Edit Profile `>`); **Subscription** (Upgrade to Pro `>`, Manage Billing & Subscription (green link), Restore Purchases (green link)); **Preferences** (Notifications `>`); **Support** (Contact Support, Privacy Policy, Terms of Service — all green tinted, external links); **Danger Zone** (Sign Out, Delete Account — both in error red, no icons). Footer card (non-interactive): Version + build number right-aligned, Environment value in amber for non-production builds. Destructive actions use error color role throughout.

---

## Phase 2 — Recipe Scanner
> Core AI scan flow — the primary value proposition · **Target: v0.1.0 Play Internal Testing**

- [ ] **2.1** `UI` — Camera capture screen — single and multi-image capture (up to 5 pages) using CameraX, photo library picker (Photo Picker API), brand green shutter button
- [ ] **2.2** `Data` — Image pre-processing — resize to 2048px, JPEG 0.78, 2MB hard cap; CPU-bound work dispatched off main thread
- [ ] **2.3** `UI` — Scan submission & loading state — multipart POST to backend API, per-page loading message, 45s timeout, 1 auto-retry on 504
- [ ] **2.4** `UI` — Scan result review screen — extracted recipe in canonical schema, field-level inline editing before saving
- [ ] **2.5** `UI` — Ingredient list editor — add, remove, reorder (drag handle), quantity/unit/prep note fields; 48dp min row height
- [ ] **2.6** `UI` — Steps editor — add, remove, reorder numbered instruction steps with drag-to-reorder
- [ ] **2.7** `Data` — Save recipe — persist to backend, write-through to local Room database, haptic + animation confirmation
- [ ] **2.8** `Subscriptions` — Scan cap enforcement — client-side check + 402/403 server enforcement, remaining scan indicator, upgrade prompt at limit for free tier
- [ ] **2.9** `UI` — Manual recipe entry — fallback flow to create a recipe from scratch without scanning

---

## Phase 3 — Cookbook & Collection
> Browse, search, and view the personal recipe library · **Target: v0.1.0 Play Internal Testing**

- [ ] **3.1** `UI` — Cookbook home screen — recipe grid/list toggle, hero photo display, tag filter chips, full two-way API sync with pagination
- [ ] **3.2** `UI` — Recipe detail screen — pushed destination. Floating back button top-left and three floating action buttons top-right (❤️ Favorite toggle, 🔖 Add to Cookbook, ⋯ More menu) — all in white rounded-pill containers overlaid on the content; no standard top app bar. Layout top-to-bottom: large bold title → full-width edge-to-edge hero image (`HorizontalPager`, pagination dots, multiple photos supported) → recipe origin attribution (link icon + source name, gray) → personal star rating row (5 stars + text label, amber) → 3-column stats cards (Cook / Total / Yield, each with green icon + bold value + gray label) → **Tags** section (green-outlined pill chips) → **Ingredients** section (bullet-dot list: ingredient name bold + metric conversion in gray below + quantity right-aligned, dividers between rows) → **Steps** section (not yet visible in reference; numbered instructions). Ingredients list scrolls within the outer `LazyColumn`; no nested scroll.
- [ ] **3.3** `UI` — Search — full-text search across title, ingredients, tags
- [ ] **3.4** `UI` — Filter & tag browsing — filter chips using green tint bg + green dark text pills, cuisine/meal type/dietary tags
- [ ] **3.5** `UI` — Edit saved recipe — edit sheet from detail view toolbar menu, updates backend then local Room database
- [ ] **3.6** `Data` — Delete recipe — confirmation dialog, removes from backend then Room, error handling on both steps
- [ ] **3.7** `UI` — Empty states — unique states for zero recipes vs. no search/filter results; CTA navigates to Scan tab
- [ ] **3.8** `UI` — Bottom navigation — 4-tab layout with brand green tint; icons for Catalog, Scan, Planner, Profile
- [ ] **3.9** `Assets` — App icon & launch screen — adaptive icon: foreground layer = bearded chef-dad head (clean version, no "D" overlay), background layer = radial gradient `#72C472` → `#2C7A2C` (approximate with a solid `#4AA448` if vector gradient unsupported in adaptive icon background). All density buckets (`mdpi` → `xxxhdpi`) + `anydpi-v26` adaptive XML. Splash screen: Android 12+ `SplashScreen` API — `windowSplashScreenBackground` = `#4AA448`, `windowSplashScreenAnimatedIcon` = full squad illustration (dad + two daughters) composited with "Chomp Squad" logotype below; for pre-API 31 fall back to a dedicated `SplashActivity` using the same green background + centred assets.

---

## Phase 4 — Polish & Pre-release Hardening
> Refinement, hero photo AI, and pre-launch hardening · **Target: v0.1.0 Play Internal Testing (4.3–4.7) · v1.0.0 Play Store (4.1–4.2)**

- [ ] **4.1** `AI / Pro` — Hero photo detection — surface backend-detected food photo as recipe hero image with crop/confirm UI *(target v1.0.0)*
- [ ] **4.2** `AI / Pro` — AI-generated hero photo — Pro feature: generate representative hero image via AI model when no photo detected *(target v1.0.0)*
- [ ] **4.3** `Polish` — Haptics & micro-interactions — haptic feedback throughout: scan ready, save confirmed, errors, deletions
- [ ] **4.4** `Polish` — Accessibility audit — large font sizes, TalkBack labels, 48dp touch targets, WCAG AA contrast verification
- [ ] **4.5** `Polish` — Dark mode audit — all screens tested in dark mode; no hardcoded colors; semantic color tokens throughout
- [ ] **4.6** `Data` — Offline handling — graceful degradation, local Room reads, pending sync queue for when connectivity returns
- [ ] **4.7** `Polish` — Error handling audit — all API failures, scan errors, auth errors surfaced with actionable Material Design-compliant messaging

---

## Phase 5 — Play Store Internal Testing Beta (v0.1.0)
> Beta infrastructure — feedback collection, crash visibility, and automated delivery · **Target: v0.1.0 Play Internal Testing**

- [ ] **5.1** `UI / Beta` — In-app feedback tool — accessible under Profile tab; form with categories (General, Bug Report, Feature Request), free-text comment, optional screenshot attachment; submits to backend feedback endpoint; first-time badge on tab icon + dismissible announcement banner
- [ ] **5.2** `Setup` — Crash analytics — Firebase Crashlytics integrated; automatic crash reports enriched with user ID, subscription tier, and last-action breadcrumb; non-fatal recording for network errors, API contract mismatches, and unexpected server errors
- [ ] **5.3** `Setup` — CI/CD pipeline — GitHub Actions workflow: automated build + Play Store internal track upload on merge to `develop`; auto-increment version code; post-release commit bumps `versionName` and pushes back to `develop`
- [ ] **5.4** `Launch` — Play Store Internal Testing distribution — configure Play Console for internal testing; recruit initial beta cohort; distribute via invite link; establish feedback cadence

---

## Phase 6 — Meal Planner (v1.0.0)
> Weekly meal planning with recipe assignment and AI-generated shopping lists · **Target: v1.0.0 Play Store**

- [ ] **6.1** `Data` — Meal plan data models — `MealPlan` + `MealSlot` Room entities, `MealType` enum, serializable schemas for API; register DAOs
- [ ] **6.2** `Data` — Meal plan repository + API — CRUD endpoints for `/v1/meal-plans/*`, repository with write-through strategy (optimistic local write + background server sync)
- [ ] **6.3** `UI` — Planner screen (tab root) — body-title layout ("Meal Planner" + "This Week" + date range, no top app bar). Shopping cart icon button top-right as a floating overlay (not in a top app bar) — navigates to Shopping List. Scrollable list of 7 day-section headers, each separated by a divider; today's day header rendered in primary green with a "Today" pill badge. Meal slot rows within each day show a meal-type icon (moon = Dinner, fork/knife = Snack/other), meal-type label, and recipe name with a trailing chevron to navigate to recipe detail. Empty days show only the day header; future/empty days show a `⊕` add button right-aligned to open the recipe picker. **Not a grid** — this is a vertical list grouped by day. Sync-on-resume.
- [ ] **6.4** `UI` — Recipe picker — bottom sheet to assign a recipe to a slot; search + filter from cookbook; "Scan New" and "Add Manually" shortcuts
- [ ] **6.5** `UI / Data` — Shopping list — on-demand list via `POST /v1/meal-plans/{id}/shopping-list` (AI-generated, backend-side); persisted snapshot; checklist with per-item persistence; "Regenerate" to rebuild after plan changes

---

## Phase 7 — Push Notifications (v1.0.0)
> Local meal-planning and meal-time reminders · **Target: v1.0.0 Play Store** · **Backend dependency: None**

- [ ] **7.1** `Setup` — Notification permissions — request `POST_NOTIFICATIONS` permission (Android 13+) on first Settings toggle; handle denied gracefully with a deep-link prompt to system Settings
- [ ] **7.2** `Data` — Notification preferences model — DataStore-backed settings: weekly planning reminder (day-of-week + time); per-meal-type toggles (Breakfast / Lunch / Dinner) each with an individual time picker
- [ ] **7.3** `UI` — Notifications settings UI — new "Notifications" section in Settings; weekly planning reminder row; three meal-type rows with time pickers; all toggles request permission on first enable
- [ ] **7.4** `Data` — Local notification scheduler — `WorkManager` / `AlarmManager` for scheduling repeating notifications; schedules / cancels when preferences change; re-evaluates on app foreground
- [ ] **7.5** `UI / Data` — Meal plan–aware notification bodies — when a `MealSlot` exists for the triggered meal type in Room, names the recipe in the notification body; falls back to generic copy when no plan is set

---

## Phase 8 — Personal Notes, Rating & Recipe Origin (v1.0.0)
> Personal annotation layer plus visual source attribution on recipe cards · **Target: v1.0.0 Play Store**

> **Backend dependency:** Tasks 8.4 and 8.5 require `PATCH /v1/recipes/{id}` to accept `personal_note` and `personal_rating`. Tasks 8.2 and 8.3 are client-side and can proceed in parallel. *(Backend complete as of iOS v0.2.x — unblock immediately.)*

- [x] **8.1** `Backend` — ✅ Confirmed in updated schema. `personal_rating` (int|null, 1–5), `personal_note` (string|null), and `is_favorited` (boolean, default false) are present in `RecipeSchema`, `RecipeListItem`, and `UpdateRecipeRequest`. `origin_type` enum also confirmed. Phases 8.2–8.5 and Favorites (D4) are all unblocked.
- [ ] **8.2** `Data` — Android data model — add `origin: RecipeOrigin`, `personalRating: Int?`, `personalNote: String?` to `Recipe` Room entity and API schema; bump Room migration version; set `origin` at creation in scan flow (`.scan`) and manual entry (`.manual`)
- [ ] **8.3** `UI` — Recipe origin badge — source attribution on cards and detail header: scanned + source → source icon + name; manual / no source → authorship icon + "My Recipe"; muted small-text treatment below title *(fully client-side)*
- [ ] **8.4** `UI / Data` — Personal star rating — 5-star widget in recipe detail; tap to set / clear; debounced PATCH sync; small star row on recipe cards
- [ ] **8.5** `UI / Data` — Personal notes — "My Notes" section in recipe detail; inline editor with keyboard-aware layout; debounced auto-save via `PATCH /v1/recipes/{id}`; character count indicator

---

## Phase 9 — Cookbooks & Collections (v1.0.0)
> Named recipe collections for personal organisation; groundwork for social sharing in v2.0 · **Target: v1.0.0 Play Store**

> **Backend dependency:** Cookbook API confirmed live as of March 2026 — all of Phase 9 is unblocked.

- [x] **9.1** `Backend` — ✅ Fully confirmed. Cookbook CRUD: `GET/POST /v1/cookbooks`; `PATCH/DELETE /v1/cookbooks/{id}`; `PUT /v1/cookbooks/{id}/recipes`; `DELETE /v1/cookbooks/{id}/recipes/{recipe_id}`; `PUT /v1/cookbooks/{id}/cover-image` (multipart); `DELETE /v1/cookbooks/{id}/cover-image`. `CookbookResponse`: `{ id, name, cover_image_url: String?, recipe_ids: UUID[], created_at, updated_at }`. Q12 ✅ fully resolved. **Favorites is `is_favorited` on Recipe, not a cookbook.**
- [ ] **9.2** `Data` — Android cookbook data models — `Cookbook` + `CookbookMembership` Room entities, API schemas, repository; register DAOs; bump Room migration version
- [ ] **9.3** `UI` — Catalog view restructure — rename bottom tab "Cookbook" → "Catalog"; add top tab row: **All Recipes · Cookbooks · Favorites**; Favorites tab filters the recipe list by `is_favorited == true` (client-side filter on cached Room data, no separate API call); All Recipes retains existing behaviour
- [ ] **9.4** `UI` — Cookbook shelf view — grid of cookbook cards (2-column): cover image (brand green placeholder icon when none set, user-uploadable), title, recipe count; `+` `FloatingActionButton` to create new cookbook; tap opens cookbook recipe list
- [ ] **9.5** `UI` — Add to Cookbook — long-press / context menu on recipe card → "Add to Cookbook…" bottom sheet with membership checkmarks and inline "New Cookbook" creation; also in recipe detail toolbar
- [ ] **9.6** `UI` — Cookbook management — rename (`PATCH /v1/cookbooks/{id} { name }`) / delete (`DELETE`) from detail toolbar; remove recipe via swipe-to-delete; delete confirmation warns recipes remain in Catalog. **Cover photo (✅ fully resolved):** brand green placeholder when `cover_image_url` is null; tap to upload via `PUT /v1/cookbooks/{id}/cover-image` (multipart); remove via `DELETE /v1/cookbooks/{id}/cover-image`; response is always updated `CookbookResponse`.

---

## Phase 10 — Play Store Launch (v1.0.0)
> Final hardening and Play Store submission · **Target: v1.0.0 Play Store**

- [ ] **10.1** `UI` — Drag-to-reorder ingredients and steps — drag handles in both editors in the scan review screen
- [ ] **10.2** `Polish` — Final accessibility + dark mode sign-off — post-beta sweep confirming all 4.4 / 4.5 items pass; include all new Phase 7–9 screens
- [ ] **10.3** `Launch` — Data safety declaration — complete Google Play data safety form; declare all data types collected (email, usage data, device identifiers); include camera and photo library rationale
- [ ] **10.4** `Launch` — Play Store submission — screenshots for all required device sizes (phone + 7" tablet), feature graphic, metadata, content rating, review notes

---

## Phase 11 — Social & Community (v2.0.0)
> Transform the personal catalog into a social recipe-sharing platform · **Target: v2.0.0 Play Store**

> ⚠️ **Data migration required.** All existing recipes must default to `private` before social ships. No recipe should become public without explicit user action.

- [ ] **11.1** `Data / UI` — Recipe privacy controls — public/private toggle per recipe; all new and existing recipes default to `private`; privacy indicator in list and detail views
- [ ] **11.2** `Social` — Squads — named groups; creator-only invitations; members share recipes and Cookbooks within the Squad; invite via link or username search
- [ ] **11.3** `UI / Social` — Homepage feed — new root tab; curated feed of public recipes and Squad activity; "Trending," "From Your Squads," "New Saves" sections; infinite scroll
- [ ] **11.4** `Social` — Recipe sharing — share any recipe to a Squad or specific user; shared recipes appear in recipient feed and optionally their Catalog
- [ ] **11.5** `Social / Data` — Social save — save a copy of any public recipe; original author attribution preserved and immutable ("Saved from @username"); `origin` set to `social_save`
- [ ] **11.6** `Data / UI` — Social attribution — author credit displayed on all socially saved and shared recipes; `@username` shown on cards and detail; cannot be edited or removed
- [ ] **11.7** `Data` — Data migration — backend schema migration for social fields; Room database version bump; all existing recipes and Cookbooks default to `private`
- [ ] **11.8** `UI / Social` — Social profile — public profile with display name, avatar, public recipe count and grid; viewable by any user; editable only by owner
- [ ] **11.9** `UI / Social` — Server push notifications — FCM device token registration with backend; push for Squad invites, direct shares, and Squad activity; extends Phase 7 notification preferences in Settings *(distinct from Phase 7's local WorkManager notifications)*
- [ ] **11.10** `Ads` — Ad integration — Google AdMob native ad cards in Catalog list and social feed; ATT-equivalent consent (UMP SDK); Pro tier excluded from all ads

---

## Progress Summary

| Phase | Name | Done | Total | Target |
|---|---|---|---|---|
| 0 | Pre-development foundations | 6 | 7 | v0.1.0 |
| 1 | Authentication & account | 3 | 8 | v0.1.0 |
| 2 | Recipe scanner | 0 | 9 | v0.1.0 |
| 3 | Cookbook & collection | 0 | 9 | v0.1.0 |
| 4 | Polish & pre-release hardening | 0 | 7 | v0.1.0 / v1.0.0 |
| 5 | Play Store Internal Testing beta | 0 | 4 | v0.1.0 |
| 6 | Meal planner | 0 | 5 | v1.0.0 |
| 7 | Push notifications | 0 | 5 | v1.0.0 |
| 8 | Personal notes, rating & recipe origin | 0 | 5 | v1.0.0 |
| 9 | Cookbooks & collections | 0 | 6 | v1.0.0 |
| 10 | Play Store launch | 0 | 4 | v1.0.0 |
| 11 | Social & community | 0 | 10 | v2.0.0 |
| | **Total** | **0** | **79** | |

---

## iOS Parity Notes

The following iOS-specific items have direct Android equivalents already reflected above. Reference the iOS implementation for product behaviour; the technology differs but the UX contract is identical.

| iOS | Android equivalent |
|---|---|
| Sign in with Apple | Sign in with Google (`POST /v1/auth/google`) |
| StoreKit 2 via RevenueCat | Google Play Billing via RevenueCat |
| Keychain | EncryptedSharedPreferences / Android Keystore |
| SwiftData | Room (SQLite) |
| AVCaptureSession / PhotosPicker | CameraX / Photo Picker API |
| UNUserNotificationCenter | NotificationManager + WorkManager |
| APNs device token | FCM device token |
| TestFlight | Play Store Internal Testing track |
| App Store submission | Play Store submission |
| `PrivacyInfo.xcprivacy` | Play Console data safety form (Phase 10.3) |
| `@AppStorage` | DataStore / SharedPreferences |
| No `UINavigationBar` on root tabs (body-title pattern) | No `TopAppBar` in root tab `Scaffold`s; title is first composable in scrollable content |
| Floating back + action buttons on Recipe Detail (over hero) | `Box` overlay with `IconButton`s in white rounded containers; not a `TopAppBar` |
| `UIPageViewController` (hero image pager, Recipe Detail) | `HorizontalPager` + `HorizontalPagerIndicator` |
| Planner as scrollable day-list (not a grid) | `LazyColumn` with sticky day-section headers and `HorizontalDivider` separators |
| Shopping cart on Planner as floating nav button | Top-right `IconButton` in a `Box` overlay — not a `TopAppBar` action |
| Settings as grouped `UITableView` sections | `ElevatedCard` per section group inside a `LazyColumn`; section label above each card |
