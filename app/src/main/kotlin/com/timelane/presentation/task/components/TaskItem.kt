package com.timelane.presentation.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.timelane.core.theme.LocalTimeTheme
import com.timelane.domain.model.Task
import com.timelane.presentation.main.DragAndDropController
import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    fontSize: Float = 16f,
    onComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onCopy: (Task) -> Unit,
    onAddSubtask: (Long) -> Unit = {},
    dndController: DragAndDropController? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalTimeTheme.current
    val alpha = if (task.isCompleted) 0.6f else 1f
    
    var handlePosition by remember { mutableStateOf(Offset.Zero) }
    var rowPosition by remember { mutableStateOf(Offset.Zero) }
    var isExpanded by remember { mutableStateOf(true) }
    
    // Theme colors for neumorphism
    val backgroundColor = Color(0xFF0A0A0A) 
    val glowColor = if (task.isCompleted) Color(0xFF00FF88) else Color(0xFFFF0000)
    
    Column(modifier = modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 8.dp)
                .onGloballyPositioned { rowPosition = it.positionInWindow() }
                // REALISTIC NEON GLOW VS2
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true
                        }
                        
                        val baseGlowColor = glowColor.toArgb()
                        
                        // 1. AMBIENT FLOOD (The "atmosphere" lit by the neon)
                        // Very wide blur, very low opacity
                        paint.color = baseGlowColor
                        paint.maskFilter = BlurMaskFilter(40.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        paint.alpha = 20 // ~8% (Reduced 30% from 28)
                        canvas.nativeCanvas.drawRoundRect(
                            -20.dp.toPx(), -20.dp.toPx(), 
                            size.width + 20.dp.toPx(), size.height + 20.dp.toPx(),
                            30.dp.toPx(), 30.dp.toPx(),
                            paint
                        )

                        // 2. PRIMARY GLOW (The main colored light)
                        // Medium blur
                        paint.maskFilter = BlurMaskFilter(12.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        paint.alpha = 88 // ~35% (Reduced 30% from 126)
                        canvas.nativeCanvas.drawRoundRect(
                            -4.dp.toPx(), -4.dp.toPx(), 
                            size.width + 4.dp.toPx(), size.height + 4.dp.toPx(),
                            16.dp.toPx(), 16.dp.toPx(),
                            paint
                        )
                        
                        // 3. INNER INTENSITY (Tight colored aura)
                        paint.maskFilter = BlurMaskFilter(4.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        paint.alpha = 125 // ~50% (Reduced 30% from 178)
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, 
                            size.width, size.height,
                            12.dp.toPx(), 12.dp.toPx(),
                            paint
                        )

                        // 4. HOT CORE (The physical "tube" or white-hot center)
                        // Almost white, very slight blur or solid
                        paint.color = glowColor.copy(alpha = 0.5f).compositeOver(Color.White).toArgb()
                        paint.maskFilter = BlurMaskFilter(2.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        paint.alpha = 140 // Reduced 30% from 200
                        
                        // Draw as a stroke or filled border to look like a tube? 
                        // For a "box" glow, we usually just want the edge to be hot.
                        // But here we are drawing a filled rounded rect behind. 
                        // Let's make the core a bit more white/bright to sell the "light source" effect.
                        paint.color = Color.White.toArgb()
                        paint.alpha = 50 // Reduced 30% from 70
                        paint.style = android.graphics.Paint.Style.STROKE
                        paint.strokeWidth = 2.dp.toPx()
                        canvas.nativeCanvas.drawRoundRect(
                            1.dp.toPx(), 1.dp.toPx(), 
                            size.width - 1.dp.toPx(), size.height - 1.dp.toPx(),
                            12.dp.toPx(), 12.dp.toPx(),
                            paint
                        )
                    }
                }
                .background(
                    color = Color(0xFF252525), // Neumorphic Surface
                    shape = RoundedCornerShape(12.dp)
                )
                .combinedClickable(
                    onClick = { /* Check handled by checkbox, or open detail? */ },
                    onLongClick = { onAddSubtask(task.id) }
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator (Checkbox)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onComplete(task) }
                    .shadow(elevation = 0.dp)
                    .background(
                        color = if (task.isCompleted) Color(0xFF00FF88).copy(alpha = 0.2f) else Color(0xFF1A1A1A),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.White.copy(alpha = 0.1f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Subtask Toggle (Plus/Chevron)
            if (task.subtasks.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded) androidx.compose.material.icons.Icons.Default.KeyboardArrowDown else androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
                    contentDescription = "Toggle Subtasks",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { isExpanded = !isExpanded }
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                 // Spacing to align titles if strictly needed, or just flow naturally
            }
            
            // Task Title
            Text(
                text = task.title,
                color = theme.textPrimary,
                modifier = Modifier.weight(1f).alpha(alpha),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
            )
            
            // Grip Icon - RESTRICTED DRAG HERE
            // Only show grip for root tasks OR if we allow subtask dragging (disabled for now for simplicity)
            if (dndController != null) {
                 Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                        .onGloballyPositioned { handlePosition = it.positionInWindow() }
                        .pointerInput(task.id) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val absoluteTouch = handlePosition + offset
                                    dndController.startDragging(task, absoluteTouch, rowPosition)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dndController.updateOffset(dragAmount)
                                },
                                onDragEnd = {
                                    dndController.stopDragging(dndController.dragOffset)
                                },
                                onDragCancel = {
                                    dndController.reset()
                                }
                            )
                        }
                )
            }
        }
        
        // Subtasks Recursive List
        if (isExpanded && task.subtasks.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp) // Indentation
            ) {
                task.subtasks.forEach { subtask ->
                    // SWIPE ACTIONS FOR SUBTASKS
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when (it) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    onDelete(subtask)
                                    true
                                }
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    onCopy(subtask)
                                    false // Don't dismiss
                                }
                                else -> false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF4444).copy(alpha = 0.6f)
                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF00FF88).copy(alpha = 0.3f)
                                    else -> Color.Transparent
                                },
                                label = "SubtaskSwipeBg"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp) // Match item padding mostly
                                    .background(color, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp)
                                    .height(50.dp), // Approx height or let it fill
                                contentAlignment = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                                } else if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                                    Icon(Icons.Default.Add, "Copy", tint = Color.Black)
                                }
                            }
                        },
                        content = {
                             TaskItem(
                                task = subtask,
                                fontSize = fontSize * 0.9f, 
                                onComplete = onComplete,
                                onDelete = onDelete,
                                onCopy = onCopy,
                                onAddSubtask = onAddSubtask,
                                dndController = null // Recursively null for safety
                            )
                        },
                        enableDismissFromStartToEnd = true
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
