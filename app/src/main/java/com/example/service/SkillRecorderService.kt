package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SkillRecorderService : AccessibilityService(), SkillRecorder {

    companion object {
        private var instance: SkillRecorderService? = null
        
        fun getInstance(): SkillRecorderService? = instance
    }

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentTrace = MutableStateFlow<List<ActionTrace>>(emptyList())
    override val currentTrace: StateFlow<List<ActionTrace>> = _currentTrace.asStateFlow()

    private var targetAppOrTool: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("SkillRecorder", "Background Accessibility Service Connected")
    }

    override fun startRecording(targetAppOrTool: String) {
        this.targetAppOrTool = targetAppOrTool
        _currentTrace.value = emptyList()
        _isRecording.value = true
        Log.d("SkillRecorder", "Started recording interactions for: $targetAppOrTool")
    }

    override fun stopRecording(): String {
        _isRecording.value = false
        Log.d("SkillRecorder", "Stopped recording interactions.")
        
        val jsonArray = JSONArray()
        _currentTrace.value.forEach { trace ->
            val obj = JSONObject().apply {
                put("timestamp", trace.timestamp)
                put("eventType", trace.eventType)
                put("targetNodeId", trace.targetNodeId ?: JSONObject.NULL)
                put("targetNodeText", trace.targetNodeText ?: JSONObject.NULL)
                put("textInput", trace.textInput ?: JSONObject.NULL)
            }
            jsonArray.put(obj)
        }
        
        return jsonArray.toString(2)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!_isRecording.value || event == null) return

        val eventType = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "CLICK"
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "TYPE"
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> "FOCUS"
            else -> return
        }

        val node = event.source
        val nodeId = node?.viewIdResourceName
        val nodeText = node?.text?.toString() ?: node?.contentDescription?.toString()
        val textInput = if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            event.text.joinToString("")
        } else null

        val trace = ActionTrace(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            targetNodeId = nodeId,
            targetNodeText = nodeText,
            textInput = textInput
        )

        _currentTrace.value = _currentTrace.value + trace
        Log.d("SkillRecorder", "Captured Trace: $trace")
    }

    override fun onInterrupt() {
        Log.d("SkillRecorder", "Accessibility Service Interrupted")
        _isRecording.value = false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }
}
