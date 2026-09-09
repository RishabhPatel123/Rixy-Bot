package com.example.service

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface representing a background recorder capable of capturing user screen interactions
 * (clicks, typing, focus changes) and synthesizing them into a reusable macro trace.
 */
interface SkillRecorder {
    /**
     * Observable state indicating if the service is actively capturing the screen.
     */
    val isRecording: StateFlow<Boolean>

    /**
     * Observable stream of the current session's recorded UI steps.
     */
    val currentTrace: StateFlow<List<ActionTrace>>

    /**
     * Target application or package the recorder should focus on tracking.
     */
    fun startRecording(targetAppOrTool: String)

    /**
     * Stops recording and returns the raw JSON representation of the trace.
     */
    fun stopRecording(): String
}

/**
 * Represents a single captured DOM/UI interaction event.
 */
data class ActionTrace(
    val timestamp: Long,
    val eventType: String,
    val targetNodeId: String?,
    val targetNodeText: String?,
    val textInput: String? = null
)
