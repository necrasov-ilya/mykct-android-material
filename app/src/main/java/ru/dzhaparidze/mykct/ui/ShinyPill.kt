package ru.dzhaparidze.mykct.ui

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.dzhaparidze.mykct.ui.theme.Violet

/**
 * Главное действие экрана: пилюля во всю ширину, залитая плоским акцентом,
 * со стеклянной плёнкой сверху и мягким ореолом под ней (референс shiny-cta,
 * градиентная заливка заменена плоской).
 *
 * Цвета не из палитры темы: заливка одинаковая в светлой и тёмной, это её роль.
 * [shine] выключает ореол - внутри листа он отвлекает от выбора.
 */
@Composable
fun ShinyPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    shine: Boolean = true,
) {
    // Стеклянный блик: белая плёнка сверху, к середине сходит на нет.
    val sheen = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = 0.30f),
        0.5f to Color.White.copy(alpha = 0.05f),
        1.0f to Color.Transparent,
    )

    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // Нажатие видно по всей капсуле, как в iOS: надпись приглушается до 0.7.
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Выключенная гасится целиком: на светлой заливке одного бледного текста мало.
    Box(modifier = modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.45f)) {
        if (CAN_BLUR && enabled && shine) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .blur(24.dp, BlurredEdgeTreatment.Unbounded)
                    .background(Violet.copy(alpha = 0.45f), CircleShape),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(Violet)
                .background(sheen)
                // Кромка стекла: в тёмной теме светлая, в светлой - тёмная,
                // иначе кнопка сливается с фоном.
                .border(
                    width = 1.dp,
                    color = if (darkTheme) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.20f),
                    shape = CircleShape,
                )
                .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val content = Color.White.copy(alpha = if (pressed) 0.7f else 1f)
            if (icon != null) {
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = content,
                modifier = Modifier.padding(start = if (icon != null) 10.dp else 0.dp),
            )
        }
    }
}

/** Размытие свечения требует Android 12; ниже кнопка остаётся без ореола. */
private val CAN_BLUR = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
