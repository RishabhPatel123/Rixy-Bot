package com.rixy.bot.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp

/**
 * Shared motion language. One set of spring/tween specs so every animation
 * in the app feels like the same fluid gesture: fast start, soft settle,
 * slight overshoot never exceeding a whisper.
 */
object Motion {

    /** The signature feel: subtle overshoot, quick settle. For entrances and morphs. */
    val Liquid: androidx.compose.animation.core.SpringSpec<Float> =
        spring(dampingRatio = 0.82f, stiffness = 380f)

    /** Same feel parameterized for other types (Dp, Int, Color via generic inference). */
    fun <T> liquid(): androidx.compose.animation.core.SpringSpec<T> =
        spring(dampingRatio = 0.82f, stiffness = 380f)

    /** No overshoot; for layout size changes (streaming text growth). */
    fun <T> settle(): androidx.compose.animation.core.SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

    /** Content crossfades and fades. */
    const val FADE_MS = 220

    /** Emphasis easing for tweens where a spring can't be used. */
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> fade() = tween<T>(FADE_MS, easing = EmphasizedEasing)

    /** Stagger step for lists of entering items (suggestion chips, plan cards). */
    const val STAGGER_MS = 55L
}
