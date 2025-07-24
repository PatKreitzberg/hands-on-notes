package com.wyldsoft.notes.editor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

import com.wyldsoft.notes.DrawingCanvas
import com.wyldsoft.notes.ui.components.Toolbar
import com.wyldsoft.notes.pen.PenProfile

@Composable
fun EditorView(
    onSurfaceViewCreated: (android.view.SurfaceView) -> Unit = {},
    onPenProfileChanged: (PenProfile) -> Unit = {}
) {
    val editorState = remember { EditorState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Drawing canvas takes full screen
        DrawingCanvas(
            onSurfaceViewCreated = onSurfaceViewCreated
        )

        // Toolbar floats on top of the canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .onGloballyPositioned { coordinates ->
                    // Update toolbar bounds in EditorState for easy access
                    val position = coordinates.positionInRoot()
                    val size = coordinates.size
                    EditorState.updateToolbarBounds(
                        offset = position,
                        size = size
                    )
                }
        ) {
            Toolbar(
                editorState = editorState,
                onPenProfileChanged = onPenProfileChanged
            )
        }
    }
}