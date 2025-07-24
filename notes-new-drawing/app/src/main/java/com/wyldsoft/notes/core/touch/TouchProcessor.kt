package com.wyldsoft.notes.core.touch

import com.wyldsoft.notes.pen.PenProfile

/**
 * Interface for processing touch input.
 * Abstracts away SDK-specific touch handling.
 */
interface TouchProcessor {
    /**
     * Processes raw touch points and converts them to shapes.
     */
    fun processTouchPoints(points: List<TouchPoint>)
    
    /**
     * Sets the current pen profile for drawing.
     */
    fun setPenProfile(profile: PenProfile)
    
    /**
     * Sets the touch mode (drawing, erasing, selecting, etc).
     */
    fun setMode(mode: TouchMode)
    
    /**
     * Gets the current touch mode.
     */
    fun getMode(): TouchMode
    
    /**
     * Enables or disables touch processing.
     */
    fun setEnabled(enabled: Boolean)
}

/**
 * Touch modes for different interactions.
 */
enum class TouchMode {
    DRAWING,
    ERASING,
    SELECTING,
    PANNING,
    ZOOMING
}

/**
 * Platform-agnostic touch point representation.
 */
data class TouchPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)