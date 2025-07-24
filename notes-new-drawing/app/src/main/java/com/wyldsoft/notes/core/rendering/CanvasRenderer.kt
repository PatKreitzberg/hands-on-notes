package com.wyldsoft.notes.core.rendering

import android.graphics.Canvas
import android.graphics.RectF
import com.wyldsoft.notes.core.drawing.Shape

/**
 * Interface for rendering shapes to a canvas.
 * Manages the rendering pipeline and refresh operations.
 */
interface CanvasRenderer {
    /**
     * Renders a single shape to the canvas.
     */
    fun renderShape(shape: Shape)
    
    /**
     * Refreshes a specific region of the canvas.
     */
    fun refreshRegion(region: RectF)
    
    /**
     * Performs a full refresh of the entire canvas.
     */
    fun fullRefresh()
    
    /**
     * Adds an object renderer to the render stack.
     */
    fun addRenderer(renderer: ObjectRenderer, priority: Int)
    
    /**
     * Removes an object renderer from the render stack.
     */
    fun removeRenderer(renderer: ObjectRenderer)
    
    /**
     * Sets the canvas to render to.
     */
    fun setCanvas(canvas: Canvas?)
    
    /**
     * Clears the canvas.
     */
    fun clear()
}

/**
 * Base interface for objects that can render to a canvas.
 */
interface ObjectRenderer {
    val priority: Int
    
    /**
     * Renders to the provided canvas.
     */
    fun render(canvas: Canvas)
    
    /**
     * Whether this renderer should render in the current frame.
     */
    fun shouldRender(): Boolean = true
    
    /**
     * Gets the dirty region that needs to be refreshed.
     */
    fun getDirtyRegion(): RectF? = null
}