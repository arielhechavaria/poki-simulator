package com.kidsgames.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Sizes
import kotlin.math.min
import kotlin.math.roundToInt

// Shades stay inside the palette: every tone is a mix of a palette color with ink or canvas.
private fun Color.shade(amount: Float) = lerp(this, ClayColors.Ink, amount)
private fun Color.tint(amount: Float) = lerp(this, ClayColors.Canvas, amount)

/**
 * A lump of clay: a body sitting on a darker lip that gives it thickness, with a soft
 * highlight on top. When [onClick] is set, pressing squashes the body onto the lip and
 * releasing springs it back with a small bounce.
 *
 * The lip takes [depth] at the bottom, so content is laid out in the body only.
 */
@Composable
fun ClaySurface(
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    cornerRadius: Dp = Radius.xl,
    depth: Dp = 10.dp,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // 0 = resting, 1 = fully pressed. The bouncy spring overshoots on release.
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "clayPress",
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val scale = 1f - 0.04f * press
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                val lip = depth.toPx()
                val bodyHeight = size.height - lip
                if (bodyHeight <= 0f) return@drawBehind
                val radius = min(cornerRadius.toPx(), min(size.width, bodyHeight) / 2f)
                val corner = CornerRadius(radius, radius)
                val bodySize = Size(size.width, bodyHeight)
                val sink = lip * press

                // Contact shadow on the table.
                drawRoundRect(
                    color = ClayColors.Ink.copy(alpha = 0.10f),
                    topLeft = Offset(0f, lip + 5.dp.toPx()),
                    size = bodySize,
                    cornerRadius = corner,
                )
                // Lip: the side of the clay slab.
                drawRoundRect(
                    color = color.shade(0.28f),
                    topLeft = Offset(0f, lip),
                    size = bodySize,
                    cornerRadius = corner,
                )
                // Body, lighter at the top where the light hits.
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color.tint(0.25f), color),
                        startY = sink,
                        endY = sink + bodyHeight,
                    ),
                    topLeft = Offset(0f, sink),
                    size = bodySize,
                    cornerRadius = corner,
                )
                // Glossy streak near the top edge.
                val streakWidth = size.width - radius * 1.2f
                val streakHeight = 5.dp.toPx()
                if (streakWidth > streakHeight) {
                    drawRoundRect(
                        color = ClayColors.Canvas.copy(alpha = 0.40f),
                        topLeft = Offset(radius * 0.6f, sink + 6.dp.toPx()),
                        size = Size(streakWidth, streakHeight),
                        cornerRadius = CornerRadius(streakHeight / 2f, streakHeight / 2f),
                    )
                }
            }
            .then(clickModifier),
        propagateMinConstraints = true,
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = depth)
                .offset { IntOffset(0, (depth.toPx() * press).roundToInt()) },
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}

/** A clay slab in one saturated color. Tappable when [onClick] is set. */
@Composable
fun ClayCard(
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    ClaySurface(
        color = color,
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        cornerRadius = Radius.xl,
        depth = 10.dp,
        content = content,
    )
}

/** Pill-shaped clay button; ink by default, as the primary action. */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = ClayColors.Ink,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    ClaySurface(
        color = color,
        modifier = modifier.defaultMinSize(minWidth = Sizes.touchMin, minHeight = Sizes.touchMin),
        onClick = onClick,
        enabled = enabled,
        cornerRadius = Radius.pill,
        depth = 6.dp,
        content = content,
    )
}

/** Cream canvas with a vertical gradient and three soft color glows, one per corner area. */
@Composable
fun ClayBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(ClayColors.Canvas, ClayColors.SurfaceSoft, ClayColors.SurfaceCard)
                    )
                )
                glow(ClayColors.Peach, Offset(size.width * 0.95f, size.height * 0.05f), size.width * 0.75f)
                glow(ClayColors.Mint, Offset(size.width * 0.05f, size.height * 0.55f), size.width * 0.70f)
                glow(ClayColors.Lavender, Offset(size.width * 0.90f, size.height * 0.98f), size.width * 0.80f)
            },
        content = content,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.glow(
    color: Color,
    center: Offset,
    radius: Float,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.38f), color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
