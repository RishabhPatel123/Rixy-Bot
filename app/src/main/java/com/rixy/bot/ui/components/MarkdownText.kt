package com.rixy.bot.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rixy.bot.ui.theme.Border
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.SurfaceElevated
import com.rixy.bot.ui.theme.TextPrimary
import com.rixy.bot.ui.theme.TextSecondary

/** Renders a lightweight markdown subset: bold, italic, inline code, fenced code blocks, headings, lists. */
@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { parseBlocks(markdown) }
    SelectionContainer {
        Column(modifier = modifier.animateContentSize()) {
            blocks.forEachIndexed { index, block ->
                when (block) {
                    is MdBlock.Code -> CodeBlockView(block, key = index)
                    is MdBlock.Heading -> Text(
                        parseInline(block.text),
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.headlineSmall
                            2 -> MaterialTheme.typography.titleLarge
                            else -> MaterialTheme.typography.titleMedium
                        },
                        color = TextPrimary,
                        modifier = Modifier.padding(top = if (index == 0) 0.dp else Spacing.md),
                    )
                    is MdBlock.Bullets -> Column {
                        block.items.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = Spacing.xs)) {
                                Text(BULLET, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Spacer(Modifier.width(Spacing.sm))
                                Text(
                                    parseInline(item),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    is MdBlock.Ordered -> Column {
                        block.items.forEachIndexed { i, item ->
                            Row(modifier = Modifier.padding(vertical = Spacing.xs)) {
                                Text("${i + 1}.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Spacer(Modifier.width(Spacing.sm))
                                Text(
                                    parseInline(item),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    is MdBlock.Paragraph -> Text(
                        parseInline(block.text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = Spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(block: MdBlock.Code, key: Int) {
    val clipboard = LocalClipboardManager.current
    var copied by rememberSaveable(key) { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        shape = MaterialTheme.shapes.small,
        color = SurfaceElevated,
        border = BorderStroke(1.dp, Border),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row {
                Text(
                    block.language.ifEmpty { "code" },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(block.code))
                    copied = true
                }, modifier = Modifier.height(24.dp)) {
                    Icon(
                        if (copied) Icons.Filled.Done else Icons.Filled.ContentCopy,
                        contentDescription = "Copy code",
                        tint = TextSecondary,
                        modifier = Modifier.height(18.dp),
                    )
                }
            }
            Text(
                block.code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                ),
                color = TextPrimary,
            )
        }
    }
}

private const val BULLET = "•"

private sealed interface MdBlock {
    data class Paragraph(val text: String) : MdBlock
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Bullets(val items: List<String>) : MdBlock
    data class Ordered(val items: List<String>) : MdBlock
    data class Code(val language: String, val code: String) : MdBlock
}

private fun parseBlocks(markdown: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = markdown.lines()
    var i = 0
    val paragraph = StringBuilder()

    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotEmpty()) blocks += MdBlock.Paragraph(text)
        paragraph.clear()
    }

    while (i < lines.size) {
        val line = lines[i]
        when {
            line.trimStart().startsWith("```") -> {
                flushParagraph()
                val language = line.trimStart().removePrefix("```").trim()
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    code.appendLine(lines[i])
                    i++
                }
                blocks += MdBlock.Code(language, code.toString().trimEnd())
            }
            line.startsWith("### ") -> { flushParagraph(); blocks += MdBlock.Heading(3, line.removePrefix("### ").trim()) }
            line.startsWith("## ") -> { flushParagraph(); blocks += MdBlock.Heading(2, line.removePrefix("## ").trim()) }
            line.startsWith("# ") -> { flushParagraph(); blocks += MdBlock.Heading(1, line.removePrefix("# ").trim()) }
            isBullet(line) -> {
                flushParagraph()
                val items = mutableListOf<String>()
                while (i < lines.size && isBullet(lines[i])) {
                    items += lines[i].trimStart().substring(2).trim()
                    i++
                }
                blocks += MdBlock.Bullets(items)
                continue
            }
            isOrdered(line) -> {
                flushParagraph()
                val items = mutableListOf<String>()
                while (i < lines.size && isOrdered(lines[i])) {
                    items += lines[i].trimStart().substringAfter(". ").trim()
                    i++
                }
                blocks += MdBlock.Ordered(items)
                continue
            }
            line.isBlank() -> flushParagraph()
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append('\n')
                paragraph.append(line.trim())
            }
        }
        i++
    }
    flushParagraph()
    return blocks
}

private fun isBullet(line: String): Boolean {
    val t = line.trimStart()
    return (t.startsWith("- ") || t.startsWith("* ")) && !t.startsWith("** ")
}

private fun isOrdered(line: String): Boolean =
    line.trimStart().let { t ->
        val dot = t.indexOf(". ")
        dot in 1..3 && t.take(dot).all { it.isDigit() }
    }

/** Inline styling: **bold**, *italic*, `code`. */
internal fun parseInline(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    val inlineCode = SpanStyle(fontFamily = FontFamily.Monospace, color = TextSecondaryUnstable, background = CodeBgUnstable)
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > 0) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            text.startsWith("`", i) -> {
                val end = text.indexOf('`', i + 1)
                if (end > 0) {
                    withStyle(inlineCode) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            text[i] == '*' && i + 1 < text.length && !text.startsWith("**", i + 1) -> {
                val end = text.indexOf('*', i + 1)
                if (end > 0 && end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> { append(text[i]); i++ }
        }
    }
}

private val TextSecondaryUnstable = com.rixy.bot.ui.theme.TextSecondary
private val CodeBgUnstable = Color(0xFF26262A)
