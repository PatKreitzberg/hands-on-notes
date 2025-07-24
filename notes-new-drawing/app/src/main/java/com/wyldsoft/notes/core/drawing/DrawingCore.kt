package com.wyldsoft.notes.core.drawing

import com.wyldsoft.notes.core.commands.Command
import com.wyldsoft.notes.core.commands.CommandExecutor
import com.wyldsoft.notes.core.rendering.CanvasRenderer
import com.wyldsoft.notes.core.repository.ShapeRepository
import com.wyldsoft.notes.features.FeatureModule

/**
 * Core interface for the drawing system.
 * Coordinates all drawing operations and manages features.
 */
interface DrawingCore {
    /**
     * Adds a shape to the drawing.
     */
    fun addShape(shape: Shape)
    
    /**
     * Removes a shape from the drawing.
     */
    fun removeShape(shapeId: String)
    
    /**
     * Executes a command (for undo/redo support).
     */
    fun executeCommand(command: Command)
    
    /**
     * Registers a feature module with the drawing system.
     */
    fun registerFeature(feature: FeatureModule)
    
    /**
     * Gets the shape repository.
     */
    fun getShapeRepository(): ShapeRepository
    
    /**
     * Gets the canvas renderer.
     */
    fun getRenderer(): CanvasRenderer
    
    /**
     * Gets the command executor for undo/redo operations.
     */
    fun getCommandExecutor(): CommandExecutor
}