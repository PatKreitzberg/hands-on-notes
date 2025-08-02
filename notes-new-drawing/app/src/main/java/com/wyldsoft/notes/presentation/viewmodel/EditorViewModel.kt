package com.wyldsoft.notes.presentation.viewmodel

import android.graphics.PointF
import android.graphics.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wyldsoft.notes.data.repository.NoteRepository
import com.wyldsoft.notes.domain.models.Shape
import com.wyldsoft.notes.domain.models.ShapeType
import com.wyldsoft.notes.pen.PenProfile
import com.wyldsoft.notes.viewport.ViewportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class EditorViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()
    
    val currentNote = noteRepository.getCurrentNote()
    
    private val _isDrawing = MutableStateFlow(false)
    val isDrawing: StateFlow<Boolean> = _isDrawing.asStateFlow()
    
    private val _currentPenProfile = MutableStateFlow(PenProfile.defaultProfiles[0])
    val currentPenProfile: StateFlow<PenProfile> = _currentPenProfile.asStateFlow()
    
    private val _refreshUi = MutableStateFlow(0L)
    val refreshUi: StateFlow<Long> = _refreshUi.asStateFlow()
    
    private val _excludeRects = MutableStateFlow<List<Rect>>(emptyList())
    val excludeRects: StateFlow<List<Rect>> = _excludeRects.asStateFlow()
    
    // Viewport manager for coordinate transformations
    val viewportManager = ViewportManager()
    val viewportState = viewportManager.viewportState
    
    init {
        viewModelScope.launch {
            if (currentNote.value == null) {
                noteRepository.createNewNote()
            }
        }
        
        // Save viewport state when it changes (with debounce to avoid too frequent saves)
        viewModelScope.launch {
            viewportState
                .debounce(500) // Wait 500ms after last change before saving
                .collect { state ->
                    currentNote.value?.let { note ->
                        noteRepository.updateViewportState(
                            note.id,
                            state.scale,
                            state.offsetX,
                            state.offsetY
                        )
                    }
                }
        }
        
        // Restore viewport state when note changes
        viewModelScope.launch {
            currentNote.collect { note ->
                note?.let {
                    viewportManager.setState(
                        it.viewportScale,
                        it.viewportOffsetX,
                        it.viewportOffsetY
                    )
                }
            }
        }
    }
    
    fun startDrawing() {
        _isDrawing.value = true
        _uiState.value = _uiState.value.copy(isStrokeOptionsOpen = false)
    }
    
    fun endDrawing() {
        _isDrawing.value = false
        forceRefresh()
    }
    
    fun addShape(points: List<PointF>, pressures: List<Float> = emptyList()) {
        viewModelScope.launch {
            currentNote.value?.let { note ->
                val shape = Shape(
                    type = ShapeType.STROKE,
                    points = points,
                    strokeWidth = _currentPenProfile.value.strokeWidth,
                    strokeColor = _currentPenProfile.value.getColorAsInt(),
                    pressure = pressures
                )
                noteRepository.addShape(note.id, shape)
            }
        }
    }
    
    fun updatePenProfile(profile: PenProfile) {
        _currentPenProfile.value = profile
    }
    
    fun toggleStrokeOptions() {
        _uiState.value = _uiState.value.copy(
            isStrokeOptionsOpen = !_uiState.value.isStrokeOptionsOpen
        )
    }
    
    fun updateExclusionZones(rects: List<Rect>) {
        _excludeRects.value = rects
    }
    
    fun forceRefresh() {
        _refreshUi.value = System.currentTimeMillis()
    }
}

data class EditorUiState(
    val isStrokeOptionsOpen: Boolean = false,
    val selectedTool: Tool = Tool.PEN
)

enum class Tool {
    PEN,
    ERASER,
    SELECTOR,
    GEOMETRY
}