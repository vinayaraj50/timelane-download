package com.timelane.presentation.calendar.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import com.timelane.core.theme.LocalTimeTheme
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventPopup(
    initialTitle: String = "",
    initialDurationMinutes: Int = 60,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDeleteAndPull: () -> Unit,
    onAdd: (String, Int, Int?, LocalDateTime) -> Unit,
    onAddAndPush: (String, Int, Int?, LocalDateTime) -> Unit,
    initialSoundRes: Int? = null,
    initialDateTime: LocalDateTime = LocalDateTime.now()
) {
    val theme = LocalTimeTheme.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Resolve initial sound from Res ID to Enum
    val initialSound = com.timelane.core.sound.NotificationSound.entries.find { it.resId == initialSoundRes } ?: com.timelane.core.sound.NotificationSound.SILENT

    var title by remember { mutableStateOf(initialTitle) }
    var durationHours by remember { mutableFloatStateOf((initialDurationMinutes / 60).toFloat()) }
    var durationMinutes by remember { mutableFloatStateOf((initialDurationMinutes % 60).toFloat()) }
    var selectedSound by remember { mutableStateOf(initialSound) }
    
    // Time Entry State
    var eventDateTime by remember { mutableStateOf(initialDateTime) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var startTimeText by remember { mutableStateOf(eventDateTime.format(timeFormatter)) }
    
    val totalDurationMinutes = (durationHours.toInt() * 60 + durationMinutes.toInt()).coerceAtLeast(5)
    var endTimeText by remember { mutableStateOf(eventDateTime.plusMinutes(totalDurationMinutes.toLong()).format(timeFormatter)) }

    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    // Sync End Time when start or duration changes
    LaunchedEffect(eventDateTime, durationHours, durationMinutes) {
        val total = (durationHours.toInt() * 60 + durationMinutes.toInt()).coerceAtLeast(5)
        endTimeText = eventDateTime.plusMinutes(total.toLong()).format(timeFormatter)
        startTimeText = eventDateTime.format(timeFormatter)
    }

    // Responsive spacing
    val verticalSpacing = if (isLandscape) 8.dp else 16.dp
    val sectionSpacing = if (isLandscape) 12.dp else 24.dp
    val contentPadding = if (isLandscape) 16.dp else 24.dp
    val buttonHeight = if (isLandscape) 44.dp else 56.dp
    
    // Use initial default of 30 mins if it's a new event
    LaunchedEffect(Unit) {
        if (initialTitle.isEmpty() && initialDurationMinutes == 60) {
            durationHours = 0f
            durationMinutes = 30f
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, 
            decorFitsSystemWindows = false
        )
    ) {
        // Full screen Box to handle system bars and IME
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // Shrink box when keyboard appears
                .padding(16.dp), // Check for system bars if needed, often handled by decorFitsSystemWindows=false default
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(if (isLandscape) 24.dp else 32.dp),
                color = Color(0xE0101820),
                modifier = Modifier
                    .then(
                        if (isLandscape) {
                            Modifier
                                .fillMaxWidth(0.7f)
                                .heightIn(max = configuration.screenHeightDp.dp * 0.9f)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = configuration.screenHeightDp.dp * 0.8f) // Reduced max height to ensure space
                        }
                    )
                    .shadow(
                        elevation = 16.dp, 
                        shape = RoundedCornerShape(if (isLandscape) 24.dp else 32.dp),
                        spotColor = theme.glowPrimary.copy(alpha = 0.5f),
                        ambientColor = theme.glowPrimary.copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(theme.glowPrimary.copy(alpha=0.6f), theme.glowSecondary.copy(alpha=0.4f))
                        ),
                        shape = RoundedCornerShape(if (isLandscape) 24.dp else 32.dp)
                    )
            ) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .verticalScroll(scrollState)
                    .heightIn(max = configuration.screenHeightDp.dp - 100.dp) // Ensure it doesn't exceed screen
            ) {
                Text(
                    text = if (initialTitle.isEmpty()) "NEW PLAN" else "EDIT TASK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.glowPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(verticalSpacing))
                
                // Studio Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Describe event...", color = theme.textSecondary.copy(alpha=0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = theme.glowPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha=0.1f),
                        cursorColor = theme.glowPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    singleLine = isLandscape // Single line in landscape to save space
                )
                
                Spacer(modifier = Modifier.height(sectionSpacing))
                
                // Time Entry Boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                         Text(
                            "START TIME", 
                            color = theme.glowPrimary, 
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = startTimeText,
                            onValueChange = { newVal ->
                                startTimeText = newVal
                                if (newVal.length == 5) {
                                    try {
                                        val parsed = LocalTime.parse(newVal)
                                        eventDateTime = eventDateTime.toLocalDate().atTime(parsed)
                                    } catch (e: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = theme.glowPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha=0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                         Text(
                            "END TIME", 
                            color = theme.glowPrimary, 
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = endTimeText,
                            onValueChange = { newVal ->
                                endTimeText = newVal
                                if (newVal.length == 5) {
                                    try {
                                        val parsed = LocalTime.parse(newVal)
                                        val endDateTime = eventDateTime.toLocalDate().atTime(parsed)
                                        val diff = Duration.between(eventDateTime, endDateTime).toMinutes()
                                        if (diff > 0) {
                                            durationHours = (diff / 60).toFloat()
                                            durationMinutes = (diff % 60).toFloat()
                                        }
                                    } catch (e: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = theme.glowPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha=0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Duration Section
                Text(
                    "DURATION", 
                    color = theme.glowPrimary, 
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black)
                )
                
                Spacer(modifier = Modifier.height(verticalSpacing))

                TimeSlider(
                    label = "HOURS",
                    value = durationHours,
                    onValueChange = { durationHours = it },
                    valueRange = 0f..8f,
                    unit = "HR",
                    compact = isLandscape
                )
                
                Spacer(modifier = Modifier.height(verticalSpacing))
                
                TimeSlider(
                    label = "MINUTES",
                    value = durationMinutes,
                    onValueChange = { durationMinutes = (it / 5).toInt() * 5f }, // Snap to 5 mins
                    valueRange = 0f..55f,
                    unit = "MIN",
                    compact = isLandscape
                )
                
                Spacer(modifier = Modifier.height(sectionSpacing))
                
                 // Sound Selection
                Text(
                    "NOTIFICATION SOUND", 
                    color = theme.glowPrimary, 
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Black)
                )
                
                Spacer(modifier = Modifier.height(verticalSpacing))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.timelane.core.sound.NotificationSound.entries.forEach { sound ->
                        val isSelected = selectedSound == sound
                        val bgColor = if (isSelected) theme.glowPrimary else Color.White.copy(alpha = 0.05f)
                        val textColor = if (isSelected) Color.Black else Color.White
                        
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .border(1.dp, if (isSelected) theme.glowPrimary else Color.White.copy(alpha=0.2f), RoundedCornerShape(50))
                                .background(bgColor, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable { selectedSound = sound }
                        ) {
                             Text(
                                text = sound.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(sectionSpacing))
                
                val totalDuration = (durationHours.toInt() * 60 + durationMinutes.toInt()).coerceAtLeast(5)

                // Use FlowRow for buttons to allow wrapping in landscape if needed
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (initialTitle.isNotEmpty()) {
                        StudioActionButton(
                            text = "DELETE",
                            icon = Icons.Default.Delete,
                            color = Color(0xFFFF3B30),
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            height = buttonHeight,
                            compact = isLandscape
                        )
                    }
                    StudioActionButton(
                        text = if (initialTitle.isEmpty()) "CREATE" else "UPDATE",
                        icon = if (initialTitle.isEmpty()) Icons.Default.Add else Icons.Default.Check,
                        color = theme.glowPrimary,
                        onClick = { onAdd(title, totalDuration, selectedSound.resId, eventDateTime) },
                        modifier = Modifier.weight(1f),
                        height = buttonHeight,
                        compact = isLandscape
                    )
                }
                
                // Add contextual options
                if (initialTitle.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StudioActionButton(
                        text = "PUSH FUTURE",
                        icon = Icons.Default.ArrowDownward,
                        color = theme.glowSecondary,
                        onClick = { onAddAndPush(title, totalDuration, selectedSound.resId, eventDateTime) },
                        modifier = Modifier.fillMaxWidth(),
                        height = buttonHeight,
                        compact = isLandscape
                    )
                }
            }
        }
    }
}
}


@Composable
private fun StudioActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 56.dp,
    compact: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(if (compact) 16.dp else 24.dp),
        modifier = modifier.height(height),
        contentPadding = if (compact) {
            androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        } else {
            ButtonDefaults.ContentPadding
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(if (compact) 14.dp else 18.dp), 
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(if (compact) 4.dp else 8.dp))
            Text(
                text = text, 
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 0.5.sp,
                    fontSize = if (compact) 11.sp else 14.sp
                ),
                color = Color.Black,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

