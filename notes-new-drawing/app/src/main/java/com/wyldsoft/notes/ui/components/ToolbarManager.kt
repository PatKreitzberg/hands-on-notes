package com.wyldsoft.notes.ui.components

import com.wyldsoft.notes.pen.PenProfile

/**
 * Manages toolbar actions and state.
 */
interface ToolbarManager {
    /**
     * Adds an action to the toolbar.
     */
    fun addAction(action: ToolbarAction)
    
    /**
     * Removes an action from the toolbar by its ID.
     */
    fun removeAction(actionId: String)
    
    /**
     * Sets the currently selected pen profile.
     */
    fun setSelectedPenProfile(profile: PenProfile)
    
    /**
     * Shows additional options in the toolbar.
     */
    fun showOptions(options: List<ToolbarOption>)
    
    /**
     * Hides additional options.
     */
    fun hideOptions()
    
    /**
     * Gets all registered actions.
     */
    fun getActions(): List<ToolbarAction>
}

/**
 * Represents an action that can be performed from the toolbar.
 */
data class ToolbarAction(
    val id: String,
    val icon: Int, // Resource ID
    val description: String,
    val onClick: () -> Unit
)

/**
 * Represents an option that can be shown in the toolbar.
 */
data class ToolbarOption(
    val id: String,
    val label: String,
    val isChecked: Boolean = false,
    val onToggle: (Boolean) -> Unit
)