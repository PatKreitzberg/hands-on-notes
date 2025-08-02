package com.wyldsoft.notes

import android.view.SurfaceView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.wyldsoft.notes.presentation.viewmodel.EditorViewModel

@Composable
fun DrawingCanvas(
    viewModel: EditorViewModel,
    onSurfaceViewCreated: (SurfaceView) -> Unit
) {
    val refreshTrigger by viewModel.refreshUi.collectAsState()
    
    AndroidView(
        factory = { context ->
            SurfaceView(context).apply {
                onSurfaceViewCreated(this)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            // Force recomposition when refresh is triggered
        }
    )
}
