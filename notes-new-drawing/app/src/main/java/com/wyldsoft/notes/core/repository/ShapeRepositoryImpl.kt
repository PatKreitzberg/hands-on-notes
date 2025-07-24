package com.wyldsoft.notes.core.repository

import android.graphics.RectF
import com.wyldsoft.notes.core.drawing.Shape
import java.util.concurrent.ConcurrentHashMap

/**
 * Default implementation of ShapeRepository using thread-safe collections.
 */
class ShapeRepositoryImpl : ShapeRepository {
    
    private val shapes = ConcurrentHashMap<String, Shape>()
    private val shapesList = mutableListOf<Shape>()
    private val lock = Any()
    
    override fun addShape(shape: Shape) {
        synchronized(lock) {
            shapes[shape.id] = shape
            shapesList.add(shape)
        }
    }
    
    override fun removeShape(shapeId: String): Boolean {
        synchronized(lock) {
            val shape = shapes.remove(shapeId)
            return if (shape != null) {
                shapesList.remove(shape)
                true
            } else {
                false
            }
        }
    }
    
    override fun getShapesInRegion(region: RectF): List<Shape> {
        synchronized(lock) {
            return shapesList.filter { shape ->
                RectF.intersects(shape.bounds, region)
            }
        }
    }
    
    override fun getAllShapes(): List<Shape> {
        synchronized(lock) {
            return shapesList.toList()
        }
    }
    
    override fun getShape(shapeId: String): Shape? {
        return shapes[shapeId]
    }
    
    override fun clear() {
        synchronized(lock) {
            shapes.clear()
            shapesList.clear()
        }
    }
    
    override fun getShapeCount(): Int {
        return shapes.size
    }
    
    override fun updateShape(shape: Shape) {
        synchronized(lock) {
            val oldShape = shapes[shape.id]
            if (oldShape != null) {
                shapes[shape.id] = shape
                val index = shapesList.indexOf(oldShape)
                if (index >= 0) {
                    shapesList[index] = shape
                }
            }
        }
    }
}