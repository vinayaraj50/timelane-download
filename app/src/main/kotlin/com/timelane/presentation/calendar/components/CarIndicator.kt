package com.timelane.presentation.calendar.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CarIndicator(
    time: LocalTime,
    color: Color = Color(0xFFFF5722), // Default orange car
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(60.dp, 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            
            // Car Body (Top down simplified shape)
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.1f, h * 0.05f),
                size = Size(w * 0.8f, h * 0.9f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            
            // Roof/Windshield
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.8f),
                topLeft = Offset(w * 0.15f, h * 0.25f),
                size = Size(w * 0.7f, h * 0.4f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            
            // Headlights (Beams) - optional if night
            // drawCircle(...)
        }
        
        // Time Printed on Roof
        Text(
            text = time.format(DateTimeFormatter.ofPattern("h:mm")),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
