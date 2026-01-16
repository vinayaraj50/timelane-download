package com.timelane.core.gesture

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.util.VelocityTracker

suspend fun PointerInputScope.detectVerticalDrag(
    onDragStart: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Float) -> Unit
) {
    detectDragGestures(
        onDragStart = onDragStart,
        onDragEnd = onDragEnd,
        onDragCancel = onDragCancel,
        onDrag = { change, dragAmount ->
            onDrag(change, dragAmount.y)
        }
    )
}

// User requested NO fling/velocity based scrolling for time logic, direct drag only.
// Keeping this simple wrapper.
suspend fun PointerInputScope.detectDirectVerticalDrag(
    onDrag: (delta: Float) -> Unit
) {
    detectDragGestures { change, dragAmount ->
        change.consume()
        onDrag(dragAmount.y)
    }
}
