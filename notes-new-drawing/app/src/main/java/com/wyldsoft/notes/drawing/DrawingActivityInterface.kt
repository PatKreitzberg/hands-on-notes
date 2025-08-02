package com.wyldsoft.notes.drawing

import android.graphics.PointF
import com.wyldsoft.notes.presentation.viewmodel.EditorViewModel

interface DrawingActivityInterface {
    fun setViewModel(viewModel: EditorViewModel)
    fun onShapeCompleted(points: List<PointF>, pressures: List<Float>)
}