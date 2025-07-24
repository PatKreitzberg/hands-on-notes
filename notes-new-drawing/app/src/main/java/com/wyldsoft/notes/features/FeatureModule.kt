package com.wyldsoft.notes.features

/**
 * Base interface for feature modules.
 * Allows features to be added to the drawing system in a modular way.
 */
interface FeatureModule {
    /**
     * Unique identifier for this feature.
     */
    val id: String
    
    /**
     * Initializes the feature with the provided context.
     */
    fun initialize(context: FeatureContext)
    
    /**
     * Cleans up resources when the feature is disabled or removed.
     */
    fun cleanup()
    
    /**
     * Checks if this feature is currently enabled.
     */
    fun isEnabled(): Boolean
}