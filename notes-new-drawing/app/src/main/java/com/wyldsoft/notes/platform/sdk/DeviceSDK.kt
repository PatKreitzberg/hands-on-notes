package com.wyldsoft.notes.platform.sdk

import android.content.Context
import android.graphics.RectF
import android.view.View

/**
 * Abstraction layer for device-specific SDK functionality.
 * Allows the app to work with different stylus devices (Onyx, Huion, etc).
 */
interface DeviceSDK {
    /**
     * Initializes the SDK with the application context.
     */
    fun initialize(context: Context)
    
    /**
     * Creates a touch helper for handling stylus input.
     * Returns Any to avoid SDK-specific types in the interface.
     */
    fun createTouchHelper(): Any
    
    /**
     * Sets up the refresh mode for the view (important for e-ink displays).
     */
    fun setupRefreshMode(view: View)
    
    /**
     * Requests a partial refresh of a specific region.
     */
    fun requestPartialRefresh(view: View, region: RectF)
    
    /**
     * Requests a full refresh of the view.
     */
    fun requestFullRefresh(view: View)
    
    /**
     * Gets the device capabilities.
     */
    fun getDeviceCapabilities(): DeviceCapabilities
    
    /**
     * Releases SDK resources.
     */
    fun release()
}

/**
 * Device capabilities that affect how the app behaves.
 */
data class DeviceCapabilities(
    val supportsPartialRefresh: Boolean = true,
    val supportsPressure: Boolean = true,
    val supportsHover: Boolean = false,
    val maxPressureLevels: Int = 4096,
    val displayType: DisplayType = DisplayType.EINK,
    val refreshRate: Int = 15 // Hz
)

/**
 * Types of displays.
 */
enum class DisplayType {
    EINK,
    LCD,
    OLED
}