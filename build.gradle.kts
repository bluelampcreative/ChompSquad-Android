// Top-level build file — plugin declarations only (apply false).
// Plugins are applied in each module's own build.gradle.kts.
plugins {
    alias(libs.plugins.android.application)     apply false
    alias(libs.plugins.kotlin.compose)          apply false
    alias(libs.plugins.kotlin.serialization)    apply false
    alias(libs.plugins.ksp)                     apply false
    alias(libs.plugins.google.services)         apply false
    alias(libs.plugins.firebase.crashlytics)    apply false
    alias(libs.plugins.firebase.appdistribution) apply false
    // protobuf plugin applied in :app when Proto DataStore schema is wired (task 1.4)
    alias(libs.plugins.protobuf)                apply false
    alias(libs.plugins.koin.compiler)           apply false
}
