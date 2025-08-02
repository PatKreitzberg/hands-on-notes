package com.wyldsoft.notes.domain.models

import android.graphics.PointF
import java.util.UUID

data class Shape(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType = ShapeType.STROKE,
    val points: List<PointF>,
    val strokeWidth: Float,
    val strokeColor: Int,
    val pressure: List<Float> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class ShapeType {
    STROKE,
    RECTANGLE,
    CIRCLE,
    TRIANGLE,
    LINE
}