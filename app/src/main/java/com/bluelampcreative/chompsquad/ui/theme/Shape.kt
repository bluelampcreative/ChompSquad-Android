package com.bluelampcreative.chompsquad.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * ChompSquad shape scale.
 *
 * small (8dp) — pills, chips, small buttons medium (12dp) — cards, image tiles large (16dp) —
 * bottom sheets, modal surfaces
 */
val ChompShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )
