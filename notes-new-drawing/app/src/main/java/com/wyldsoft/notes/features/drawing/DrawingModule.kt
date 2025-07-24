package com.wyldsoft.notes.features.drawing

import com.wyldsoft.notes.features.FeatureContext
import com.wyldsoft.notes.features.FeatureModule

/**
 * Core drawing feature module.
 * Handles basic drawing operations.
 */
class DrawingModule : FeatureModule {
    
    override val id: String = "core_drawing"
    
    private var isEnabled = true
    
    override fun initialize(context: FeatureContext) {
        // Drawing is the default mode, so no special initialization needed
        // The TouchProcessor already handles drawing by default
    }
    
    override fun cleanup() {
        // No special cleanup needed for basic drawing
    }
    
    override fun isEnabled(): Boolean = isEnabled
}