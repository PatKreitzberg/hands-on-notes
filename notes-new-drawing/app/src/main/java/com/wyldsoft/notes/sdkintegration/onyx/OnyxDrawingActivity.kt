package com.wyldsoft.notes.sdkintegration.onyx

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.SurfaceView
import com.onyx.android.sdk.data.note.TouchPoint
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.data.TouchPointList
import com.onyx.android.sdk.rx.RxManager
import com.wyldsoft.notes.sdkintegration.GlobalDeviceReceiver
import com.wyldsoft.notes.rendering.RendererToScreenRequest
import com.wyldsoft.notes.rendering.RendererHelper
import com.wyldsoft.notes.touchhandling.TouchUtils
import com.wyldsoft.notes.sdkintegration.BaseDeviceReceiver
import com.wyldsoft.notes.sdkintegration.BaseDrawingActivity
import com.wyldsoft.notes.shapemanagement.ShapeFactory
import com.wyldsoft.notes.shapemanagement.shapes.Shape
import com.wyldsoft.notes.pen.PenType
import androidx.core.graphics.createBitmap
import com.wyldsoft.notes.shapemanagement.EraseManager
import com.wyldsoft.notes.refreshingscreen.PartialEraseRefresh
import com.wyldsoft.notes.gestures.GestureHandler
import android.view.MotionEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.wyldsoft.notes.presentation.viewmodel.EditorViewModel


open class OnyxDrawingActivity : BaseDrawingActivity() {
    private var rxManager: RxManager? = null
    private var onyxTouchHelper: TouchHelper? = null
    private var onyxDeviceReceiver: GlobalDeviceReceiver? = null

    // Store all drawn shapes for re-rendering
    private val drawnShapes = mutableListOf<Shape>()

    // Renderer helper for shape rendering
    private var rendererHelper: RendererHelper? = null
    
    // Erase management
    private val eraseManager = EraseManager()
    private val partialEraseRefresh = PartialEraseRefresh()
    
    // Gesture handler
    private var gestureHandler: GestureHandler? = null

