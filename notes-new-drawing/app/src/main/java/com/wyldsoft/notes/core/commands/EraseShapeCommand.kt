package com.wyldsoft.notes.core.commands

import com.wyldsoft.notes.core.drawing.Shape
import com.wyldsoft.notes.core.rendering.CanvasRenderer
import com.wyldsoft.notes.core.repository.ShapeRepository

/**
 * Command for erasing shapes.
 */
class EraseShapeCommand(
    private val erasedShapes: List<Shape>,
    private val repository: ShapeRepository,
    private val renderer: CanvasRenderer
) : Command {
    
    override fun execute() {
        erasedShapes.forEach { shape ->
            repository.removeShape(shape.id)
        }
        renderer.fullRefresh()
    }
    
    override fun undo() {
        erasedShapes.forEach { shape ->
            repository.addShape(shape)
        }
        renderer.fullRefresh()
    }
    
    override fun getDescription(): String = "Erase ${erasedShapes.size} shape(s)"
    
    override fun canMergeWith(other: Command): Boolean {
        // Could merge multiple erase commands that happen quickly
        return other is EraseShapeCommand && 
               other.erasedShapes.isNotEmpty() &&
               System.currentTimeMillis() - erasedShapes.first().timestamp < 1000
    }
    
    override fun mergeWith(other: Command) {
        if (other is EraseShapeCommand) {
            (erasedShapes as? MutableList<Shape>)?.addAll(other.erasedShapes)
        }
    }
}