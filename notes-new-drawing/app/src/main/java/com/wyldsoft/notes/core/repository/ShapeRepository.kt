package com.wyldsoft.notes.core.repository

import android.graphics.RectF
import com.wyldsoft.notes.core.drawing.Shape

/**
 * Repository for managing shapes in memory.
 * Provides efficient access and querying of shapes.
 */
interface ShapeRepository {
    /**
     * Adds a shape to the repository.
     */
    fun addShape(shape: Shape)
    
    /**
     * Removes a shape by its ID.
     */
    fun removeShape(shapeId: String): Boolean
    
    /**
     * Gets all shapes that intersect with the given region.
     */
    fun getShapesInRegion(region: RectF): List<Shape>
    
    /**
     * Gets all shapes in the repository.
     */
    fun getAllShapes(): List<Shape>
    
    /**
     * Gets a specific shape by ID.
     */
    fun getShape(shapeId: String): Shape?
    
    /**
     * Clears all shapes from the repository.
     */
    fun clear()
    
    /**
     * Gets the total number of shapes.
     */
    fun getShapeCount(): Int
    
    /**
     * Updates an existing shape.
     */
    fun updateShape(shape: Shape)
}