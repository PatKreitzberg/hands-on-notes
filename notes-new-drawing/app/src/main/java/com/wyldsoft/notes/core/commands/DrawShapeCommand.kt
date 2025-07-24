package com.wyldsoft.notes.core.commands

import com.wyldsoft.notes.core.drawing.Shape
import com.wyldsoft.notes.core.rendering.CanvasRenderer
import com.wyldsoft.notes.core.repository.ShapeRepository

/**
 * Command for drawing a shape.
 */
class DrawShapeCommand(
    private val shape: Shape,
    private val repository: ShapeRepository,
    private val renderer: CanvasRenderer
) : Command {
    
    override fun execute() {
        repository.addShape(shape)
        renderer.renderShape(shape)
    }
    
    override fun undo() {
        repository.removeShape(shape.id)
        renderer.fullRefresh()
    }
    
    override fun getDescription(): String = "Draw shape"
    
    override fun canMergeWith(other: Command): Boolean {
        // Could merge continuous drawing of the same stroke
        return false
    }
}