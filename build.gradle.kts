// Top-level build file — plugin declarations only (apply false) for module plugins.
// Spotless and Detekt are applied here so they span all modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
  // protobuf plugin removed — Preferences DataStore replaces Proto DataStore for token storage
  alias(libs.plugins.koin.compiler) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.detekt) apply false // applied in app module to enable detektPlugins config
}

// ── Spotless — zero-config ktfmt formatting ───────────────────────────────────
spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**/*.kt")
    ktfmt()
  }
  kotlinGradle {
    target("**/*.kts")
    targetExclude("**/build/**/*.kts")
    ktfmt()
  }
}

// ── Detekt — static analysis ──────────────────────────────────────────────────
// Wire spotlessCheck into the standard check task so CI only needs `./gradlew check`
tasks.named("check") { dependsOn("spotlessCheck") }
