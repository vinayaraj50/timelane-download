package com.timelane.presentation.main

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import com.timelane.presentation.calendar.CalendarScreen
import com.timelane.presentation.task.TaskScreen
import com.timelane.core.theme.LocalTimeTheme
import com.timelane.core.undo.UndoManager
import com.timelane.core.undo.UndoAction
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.roundToInt
import javax.inject.Inject
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures

@Composable
fun MainScreen() {
    val configuration = LocalConfiguration.current
    
    // State for coordinating "Now" button action between screens
    var scrollToNowTrigger by remember { mutableStateOf(0) }
    var resetIdleTrigger by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val dndController = remember { DragAndDropController() }
    val theme = LocalTimeTheme.current
    
    // Inject UndoManager (Usually via Hilt, but MainScreen is Composable)
    // For now we get it from CalendarViewModel which is the main holder or Hilt Entry Point
    // Since we don't have easy DI here without modifying Activity, we'll access it via CalendarViewModel
    val calendarViewModel: com.timelane.presentation.calendar.CalendarViewModel = hiltViewModel()
    val undoManager = calendarViewModel.undoManagerInstance
    
    // Attach undoManager to dndController for global access in canvas
    LaunchedEffect(undoManager) {
        dndController.undoManager = undoManager
    }
    
    // Auto-dismiss ACTION RECORDED popup after 15 seconds
    LaunchedEffect(undoManager.showActionPopup) {
        if (undoManager.showActionPopup) {
            kotlinx.coroutines.delay(15000) // 15 seconds
            undoManager.dismissActionPopup()
        }
    }
    
    // --- SCREEN SAVER MODE ---
    var isScreenSaverEnabled by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(isScreenSaverEnabled) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            
            if (isScreenSaverEnabled) {
                // ENABLE: Keep Screen On + Hide Bars
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                
                // Auto-Exit Timer (2 Hours)
                kotlinx.coroutines.delay(2L * 60 * 60 * 1000) // 2 hours
                isScreenSaverEnabled = false
            } else {
                // DISABLE: Clear Flag + Show Bars
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeightPx = constraints.maxHeight.toFloat()
        
        // State for resizable tasks panel (Portrait only)
        var tasksHeightFraction by remember { mutableStateOf(0.5f) }
        
        // Logic Refinement: Calculate dynamic action label
        var actionLabel by remember { mutableStateOf("") }
        val lastAction = undoManager.lastAction

        LaunchedEffect(lastAction) {
            actionLabel = when(lastAction) {
                is UndoAction.EventAdded -> {
                     if (calendarViewModel.hasFutureEvents(lastAction.event.startTime)) "PUSH FUTURE" else ""
                }
                is UndoAction.EventMoved -> {
                    if (lastAction.newEvent.startTime.isBefore(lastAction.oldEvent.startTime)) {
                        // Pull logic
                        if (calendarViewModel.hasFutureEvents(lastAction.oldEvent.startTime)) "FILL GAP" else ""
                    } else {
                         // Push logic
                         if (calendarViewModel.hasFutureEvents(lastAction.newEvent.startTime)) "PUSH FUTURE" else ""
                    }
                }
                is UndoAction.EventResized -> {
                    val delta = lastAction.newEvent.durationMinutes - lastAction.oldEvent.durationMinutes
                    if (delta > 0) {
                        // Extending -> Push
                        if (calendarViewModel.hasFutureEvents(lastAction.oldEvent.startTime.plusMinutes(lastAction.oldEvent.durationMinutes.toLong()))) "PUSH FUTURE" else ""
                    } else {
                        // Shrinking -> Fill Gap
                        // Technically "Fill Gap" implies pulling future events back.
                        // If there are future events, we can pull them.
                       if (calendarViewModel.hasFutureEvents(lastAction.oldEvent.startTime.plusMinutes(lastAction.oldEvent.durationMinutes.toLong()))) "FILL GAP" else ""
                    }
                }
                is UndoAction.EventUpdated -> ""
                is UndoAction.EventDeleted -> {
                     if (calendarViewModel.hasFutureEvents(lastAction.event.startTime)) "FILL GAP" else ""
                }
                is UndoAction.TaskDeleted -> "RECOVERED"
                else -> ""
            }
        }
        
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (isPortrait) {
            // PORTRAIT: Vertical Split (Column)
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Calendar View (Top, Takes remaining space)
                CalendarScreen(
                    scrollToNowKey = scrollToNowTrigger,
                    resetIdleKey = resetIdleTrigger,
                    dndController = dndController,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    tasksHeightFraction = 0f // Not used for overlap anymore
                )
                
                // 2. Tasks View (Bottom, Specific height)
                val taskPanelHeight = with(androidx.compose.ui.platform.LocalDensity.current) { (screenHeightPx * tasksHeightFraction).toDp() }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(taskPanelHeight)
                ) {
                    TaskScreen(
                        snackbarHostState = snackbarHostState,
                        dndController = dndController,
                        onNowClicked = { scrollToNowTrigger++ },
                        showActionPopup = undoManager.showActionPopup,
                        actionLabel = actionLabel,
                        onUndo = { calendarViewModel.undo() },
                        onPush = { calendarViewModel.applyFlow() },
                        onPull = { calendarViewModel.applyFlow() },
                        onOk = { undoManager.dismissActionPopup() },
                        onInteraction = { resetIdleTrigger++ },
                        onDeleteAllEvents = { calendarViewModel.deleteAllEvents() },
                        onDeleteOldEvents = { calendarViewModel.deleteOldEvents() },
                        onToggleScreenSaver = { isScreenSaverEnabled = !isScreenSaverEnabled },
                        isScreenSaverEnabled = isScreenSaverEnabled
                    )
                    
                    // Re-sizable Handle (Top of Tasks Panel)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(30.dp) // Touch target
                            .offset(y = (-15).dp) // Center on the divder
                            .zIndex(100f)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    change.consume()
                                    val delta = -dragAmount
                                    val fractionDelta = delta / screenHeightPx
                                    tasksHeightFraction = (tasksHeightFraction + fractionDelta).coerceIn(0.2f, 0.85f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Visual Handle Pill
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(4.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.White.copy(alpha = 0.5f), CircleShape)
                                .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                }
            }
        } else {
            // LANDSCAPE: Side-by-Side (Keep existing logic roughly)
            Box(Modifier.fillMaxSize()) {
                 CalendarScreen(
                    scrollToNowKey = scrollToNowTrigger,
                    resetIdleKey = resetIdleTrigger,
                    dndController = dndController,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterStart),
                    tasksHeightFraction = 0f
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterEnd)
                ) {
                      TaskScreen(
                        snackbarHostState = snackbarHostState,
                        dndController = dndController,
                        onNowClicked = { scrollToNowTrigger++ },
                        showActionPopup = undoManager.showActionPopup,
                        actionLabel = actionLabel,
                        onUndo = { calendarViewModel.undo() },
                        onPush = { calendarViewModel.applyFlow() },
                        onPull = { calendarViewModel.applyFlow() },
                        onOk = { undoManager.dismissActionPopup() },
                        onInteraction = { resetIdleTrigger++ },
                        onDeleteAllEvents = { calendarViewModel.deleteAllEvents() },
                        onDeleteOldEvents = { calendarViewModel.deleteOldEvents() },
                        onToggleScreenSaver = { isScreenSaverEnabled = !isScreenSaverEnabled },
                        isScreenSaverEnabled = isScreenSaverEnabled
                    )
                }
            }
        }

        // 3. Global Snackbar (High Visibility)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1000f)
                .padding(bottom = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 400.dp else 16.dp)
        )

        if (dndController.isDragging && dndController.draggingTask != null) {
            Box(
                modifier = Modifier
                    .offset { 
                        IntOffset(
                            (dndController.dragOffset.x - dndController.dragTouchOffset.x).roundToInt(),
                            (dndController.dragOffset.y - dndController.dragTouchOffset.y).roundToInt()
                        )
                    }
                    .width(320.dp) 
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(4.dp), spotColor = theme.glowPrimary.copy(alpha=0.5f))
                    .background(Color(0xFF0A0A0A).copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f))
                    .padding(12.dp)
                    .zIndex(2000f),
                contentAlignment = Alignment.CenterStart
            ) {
                 Row(
                     verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.fillMaxWidth()
                 ) {
                     // Mimic TaskItem status circle
                     Box(
                         modifier = Modifier
                             .size(24.dp)
                             .background(Color.White.copy(alpha = 0.1f), CircleShape)
                             .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                     )
                     
                     Spacer(modifier = Modifier.width(16.dp))
                     
                     Text(
                        text = dndController.draggingTask!!.title,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                 }
            }
        }
    }
}
