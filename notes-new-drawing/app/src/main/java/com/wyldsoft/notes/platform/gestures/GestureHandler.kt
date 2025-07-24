package com.wyldsoft.notes.platform.gestures

import android.graphics.PointF

/**
 * Handles gesture recognition for the drawing surface.
 */
interface GestureHandler {
    /**
     * Adds a gesture recognizer to the handler.
     */
    fun addGestureRecognizer(recognizer: GestureRecognizer)
    
    /**
     * Removes a gesture recognizer from the handler.
     */
    fun removeGestureRecognizer(recognizer: GestureRecognizer)
    
    /**
     * Sets a recognizer to be in exclusive mode (no other gestures recognized).
     */
    fun setExclusiveMode(recognizer: GestureRecognizer?)
    
    /**
     * Processes touch input for gesture recognition.
     */
    fun onTouchEvent(event: TouchEvent): Boolean
}

/**
 * Base interface for gesture recognizers.
 */
interface GestureRecognizer {
    /**
     * Unique identifier for this recognizer.
     */
    val id: String
    
    /**
     * Priority for this recognizer (higher priority processes first).
     */
    val priority: Int
    
    /**
     * Processes a touch event.
     * @return true if the gesture was recognized and handled
     */
    fun onTouchEvent(event: TouchEvent): Boolean
    
    /**
     * Resets the recognizer state.
     */
    fun reset()
}

/**
 * Simplified touch event for gesture recognition.
 */
data class TouchEvent(
    val action: TouchAction,
    val points: List<PointF>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Touch actions.
 */
enum class TouchAction {
    DOWN,
    MOVE,
    UP,
    CANCEL
}