package com.wyldsoft.notes.platform.sdk

import android.graphics.PointF
import com.wyldsoft.notes.core.drawing.Shape
import com.wyldsoft.notes.core.touch.TouchPoint
import com.wyldsoft.notes.pen.PenProfile

/**
 * Factory for creating shapes from touch input.
 * Abstracts the conversion from SDK-specific touch data to our Shape model.
 */
interface ShapeFactory {
    /**
     * Creates a shape from touch points and pen settings.
     */
    fun createShape(
        points: List<TouchPoint>,
        penProfile: PenProfile,
        averagePressure: Float = 1.0f
    ): Shape
    
    /**
     * Converts SDK-specific touch points to our TouchPoint model.
     */
    fun convertTouchPoints(sdkPoints: Any): List<TouchPoint>
}

/**
 * Default implementation of ShapeFactory.
 */
open class DefaultShapeFactory : ShapeFactory {
    override fun createShape(
        points: List<TouchPoint>,
        penProfile: PenProfile,
        averagePressure: Float
    ): Shape {
        val pointFs = points.map { PointF(it.x, it.y) }
        val bounds = Shape.calculateBounds(pointFs)
        
        return Shape(
            points = pointFs,
            penProfile = penProfile,
            bounds = bounds
        )
    }
    
    override fun convertTouchPoints(sdkPoints: Any): List<TouchPoint> {
        // This will be overridden by SDK-specific implementations
        return emptyList()
    }
}