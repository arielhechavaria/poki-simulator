package com.kidsgames.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** The whole palette. No screen may use a color outside this set (learning content aside). */
object ClayColors {
    val Canvas = Color(0xFFFFFAF0)
    val SurfaceSoft = Color(0xFFFAF5E8)
    val SurfaceCard = Color(0xFFF5F0E0)
    val SurfaceStrong = Color(0xFFEBE6D6)
    val Ink = Color(0xFF0A0A0A)
    val Body = Color(0xFF3A3A3A)
    val Muted = Color(0xFF6A6A6A)
    val Hairline = Color(0xFFE5E5E5)
    val DarkSurface = Color(0xFF0A1A1A)

    val Pink = Color(0xFFFF4D8B)
    val Teal = Color(0xFF1A3A3A)
    val Lavender = Color(0xFFB8A4ED)
    val Peach = Color(0xFFFFB084)
    val Ochre = Color(0xFFE8B94A)
    val Mint = Color(0xFFA4D4C5)
    val Coral = Color(0xFFFF6B5A)

    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
}

object Radius {
    val xs = 6.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp

    /** Larger than any half-size, so the shape resolves to a pill or a circle. */
    val pill = 999.dp
}

object Space {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object Sizes {
    /** Smallest touch target for a 4-year-old's finger. */
    val touchMin = 64.dp
}
