package com.wyldsoft.notes.sdkintegration

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.core.graphics.createBitmap
import com.onyx.android.sdk.pen.TouchHelper
import com.wyldsoft.notes.editor.EditorView
import com.wyldsoft.notes.pen.PenProfile
import com.wyldsoft.notes.pen.PenType
import com.wyldsoft.notes.ui.theme.MinimaleditorTheme
import com.wyldsoft.notes.presentation.viewmodel.EditorViewModel
import com.wyldsoft.notes.presentation.viewmodel.ViewModelFactory
import com.wyldsoft.notes.data.repository.*
import com.wyldsoft.notes.data.database.NotesDatabase
import com.wyldsoft.notes.drawing.DrawingActivityInterface
import android.graphics.PointF
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.onyx.android.sdk.api.device.epd.EpdController
import com.wyldsoft.notes.home.HomeView

abstract class BaseDrawingActivity : ComponentActivity(), DrawingActivityInterface {
    protected val TAG = "BaseDrawingActivity"

    // Common drawing state
    protected var paint = Paint()
    protected var bitmap: Bitmap? = null
    protected var bitmapCanvas: Canvas? = null
    protected var surfaceView: SurfaceView? = null
    protected var isDrawingInProgress = false
    protected var currentPenProfile = PenProfile.getDefaultProfile(PenType.BALLPEN)
    private var editorViewModel: EditorViewModel? = null
    protected val viewModel: EditorViewModel?
        get() = editorViewModel
    private lateinit var viewModelFactory: ViewModelFactory

    // Abstract methods that must be implemented by SDK-specific classes
    abstract fun initializeSDK()
    abstract fun createDeviceReceiver(): BaseDeviceReceiver
    abstract fun enableFingerTouch()
    abstract fun disableFingerTouch()
    abstract fun cleanSurfaceView(surfaceView: SurfaceView): Boolean
    abstract fun renderToScreen(surfaceView: SurfaceView, bitmap: Bitmap?)

    // Template methods - common implementation for all SDKs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repositories
        val database = NotesDatabase.getDatabase(this)
        val noteRepository = NoteRepositoryImpl(
            noteDao = database.noteDao(),
            shapeDao = database.shapeDao()
        )
        val folderRepository = FolderRepositoryImpl(
            folderDao = database.folderDao()
        )
        val notebookRepository = NotebookRepositoryImpl(
            notebookDao = database.notebookDao(),
            noteDao = database.noteDao()
        )
        
        viewModelFactory = ViewModelFactory(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            notebookRepository = notebookRepository
        )

        initializeSDK()
        initializePaint()
        initializeDeviceReceiver()
        
        setContent {
            MinimaleditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeView(
                                viewModelFactory = viewModelFactory,
                                onNotebookSelected = { notebookId, noteId ->
                                    navController.navigate("editor/$notebookId/$noteId")
                                }
                            )
                        }
                        
                        composable(
                            "editor/{notebookId}/{noteId}",
                            arguments = listOf(
                                navArgument("notebookId") { type = NavType.StringType },
                                navArgument("noteId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
                            
                            LaunchedEffect(noteId) {
                                noteRepository.setCurrentNote(noteId)
                            }
                            
                            EditorView(
                                viewModelFactory = viewModelFactory,
                                onSurfaceViewCreated = { sv ->
                                    handleSurfaceViewCreated(sv)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    open fun createTouchHelper(surfaceView: SurfaceView) { }

    override fun onResume() {
        super.onResume()
        onResumeDrawing()
    }

    override fun onPause() {
        super.onPause()
        onPauseDrawing()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupResources()
    }

    // Common functionality
    private fun initializePaint() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        updatePaintFromProfile()
    }

    protected fun updatePaintFromProfile() {
        paint.color = currentPenProfile.getColorAsInt()
        paint.strokeWidth = currentPenProfile.strokeWidth
        Log.d(TAG, "Updated paint: color=${currentPenProfile.strokeColor}, width=${currentPenProfile.strokeWidth}")
    }

    private fun handleSurfaceViewCreated(sv: SurfaceView) {
        surfaceView = sv
        initializeTouchHelper(sv)
        createTouchHelper(sv)
    }

    protected open fun initializeTouchHelper(surfaceView: SurfaceView) {
        surfaceView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateActiveSurface()
        }

        val surfaceCallback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                cleanSurfaceView(surfaceView)
                bitmap?.let { renderToScreen(surfaceView, it) }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                updateActiveSurface()
                bitmap?.let { renderToScreen(surfaceView, it) }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                holder.removeCallback(this)
            }
        }
        surfaceView.holder.addCallback(surfaceCallback)
    }

    override fun setViewModel(viewModel: EditorViewModel) {
        this.editorViewModel = viewModel
        
        // Observe pen profile changes
        lifecycleScope.launch {
            viewModel.currentPenProfile.collect { profile ->
                updatePenProfile(profile)
            }
        }
    }

    override fun onShapeCompleted(points: List<PointF>, pressures: List<Float>) {
        viewModel?.addShape(points, pressures)
    }

    fun updatePenProfile(penProfile: PenProfile) {
        Log.d(TAG, "Updating pen profile: $penProfile")
        currentPenProfile = penProfile
        updatePaintFromProfile()
        updateTouchHelperWithProfile()
        // Don't call viewModel?.updatePenProfile here to avoid infinite loop
    }

    fun updateExclusionZones(excludeRects: List<Rect>) {
        updateTouchHelperExclusionZones(excludeRects)
        viewModel?.updateExclusionZones(excludeRects)
        println("forceScreenRefresh() from updateExclusionZone")
        //forceScreenRefresh()
    }

    override fun forceScreenRefresh() {
        Log.d("BaseDrawingActivity:", "forceScreenRefresh()")
        EpdController.enablePost(surfaceView, 1)
        surfaceView?.let { sv ->
            cleanSurfaceView(sv)
            bitmap?.let { renderToScreen(sv, it) }
        }
    }

    protected fun createDrawingBitmap(): Bitmap? {
        return surfaceView?.let { sv ->
            if (bitmap == null) {
                bitmap = createBitmap(sv.width, sv.height)
                bitmapCanvas = Canvas(bitmap!!)
                bitmapCanvas?.drawColor(Color.WHITE)
            }
            bitmap
        }
    }

    private fun cleanupResources() {
        onCleanupSDK()
        bitmap?.recycle()
        bitmap = null
        onCleanupDeviceReceiver()
    }

    // Abstract methods for SDK-specific lifecycle
    protected abstract fun onResumeDrawing()
    protected abstract fun onPauseDrawing()
    protected abstract fun onCleanupSDK()
    protected abstract fun updateActiveSurface()
    protected abstract fun updateTouchHelperWithProfile()
    protected abstract fun updateTouchHelperExclusionZones(excludeRects: List<Rect>)
    protected abstract fun initializeDeviceReceiver()
    protected abstract fun onCleanupDeviceReceiver()
}
