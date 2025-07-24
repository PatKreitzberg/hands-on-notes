package com.wyldsoft.notes.platform

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.wyldsoft.notes.core.commands.CommandExecutorImpl
import com.wyldsoft.notes.core.drawing.DrawingCore
import com.wyldsoft.notes.core.drawing.DrawingCoreImpl
import com.wyldsoft.notes.core.rendering.CanvasRenderer
import com.wyldsoft.notes.core.rendering.CanvasRendererImpl
import com.wyldsoft.notes.core.repository.ShapeRepository
import com.wyldsoft.notes.core.repository.ShapeRepositoryImpl
import com.wyldsoft.notes.core.touch.TouchProcessor
import com.wyldsoft.notes.core.touch.TouchProcessorImpl
import com.wyldsoft.notes.features.FeatureContext
import com.wyldsoft.notes.features.FeatureModule
import com.wyldsoft.notes.platform.sdk.DeviceSDK
import com.wyldsoft.notes.platform.sdk.ShapeFactory

/**
 * Base activity for drawing functionality.
 * Uses our new architecture with proper abstractions.
 */
abstract class BaseDrawingActivity : ComponentActivity() {
    
    // Core components
    protected lateinit var drawingCore: DrawingCore
    protected lateinit var touchProcessor: TouchProcessor
    protected lateinit var canvasRenderer: CanvasRenderer
    protected lateinit var shapeRepository: ShapeRepository
    protected lateinit var commandExecutor: CommandExecutorImpl
    
    // Platform components
    protected lateinit var deviceSDK: DeviceSDK
    protected lateinit var shapeFactory: ShapeFactory
    
    // UI components
    protected var surfaceView: SurfaceView? = null
    
    // Feature modules
    private val featureModules = mutableListOf<FeatureModule>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initializeComponents()
        setupUI()
        registerFeatures()
    }
    
    private fun initializeComponents() {
        // Create platform-specific components
        deviceSDK = createDeviceSDK()
        deviceSDK.initialize(this)
        shapeFactory = createShapeFactory()
        
        // Create core components
        shapeRepository = ShapeRepositoryImpl()
        commandExecutor = CommandExecutorImpl()
        canvasRenderer = CanvasRendererImpl(shapeRepository, 1920, 1080) // TODO: Get actual screen size
        
        drawingCore = DrawingCoreImpl(
            shapeRepository = shapeRepository,
            canvasRenderer = canvasRenderer,
            commandExecutor = commandExecutor
        )
        
        touchProcessor = TouchProcessorImpl(
            drawingCore = drawingCore,
            shapeFactory = shapeFactory,
            scope = lifecycleScope
        )
    }
    
    private fun setupUI() {
        setContent {
            DrawingScreen(
                onSurfaceCreated = { surface ->
                    surfaceView = surface
                    setupSurfaceView(surface)
                }
            )
        }
    }
    
    private fun setupSurfaceView(surfaceView: SurfaceView) {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val canvas = holder.lockCanvas()
                canvas?.let {
                    canvasRenderer.setCanvas(it)
                    holder.unlockCanvasAndPost(it)
                }
            }
            
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Handle surface size changes
            }
            
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                canvasRenderer.setCanvas(null)
            }
        })
        
        // Set up touch handling
        setupTouchHandling(surfaceView)
    }
    
    private fun setupTouchHandling(surfaceView: SurfaceView) {
        // This will be implemented by subclasses using SDK-specific touch handling
        configureTouchHandler(surfaceView)
    }
    
    private fun registerFeatures() {
        val features = createFeatureModules()
        features.forEach { feature ->
            featureModules.add(feature)
            drawingCore.registerFeature(feature)
            
            val context = createFeatureContext()
            feature.initialize(context)
        }
    }
    
    private fun createFeatureContext(): FeatureContext {
        // TODO: Implement FeatureContext with all required components
        throw NotImplementedError("FeatureContext implementation needed")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up features
        featureModules.forEach { it.cleanup() }
        
        // Release SDK resources
        deviceSDK.release()
    }
    
    // Abstract methods to be implemented by subclasses
    abstract fun createDeviceSDK(): DeviceSDK
    abstract fun createShapeFactory(): ShapeFactory
    abstract fun configureTouchHandler(surfaceView: SurfaceView)
    abstract fun createFeatureModules(): List<FeatureModule>
    
    @Composable
    abstract fun DrawingScreen(onSurfaceCreated: (SurfaceView) -> Unit)
}