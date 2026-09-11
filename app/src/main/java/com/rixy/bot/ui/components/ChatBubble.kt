package com.rixy.bot.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rixy.bot.R
import com.rixy.bot.data.prefs.ImageStore
import com.rixy.bot.network.Source
import com.rixy.bot.ui.theme.Motion
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.SurfaceElevated
import com.rixy.bot.ui.theme.TextTertiary
import org.json.JSONArray

private val UserBubbleShape = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)

@Composable
private fun MessageImage(path: String, imageStore: ImageStore, modifier: Modifier = Modifier) {
    val bitmap = remember(path) { imageStore.decodeBounded(path, 1600) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium),
        )
    }
}

/** A message from the user: right-aligned elevated bubble with optional attached image. */
@Composable
fun UserBubble(
    text: String,
    imagePath: String? = null,
    imageStore: ImageStore? = null,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val bubble: @Composable () -> Unit = {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .animateContentSize(animationSpec = Motion.settle()),
            shape = UserBubbleShape,
            color = SurfaceElevated,
        ) {
            Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
                if (imagePath != null && imageStore != null) {
                    MessageImage(imagePath, imageStore, Modifier.padding(bottom = Spacing.sm))
                }
                if (text.isNotEmpty()) {
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
    if (animate) EnterAnimation(modifier = modifier) { bubble() } else Box(modifier) { bubble() }
}

/**
 * A reply from Rixy: borderless, full-width, markdown-rendered, with optional
 * generated image, web sources, and a speaker button (TTS).
 */
@Composable
fun RixyBubble(
    text: String,
    imagePath: String? = null,
    sourcesJson: String? = null,
    imageStore: ImageStore? = null,
    streaming: Boolean = false,
    animate: Boolean = true,
    speaking: Boolean = false,
    onSpeak: (() -> Unit)? = null,
    onSaveImage: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .padding(top = Spacing.xs)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
            Spacer(Modifier.size(Spacing.md))
            Column(
                Modifier
                    .weight(1f)
                    .animateContentSize(animationSpec = Motion.settle()),
            ) {
                if (imagePath != null && imageStore != null) {
                    MessageImage(imagePath, imageStore, Modifier.padding(bottom = Spacing.sm))
                    if (onSaveImage != null) {
                        IconButton(onClick = onSaveImage, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = stringResource(R.string.chat_save_image),
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                if (text.isEmpty() && streaming) {
                    TypingIndicator()
                } else {
                    if (text.isNotEmpty()) {
                        MarkdownText(text)
                        if (streaming) StreamingCursor()
                    }
                    if (sourcesJson != null) {
                        SourceChips(sourcesJson)
                    }
                    if (onSpeak != null && text.isNotBlank()) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(top = Spacing.xs),
                        ) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription = stringResource(R.string.chat_speak),
                                tint = if (speaking) MaterialTheme.colorScheme.primary else TextTertiary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
    if (animate) EnterAnimation(modifier = modifier) { content() } else Box(modifier) { content() }
}

@Composable
private fun SourceChips(sourcesJson: String) {
    val sources = remember(sourcesJson) {
        runCatching {
            val arr = JSONArray(sourcesJson)
            buildList {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { obj ->
                        add(Source(obj.optString("title"), obj.optString("uri")))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
    if (sources.isEmpty()) return
    val context = LocalContext.current
    Column(
        Modifier.padding(top = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            stringResource(R.string.chat_sources),
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
        )
        sources.take(6).forEach { source ->
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    source.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.uri)))
                            }
                        }
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                )
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
