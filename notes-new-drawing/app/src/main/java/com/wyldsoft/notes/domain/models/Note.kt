package com.wyldsoft.notes.domain.models

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled",
    val shapes: MutableList<Shape> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val template: Template? = null
)

data class Template(
    val type: TemplateType,
    val spacing: Float = 20f
)

enum class TemplateType {
    BLANK,
    GRID,
    RULED,
    DOTTED
}