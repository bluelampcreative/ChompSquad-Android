import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.koin.compiler)
    // alias(libs.plugins.protobuf)  — applied in task 1.4 when .proto token schema is created
}

// Load signing credentials from gitignored keystore.properties.
// Copy keystore.properties.example → keystore.properties and fill in values.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val hasSigningConfig = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
    .all { keystoreProperties.containsKey(it) }

android {
    namespace = "com.bluelampcreative.chompsquad"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
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
    }

    signingConfigs {
        if (hasSigningConfig) {
            create("release") {
                storeFile     = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias      = keystoreProperties["keyAlias"]      as String
                keyPassword   = keystoreProperties["keyPassword"]   as String
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix   = "-debug"
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true  // Required for BuildConfig.DEBUG, VERSION_NAME, VERSION_CODE
    }
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
    // protobuf {} config block + .proto schema added in task 1.4
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.kotlin.lite)

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

    // ── Test ─────────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
