package com.timelane.presentation.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timelane.core.theme.LocalTimeTheme

@Composable
fun TimeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val theme = LocalTimeTheme.current
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 10.sp else 11.sp
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(theme.glowPrimary.copy(alpha = 0.1f), RoundedCornerShape(if (compact) 6.dp else 8.dp))
                    .border(1.dp, theme.glowPrimary.copy(alpha = 0.2f), RoundedCornerShape(if (compact) 6.dp else 8.dp))
                    .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 2.dp else 4.dp)
            ) {
                Text(
                    text = "${value.toInt()} $unit",
                    color = theme.glowPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = if (compact) 12.sp else 14.sp
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = if (valueRange.endInclusive > 23f) 0 else (valueRange.endInclusive.toInt() - 1),
            colors = SliderDefaults.colors(
                thumbColor = theme.glowPrimary,
                activeTrackColor = theme.glowPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.height(if (compact) 24.dp else 32.dp)
        )
    }
}

