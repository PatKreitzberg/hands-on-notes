package com.wyldsoft.notes;

import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.data.TouchPointList;


public class EraseArgs {
    public TouchPointList eraseTrackPoints;
    public float eraserWidth = 20f;
    public float drawRadius = eraserWidth / 2;
    public boolean showEraseCircle;
    private int eraserType;

    public TouchPoint getErasePoint() {
        TouchPoint erasePoint = eraseTrackPoints.get(eraseTrackPoints.size() - 1);
        return erasePoint;
    }
}