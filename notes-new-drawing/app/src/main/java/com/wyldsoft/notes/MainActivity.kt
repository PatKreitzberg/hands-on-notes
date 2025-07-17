// MainActivity.kt - Refactored
package com.wyldsoft.notes

import com.wyldsoft.notes.sdkintegration.DrawingActivity

/**
 * Main activity that uses Onyx SDK implementation.
 * This class now simply extends the Onyx-specific implementation.
 *
 * To support a different SDK in the future, you would:
 * 1. Create a new implementation like HuionDrawingActivity
 * 2. Change this class to extend that implementation instead
 * 3. Or use a factory pattern to choose the implementation at runtime
 */
class MainActivity : DrawingActivity() {

    companion object {
    }

    // MainActivity can add any app-specific functionality here
    // while inheriting all the drawing capabilities from OnyxDrawingActivity
}