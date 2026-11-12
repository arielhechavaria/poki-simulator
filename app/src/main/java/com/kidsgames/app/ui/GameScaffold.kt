package com.kidsgames.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsgames.app.ui.theme.ClayColors
import com.kidsgames.app.ui.theme.KidsType
import com.kidsgames.app.ui.theme.Radius
import com.kidsgames.app.ui.theme.Space

/** Common game frame: big home button on the left, star counter on the right. */
@Composable
fun GameScaffold(
    onHome: () -> Unit,
    stars: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(Space.md)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 6dp taller than wide: the lip sits below a round body.
            ClayButton(onClick = onHome, modifier = Modifier.size(width = 72.dp, height = 78.dp)) {
                Text("🏠", fontSize = 32.sp)
            }
            Spacer(Modifier.weight(1f))
            StarCounter(stars)
        }
        content()
    }
}

/** Star total in a dark pill; it pops every time a star is earned. */
@Composable
fun StarCounter(stars: Int, modifier: Modifier = Modifier) {
    val pop = remember { Animatable(1f) }
    LaunchedEffect(stars) {
        if (stars > 0) {
            pop.snapTo(1.35f)
            pop.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }
    ClaySurface(
        color = ClayColors.Teal,
        modifier = modifier.graphicsLayer {
            scaleX = pop.value
            scaleY = pop.value
        },
        cornerRadius = Radius.pill,
        depth = 6.dp,
    ) {
        Text(
            text = "⭐ $stars",
            style = KidsType.title,
            color = ClayColors.Canvas,
            modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.sm),
        )
    }
}