    override fun initializeSDK() {
        // Onyx-specific initialization
        // Initialize renderer helper
        rendererHelper = RendererHelper()
        
        // Subscribe to current note changes to load existing shapes
        lifecycleScope.launch {
            viewModel?.currentNote?.collect { note ->
                note?.let { 
                    loadShapesFromNote(it)
                    // Viewport state is restored in ViewModel, just need to refresh
                    forceScreenRefresh()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun createTouchHelper(surfaceView: SurfaceView) {
        val callback = createOnyxCallback()
        onyxTouchHelper = TouchHelper.create(surfaceView, callback)
        
        // Initialize gesture handler (viewportManager will be set later when viewModel is available)
        gestureHandler = GestureHandler(this, surfaceView)
        
        // Set touch listener on the surface view to capture gestures
        surfaceView.setOnTouchListener { _, event ->
            // Check if any pointer is a stylus
            var hasStylus = false
            for (i in 0 until event.pointerCount) {
                if (event.getToolType(i) == MotionEvent.TOOL_TYPE_STYLUS) {
                    hasStylus = true
                    break
                }
            }
            
            // Only handle events if no stylus is detected
            if (!hasStylus) {
                gestureHandler?.onTouchEvent(event) ?: false
            } else {
                Log.d(TAG, "Stylus detected, ignoring gesture handling")
                false // Let Onyx SDK handle stylus events
            }
        }
    }

    override fun createDeviceReceiver(): BaseDeviceReceiver {
        onyxDeviceReceiver = GlobalDeviceReceiver()
        return OnyxDeviceReceiverWrapper(onyxDeviceReceiver!!)
    }

    override fun enableFingerTouch() {
        TouchUtils.enableFingerTouch(applicationContext)
    }

    override fun disableFingerTouch() {
        TouchUtils.disableFingerTouch(applicationContext)
    }

    override fun cleanSurfaceView(surfaceView: SurfaceView): Boolean {
        val holder = surfaceView.holder ?: return false
        val canvas = holder.lockCanvas() ?: return false
        canvas.drawColor(Color.WHITE)
        holder.unlockCanvasAndPost(canvas)
        return true
    }

    override fun renderToScreen(surfaceView: SurfaceView, bitmap: Bitmap?) {
        if (bitmap != null) {
            getRxManager().enqueue(
                RendererToScreenRequest(
                    surfaceView,
                    bitmap
                ), null)
        }
    }

    override fun onResumeDrawing() {
        onyxTouchHelper?.setRawDrawingEnabled(true)
    }

    override fun onPauseDrawing() {
        onyxTouchHelper?.setRawDrawingEnabled(false)
    }

    override fun onCleanupSDK() {
        onyxTouchHelper?.closeRawDrawing()
        drawnShapes.clear()
        gestureHandler?.cleanup()
        gestureHandler = null
    }

    override fun updateActiveSurface() {
        updateTouchHelperWithProfile()
    }

    override fun updateTouchHelperWithProfile() {
        onyxTouchHelper?.let { helper ->
            helper.setRawDrawingEnabled(false)
            helper.closeRawDrawing()

            val limit = Rect()
            surfaceView?.getLocalVisibleRect(limit)

            val excludeRects = viewModel?.excludeRects?.value ?: emptyList()
            Log.d("ExclusionRects", "Current exclusion rects ${excludeRects.size}")
            helper.setStrokeWidth(currentPenProfile.strokeWidth)
                .setStrokeColor(currentPenProfile.getColorAsInt())
                .setLimitRect(limit, ArrayList(excludeRects))
                .openRawDrawing()

            helper.setStrokeStyle(currentPenProfile.getOnyxStrokeStyleInternal())
            helper.setRawDrawingEnabled(true)
            helper.setRawDrawingRenderEnabled(true)
        }
    }

    override fun updateTouchHelperExclusionZones(excludeRects: List<Rect>) {
        onyxTouchHelper?.let { helper ->
            helper.setRawDrawingEnabled(false)
            helper.closeRawDrawing()

            val limit = Rect()
            surfaceView?.getLocalVisibleRect(limit)

            Log.d("ExclusionRects", "Current exclusion rects ${excludeRects.size}")
            helper.setStrokeWidth(currentPenProfile.strokeWidth)
                .setLimitRect(limit, ArrayList(excludeRects))
                .openRawDrawing()
            helper.setStrokeStyle(currentPenProfile.getOnyxStrokeStyleInternal())

            helper.setRawDrawingEnabled(true)
            helper.setRawDrawingRenderEnabled(true)
        }
    }

    override fun initializeDeviceReceiver() {
        val deviceReceiver = createDeviceReceiver() as OnyxDeviceReceiverWrapper
        deviceReceiver.enable(this, true)
        deviceReceiver.setSystemNotificationPanelChangeListener { open ->
            onyxTouchHelper?.setRawDrawingEnabled(!open)
            surfaceView?.let { sv ->
                renderToScreen(sv, bitmap)
            }
        }.setSystemScreenOnListener {
            surfaceView?.let { sv ->
                renderToScreen(sv, bitmap)
            }
        }
    }

    override fun onCleanupDeviceReceiver() {
        onyxDeviceReceiver?.enable(this, false)
    }

    override fun forceScreenRefresh() {
        Log.d("Onyx", "forceScreenRefresh() called")
        surfaceView?.let { sv ->
            cleanSurfaceView(sv)
            // Recreate bitmap from all stored shapes
            recreateBitmapFromShapes()
            bitmap?.let { renderToScreen(sv, it) }
        }
    }
    
    override fun setViewModel(viewModel: EditorViewModel) {
        super.setViewModel(viewModel)
        // Update gesture handler with viewport manager now that it's available
        gestureHandler?.setViewportManager(viewModel.viewportManager)
        Log.d(TAG, "Updated GestureHandler with ViewportManager")
    }

    private fun getRxManager(): RxManager {
        if (rxManager == null) {
            rxManager = RxManager.Builder.sharedSingleThreadManager()
        }
        return rxManager!!
    }

    private fun createOnyxCallback() = object : com.onyx.android.sdk.pen.RawInputCallback() {
        override fun onBeginRawDrawing(b: Boolean, touchPoint: TouchPoint?) {
            isDrawingInProgress = true
            disableFingerTouch()
            viewModel?.startDrawing()
        }

        override fun onEndRawDrawing(b: Boolean, touchPoint: TouchPoint?) {
            isDrawingInProgress = false
            enableFingerTouch()
            forceScreenRefresh()
            viewModel?.endDrawing()
        }

        override fun onRawDrawingTouchPointMoveReceived(touchPoint: TouchPoint?) {
            // Handle move events if needed
        }

        override fun onRawDrawingTouchPointListReceived(touchPointList: TouchPointList?) {
            touchPointList?.points?.let { points ->
                if (!isDrawingInProgress) {
                    isDrawingInProgress = true
                }
                drawScribbleToBitmap(points, touchPointList)
            }
        }

        override fun onBeginRawErasing(b: Boolean, touchPoint: TouchPoint?) {
            // Handle erasing start
        }

        override fun onEndRawErasing(b: Boolean, touchPoint: TouchPoint?) {
            // Handle erasing end
        }

        override fun onRawErasingTouchPointMoveReceived(touchPoint: TouchPoint?) {
            // Handle erase move
        }

        override fun onRawErasingTouchPointListReceived(touchPointList: TouchPointList?) {
            touchPointList?.let { erasePointList ->
                handleErasing(erasePointList)
            }

        }
    }

    private fun handleErasing(erasePointList: TouchPointList) {
        Log.d(TAG, "handleErasing called with ${erasePointList.size()} points")
        
        // Find shapes that intersect with the erase touch points
        val intersectingShapes = eraseManager.findIntersectingShapes(
            erasePointList, 
            drawnShapes
        )
        
        if (intersectingShapes.isNotEmpty()) {
            Log.d(TAG, "Found ${intersectingShapes.size} shapes to erase")
            
            // Calculate refresh area before removing shapes
            val refreshRect = eraseManager.calculateRefreshRect(intersectingShapes)
            
            // Remove intersecting shapes from our shape list
            drawnShapes.removeAll(intersectingShapes.toSet())
            
            // Perform partial refresh of the erased area
            refreshRect?.let { rect: RectF ->

                surfaceView?.let { sv ->
                    partialEraseRefresh.performPartialRefresh(
                        sv,
                        rect,
                        drawnShapes, // Pass remaining shapes
                        rendererHelper!!,
                        getRxManager()
                    )
                }
            }
            
            // Also update the main bitmap by recreating it from remaining shapes
            recreateBitmapFromShapes()
        }
    }

    private fun drawScribbleToBitmap(points: List<TouchPoint>, touchPointList: TouchPointList) {
        Log.d(TAG, "drawScribbleToBitmap called list size " + touchPointList.size())
        surfaceView?.let { sv ->
            createDrawingBitmap()

            // Create shape with original touch points (in SurfaceViewCoordinates)
            val shape = createShapeFromPenType(touchPointList)
            
            // Convert touch points to NoteCoordinates for storage
            val notePointList = TouchPointList()
            val viewportManager = viewModel?.viewportManager
            
            for (i in 0 until touchPointList.size()) {
                val tp = touchPointList.get(i)
                if (viewportManager != null) {
                    // Convert from SurfaceViewCoordinates to NoteCoordinates
                    val notePoint = viewportManager.surfaceToNoteCoordinates(tp.x, tp.y)
                    val noteTouchPoint = TouchPoint(
                        notePoint.x, 
                        notePoint.y, 
                        tp.pressure, 
                        tp.size, 
                        tp.timestamp
                    )
                    notePointList.add(noteTouchPoint)
                } else {
                    // If no viewport manager, use original coordinates
                    notePointList.add(tp)
                }
            }
            
            // Update shape with note coordinates
            shape.setTouchPointList(notePointList)
            shape.updateShapeRect()
            
            drawnShapes.add(shape)

            // Convert TouchPointList to List<PointF> for ViewModel (in NoteCoordinates)
            val pointFs = mutableListOf<PointF>()
            val pressures = mutableListOf<Float>()
            for (i in 0 until notePointList.size()) {
                val tp = notePointList.get(i)
                pointFs.add(PointF(tp.x, tp.y))
                pressures.add(tp.pressure)
            }
            onShapeCompleted(pointFs, pressures)

            // Render the new shape to the bitmap
            renderShapeToBitmap(shape)
            renderToScreen(sv, bitmap)
        }
    }

    private fun createShapeFromPenType(touchPointList: TouchPointList): Shape {
        // Map pen type to shape type
        val shapeType = when (currentPenProfile.penType) {
            PenType.BALLPEN, PenType.PENCIL -> ShapeFactory.SHAPE_PENCIL_SCRIBBLE
            PenType.FOUNTAIN -> ShapeFactory.SHAPE_BRUSH_SCRIBBLE
            PenType.MARKER -> ShapeFactory.SHAPE_MARKER_SCRIBBLE
            PenType.CHARCOAL, PenType.CHARCOAL_V2 -> ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE
            PenType.NEO_BRUSH -> ShapeFactory.SHAPE_NEO_BRUSH_SCRIBBLE
            PenType.DASH -> ShapeFactory.SHAPE_PENCIL_SCRIBBLE // Default to pencil for dash
        }

        // Create the shape
        val shape = ShapeFactory.createShape(shapeType)
        shape.setTouchPointList(touchPointList)
            .setStrokeColor(currentPenProfile.getColorAsInt())
            .setStrokeWidth(currentPenProfile.strokeWidth)
            .setShapeType(shapeType)
            
        // Update bounding rect for hit testing
        shape.updateShapeRect()

        // Set texture for charcoal if needed
        if (currentPenProfile.penType == PenType.CHARCOAL_V2) {
            shape.setTexture(com.onyx.android.sdk.data.note.PenTexture.CHARCOAL_SHAPE_V2)
        } else if (currentPenProfile.penType == PenType.CHARCOAL) {
            shape.setTexture(com.onyx.android.sdk.data.note.PenTexture.CHARCOAL_SHAPE_V1)
        }

        return shape
    }

    private fun renderShapeToBitmap(shape: Shape) {
        bitmap?.let { bmp ->
            val renderContext = rendererHelper?.getRenderContext() ?: return
            val canvas = Canvas(bmp)
            
            // Apply viewport transformation if available
            canvas.save()
            viewModel?.viewportManager?.let { viewportManager ->
                canvas.concat(viewportManager.getTransformMatrix())
            }
            
            renderContext.bitmap = bmp
            renderContext.canvas = canvas
            renderContext.paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            // Initialize viewPoint for shapes that need it (like CharcoalScribbleShape)
            renderContext.viewPoint = android.graphics.Point(0, 0)

            shape.render(renderContext)
            canvas.restore()
        }
    }

    private fun recreateBitmapFromShapes() {
        surfaceView?.let { sv ->
            // Create a fresh bitmap
            bitmap?.recycle()
            bitmap = createBitmap(sv.width, sv.height)
            bitmapCanvas = Canvas(bitmap!!)
            bitmapCanvas?.drawColor(Color.WHITE)

            // Get render context
            val renderContext = rendererHelper?.getRenderContext() ?: return
            renderContext.bitmap = bitmap
            
            val canvas = bitmapCanvas!!
            
            // Apply viewport transformation if available
            canvas.save()
            viewModel?.viewportManager?.let { viewportManager ->
                canvas.concat(viewportManager.getTransformMatrix())
            }
            
            renderContext.canvas = canvas
            renderContext.paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            // Initialize viewPoint for shapes that need it (like CharcoalScribbleShape)
            renderContext.viewPoint = android.graphics.Point(0, 0)

            // Render all shapes
            for (shape in drawnShapes) {
                shape.render(renderContext)
            }
            
            canvas.restore()
        }
    }

    // Add method to clear all drawings
    fun clearDrawing() {
        drawnShapes.clear()
        surfaceView?.let { sv ->
            bitmap?.recycle()
            bitmap = null
            bitmapCanvas = null
            cleanSurfaceView(sv)
        }
    }
    
    // Load shapes from note into the drawing
    private fun loadShapesFromNote(note: com.wyldsoft.notes.domain.models.Note) {
        // Clear existing shapes
        drawnShapes.clear()
        
        // Convert domain shapes to SDK shapes
        for (domainShape in note.shapes) {
            val sdkShape = convertDomainShapeToSdkShape(domainShape)
            drawnShapes.add(sdkShape)
        }
        
        // Recreate bitmap with all shapes
        forceScreenRefresh()
    }
    
    // Convert domain model shape to Onyx SDK shape
    private fun convertDomainShapeToSdkShape(domainShape: com.wyldsoft.notes.domain.models.Shape): Shape {
        // Create TouchPointList from domain shape points
        val touchPointList = TouchPointList()
        for (i in domainShape.points.indices) {
            val point = domainShape.points[i]
            val pressure = if (i < domainShape.pressure.size) domainShape.pressure[i] else 0.5f
            val touchPoint = TouchPoint(point.x, point.y, pressure, 1f, System.currentTimeMillis())
            touchPointList.add(touchPoint)
        }
        
        // Map shape type - for now assuming all are strokes
        val shapeType = ShapeFactory.SHAPE_PENCIL_SCRIBBLE
        
        val shape = ShapeFactory.createShape(shapeType)
        shape.setTouchPointList(touchPointList)
            .setStrokeColor(domainShape.strokeColor)
            .setStrokeWidth(domainShape.strokeWidth)
            .setShapeType(shapeType)
            
        // Update bounding rect for hit testing
        shape.updateShapeRect()
        
        return shape
    }
}