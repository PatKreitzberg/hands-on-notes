package com.wyldsoft.notes.editor

import android.graphics.Rect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.wyldsoft.notes.ExcludeRects
import com.wyldsoft.notes.MainActivity
import com.wyldsoft.notes.sdkintegration.DrawingActivity
import com.wyldsoft.notes.pen.PenProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorState {
    var stateExcludeRects = mutableMapOf<ExcludeRects, Rect>()
    var stateExcludeRectsModified by mutableStateOf(false)

    companion object {
        val refreshUi = MutableSharedFlow<Unit>()
        val isStrokeOptionsOpen = MutableSharedFlow<Boolean>()
        val drawingStarted = MutableSharedFlow<Unit>()
        val drawingEnded = MutableSharedFlow<Unit>()
        val forceScreenRefresh = MutableSharedFlow<Unit>()
        val penProfileChanged = MutableSharedFlow<PenProfile>()

        private val _toolbarBounds = MutableStateFlow(ToolbarBounds())
        val toolbarBounds: StateFlow<ToolbarBounds> = _toolbarBounds.asStateFlow()

        private var mainActivity: DrawingActivity? = null

        fun setMainActivity(activity: MainActivity) {
            mainActivity = activity
        }

        fun notifyDrawingStarted() {
            kotlinx.coroutines.GlobalScope.launch {
                drawingStarted.emit(Unit)
                isStrokeOptionsOpen.emit(false)
            }
        }

        fun notifyDrawingEnded() {
            kotlinx.coroutines.GlobalScope.launch {
                drawingEnded.emit(Unit)
                forceScreenRefresh.emit(Unit)
            }
        }

        fun getCurrentExclusionRects(): List<Rect> {
            return mainActivity?.let { activity ->
                // Include toolbar bounds as an exclusion rect
                val bounds = _toolbarBounds.value
                if (bounds.width > 0 && bounds.height > 0) {
                    listOf(bounds.toRect())
                } else {
                    emptyList()
                }
            } ?: emptyList()
        }

        fun updatePenProfile(penProfile: PenProfile) {
            kotlinx.coroutines.GlobalScope.launch {
                penProfileChanged.emit(penProfile)
            }
            mainActivity?.updatePenProfile(penProfile)
        }

        fun updateToolbarBounds(offset: Offset, size: IntSize) {
            _toolbarBounds.value = ToolbarBounds(
                x = offset.x.toInt(),
                y = offset.y.toInt(),
                width = size.width,
                height = size.height
            )
        }

    }
}

data class ToolbarBounds(
    val x: Int = 0,
    val y: Int = 0,
    val width: Int = 0,
    val height: Int = 0
) {
    fun toRect(): Rect = Rect(x, y, x + width, y + height)
}