package com.timelane.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.timelane.presentation.calendar.components.EventPopup
import com.timelane.presentation.calendar.components.RoadCanvas
import com.timelane.core.theme.LocalTimeTheme
import com.timelane.domain.model.Event
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.timelane.presentation.main.DragAndDropController

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    scrollToNowKey: Int = 0,
    resetIdleKey: Int = 0,
    dndController: DragAndDropController,
    modifier: Modifier = Modifier,
    tasksHeightFraction: Float = 0.5f
) {
    val theme = LocalTimeTheme.current
    val events by viewModel.events.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    // Calculate dynamic offset to position car based on orientation
    // Portrait: Car just above the tasks panel
    // Landscape: Car at ~75% height (Bottom half)
    val screenHeightDp = config.screenHeightDp.dp
    val canvasHeightPx = with(density) { screenHeightDp.toPx() }
    val ppm = with(density) { 2.dp.toPx() }
    
    val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val calendarHeightPx = canvasHeightPx * (1.0f - tasksHeightFraction)
    
    val targetHeightRatio = if (isLandscape) {
        0.75f 
    } else {
        // Car Length is roughly 160dp. Half is 80dp.
        val carHalfLength = with(density) { 80.dp.toPx() } 
        val gap = with(density) { 10.dp.toPx() }
        val taskPanelTop = calendarHeightPx // Bottom of the calendar area
        val carCenterY = (taskPanelTop - gap - carHalfLength)
        
        (carCenterY / calendarHeightPx).coerceIn(0.1f, 0.9f)
    }
    
    val offsetMinutesFromNow = 0f
    
    // Real-time "Car" time (System time) - Unified State
    var currentRealTime by remember { mutableStateOf(LocalTime.now()) }
    
    val initialOffset = (currentRealTime.hour * 60 + currentRealTime.minute + currentRealTime.second / 60f)
    val scrollOffsetAnim = remember { androidx.compose.animation.core.Animatable(initialOffset) }
    val scrollOffsetMinutes = scrollOffsetAnim.value
    var autoScrollEnabled by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Auto-scroll to keep car at fixed position as time progresses
    LaunchedEffect(Unit) {
        while (true) {
            currentRealTime = LocalTime.now()
            kotlinx.coroutines.delay(1000) // Update every second
            
            val now = System.currentTimeMillis()
            if (!autoScrollEnabled && (now - lastInteractionTime) > 60000) {
                autoScrollEnabled = true
            }
            
            if (autoScrollEnabled) {
                val nowMinutes = currentRealTime.hour * 60 + currentRealTime.minute + currentRealTime.second / 60f
                scrollOffsetAnim.snapTo(nowMinutes - offsetMinutesFromNow)
                
                // Trigger Sound Check (Internal minute check inside ViewModel)
                viewModel.checkEventNotification(LocalDateTime.now())
            }
        }
    }
    
    // Reset idle timer when active from anywhere
    LaunchedEffect(resetIdleKey) {
        if (resetIdleKey > 0) {
            lastInteractionTime = System.currentTimeMillis()
        }
    }
    
    // Reset scroll when scrollToNowKey changes (triggered by Now button)
    LaunchedEffect(scrollToNowKey) {
        if (scrollToNowKey > 0) {
            val nowMinutes = currentRealTime.hour * 60 + currentRealTime.minute
            scrollOffsetAnim.animateTo(
                targetValue = nowMinutes - offsetMinutesFromNow.toFloat(),
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 800)
            )
            autoScrollEnabled = true // Re-enable auto-scroll
        }
    }
    
    var showPopup by remember { mutableStateOf(false) }
    var popupDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    
    val scope = rememberCoroutineScope()
    // snackbarHostState is passed from parent
    
    Surface(
        color = theme.road,
        modifier = modifier // CRITICAL FIX: Use passed modifier to respect MainScreen constraints
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasHeightPx = constraints.maxHeight.toFloat()
            
            // Calculate dynamic offset to position car based on orientation
            // Portrait: Car just above the tasks panel (now bottom of view)
            // Landscape: Car at ~75% height (Bottom half)
            
            val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            
            // In the new layout, this view IS the visible calendar.
            val calendarHeightPx = canvasHeightPx 
            
            val targetHeightRatio = if (isLandscape) {
                0.75f 
            } else {
                // Car Length is roughly 160dp. Half is 80dp.
                val carHalfLength = with(density) { 80.dp.toPx() } 
                val gap = with(density) { 10.dp.toPx() }
                val taskPanelTop = calendarHeightPx // Bottom of the calendar area
                val carCenterY = (taskPanelTop - gap - carHalfLength)
                
                (carCenterY / calendarHeightPx).coerceIn(0.1f, 0.9f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Background Road Canvas
                RoadCanvas(
                    scrollOffsetMinutes = scrollOffsetMinutes,
                    currentRealTime = currentRealTime,
                    events = events,
                    onScroll = { delta ->
                        // delta is already in minutes from RoadCanvas (inverted)
                        // Dragging DOWN (delta > 0) -> Reveal Above (Future) -> scrollOffset INCREASES
                        autoScrollEnabled = false // Disable auto-scroll when user manually scrolls
                        scope.launch { scrollOffsetAnim.snapTo(scrollOffsetAnim.value + delta) }
                    },
                    onTap = { clickedDateTime ->
                        selectedEvent = null
                        popupDateTime = clickedDateTime
                        showPopup = true
                    },
                    onEventClick = { event ->
                        selectedEvent = event
                        popupDateTime = event.startTime
                        showPopup = true
                    },
                    onEventSwipe = { event ->
                        viewModel.deleteEvent(event.id)
                    },
                    onEventUpdate = { event ->
                        viewModel.updateEventSilently(event)
                    },
                    onEventMove = { event ->
                        viewModel.moveEvent(event)
                    },
                    onEventAdd = { event ->
                        viewModel.addEvent(event.title, event.durationMinutes, event.startTime)
                    },
                    onInteraction = {
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    dndController = dndController,
                    onDrop = { _, _ ->
                        // Auto-creation removed as per user request
                    },
                    carHeightRatio = targetHeightRatio,
                    eventFontSize = viewModel.eventFontSize.collectAsState().value,
                    currentDate = currentDate
                )

                
                // Car is drawn inside RoadCanvas for Z-ordering and scrolling logic
                /*
                CarIndicator(
                    time = currentRealTime,
                    color = theme.accent,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 48.dp)
                )
                */
                
                // Global Snackbar is now in MainScreen
                
                // Highlight road during drag
                if (dndController.isDragging) {
                    // Future: Add visual feedback in RoadCanvas
                }
    
                if (showPopup) {
                    val initialTitle = if (selectedEvent == null) {
                        dndController.clipboardTask?.title ?: ""
                    } else {
                        selectedEvent?.title ?: ""
                    }
                    
                    EventPopup(
                        initialTitle = initialTitle,
                        initialDurationMinutes = selectedEvent?.durationMinutes ?: 60,
                        initialSoundRes = selectedEvent?.soundRes,
                        initialDateTime = popupDateTime,
                        onDismiss = { showPopup = false },
                        onDelete = { 
                            selectedEvent?.let { viewModel.deleteEvent(it.id) }
                            showPopup = false 
                        },
                        onDeleteAndPull = { 
                            selectedEvent?.let { viewModel.deleteEventPullingOthers(it) }
                            showPopup = false 
                        },
                        onAdd = { title, duration, soundRes, finalTime ->
                            // Use finalTime from popup which might have been edited
                            val baseTime = finalTime.withSecond(0).withNano(0)
                            if (selectedEvent == null) {
                                viewModel.addEvent(title, duration, baseTime, soundRes)
                            } else {
                                // Update logic (Delete and Re-add for simplicity in one pass)
                                viewModel.deleteEvent(selectedEvent!!.id)
                                viewModel.addEvent(title, duration, baseTime, soundRes)
                            }
                            showPopup = false
                        },
                        onAddAndPush = { title, duration, soundRes, finalTime ->
                            // Use finalTime from popup
                            val baseTime = finalTime.withSecond(0).withNano(0)
                            if (selectedEvent != null) {
                                viewModel.deleteEvent(selectedEvent!!.id)
                            }
                            viewModel.addEventPushingOthers(title, duration, baseTime, soundRes)
                            showPopup = false
                        }
                    )
                }
            }
        }
    }
}
