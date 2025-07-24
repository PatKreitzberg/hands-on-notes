package com.wyldsoft.notes

import android.view.SurfaceView
import androidx.compose.runtime.Composable
import com.onyx.android.sdk.pen.TouchHelper
import com.wyldsoft.notes.features.FeatureModule
import com.wyldsoft.notes.features.drawing.DrawingModule
import com.wyldsoft.notes.features.undo.UndoRedoModule
import com.wyldsoft.notes.platform.BaseDrawingActivity
import com.wyldsoft.notes.platform.sdk.DeviceSDK
import com.wyldsoft.notes.platform.sdk.ShapeFactory
import com.wyldsoft.notes.platform.sdk.onyx.OnyxDeviceSDK
import com.wyldsoft.notes.platform.sdk.onyx.OnyxShapeFactory
import com.wyldsoft.notes.editor.EditorView

/**
 * Refactored MainActivity using the new architecture.
 * This demonstrates how to use the abstracted components.
 */
class MainActivityRefactored : BaseDrawingActivity() {
    
    private var touchHelper: TouchHelper? = null
    
    override fun createDeviceSDK(): DeviceSDK {
        return OnyxDeviceSDK()
    }
    
    override fun createShapeFactory(): ShapeFactory {
        return OnyxShapeFactory()
    }
    
    override fun configureTouchHandler(surfaceView: SurfaceView) {
        // Create Onyx-specific touch helper
        val helper = deviceSDK.createTouchHelper() as TouchHelper
        touchHelper = helper
        
        // For now, just configure basic touch handling
        // The actual Onyx integration would require proper RawInputCallback setup
        // which needs more investigation of the SDK API
        touchHelper?.apply {
            setRawDrawingEnabled(true)
            setStrokeWidth(3f)
        }
    }
    
    
    override fun createFeatureModules(): List<FeatureModule> {
        return listOf(
            DrawingModule(),
            UndoRedoModule()
        )
    }
    
    @Composable
    override fun DrawingScreen(onSurfaceCreated: (SurfaceView) -> Unit) {
        EditorView(
            onSurfaceViewCreated = onSurfaceCreated,
            onPenProfileChanged = { profile ->
                touchProcessor.setPenProfile(profile)
            }
        )
    }
    
    override fun onDestroy() {
        touchHelper?.closeRawDrawing()
        super.onDestroy()
    }
}