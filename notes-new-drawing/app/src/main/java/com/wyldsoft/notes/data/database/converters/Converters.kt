package com.wyldsoft.notes.data.database.converters

import androidx.room.TypeConverter
import android.graphics.PointF
import com.wyldsoft.notes.domain.models.ShapeType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable

class Converters {
    
    @TypeConverter
    fun fromShapeType(type: ShapeType): String {
        return type.name
    }
    
    @TypeConverter
    fun toShapeType(typeName: String): ShapeType {
        return ShapeType.valueOf(typeName)
    }
    
    @TypeConverter
    fun fromPointFList(points: List<PointF>): String {
        val pointData = points.map { PointData(it.x, it.y) }
        return Json.encodeToString(pointData)
    }
    
    @TypeConverter
    fun toPointFList(pointsJson: String): List<PointF> {
        val pointData = Json.decodeFromString<List<PointData>>(pointsJson)
        return pointData.map { PointF(it.x, it.y) }
    }
    
    @TypeConverter
    fun fromFloatList(floats: List<Float>): String {
        return Json.encodeToString(floats)
    }
    
    @TypeConverter
    fun toFloatList(floatsJson: String): List<Float> {
        return Json.decodeFromString(floatsJson)
    }
}

@Serializable
data class PointData(val x: Float, val y: Float)