package com.wyldsoft.notes.core.rendering

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.wyldsoft.notes.core.drawing.Shape
import com.wyldsoft.notes.core.repository.ShapeRepository
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Default implementation of CanvasRenderer.
 * Manages rendering of shapes to a bitmap.
 */
class CanvasRendererImpl(
    private val shapeRepository: ShapeRepository,
    private val width: Int,
    private val height: Int
) : CanvasRenderer {
    
    private var bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private var canvas: Canvas = Canvas(bitmap)
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()
    
    private val renderers = ConcurrentSkipListSet<ObjectRenderer>(
        compareBy { it.priority }
    )
    
    private var externalCanvas: Canvas? = null
    
    override fun renderShape(shape: Shape) {
        drawShape(shape, canvas)
        
        // Also draw to external canvas if set
        externalCanvas?.let { drawShape(shape, it) }
    }
    
    private fun drawShape(shape: Shape, targetCanvas: Canvas) {
        if (shape.points.size < 2) return
        
        paint.color = shape.penProfile.getColorAsInt()
        paint.strokeWidth = shape.penProfile.strokeWidth
        
        path.reset()
        path.moveTo(shape.points[0].x, shape.points[0].y)
        
        for (i in 1 until shape.points.size) {
            path.lineTo(shape.points[i].x, shape.points[i].y)
        }
        
        targetCanvas.drawPath(path, paint)
    }
    
    override fun refreshRegion(region: RectF) {
        // For e-ink displays, we would trigger partial refresh here
        // For now, just redraw shapes in the region
        val shapesInRegion = shapeRepository.getShapesInRegion(region)
        shapesInRegion.forEach { shape ->
            renderShape(shape)
        }
    }
    
    override fun fullRefresh() {
        // Clear and redraw everything
        canvas.drawColor(android.graphics.Color.WHITE)
        externalCanvas?.drawColor(android.graphics.Color.WHITE)
        
        // Render all shapes
        shapeRepository.getAllShapes().forEach { shape ->
            renderShape(shape)
        }
        
        // Render all object renderers
        renderers.forEach { renderer ->
            if (renderer.shouldRender()) {
                renderer.render(canvas)
                externalCanvas?.let { renderer.render(it) }
            }
        }
    }
    
    override fun addRenderer(renderer: ObjectRenderer, priority: Int) {
        renderers.add(renderer)
    }
    
    override fun removeRenderer(renderer: ObjectRenderer) {
        renderers.remove(renderer)
    }
    
    override fun setCanvas(canvas: Canvas?) {
        externalCanvas = canvas
        if (canvas != null) {
            fullRefresh()
        }
    }
    
    override fun clear() {
        canvas.drawColor(android.graphics.Color.WHITE)
        externalCanvas?.drawColor(android.graphics.Color.WHITE)
    }
    
    fun getBitmap(): Bitmap = bitmap
}