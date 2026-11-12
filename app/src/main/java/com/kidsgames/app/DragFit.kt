package com.kidsgames.app

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Where everything sits, in pixels from the top-left of the play area. Piece `i` belongs in
 * slot `i`; [homes] is where each piece starts, already shuffled.
 */
internal class FitLayout(val slots: List<Offset>, val homes: List<Offset>, val cell: Float)

/**
 * A board of [count] slots in [cols] columns on top and the loose pieces in a tray below,
 * with every cell the same square size so that it all fits in [width] x [height].
 */
internal fun fitLayout(
    width: Float,
    height: Float,
    cols: Int,
    count: Int,
    gap: Float,
    maxCell: Float,
): FitLayout {
    val rows = (count + cols - 1) / cols
    val trayRows = if (count <= 4) 1 else 2
    val cell = minOf(width * 0.92f / cols, (height - gap) / (rows + trayRows), maxCell)
    val boardLeft = (width - cols * cell) / 2f
    val slots = List(count) { i -> Offset(boardLeft + (i % cols) * cell, (i / cols) * cell) }

    val perRow = (count + trayRows - 1) / trayRows
    val step = if (perRow > 1) minOf(cell * 1.08f, (width - cell) / (perRow - 1)) else 0f
    val trayLeft = (width - (cell + step * (perRow - 1))) / 2f
    val trayTop = rows * cell + gap
    val homes = List(count) { i -> Offset(trayLeft + (i % perRow) * step, trayTop + (i / perRow) * cell) }
    return FitLayout(slots, homes.shuffled(), cell)
}

/**
 * Drag-to-fit play area shared by puzzles and shape sorting. The child drags a piece onto
 * its slot and it snaps in; anywhere else it glides back, and nothing is ever lost.
 * [onMiss] fires when a piece is dropped on the wrong slot, [onPlaced] after each good fit.
 */
@Composable
internal fun DragFit(
    layout: FitLayout,
    slotContent: @Composable (index: Int) -> Unit,
    pieceContent: @Composable (index: Int) -> Unit,
    onPlaced: (index: Int, allPlaced: Boolean) -> Unit,
    onMiss: () -> Unit,
) {
    val density = LocalDensity.current
    val cellDp = with(density) { layout.cell.toDp() }
    val scope = rememberCoroutineScope()
    val count = layout.slots.size
    val offsets = remember(layout) { mutableStateListOf<Offset>().apply { addAll(layout.homes) } }
    val placed = remember(layout) { mutableStateListOf<Boolean>().apply { repeat(count) { add(false) } } }
    val jobs = remember(layout) { arrayOfNulls<Job>(count) }
    var dragging by remember(layout) { mutableIntStateOf(-1) }
    val placedCallback by rememberUpdatedState(onPlaced)
    val missCallback by rememberUpdatedState(onMiss)
    val half = Offset(layout.cell / 2f, layout.cell / 2f)

    fun drop(index: Int) {
        if (dragging != index) return
        dragging = -1
        val center = offsets[index] + half
        if ((center - (layout.slots[index] + half)).getDistance() < layout.cell * 0.6f) {
            offsets[index] = layout.slots[index]
            placed[index] = true
            placedCallback(index, placed.all { it })
            return
        }
        val overOther = layout.slots.indices.any { other ->
            other != index && (center - (layout.slots[other] + half)).getDistance() < layout.cell * 0.5f
        }
        if (overOther) missCallback()
        val from = offsets[index]
        val to = layout.homes[index]
        jobs[index]?.cancel()
        jobs[index] = scope.launch {
            animate(0f, 1f, animationSpec = tween(260)) { fraction, _ ->
                offsets[index] = lerp(from, to, fraction)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        layout.slots.forEachIndexed { index, position ->
            Box(
                Modifier
                    .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                    .size(cellDp)
            ) { slotContent(index) }
        }
        offsets.forEachIndexed { index, position ->
            val lift = when {
                dragging == index -> 3f
                placed[index] -> 1f
                else -> 2f
            }
            Box(
                Modifier
                    .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                    .size(cellDp)
                    .zIndex(lift)
                    .pointerInput(layout, index) {
                        detectDragGestures(
                            onDragStart = {
                                if (!placed[index]) {
                                    jobs[index]?.cancel()
                                    dragging = index
                                }
                            },
                            onDragEnd = { drop(index) },
                            onDragCancel = { drop(index) },
                            onDrag = { change, amount ->
                                if (dragging == index) {
                                    change.consume()
                                    offsets[index] = offsets[index] + amount
                                }
                            },
                        )
                    }
            ) { pieceContent(index) }
        }
    }
}
