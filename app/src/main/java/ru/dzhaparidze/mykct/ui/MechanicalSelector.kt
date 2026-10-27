package ru.dzhaparidze.mykct.ui

import androidx.annotation.DrawableRes
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.dzhaparidze.mykct.ui.theme.Violet
import ru.dzhaparidze.mykct.ui.theme.VioletDeep
import ru.dzhaparidze.mykct.ui.theme.VioletLight
import kotlin.math.abs
import kotlin.math.roundToInt

/** Fixed housing with the original detent spring, direct manipulation and inertial release. */
@Composable
fun MechanicalSelector(
    @DrawableRes icons: List<Int>,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val instant = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val select by rememberUpdatedState(onSelect)
    val currentIndex by rememberUpdatedState(selectedIndex)
    val surface = MaterialTheme.colorScheme.surface
    val ink = MaterialTheme.colorScheme.onSurface
    val idle = MaterialTheme.colorScheme.onSurfaceVariant
    val background = MaterialTheme.colorScheme.background
    val geometry = remember(density, surface, ink, background) {
        SelectorGeometry(density.density, surface, ink, background)
    }
    var position by remember { mutableFloatStateOf(selectedIndex.toFloat()) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var target by remember { mutableIntStateOf(selectedIndex) }
    var dragging by remember { mutableStateOf(false) }

    fun choose(index: Int) {
        target = index
        if (instant) { position = index.toFloat(); velocity = 0f }
        if (index != currentIndex) select(index)
    }

    LaunchedEffect(selectedIndex) {
        if (!dragging && target != selectedIndex) choose(selectedIndex)
    }
    LaunchedEffect(target, dragging) {
        if (dragging) return@LaunchedEffect
        if (instant) { position = target.toFloat(); velocity = 0f; return@LaunchedEffect }
        var previous = withFrameNanos { it }
        var detent = position.roundToInt().coerceIn(0, 2)
        while (abs(position - target) > 0.001f || abs(velocity) > 0.012f) {
            val now = withFrameNanos { it }
            val dt = ((now - previous) / 1e9f).coerceIn(0f, 0.032f) / 2f
            previous = now
            repeat(2) {
                velocity += (72f * (target - position) - 10f * velocity) * dt
                position += velocity * dt
                if (position < 0f && velocity < 0f) { position = 0f; velocity *= -0.35f }
                if (position > 2f && velocity > 0f) { position = 2f; velocity *= -0.35f }
            }
            val next = position.roundToInt().coerceIn(0, 2)
            if (next != detent) {
                detent = next
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            }
        }
        position = target.toFloat()
        velocity = 0f
    }

    Box(modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
        Box(
            Modifier
                .width(SELECTOR_WIDTH.dp)
                .height(SELECTOR_CLEARANCE.dp)
                .clipToBounds()
                .pointerInput(geometry) {
                    val tracker = VelocityTracker()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val initialPosition = position
                        var last = down
                        var engaged = false
                        var detent = position.roundToInt().coerceIn(0, 2)
                        dragging = true
                        velocity = 0f
                        tracker.resetTracking()
                        tracker.addPosition(down.uptimeMillis, down.position)
                        try {
                            while (last.pressed) {
                                val remaining = viewConfiguration.longPressTimeoutMillis -
                                    (last.uptimeMillis - down.uptimeMillis)
                                val event = if (!engaged && remaining > 0) {
                                    withTimeoutOrNull(remaining) { awaitPointerEvent() }
                                } else if (!engaged) null else awaitPointerEvent()
                                if (event == null) {
                                    engaged = true
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    continue
                                }
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                last = change
                                tracker.addPosition(change.uptimeMillis, change.position)
                                val distance = change.position.x - down.position.x
                                if (!engaged && abs(distance) > viewConfiguration.touchSlop) {
                                    engaged = true
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                if (engaged) {
                                    val raw = initialPosition + distance / geometry.pitch
                                    position = when {
                                        raw < 0f -> raw * 0.25f
                                        raw > 2f -> 2f + (raw - 2f) * 0.25f
                                        else -> raw
                                    }
                                    val next = position.roundToInt().coerceIn(0, 2)
                                    if (next != detent) {
                                        detent = next
                                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                        select(next)
                                    }
                                }
                                change.consume()
                                if (!change.pressed) {
                                    if (!engaged) {
                                        choose(geometry.hitSlot(change.position.x))
                                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    } else {
                                        velocity = (tracker.calculateVelocity().x / geometry.pitch).coerceIn(-8f, 8f)
                                        choose((position + velocity * 0.15f).roundToInt().coerceIn(0, 2))
                                    }
                                }
                            }
                        } finally {
                            dragging = false
                        }
                    }
                },
        ) {
            Canvas(Modifier.matchParentSize()) { geometry.draw(this, position) }
            for (index in 0..2) {
                Icon(
                    painterResource(icons[index]),
                    contentDescription = null,
                    tint = lerp(idle, Color.White, emergence(position, index)),
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (geometry.iconX(index) - geometry.iconSize / 2f).roundToInt(),
                                (geometry.iconY - geometry.iconSize / 2f).roundToInt(),
                            )
                        }
                        .size(ICON_SIZE.dp),
                )
                Box(
                    Modifier
                        .offset { IntOffset((geometry.iconX(index) - geometry.pitch / 2f).roundToInt(), 0) }
                        .width(SLOT_PITCH.dp)
                        .height(SELECTOR_CLEARANCE.dp)
                        .semantics {
                            contentDescription = labels[index]
                            role = Role.Tab
                            selected = selectedIndex == index
                            onClick(labels[index]) { choose(index); true }
                        },
                )
            }
        }
    }
}

