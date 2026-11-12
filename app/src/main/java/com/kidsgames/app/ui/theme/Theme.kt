package com.kidsgames.app.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kidsgames.app.R

/** Sniglet (SIL OFL 1.1), bundled in res/font. See licenses/Sniglet-OFL.txt. */
val Sniglet = FontFamily(
    Font(R.font.sniglet_regular, FontWeight.Normal),
    Font(R.font.sniglet_extrabold, FontWeight.ExtraBold),
)

/** Type scale, sized up for a child who cannot read yet: text is a shape to recognize. */
object KidsType {
    val number = TextStyle(
        fontFamily = Sniglet,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 64.sp,
        lineHeight = 68.sp,
    )
    val display = TextStyle(
        fontFamily = Sniglet,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
    )
    val title = TextStyle(
        fontFamily = Sniglet,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
    )
    val label = TextStyle(
        fontFamily = Sniglet,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    )
    val body = TextStyle(
        fontFamily = Sniglet,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    )
}

private val KidsColorScheme = lightColorScheme(
    primary = ClayColors.Ink,
    onPrimary = ClayColors.Canvas,
    secondary = ClayColors.Pink,
    onSecondary = ClayColors.Ink,
    background = ClayColors.Canvas,
    onBackground = ClayColors.Ink,
    surface = ClayColors.SurfaceCard,
    onSurface = ClayColors.Ink,
    error = ClayColors.Error,
)

private val KidsTypography = Typography(
    displayLarge = KidsType.number,
    displayMedium = KidsType.display,
    titleLarge = KidsType.title,
    labelLarge = KidsType.label,
    bodyLarge = KidsType.body,
)

@Composable
fun KidsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = KidsColorScheme, typography = KidsTypography) {
        CompositionLocalProvider(LocalContentColor provides ClayColors.Ink, content = content)
    }
}
