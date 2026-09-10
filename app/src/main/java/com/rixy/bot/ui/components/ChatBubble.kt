package com.rixy.bot.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.SurfaceElevated
import com.rixy.bot.ui.theme.TextTertiary

private val UserBubbleShape = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)

/** A message from the user: right-aligned elevated bubble. */
@Composable
fun UserBubble(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.widthIn(max = 300.dp),
        shape = UserBubbleShape,
        color = SurfaceElevated,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
        )
    }
}

/** A reply from Rixy: borderless, full-width, markdown-rendered. */
@Composable
fun RixyBubble(
    text: String,
    modifier: Modifier = Modifier,
    streaming: Boolean = false,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = Spacing.xs)
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Spacer(Modifier.size(Spacing.md))
        Column(Modifier.weight(1f)) {
            if (text.isEmpty() && streaming) {
                TypingIndicator()
            } else {
                MarkdownText(text)
                if (streaming) {
                    StreamingCursor()
                }
            }
        }
    }
}

@Composable
private fun StreamingCursor() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "cursor-alpha",
    )
    Text(
        "▍",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.alpha(alpha),
    )
}

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val dots = listOf(0, 1, 2).map { index ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(450, delayMillis = index * 180),
                RepeatMode.Reverse,
            ),
            label = "dot$index",
        )
    }
    Row(
        modifier = modifier.padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dots.forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .alpha(alpha.value)
                    .background(TextTertiary, CircleShape),
            )
        }
    }
}
