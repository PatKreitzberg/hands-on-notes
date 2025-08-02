package com.wyldsoft.notes.drawing

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import com.wyldsoft.notes.domain.models.Shape
import com.wyldsoft.notes.domain.models.ShapeType
import com.wyldsoft.notes.pen.PenProfile

class DrawingManager {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    
    fun drawShape(canvas: Canvas, shape: Shape) {
        paint.color = shape.strokeColor
        paint.strokeWidth = shape.strokeWidth
        
        when (shape.type) {
            ShapeType.STROKE -> drawStroke(canvas, shape)
            ShapeType.RECTANGLE -> drawRectangle(canvas, shape)
            ShapeType.CIRCLE -> drawCircle(canvas, shape)
            ShapeType.TRIANGLE -> drawTriangle(canvas, shape)
            ShapeType.LINE -> drawLine(canvas, shape)
        }
    }
    
    private fun drawStroke(canvas: Canvas, shape: Shape) {
        if (shape.points.size < 2) return
        
        val path = Path()
        path.moveTo(shape.points[0].x, shape.points[0].y)
        
        for (i in 1 until shape.points.size) {
            val prevPoint = shape.points[i - 1]
            val currPoint = shape.points[i]
            
            if (shape.pressure.isNotEmpty() && i < shape.pressure.size) {
                paint.strokeWidth = shape.strokeWidth * shape.pressure[i]
            }
            
            path.quadTo(
                prevPoint.x, prevPoint.y,
                (prevPoint.x + currPoint.x) / 2,
                (prevPoint.y + currPoint.y) / 2
            )
        }
        
        if (shape.points.size > 1) {
            val lastPoint = shape.points.last()
            path.lineTo(lastPoint.x, lastPoint.y)
        }
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawRectangle(canvas: Canvas, shape: Shape) {
        if (shape.points.size >= 2) {
            val startPoint = shape.points.first()
            val endPoint = shape.points.last()
            canvas.drawRect(
                startPoint.x, startPoint.y,
                endPoint.x, endPoint.y,
                paint
            )
        }
    }
    
    private fun drawCircle(canvas: Canvas, shape: Shape) {
        if (shape.points.size >= 2) {
            val center = shape.points.first()
            val radiusPoint = shape.points.last()
            val radius = kotlin.math.sqrt(
                (radiusPoint.x - center.x) * (radiusPoint.x - center.x) +
                (radiusPoint.y - center.y) * (radiusPoint.y - center.y)
            )
            canvas.drawCircle(center.x, center.y, radius.toFloat(), paint)
        }
    }
    
    private fun drawTriangle(canvas: Canvas, shape: Shape) {
        if (shape.points.size >= 3) {
            val path = Path()
            path.moveTo(shape.points[0].x, shape.points[0].y)
            path.lineTo(shape.points[1].x, shape.points[1].y)
            path.lineTo(shape.points[2].x, shape.points[2].y)
            path.close()
            canvas.drawPath(path, paint)
        }
    }
    
    private fun drawLine(canvas: Canvas, shape: Shape) {
        if (shape.points.size >= 2) {
            val startPoint = shape.points.first()
            val endPoint = shape.points.last()
            canvas.drawLine(
                startPoint.x, startPoint.y,
                endPoint.x, endPoint.y,
                paint
            )
        }
    }
    
    fun createPaintFromProfile(profile: PenProfile): Paint {
        return Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = profile.getColorAsInt()
            strokeWidth = profile.strokeWidth
        }
    }
}