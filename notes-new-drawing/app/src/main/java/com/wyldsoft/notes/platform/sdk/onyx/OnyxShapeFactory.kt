package com.wyldsoft.notes.platform.sdk.onyx

import com.onyx.android.sdk.data.note.TouchPoint as OnyxTouchPoint
import com.onyx.android.sdk.pen.data.TouchPointList
import com.wyldsoft.notes.core.touch.TouchPoint
import com.wyldsoft.notes.platform.sdk.DefaultShapeFactory

/**
 * Onyx-specific shape factory that converts Onyx touch points to our model.
 */
class OnyxShapeFactory : DefaultShapeFactory() {
    
    override fun convertTouchPoints(sdkPoints: Any): List<TouchPoint> {
        return when (sdkPoints) {
            is TouchPointList -> {
                sdkPoints.points?.map { onyxPoint ->
                    TouchPoint(
                        x = onyxPoint.x,
                        y = onyxPoint.y,
                        pressure = onyxPoint.pressure,
                        timestamp = onyxPoint.timestamp
                    )
                } ?: emptyList()
            }
            is List<*> -> {
                sdkPoints.filterIsInstance<OnyxTouchPoint>().map { onyxPoint ->
                    TouchPoint(
                        x = onyxPoint.x,
                        y = onyxPoint.y,
                        pressure = onyxPoint.pressure,
                        timestamp = onyxPoint.timestamp
                    )
                }
            }
            else -> emptyList()
        }
    }
}