package com.wyldsoft.notes.features.undo

import com.wyldsoft.notes.features.FeatureContext
import com.wyldsoft.notes.features.FeatureModule
import com.wyldsoft.notes.ui.components.ToolbarAction

/**
 * Undo/Redo feature module.
 * Adds undo and redo actions to the toolbar.
 */
class UndoRedoModule : FeatureModule {
    
    override val id: String = "undo_redo"
    
    private var isEnabled = true
    
    override fun initialize(context: FeatureContext) {
        // Add undo action to toolbar
        context.toolbarManager.addAction(
            ToolbarAction(
                id = "action_undo",
                icon = android.R.drawable.ic_menu_revert,
                description = "Undo",
                onClick = {
                    context.drawingCore.getCommandExecutor().undo()
                }
            )
        )
        
        // Add redo action to toolbar
        context.toolbarManager.addAction(
            ToolbarAction(
                id = "action_redo",
                icon = android.R.drawable.ic_menu_rotate,
                description = "Redo",
                onClick = {
                    context.drawingCore.getCommandExecutor().redo()
                }
            )
        )
    }
    
    override fun cleanup() {
        // Remove toolbar actions
        // Note: In a real implementation, we'd need to keep track of the context
    }
    
    override fun isEnabled(): Boolean = isEnabled
}