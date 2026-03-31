package com.bluelampcreative.chompsquad.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand palette ─────────────────────────────────────────────────────────────
// Derived from app icon radial gradient and logotype artwork.
val brandGreenDark = Color(0xFF2C7A2C) // icon gradient dark stop
val brandGreen = Color(0xFF4AA448) // icon gradient midpoint — primary action colour
val brandGreenMid = Color(0xFF6BBF6B) // logotype fill / on-dark accent
val brandGreenLight = Color(0xFF72C472) // icon gradient light stop
val brandGreenContainer = Color(0xFFD4EDDA) // chips, recipe tile tint, tag backgrounds
val brandGreenOnDark = Color(0xFF0A2C0A) // text on primary in dark theme
// Brand pink retained for promo / marketing material — not a primary UI colour.
val brandPink = Color(0xFFE84A8A)

// ── Tertiary / debug ──────────────────────────────────────────────────────────
// Amber is used exclusively for debug / developer UI (badge, Environment label).
val debugAmber = Color(0xFFD97706)
val debugAmberContainer = Color(0xFFFEF3C7)
val debugAmberOn = Color(0xFF431407)

// ── Neutral ───────────────────────────────────────────────────────────────────
val neutralBackground = Color(0xFFF2F2F7) // iOS-equivalent system background
val neutralSurfaceVariant = Color(0xFFDDE5D8)
val neutralOnSurface = Color(0xFF1C1B1F)
val neutralOnSurfaceVar = Color(0xFF414940)
val neutralSubtle = Color(0xFF6B7280) // captions, secondary text
val neutralOutline = Color(0xFF717970)
val neutralOutlineVariant = Color(0xFFC1C9BC)

// ── Error ─────────────────────────────────────────────────────────────────────
val errorRed = Color(0xFFB3261E)
val errorRedContainer = Color(0xFFF9DEDC)
val errorRedOn = Color(0xFF410E0B)
val errorRedOnDark = Color(0xFFF9DEDC) // light pink — readable on dark errorContainer (#8C1D18)

// ── Dark-mode neutrals ────────────────────────────────────────────────────────
val darkBackground = Color(0xFF1A1C19)
val darkOnBackground = Color(0xFFE2E3DD)
val darkSurfaceVariant = Color(0xFF414940)
val darkOutline = Color(0xFF8B9388)
