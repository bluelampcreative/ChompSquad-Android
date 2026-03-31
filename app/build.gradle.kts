import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.google.services)
  alias(libs.plugins.firebase.crashlytics)
  alias(libs.plugins.koin.compiler)
  alias(libs.plugins.detekt)
}

// Load local.properties for API keys injected into BuildConfig.
val localProperties =
    Properties().apply {
      val f = rootProject.file("local.properties")
      if (f.exists()) f.inputStream().use { load(it) }
    }

// Load signing credentials from gitignored keystore.properties.
// Copy keystore.properties.example → keystore.properties and fill in values.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties =
    Properties().apply {
      if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
      }
    }
val hasSigningConfig =
    listOf("storeFile", "storePassword", "keyAlias", "keyPassword").all {
      keystoreProperties.containsKey(it)
    }

android {
  namespace = "com.bluelampcreative.chompsquad"
  compileSdk {
    version =
        release(libs.versions.compileSdk.get().toInt()) {
          minorApiLevel = libs.versions.minorApiLevel.get().toInt()
        }
  }

  defaultConfig {
    applicationId = "com.bluelampcreative.chompsquad"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = libs.versions.appVersionCode.get().toInt()
    versionName = libs.versions.appVersionName.get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    val isReleaseBuild =
        gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

    val revenueCatKey = localProperties["revenuecat.api.key.android"] as String? ?: ""
    if (revenueCatKey.isBlank()) {
      if (isReleaseBuild) {
        error(
            "Missing revenuecat.api.key.android in local.properties. " +
                "Release builds require a valid RevenueCat API key."
        )
      } else {
        logger.warn(
            "⚠️  revenuecat.api.key.android not set in local.properties — " +
                "RevenueCat will not initialize in this debug build."
        )
      }
    }
    buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatKey\"")

    val apiBaseUrl = localProperties["api.base.url"] as String? ?: ""
    if (apiBaseUrl.isBlank()) {
      if (isReleaseBuild) {
        error(
            "Missing api.base.url in local.properties. " +
                "Release builds require a valid API base URL."
        )
      } else {
        logger.warn(
            "⚠️  api.base.url not set in local.properties — " +
                "network calls will fail in this debug build."
        )
      }
    }
    buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

    val googleWebClientId = localProperties["google.web.client.id"] as String? ?: ""
    if (googleWebClientId.isBlank()) {
      if (isReleaseBuild) {
        error(
            "Missing google.web.client.id in local.properties. " +
                "Release builds require a valid Google Web Client ID."
        )
      } else {
        logger.warn(
            "⚠️  google.web.client.id not set in local.properties — " +
                "Google Sign-In will fail in this debug build."
        )
      }
    }
    buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
  }

  signingConfigs {
    if (hasSigningConfig) {
      create("release") {
        storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
        storePassword = keystoreProperties["storePassword"] as String
        keyAlias = keystoreProperties["keyAlias"] as String
        keyPassword = keystoreProperties["keyPassword"] as String
      }
    }
  }

  buildTypes {
    debug {
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
      // Uses default debug signing config automatically.
    }
    release {
      isMinifyEnabled = false
      if (hasSigningConfig) {
        signingConfig = signingConfigs.getByName("release")
      } else {
        logger.warn(
            "⚠️  keystore.properties missing or incomplete — " +
                "release build will not be signed. " +
                "Copy keystore.properties.example and fill in values."
        )
      }
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  buildFeatures {
    compose = true
    buildConfig = true // Required for BuildConfig.DEBUG, VERSION_NAME, VERSION_CODE
  }
}

// ── Detekt — static analysis ──────────────────────────────────────────────────
// Detekt 1.23.8 stable + mrmans0n/compose-rules 0.4.28 (latest 1.x-compatible release).
// Upgrade to Detekt 2.x + compose-rules 0.5.x when 2.x lands in Maven Central.
detekt {
  config.setFrom(files("${rootProject.rootDir}/config/detekt/detekt.yml"))
  buildUponDefaultConfig = true
  baseline = file("${rootProject.rootDir}/config/detekt/baseline.xml")
}

// Export Room schema JSON files for migration history tracking.
// Must be top-level — ksp {} is a Gradle extension, not an android {} sub-block.
ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
  arg("room.incremental", "true")
}

dependencies {
  // ── AndroidX Core ────────────────────────────────────────────────────────
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // ── Compose BOM ──────────────────────────────────────────────────────────
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.adaptive.nav.suite)
  implementation(libs.androidx.compose.material.icons.core)

  // ── Navigation 3 ─────────────────────────────────────────────────────────
  implementation(libs.bundles.navigation3)

  // ── Koin 4 ───────────────────────────────────────────────────────────────
  implementation(platform(libs.koin.bom))
  implementation(libs.koin.android)
  implementation(libs.koin.androidx.compose)
  implementation(libs.koin.annotations)

  // ── Ktor Client 3 (CIO engine) ───────────────────────────────────────────
  implementation(libs.bundles.ktor)

  // ── kotlinx ──────────────────────────────────────────────────────────────
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.coroutines.android)

  // ── Room ─────────────────────────────────────────────────────────────────
  implementation(libs.bundles.room)
  ksp(libs.androidx.room.compiler)

  // ── Proto DataStore ───────────────────────────────────────────────────────
  // Preferences DataStore — two string keys for JWT access + refresh tokens
  implementation(libs.androidx.datastore.preferences)

  // ── Coil 3 ───────────────────────────────────────────────────────────────
  implementation(libs.coil.compose)
  implementation(libs.coil.network.ktor)

  // ── CameraX ──────────────────────────────────────────────────────────────
  implementation(libs.bundles.camerax)

  // ── Firebase (BOM-managed) ────────────────────────────────────────────────
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.messaging)

  // ── RevenueCat ───────────────────────────────────────────────────────────
  implementation(libs.revenuecat)
  implementation(libs.revenuecat.ui)

  // ── Credential Manager + Google Sign-In ──────────────────────────────────
  implementation(libs.bundles.credentials)

  // ── Code quality ─────────────────────────────────────────────────────────
  // Compose-specific Detekt rules (compatible with Detekt 1.23.x)
  detektPlugins(libs.detekt.compose.rules)
  // Compose-specific Android Lint rules (slackhq/compose-lints)
  lintChecks(libs.compose.lint.checks)

  // ── Test ─────────────────────────────────────────────────────────────────
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
