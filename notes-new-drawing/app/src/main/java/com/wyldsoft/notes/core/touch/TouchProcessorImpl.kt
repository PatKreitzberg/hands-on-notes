package com.wyldsoft.notes.core.touch

import com.wyldsoft.notes.core.drawing.DrawingCore
import com.wyldsoft.notes.pen.PenProfile
import com.wyldsoft.notes.platform.sdk.ShapeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Default implementation of TouchProcessor.
 * Handles touch input and converts it to drawing commands.
 */
class TouchProcessorImpl(
    private val drawingCore: DrawingCore,
    private val shapeFactory: ShapeFactory,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : TouchProcessor {
    
    private var currentMode = TouchMode.DRAWING
    private var currentPenProfile = PenProfile.createDefaultProfiles()[0]
    private var isEnabled = true
    private val currentStroke = mutableListOf<TouchPoint>()
    
    override fun processTouchPoints(points: List<TouchPoint>) {
        if (!isEnabled) return
        
        when (currentMode) {
            TouchMode.DRAWING -> processDrawingPoints(points)
            TouchMode.ERASING -> processErasingPoints(points)
            TouchMode.SELECTING -> processSelectingPoints(points)
            TouchMode.PANNING -> processPanningPoints(points)
            TouchMode.ZOOMING -> processZoomingPoints(points)
        }
    }
    
    private fun processDrawingPoints(points: List<TouchPoint>) {
        if (points.isEmpty()) return
        
        // Check if this is the start of a new stroke
        val isNewStroke = currentStroke.isEmpty() || 
                         (points.first().timestamp - currentStroke.last().timestamp > 100)
        
        if (isNewStroke && currentStroke.isNotEmpty()) {
            // Complete the previous stroke
            completeCurrentStroke()
        }
        
        // Add points to current stroke
        currentStroke.addAll(points)
        
        // Check if stroke is complete (lifted pen)
        if (points.size == 1 && points.first().pressure == 0f) {
            completeCurrentStroke()
        }
    }
    
    private fun completeCurrentStroke() {
        if (currentStroke.isEmpty()) return
        
        scope.launch {
            val shape = shapeFactory.createShape(
                points = currentStroke.toList(),
                penProfile = currentPenProfile,
                averagePressure = currentStroke.map { it.pressure }.average().toFloat()
            )
            drawingCore.addShape(shape)
        }
        
        currentStroke.clear()
    }
    
    private fun processErasingPoints(points: List<TouchPoint>) {
        // TODO: Implement erasing logic
        // This would find shapes that intersect with the eraser path
        // and execute EraseShapeCommand
    }
    
    private fun processSelectingPoints(points: List<TouchPoint>) {
        // TODO: Implement selection logic
        // This would be handled by the selection feature module
    }
    
    private fun processPanningPoints(points: List<TouchPoint>) {
        // TODO: Implement panning logic
        // This would update the canvas viewport
    }
    
    private fun processZoomingPoints(points: List<TouchPoint>) {
        // TODO: Implement zooming logic
        // This would update the canvas scale
    }
    
    override fun setPenProfile(profile: PenProfile) {
        currentPenProfile = profile
    }
    
    override fun setMode(mode: TouchMode) {
        // Complete any pending stroke when switching modes
        if (currentMode == TouchMode.DRAWING && currentStroke.isNotEmpty()) {
            completeCurrentStroke()
        }
        currentMode = mode
    }
    
    override fun getMode(): TouchMode = currentMode
    
    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled && currentStroke.isNotEmpty()) {
            completeCurrentStroke()
        }
    }
}