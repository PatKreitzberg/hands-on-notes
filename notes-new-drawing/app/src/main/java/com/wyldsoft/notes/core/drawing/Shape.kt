package com.wyldsoft.notes.core.drawing

import android.graphics.PointF
import android.graphics.RectF
import com.wyldsoft.notes.pen.PenProfile
import java.util.UUID

/**
 * Core shape abstraction that is independent of any SDK.
 * This replaces direct usage of Onyx SDK shapes.
 */
data class Shape(
    val id: String = UUID.randomUUID().toString(),
    val points: List<PointF>,
    val penProfile: PenProfile,
    val bounds: RectF,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Creates a bounding rectangle from the points.
     */
    companion object {
        fun calculateBounds(points: List<PointF>): RectF {
            if (points.isEmpty()) return RectF()
            
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            
            points.forEach { point ->
                minX = minOf(minX, point.x)
                minY = minOf(minY, point.y)
                maxX = maxOf(maxX, point.x)
                maxY = maxOf(maxY, point.y)
            }
            
            return RectF(minX, minY, maxX, maxY)
        }
    }
}