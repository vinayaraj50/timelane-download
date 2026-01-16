package com.timelane.presentation.calendar.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timelane.R
import com.timelane.core.theme.LocalTimeTheme
import com.timelane.domain.model.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.timelane.presentation.main.DragAndDropController
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.timelane.domain.model.Task

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

data class EventLayout(
    val event: Event,
    val rect: Rect,
    val columnIndex: Int,
    val totalColumns: Int
)

enum class InteractionMode { NONE, MOVE, RESIZE_TOP, RESIZE_BOTTOM, SWIPE, PASS_THROUGH }

@Composable
fun RoadCanvas(
    scrollOffsetMinutes: Float, // Center Time (Time at viewport center)
    @Suppress("UNUSED_PARAMETER") currentRealTime: LocalTime,
    events: List<Event>,
    onScroll: (Float) -> Unit, // Delta scroll (minutes)
    onTap: (LocalDateTime) -> Unit, // Tap for new event (date+time)
    onEventClick: (Event) -> Unit, // New parameter for editing
    modifier: Modifier = Modifier,
    onEventSwipe: (Event) -> Unit = {}, // Swipe to delete
    onEventUpdate: (Event) -> Unit = {}, // Resize
    onEventMove: (Event) -> Unit = {}, // Drag/Move
    @Suppress("UNUSED_PARAMETER") onEventAdd: (Event) -> Unit = {}, // Paste/New from copy
    onInteraction: () -> Unit = {}, // New interaction hook
    dndController: DragAndDropController,
    onDrop: (Task, LocalTime) -> Unit,
    carHeightRatio: Float = 0.5f,
    eventFontSize: Float = 16f,
    currentDate: LocalDate = LocalDate.now()
) {
    val theme = LocalTimeTheme.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val googleSans = remember { 
        try {
            val typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.timelane_font)
            if (typeface != null) FontFamily(typeface) else FontFamily.Default
        } catch (e: Exception) {
            FontFamily.Default
        }
    }
    val textMeasurer = rememberTextMeasurer()
    
    // Animation State
    val scope = rememberCoroutineScope()
    var swipeEventId by remember { mutableStateOf<Long?>(null) }
    val swipeOffset = remember { Animatable(0f) }
    // Clipboard is now in dndController
    
    // Config
    val density = androidx.compose.ui.platform.LocalDensity.current
    val pixelsPerMinute = remember(density) { with(density) { 2.dp.toPx() } }
    val sidewalkWidth = with(density) { 80.dp.toPx() }
    
    // Drag and Drop State
    var canvasPosition by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    
    LaunchedEffect(dndController) {
        dndController.dropHandler = { task, globalOffset ->
            val localOffset = globalOffset - canvasPosition
            
            // Layout Boundaries
            val roadLeft = sidewalkWidth
            val roadRight = canvasSize.width
            
            // Hit Test for Road
            if (localOffset.x >= roadLeft.toFloat() && localOffset.x <= roadRight.toFloat()) {
                val anchorY = canvasSize.height * carHeightRatio
                val deltaMinutes = (anchorY - localOffset.y) / pixelsPerMinute
                val touchedMinutes = scrollOffsetMinutes + deltaMinutes
                val totalMinutes = (touchedMinutes.toLong() % 1440 + 1440) % 1440
                val dropTime = LocalTime.of((totalMinutes / 60).toInt(), (totalMinutes % 60).toInt())
                onDrop(task, dropTime)
            }
        }
    }

    // Unified Now from Parent
    val now = currentRealTime
    
    // UI Interaction Tracking
    var activeInteractionId by remember { mutableStateOf<Long?>(null) }
    var interactionMode by remember { mutableStateOf(InteractionMode.NONE) }
    var accumulatedDragMinutes by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var dragDisplayOffset by remember { androidx.compose.runtime.mutableFloatStateOf(0f) } // pixels
    
    // To track resizing for push/pull feature
    var originalEventForResizing by remember { mutableStateOf<Event?>(null) }

    // Time Picker Drag State
    var isTimeDragging by remember { mutableStateOf(false) }
    var timeDragY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var timeDragDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    // Calculate Event Layouts
    val eventLayouts = remember(events, scrollOffsetMinutes, canvasSize) {
        if (canvasSize.width <= 0) emptyList()
        else {
            val width = canvasSize.width
            val height = canvasSize.height
            val anchorY = height * carHeightRatio
            
            val horizontalMargin = with(density) { 12.dp.toPx() }
            val eventLaneStart = width * 0.2f + horizontalMargin
            val eventLaneEnd = width - horizontalMargin
            val eventLaneWidth = eventLaneEnd - eventLaneStart
            
            val currentDateTime = currentDate.atStartOfDay()
            val sorted = events.sortedBy { it.startTime }
            val layouts = mutableListOf<EventLayout>()
            val intermediate = mutableListOf<Triple<Event, Long, Int>>() // Event, Column, TotalCols
            
            sorted.forEach { event ->
                val startMin = ChronoUnit.MINUTES.between(currentDateTime, event.startTime)
                val endMin = startMin + event.durationMinutes
                val overlapping = intermediate.filter { (otherEv, _, _) ->
                    val otherStart = ChronoUnit.MINUTES.between(currentDateTime, otherEv.startTime)
                    val otherEnd = otherStart + otherEv.durationMinutes
                    !(endMin <= otherStart || startMin >= otherEnd)
                }
                val usedCols = overlapping.map { it.second }.toSet()
                var col = 0L
                while (col in usedCols) col++
                val newTotal = maxOf(overlapping.maxOfOrNull { it.third } ?: 1, (col + 1).toInt())
                intermediate.add(Triple(event, col, newTotal))
            }
            
            intermediate.forEach { (event, col, _) ->
                val startMin = ChronoUnit.MINUTES.between(currentDateTime, event.startTime)
                val endMin = startMin + event.durationMinutes
                val overlapping = intermediate.filter { (oE, _, _) ->
                    val oS = ChronoUnit.MINUTES.between(currentDateTime, oE.startTime)
                    val oEnd = oS + oE.durationMinutes
                    !(endMin <= oS || startMin >= oEnd)
                }
                val maxCols = overlapping.maxOf { it.third }
                
                val yStart = anchorY - (startMin.toFloat() - scrollOffsetMinutes) * pixelsPerMinute
                val yEnd = anchorY - (endMin.toFloat() - scrollOffsetMinutes) * pixelsPerMinute
                val colWidth = eventLaneWidth / maxCols.toFloat()
                val evLeft = eventLaneStart + (col.toFloat() * colWidth)
                val evRect = Rect(Offset(evLeft + 4f, yEnd + 4f), Size(colWidth - 8f, (yStart - yEnd) - 8f))
                
                layouts.add(EventLayout(event, evRect, col.toInt(), maxCols))
            }
            layouts
        }
    }

    // --- STATE & RESOURCES ---
    val currentEventLayouts by androidx.compose.runtime.rememberUpdatedState(eventLayouts)
    val currentEvents by androidx.compose.runtime.rememberUpdatedState(events)
    val currentScrollOffsetMinutes by androidx.compose.runtime.rememberUpdatedState(scrollOffsetMinutes)
    
    val carPainter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_car_new)
    val bgRoadPainter = androidx.compose.ui.res.painterResource(id = R.drawable.bg_road)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                canvasPosition = coordinates.positionInWindow()
                canvasSize = Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
            }
            .pointerInput(Unit) { // Consolidated Gesture Block
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onInteraction()
                    var dragHandled = false
                    
                    // 1. Initial Hit Test
                    val hit = currentEventLayouts.find { it.rect.contains(down.position) }
                    
                    if (hit != null) {
                        activeInteractionId = hit.event.id
                        val rect = hit.rect
                        val hitHeight = rect.height
                        val hitSize = with(density) { 
                            val minSize = 44.dp.toPx()
                            if (hitHeight < minSize * 2) (hitHeight / 3f) // Tight hit zones for short events to avoid overlap
                            else minSize
                        }
                        
                        val topHandleRect = Rect(Offset(rect.right - 44.dp.toPx(), rect.top), Size(44.dp.toPx(), hitSize))
                        val botHandleRect = Rect(Offset(rect.right - 44.dp.toPx(), rect.bottom - hitSize), Size(44.dp.toPx(), hitSize))
                        val midHandleRect = Rect(Offset(rect.right - 44.dp.toPx(), rect.top + rect.height/2 - hitSize/2), Size(44.dp.toPx(), hitSize))
                        
                        interactionMode = when {
                            // Prioritize MOVE for events between 30 and 45 mins because resize handles overlap too much
                            midHandleRect.contains(down.position) && hit.event.durationMinutes in 30..45 -> InteractionMode.MOVE
                            topHandleRect.contains(down.position) -> InteractionMode.RESIZE_TOP
                            botHandleRect.contains(down.position) -> InteractionMode.RESIZE_BOTTOM
                            midHandleRect.contains(down.position) && hit.event.durationMinutes >= 30 -> InteractionMode.MOVE
                            else -> InteractionMode.PASS_THROUGH
                        }

                        if (interactionMode == InteractionMode.RESIZE_TOP || interactionMode == InteractionMode.RESIZE_BOTTOM) {
                            originalEventForResizing = hit.event
                        } else {
                            originalEventForResizing = null
                        }
                        
                        accumulatedDragMinutes = 0f
                        dragDisplayOffset = 0f

                        // Handle EVENT Interaction (Drag)
                        var lastSwipeX = 0f
                        
                        drag(down.id) { change ->
                            val dragAmount = change.positionChange()
                            if (dragAmount != Offset.Zero) {
                                dragHandled = true
                                change.consume()
                                val currentEvent = currentEvents.find { it.id == activeInteractionId }
                                
                                if (currentEvent != null) {
                                    when (interactionMode) {
                                        InteractionMode.PASS_THROUGH -> {
                                            if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y) && swipeEventId == null) {
                                                swipeEventId = activeInteractionId
                                                interactionMode = InteractionMode.SWIPE
                                                lastSwipeX = dragAmount.x
                                                scope.launch { swipeOffset.snapTo(lastSwipeX) }
                                            } else {
                                                onScroll(dragAmount.y / pixelsPerMinute)
                                            }
                                        }
                                        InteractionMode.SWIPE -> {
                                            lastSwipeX += dragAmount.x
                                            scope.launch { swipeOffset.snapTo(lastSwipeX) }
                                        }
                                         InteractionMode.MOVE -> {
                                            dragDisplayOffset += dragAmount.y
                                            accumulatedDragMinutes += dragAmount.y / pixelsPerMinute
                                            
                                            // Auto-scroll
                                            val scrollThreshold = 100f
                                            if (change.position.y < scrollThreshold) {
                                                onScroll(5f)
                                                accumulatedDragMinutes += 5f
                                            } else if (change.position.y > size.height - scrollThreshold) {
                                                onScroll(-5f)
                                                accumulatedDragMinutes -= 5f
                                            }
                                        }
                                        InteractionMode.RESIZE_TOP -> {
                                            accumulatedDragMinutes += dragAmount.y / pixelsPerMinute
                                            if (kotlin.math.abs(accumulatedDragMinutes) >= 1f) {
                                                val delta = accumulatedDragMinutes.roundToInt()
                                                val newDuration = (currentEvent.durationMinutes - delta).coerceAtLeast(15)
                                                onEventUpdate(currentEvent.copy(durationMinutes = newDuration))
                                                accumulatedDragMinutes -= delta.toFloat()
                                            }
                                        }
                                        InteractionMode.RESIZE_BOTTOM -> {
                                            accumulatedDragMinutes += dragAmount.y / pixelsPerMinute
                                            if (kotlin.math.abs(accumulatedDragMinutes) >= 1f) {
                                                val delta = accumulatedDragMinutes.roundToInt()
                                                val newDuration = (currentEvent.durationMinutes + delta).coerceAtLeast(15)
                                                val actualAddedMinutes = newDuration - currentEvent.durationMinutes
                                                onEventUpdate(currentEvent.copy(
                                                    startTime = currentEvent.startTime.minusMinutes(actualAddedMinutes.toLong()),
                                                    durationMinutes = newDuration
                                                ))
                                                accumulatedDragMinutes -= delta.toFloat()
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    
                        // Finalize Interaction
                        if (interactionMode == InteractionMode.SWIPE && swipeEventId != null) {
                            val deleteThreshold = -70f
                            val copyThreshold = 70f
                            
                            when {
                                swipeOffset.value < deleteThreshold -> {
                                    scope.launch {
                                        swipeOffset.animateTo(-size.width.toFloat())
                                        currentEvents.find { it.id == swipeEventId }?.let { onEventSwipe(it) }
                                        swipeEventId = null
                                        swipeOffset.snapTo(0f)
                                    }
                                }
                                 swipeOffset.value > copyThreshold -> {
                                    scope.launch {
                                        swipeOffset.animateTo(0f)
                                        swipeEventId = null
                                    }
                                }
                                else -> {
                                    scope.launch {
                                        swipeOffset.animateTo(0f)
                                        swipeEventId = null
                                    }
                                }
                            }
                        }
                        
                        // Finalize Interaction (Move)
                        if (interactionMode == InteractionMode.MOVE && activeInteractionId != null) {
                            val currentEvent = currentEvents.find { it.id == activeInteractionId }
                            if (currentEvent != null && kotlin.math.abs(accumulatedDragMinutes) >= 1f) {
                                val delta = accumulatedDragMinutes.roundToInt()
                                onEventMove(currentEvent.copy(startTime = currentEvent.startTime.minusMinutes(delta.toLong())))
                            }
                            dragDisplayOffset = 0f
                        }

                        if ((interactionMode == InteractionMode.RESIZE_TOP || interactionMode == InteractionMode.RESIZE_BOTTOM) && activeInteractionId != null) {
                            val currentEvent = currentEvents.find { it.id == activeInteractionId }
                            val original = originalEventForResizing
                            if (currentEvent != null && original != null && currentEvent.durationMinutes != original.durationMinutes) {
                                // Only show PUSH/PULL if there are events later in the day to push/pull
                                val hasFutureEvents = currentEvents.any { it.id != currentEvent.id && it.startTime.isAfter(original.startTime) }
                                
                                if (hasFutureEvents) {
                                    dndController.undoManager?.registerAction(com.timelane.core.undo.UndoAction.EventResized(original, currentEvent))
                                } else {
                                    // Just a normal update, only UNDO will show
                                    dndController.undoManager?.registerAction(com.timelane.core.undo.UndoAction.EventUpdated(original, currentEvent))
                                }
                            }
                        }
                        originalEventForResizing = null

                        // Check for Tap if it wasn't a drag
                        if (!dragHandled) { 
                             val ev = currentEvents.find { it.id == activeInteractionId }
                             if (ev != null) onEventClick(ev)
                        }
                    } else {
                        // Handle BACKGROUND Interaction (Scroll, Tap, or Time Picker Drag)
                        val isLeftArea = down.position.x < size.width * 0.2f
                        
                        if (isLeftArea) {
                            // Time Picker Drag on Left Scale
                            isTimeDragging = true
                            timeDragY = down.position.y
                            
                            // Calculate initial time
                            val anchorY = size.height * carHeightRatio
                            val deltaMinutes = (anchorY - down.position.y) / pixelsPerMinute
                            val touchedMinutes = currentScrollOffsetMinutes + deltaMinutes
                            val dayOffset = kotlin.math.floor(touchedMinutes / 1440.0).toLong()
                            val totalMinutes = touchedMinutes.toLong()
                            val normalizedMinutes = ((totalMinutes % 1440) + 1440) % 1440
                            val clickedTime = LocalTime.of((normalizedMinutes / 60).toInt(), (normalizedMinutes % 60).toInt())
                            timeDragDateTime = LocalDateTime.of(currentDate.plusDays(dayOffset), clickedTime)
                            
                            verticalDrag(down.id) { change ->
                                val dragAmount = change.positionChange()
                                if (dragAmount.y != 0f) {
                                    dragHandled = true
                                    change.consume()
                                    timeDragY = change.position.y
                                    
                                    // Recalculate time based on new Y position
                                    val newDeltaMinutes = (anchorY - change.position.y) / pixelsPerMinute
                                    val newTouchedMinutes = currentScrollOffsetMinutes + newDeltaMinutes
                                    val newDayOffset = kotlin.math.floor(newTouchedMinutes / 1440.0).toLong()
                                    val newTotalMinutes = newTouchedMinutes.toLong()
                                    val newNormalizedMinutes = ((newTotalMinutes % 1440) + 1440) % 1440
                                    val newTime = LocalTime.of((newNormalizedMinutes / 60).toInt(), (newNormalizedMinutes % 60).toInt())
                                    timeDragDateTime = LocalDateTime.of(currentDate.plusDays(newDayOffset), newTime)
                                }
                            }
                            
                            // On Release
                            timeDragDateTime?.let { onTap(it) }
                            isTimeDragging = false
                            timeDragDateTime = null
                        } else {
                            // Normal Scroll on Right Area
                            verticalDrag(down.id) { change ->
                                val dragAmount = change.positionChange()
                                if (dragAmount.y != 0f) {
                                    dragHandled = true
                                    change.consume()
                                    onScroll(dragAmount.y / pixelsPerMinute)
                                }
                            }
                        }
                    }

                    
                    activeInteractionId = null
                    interactionMode = InteractionMode.NONE
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val anchorY = height * carHeightRatio
        
        // --- LAYOUT DEFINITIONS ---
        val timelineWidth = 60.dp.toPx()
        val carLaneCenterX = timelineWidth + 12.dp.toPx() 
        
        // 1. Draw Background
        val bgWidth = width
        val bgHeight = 1440f * pixelsPerMinute
        val midnightY = anchorY - (0 - scrollOffsetMinutes) * pixelsPerMinute
        val pixelOffset = midnightY % bgHeight
        
        var currentTileTop = pixelOffset
        while (currentTileTop > 0) currentTileTop -= bgHeight
        
        while (currentTileTop < height) {
            translate(left = 0f, top = currentTileTop) {
                with(bgRoadPainter) {
                    draw(size = Size(bgWidth, bgHeight))
                }
            }
            currentTileTop += bgHeight
        }
        
        // 4. Render Loop (Time Labels)
        val minutesAbove = anchorY / pixelsPerMinute
        val minutesBelow = (height - anchorY) / pixelsPerMinute
        val timeTop = scrollOffsetMinutes + minutesAbove
        val timeBottom = scrollOffsetMinutes - minutesBelow
        
        val startHour = (timeBottom / 60).toInt() - 1
        val endHour = (timeTop / 60).toInt() + 1
        
        for (h in startHour..endHour) {
            val milestoneMinutes = h * 60
            val yPos = anchorY - (milestoneMinutes - scrollOffsetMinutes) * pixelsPerMinute
            
            val normalizedHour = ((h % 24) + 24) % 24
            val timeText = if (normalizedHour == 12) "12 PM" else if (normalizedHour == 0) "12 AM" else if (normalizedHour > 12) "${normalizedHour-12} PM" else "$normalizedHour AM"
            
            // MEASURE TEXT (Enhanced Visibility - Modern Slim Look)
            val textLayoutResult = textMeasurer.measure(
                text = timeText,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.9f), // Slightly translucent white
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light, // Slim
                    letterSpacing = 1.sp,
                    fontFamily = googleSans
                )
            )
            
            // REMOVED PILL BACKGROUNDS AND BORDERS (Modern minimal look)

            val textX = (timelineWidth - textLayoutResult.size.width) / 2
            val textY = yPos - (textLayoutResult.size.height / 2)
            
            drawText(textLayoutResult, topLeft = Offset(textX, textY))
            
            // Subtle dividing lines - even more subtle now
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(0f, yPos),
                end = Offset(width, yPos),
                strokeWidth = 1f
            )
        }
        
        // 5. Draw Events (Liquid Glass Style)
        eventLayouts.forEach { layout ->
            val event = layout.event
            val rect = layout.rect
            
            if (rect.bottom > 0 && rect.top < height) {
                // Horizontal Fade-out after 80% screen width
                val fadeStart = width * 0.8f
                val eventAlpha = if (rect.left > fadeStart) {
                    (1f - (rect.left - fadeStart) / (width - fadeStart)).coerceIn(0f, 1f)
                } else 1f
                
                val xOffset = if (event.id == swipeEventId) swipeOffset.value else 0f
                val yOffset = if (event.id == activeInteractionId && interactionMode == InteractionMode.MOVE) dragDisplayOffset else 0f
                
                     translate(left = xOffset, top = yOffset) {
                         
                         // Action Visual Indicators during swipe
                         if (event.id == swipeEventId) {
                             if (swipeOffset.value > 20f) {
                                 val copyAlpha = (swipeOffset.value / 100f).coerceIn(0f, 1f)
                                 drawText(
                                     textLayoutResult = textMeasurer.measure("COPYING...", TextStyle(color = Color(0xFF00FF88).copy(alpha = copyAlpha), fontWeight = FontWeight.Black, fontSize = 10.sp, fontFamily = googleSans)),
                                     topLeft = rect.topLeft + Offset(20f, -40f)
                                 )
                             } else if (swipeOffset.value < -20f) {
                                 val delAlpha = (-swipeOffset.value / 100f).coerceIn(0f, 1f)
                                 drawText(
                                     textLayoutResult = textMeasurer.measure("TERMINATING...", TextStyle(color = Color.Red.copy(alpha = delAlpha), fontWeight = FontWeight.Black, fontSize = 10.sp, fontFamily = googleSans)),
                                     topLeft = rect.topLeft + Offset(20f, -40f)
                                 )
                             }
                         }
                         
                     // 1. NEOMORPHIC DEPTH (Dual Shadows)
                     // Darker Shadow (Bottom-Right)
                     drawRoundRect(
                         color = Color.Black.copy(alpha = 0.12f * eventAlpha),
                         topLeft = rect.topLeft + Offset(8f, 8f),
                         size = rect.size,
                         cornerRadius = CornerRadius(28f, 28f)
                     )

                     // (INTENSE GLOW RIM REMOVED)
                     // Lighter Highlight (Top-Left)
                     drawRoundRect(
                         color = Color.White.copy(alpha = 0.15f * eventAlpha),
                         topLeft = rect.topLeft - Offset(4f, 4f),
                         size = rect.size,
                         cornerRadius = CornerRadius(28f, 28f)
                     )
 
                     // 2. MAIN GLASS BASE (Frosted Black/Dark)
                     drawRoundRect(
                         brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                             colors = listOf(
                                 Color.Black.copy(alpha = 0.5f * eventAlpha), // High frosting density
                                 Color.Black.copy(alpha = 0.3f * eventAlpha),
                                 Color.Black.copy(alpha = 0.45f * eventAlpha)
                             ),
                             startY = rect.top,
                             endY = rect.bottom
                         ),
                         topLeft = rect.topLeft,
                         size = rect.size,
                         cornerRadius = CornerRadius(28f, 28f)
                     )
                     
                     // 3. ZERO TINT OVERLAY (Ensuring no pink color bleed)
                     // Already neutral.
 
                     // 4. GLASS RIM (Sharp light refraction for dark glass)
                     drawRoundRect(
                         brush = androidx.compose.ui.graphics.Brush.linearGradient(
                             colors = listOf(
                                 Color.White.copy(alpha = 0.6f * eventAlpha), // Dimmer white for dark glass
                                 Color.White.copy(alpha = 0.05f * eventAlpha),
                                 Color.White.copy(alpha = 0.25f * eventAlpha)
                             ),
                             start = rect.topLeft,
                             end = rect.bottomRight
                         ),
                         topLeft = rect.topLeft,
                         size = rect.size,
                         cornerRadius = CornerRadius(28f, 28f),
                         style = Stroke(width = 1.2.dp.toPx())
                     )
 
                     // Interaction Handles (Corner based)
                     val iconSize = 12.dp.toPx()
                     val iconColor = Color.Gray.copy(alpha = 0.9f * eventAlpha)
                     val iconStrokeWidth = 2.dp.toPx()
                     
                     // 1. Top-Right Handle (Resize Top)
                     val trX = rect.right - iconSize - 12f
                     val trY = rect.top + 12f
                     val triTopPath = Path().apply {
                         moveTo(trX + iconSize/2, trY)
                         lineTo(trX, trY + iconSize)
                         lineTo(trX + iconSize, trY + iconSize)
                         close()
                     }
                     drawPath(path = triTopPath, color = iconColor, style = Stroke(width = iconStrokeWidth))
                     
                     // 2. Middle-Right Handle (Drag/Move)
                     // Hide handle if event is too short (<= 30 mins) as per user request
                     if (event.durationMinutes > 30) {
                         val mrX = rect.right - iconSize - 12f
                         val mrY = rect.top + rect.height/2
                         drawCircle(
                             color = iconColor,
                             radius = iconSize/1.8f,
                             center = Offset(mrX + iconSize/2, mrY),
                             style = Stroke(width = iconStrokeWidth)
                         )
                     }
                     
                     // 3. Bottom-Right Handle (Resize Bottom)
                     val brX = rect.right - iconSize - 12f
                     val brY = rect.bottom - iconSize - 12f
                     val triBotPath = Path().apply {
                         moveTo(brX + iconSize/2, brY + iconSize)
                         lineTo(brX, brY)
                         lineTo(brX + iconSize, brY)
                         close()
                     }
                     drawPath(path = triBotPath, color = iconColor, style = Stroke(width = iconStrokeWidth))
                     
                     val eventTextResult = textMeasurer.measure(
                        text = event.title, 
                        style = TextStyle(
                            color = Color.White,
                            fontSize = eventFontSize.sp, // Use the preferred font size
                            fontWeight = FontWeight.Bold,
                            fontFamily = googleSans,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black.copy(alpha=0.6f * eventAlpha), blurRadius = 4f)
                        ),
                        constraints = Constraints(maxWidth = (rect.width - 40f).toInt().coerceAtLeast(1))
                    )
                    
                    drawText(
                        textLayoutResult = eventTextResult, 
                        topLeft = Offset(
                            rect.left + (rect.width - eventTextResult.size.width) / 2f, 
                            rect.top + (rect.height - eventTextResult.size.height.toFloat()) / 2f
                        )
                    )
                }
            }
        }
        
        // 6. Draw Car
        val currentMinutes = now.hour * 60 + now.minute
        val carYBase = anchorY - (currentMinutes - scrollOffsetMinutes) * pixelsPerMinute
        
        if (carYBase > -300f && carYBase < height + 300f) {
            val carWidth = with(density) { 80.dp.toPx() } // Standardized width
            val carLength = with(density) { 160.dp.toPx() } // Standardized length
            
            val carLeft = carLaneCenterX - (carWidth / 2)
            val carTop = carYBase - (carLength / 2)
            
            translate(left = carLeft, top = carTop) {
                with(carPainter) {
                    draw(size = Size(carWidth, carLength))
                }
            }
            
            // Format time: Hour and Minute separated nicely
            val hourString = now.format(DateTimeFormatter.ofPattern("h"))
            val minuteString = now.format(DateTimeFormatter.ofPattern("mm"))
            
            val hourLayout = textMeasurer.measure(
                hourString, 
                TextStyle(
                    color = Color.Black, 
                    fontSize = 15.sp, // Bigger
                    fontWeight = FontWeight.Bold,
                    fontFamily = googleSans
                )
            )
            val minuteLayout = textMeasurer.measure(
                minuteString, 
                TextStyle(
                    color = Color.Black, 
                    fontSize = 15.sp, // Bigger
                    fontWeight = FontWeight.Bold,
                    fontFamily = googleSans
                )
            )
            
            // Calculate fixed bubble size to preserve original look but with bigger text
            // Hardcoded slightly generous radius to fit 15sp stacked text without changing size dynamically
            val bubbleRadius = with(density) { 20.dp.toPx() }
            
            // POSITION ON ROOFTOP
            val bubbleY = carYBase + with(density) { 16.dp.toPx() } 
            val bubbleX = carLaneCenterX - with(density) { 2.dp.toPx() }
            
            // 1. SOLID WHITE CIRCLE
            drawCircle(
                color = Color.White,
                center = Offset(bubbleX, bubbleY),
                radius = bubbleRadius
            )
            
            // Stacked Text Drawing
            val totalHeight = hourLayout.size.height + minuteLayout.size.height - with(density) { 4.dp.toPx() } // Tighter spacing
            val startY = bubbleY - (totalHeight / 2f)
            
            drawText(
                textLayoutResult = hourLayout, 
                topLeft = Offset(bubbleX - (hourLayout.size.width / 2f), startY)
            )
            
            drawText(
                textLayoutResult = minuteLayout, 
                topLeft = Offset(bubbleX - (minuteLayout.size.width / 2f), startY + hourLayout.size.height - with(density) { 4.dp.toPx() })
            )
        }
        
        // Time Picker Drag Bubble
        if (isTimeDragging && timeDragDateTime != null) {
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            val timeText = timeDragDateTime!!.format(timeFormatter)
            
            val timeLayoutResult = textMeasurer.measure(
                timeText,
                TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = googleSans
                )
            )
            
            val bubblePadding = with(density) { 12.dp.toPx() }
            val bubbleWidthPicker = timeLayoutResult.size.width + bubblePadding * 2
            val bubbleHeightPicker = timeLayoutResult.size.height + bubblePadding * 2
            val bubbleXPicker = with(density) { 70.dp.toPx() }
            val bubbleYPicker = timeDragY - bubbleHeightPicker / 2
            
            drawRoundRect(
                color = theme.glowPrimary,
                topLeft = Offset(bubbleXPicker, bubbleYPicker),
                size = Size(bubbleWidthPicker, bubbleHeightPicker),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            
            drawRoundRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(bubbleXPicker, bubbleYPicker),
                size = Size(bubbleWidthPicker, bubbleHeightPicker),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
            
            drawText(
                textLayoutResult = timeLayoutResult,
                topLeft = Offset(bubbleXPicker + bubblePadding, bubbleYPicker + bubblePadding)
            )
        }
    }
}
