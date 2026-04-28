# ChompSquad — Android

ChompSquad is an Android app for digitising physical recipe collections. Point your camera at a recipe book, card, or printout, and the app extracts the title, ingredients, and steps via a backend scanning pipeline. Recipes are stored locally, browsable through a searchable catalog, and viewable in a full detail screen. Manual entry is available for recipes that don't scan well.

---

## Features

| Feature | Description |
|---|---|
| **Camera scan** | Capture up to 5 pages of a recipe using CameraX or the system photo picker |
| **Scan pipeline** | Images are preprocessed on-device, then POSTed to the scan API for extraction |
| **Ingredient & step editor** | Review and correct extracted ingredients and steps before saving |
| **Manual entry** | Create a recipe from scratch without scanning |
| **Recipe catalog** | Browse, search, and filter all saved recipes |
| **Recipe detail** | Full ingredient list and step-by-step instructions |
| **Authentication** | Sign in / sign up with Google |
| **Subscription** | Paywall for premium features via RevenueCat |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | AndroidX Navigation 3 (rc01) |
| Dependency injection | Koin 4 — annotation-based (`@Module`, `@KoinViewModel`) |
| Networking | Ktor (CIO engine) |
| Local storage | Room (SQLite) |
| Token storage | Preferences DataStore |
| Image loading | Coil + Ktor network backend |
| Camera | CameraX |
| Serialization | kotlinx.serialization |
| Subscriptions | RevenueCat + RevenueCat UI |
| Analytics / Crash | Firebase Analytics + Crashlytics |
| Push notifications | Firebase Cloud Messaging |
| Min SDK | 29 (Android 10) |
| Target / Compile SDK | 36 |

---

## Architecture

ChompSquad follows Clean Architecture with a strict unidirectional data-flow layer on top.

### Layers

```
feature/          ← Screen + ViewModel + ViewState per feature
domain/model/     ← Pure Kotlin domain models; no Android or framework imports
data/
  local/          ← Room entities, DAOs, AppDatabase, DataStore token repo
  remote/         ← Ktor API clients + kotlinx.serialization DTOs
  mapper/         ← DTO ↔ Entity ↔ Domain mappers
  scanner/        ← Image preprocessing and scan session state
di/               ← Koin modules (DataModule, NetworkModule, ViewModelModule, …)
core/             ← Base ViewModel, StateReducer, ViewAction, UIEventHandler
ui/
  navigation/     ← AppRoute sealed interface, AppNavigation composable
  theme/          ← Color, Type, Shape, ChompSpacing, Theme
```

### State management — `CoreViewModel`

Every ViewModel extends `CoreViewModel<StateType, ActionType, UIEventType>`. State is managed by a `StateReducer` backed by a `Channel`-based `runningFold`, which guarantees that state transitions are:

- **Pure** — `reducer(state, action): state` — no side effects inside the reducer
- **Sequential** — actions are serialised through a `Channel.BUFFERED` queue
- **Observable** — the resulting `StateFlow` is collected by the composable via `viewModel.viewState`

```
UI                    ViewModel
 │─ handleEvent() ───▶ reducer(state, action) → new state
 │                    │
 │◀─ viewState ───────┘  (StateFlow, collected in composable)
 │
 │◀─ navEvents ────────── Channel<NavEvent> (one-shot, LaunchedEffect)
```

**Dispatching state changes:**

```kotlin
state.dispatch(MyAction.SetLoading(true))   // never mutate state directly
```

**One-shot navigation events (VM → UI):**

```kotlin
// ViewModel
navigate(NavEvent.GoToRecipeDetail(id))

// Composable
val navEvents = viewModel.navEvents
LaunchedEffect(Unit) {
    navEvents.collect { event -> /* handle */ }
}
```

**Hardware callbacks** (CameraX, sensors, Bluetooth) bypass `handleEvent` and call ViewModel methods directly — they are latency-sensitive and arrive on non-main threads. These methods are named in past tense (`onImageCaptured`, `onCaptureFailed`) and only dispatch actions; no async work inside them.

### Navigation — Navigation 3

All routes are declared in `AppRoute`, a sealed interface where every entry is annotated `@Serializable`. Navigation 3 uses kotlinx.serialization to persist the backstack across configuration changes.

```kotlin
@Serializable data class RecipeDetail(val id: String) : AppRoute
```

### Dependency injection — Koin 4

Modules use `@Module` + `@ComponentScan` and are wired at startup via `startKoin<KoinConfig>` (from `org.koin.plugin.module.dsl`, **not** `org.koin.core.context`). Each module owns a package:

| Module | Scans |
|---|---|
| `DataModule` | `data.local` |
| `NetworkModule` | `data.remote` |
| `ViewModelModule` | `feature` |

Cross-module dependencies are declared explicitly with `@Module(includes = [OtherModule::class])`.

### Key decisions

| Concern | Decision | Reason |
|---|---|---|
| Token storage | Preferences DataStore (two string keys) | Proto DataStore overhead not justified for two strings |
| DI | Koin 4 annotation-based | KSP approach (`koin-ksp-compiler`) is deprecated for Koin 4.x |
| Serialization | kotlinx.serialization | Shared between API DTOs and Navigation 3 route serialization |
| Room schema | `exportSchema = true` → `app/schemas/` | Schema tracked in version control |

---

## Getting Started

### Prerequisites

- Android Studio Meerkat or newer
- JDK 17+
- A `local.properties` file in the project root with the following keys:

```properties
api.base.url=https://your-api-host/
google.web.client.id=YOUR_GOOGLE_WEB_CLIENT_ID
revenuecat.api.key.android=YOUR_REVENUECAT_KEY
```

### Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires keystore.properties — see keystore.properties.example)
./gradlew assembleRelease
```

### Code quality

Run before every PR:

```bash
./gradlew spotlessApply && ./gradlew detekt lint
```

| Tool | Role |
|---|---|
| Spotless / ktfmt | Zero-config formatter |
| Detekt | Static analysis + `io.nlopez.compose.rules` |
| compose-lint-checks | Compose Android Lint rules |

---

## Branch Workflow

```
main            ← production releases only
develop         ← integration branch; all feature PRs merge here
feature/X.X-name  ← one branch per checklist task
```

Commit style: `type(scope): description` (Conventional Commits).
