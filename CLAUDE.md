# ChompSquad Android — Claude Guidelines

Project conventions, architectural decisions, and gotchas for working in this codebase.
Keep this file updated as new patterns are established or corrections are made.

---

## Directory structure

Feature code lives under `feature/`, not `ui/`:

```
app/src/main/java/com/bluelampcreative/chompsquad/
├── core/               # Base ViewModel, StateReducer, ViewAction, UIEventHandler
├── data/
│   ├── local/          # Room entities, DAOs, AppDatabase
│   ├── mapper/         # DTO ↔ Entity ↔ Domain mappers
│   └── remote/dto/     # Kotlinx.serialization API DTOs
├── di/                 # Koin modules (DataModule, PurchasesModule, …)
├── domain/model/       # Clean domain models
├── feature/            # One sub-package per feature
│   └── <name>/         # Screen, ViewModel, ViewState all live here
│       ├── <Name>Screen.kt
│       ├── <Name>ViewModel.kt   (when needed)
│       └── <Name>ViewState.kt   (when needed)
├── models/             # Shared models (Outcome, etc.)
└── ui/
    ├── navigation/     # AppRoute, AppNavigation
    └── theme/          # Color, Type, Shape, Theme, ChompSpacing
```

## Navigation

- Every `AppRoute` entry **must** be annotated `@Serializable` — Navigation 3 rc01 uses
  kotlinx.serialization to persist the backstack across configuration changes.
- Routes with parameters follow: `@Serializable data class RecipeDetail(val id: String) : AppRoute`
- `NavKey` is the required marker interface; `@Serializable` is separate and also required.

## ViewModel pattern

Extend `CoreViewModel<StateType, ActionType, UIEventType>`. The `StateReducer` handles
state via a `Channel`-backed running fold — dispatch actions, never mutate state directly.

- **State**: dispatch `ActionType` via `state.dispatch(action)` — never mutate directly.
- **Navigation events** (VM → UI one-shots): call `navigate(event: UIEventType)`. Collect
  `viewModel.navEvents` in the composable via `LaunchedEffect(Unit)`. Use
  `rememberUpdatedState` when the navigation lambda is captured inside the effect.
- **UI events** (UI → VM): implement `handleEvent(event: UIEventType)` from `UIEventHandler`
  for button-press style interactions when needed.

## Gradle conventions

- `ksp {}` block goes at **top level** in `app/build.gradle.kts`, not inside `android {}`.
- All version aliases live in `gradle/libs.versions.toml` — no inline versions in build files.
- SDK version numbers (`compileSdk`, `minSdk`, `targetSdk`, etc.) are catalog version entries.

## Code quality

Run before every PR:

```bash
./gradlew spotlessCheck detekt lint
```

Or to auto-format first:

```bash
./gradlew spotlessApply && ./gradlew detekt lint
```

- **Spotless/ktfmt** — zero-config formatter. Run `spotlessApply` to fix, `spotlessCheck` to gate.
- **Detekt** — static analysis + `io.nlopez.compose.rules` for Compose-specific checks.
- **compose-lint-checks** — Compose Android Lint rules (runs as part of `lint`).
- No detekt baseline file — all violations are fixed, not suppressed.

## Key architectural decisions

| Concern | Decision | Reason |
|---|---|---|
| Token storage | Preferences DataStore (two string keys) | Proto DataStore overhead not justified for two strings |
| DI | Koin 4 DSL (`viewModelOf`, `single`, `factory`) | Compiler plugin v0.4.1 removed — rejects DSL-declared modules with spurious errors |
| Serialization | kotlinx.serialization | Used for both API DTOs and Navigation 3 route serialization |
| Room schema | `exportSchema = true`, exports to `app/schemas/` | Schema tracked in version control |

## Room

- Child rows (ingredients, steps, images) must be **deleted before re-insert** in upsert
  operations — orphan rows accumulate silently otherwise.
- Tag filtering uses delimiter-wrapped LIKE: `'%,' || :tag || ',%'` — prevents substring
  false positives (e.g. `"vegan"` matching `"non-vegan"`).
- `refreshImageUrl` targets the `recipe_images` table, not `recipes`.

## Branch workflow

```
main          ← production releases only
develop       ← integration branch; all feature PRs merge here
feature/X.X-name  ← one branch per checklist task
```

Commit style: `type(scope): description` (conventional commits).
