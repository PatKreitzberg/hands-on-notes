package com.wyldsoft.notes.features

import com.wyldsoft.notes.core.drawing.DrawingCore
import com.wyldsoft.notes.platform.gestures.GestureHandler
import com.wyldsoft.notes.ui.components.ToolbarManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

/**
 * Context provided to feature modules for accessing core system components.
 */
interface FeatureContext {
    /**
     * The core drawing system.
     */
    val drawingCore: DrawingCore
    
    /**
     * Handler for gesture recognition.
     */
    val gestureHandler: GestureHandler
    
    /**
     * Manager for toolbar actions.
     */
    val toolbarManager: ToolbarManager
    
    /**
     * Channel for sending events to other features.
     */
    val eventChannel: EventChannel
}

/**
 * Channel for inter-feature communication.
 */
interface EventChannel {
    /**
     * Sends an event to the channel.
     */
    suspend fun send(event: AppEvent)
    
    /**
     * Tries to send an event without suspending.
     */
    fun tryOffer(event: AppEvent): Boolean
    
    /**
     * Gets a flow of events.
     */
    fun asFlow(): Flow<AppEvent>
}

/**
 * Base class for application events.
 */
abstract class AppEvent {
    val timestamp: Long = System.currentTimeMillis()
}