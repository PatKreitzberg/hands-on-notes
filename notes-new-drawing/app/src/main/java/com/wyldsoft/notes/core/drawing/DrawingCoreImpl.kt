package com.wyldsoft.notes.core.drawing

import com.wyldsoft.notes.core.commands.Command
import com.wyldsoft.notes.core.commands.CommandExecutor
import com.wyldsoft.notes.core.commands.DrawShapeCommand
import com.wyldsoft.notes.core.rendering.CanvasRenderer
import com.wyldsoft.notes.core.repository.ShapeRepository
import com.wyldsoft.notes.features.FeatureModule

/**
 * Default implementation of DrawingCore.
 * Coordinates all drawing operations and manages features.
 */
class DrawingCoreImpl(
    private val shapeRepository: ShapeRepository,
    private val canvasRenderer: CanvasRenderer,
    private val commandExecutor: CommandExecutor
) : DrawingCore {
    
    private val features = mutableMapOf<String, FeatureModule>()
    
    override fun addShape(shape: Shape) {
        val command = DrawShapeCommand(shape, shapeRepository, canvasRenderer)
        executeCommand(command)
    }
    
    override fun removeShape(shapeId: String) {
        val shape = shapeRepository.getShape(shapeId)
        if (shape != null) {
            shapeRepository.removeShape(shapeId)
            canvasRenderer.fullRefresh()
        }
    }
    
    override fun executeCommand(command: Command) {
        commandExecutor.execute(command)
    }
    
    override fun registerFeature(feature: FeatureModule) {
        features[feature.id] = feature
    }
    
    override fun getShapeRepository(): ShapeRepository = shapeRepository
    
    override fun getRenderer(): CanvasRenderer = canvasRenderer
    
    override fun getCommandExecutor(): CommandExecutor = commandExecutor
}