private fun emergence(position: Float, index: Int): Float {
    val t = (1f - abs(position - index)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private class SelectorGeometry(density: Float, surface: Color, ink: Color, background: Color) {
    val pitch = SLOT_PITCH * density
    val iconSize = ICON_SIZE * density
    private val baseline = SELECTOR_CLEARANCE * density
    private val cx = SELECTOR_WIDTH * density / 2f
    val iconY = baseline - 27f * density
    private val radius = 23f * density
    private val rim = Stroke(0.65f * density)
    // Feather only the housing into the lower background; buttons and icons stay crisp.
    private val edgeBrush = Brush.verticalGradient(
        0f to ink.copy(alpha = 0.17f),
        0.35f to ink.copy(alpha = 0.11f),
        0.7f to ink.copy(alpha = 0.035f),
        1f to ink.copy(alpha = 0f),
        startY = baseline - 16f * density, endY = baseline,
    )
    private val bodyBrush = Brush.verticalGradient(
        0f to lerp(surface, ink, 0.075f),
        0.62f to background.copy(alpha = 0.95f),
        0.8f to background.copy(alpha = 0.70f),
        0.92f to background.copy(alpha = 0.22f),
        1f to background.copy(alpha = 0f),
        startY = baseline - 52f * density, endY = baseline,
    )
    private val buttonBrush = Brush.linearGradient(
        listOf(VioletLight, Violet, VioletDeep),
        start = Offset(-radius, -radius), end = Offset(radius, radius),
    )
    private val highlight = Brush.radialGradient(
        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
        center = Offset(-radius * 0.35f, -radius * 0.6f), radius = radius * 1.2f,
    )
    private val body = Path().apply {
        // Fixed, symmetric shoulders and a nearly level crown. The bottom is clipped by the screen edge.
        fun x(value: Float) = cx + value * density
        fun y(height: Float) = baseline - height * density
        moveTo(x(-116f), y(-8f))
        cubicTo(x(-116f), y(22f), x(-100f), y(37f), x(-72f), y(37f))
        cubicTo(x(-44f), y(37f), x(-24f), y(38f), x(0f), y(38f))
        cubicTo(x(24f), y(38f), x(44f), y(37f), x(72f), y(37f))
        cubicTo(x(100f), y(37f), x(116f), y(22f), x(116f), y(-8f))
        close()
    }

    fun iconX(index: Int) = cx + (index - 1) * pitch
    fun hitSlot(x: Float) = (1f + (x - cx) / pitch).roundToInt().coerceIn(0, 2)

    fun draw(scope: DrawScope, position: Float) = with(scope) {
        drawPath(body, bodyBrush)
        drawPath(body, edgeBrush, style = rim)
        // Only the local buttons grow/retract; housing and icon positions never follow the gesture.
        for (index in 0..2) {
            val growth = emergence(position, index)
            if (growth <= 0.001f) continue
            translate(iconX(index), iconY) {
                scale(growth, growth, pivot = Offset.Zero) {
                    drawCircle(buttonBrush, radius, center = Offset.Zero)
                    drawCircle(highlight, radius, center = Offset.Zero)
                    drawCircle(Color.White.copy(alpha = 0.23f), radius - rim.width,
                        center = Offset.Zero, style = rim)
                }
            }
        }
    }
}

private const val SELECTOR_WIDTH = 232f
private const val SELECTOR_CLEARANCE = 64f
private const val SLOT_PITCH = 64f
private const val ICON_SIZE = 23f
