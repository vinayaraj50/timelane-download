package com.timelane.presentation.task

import androidx.compose.foundation.background
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timelane.core.theme.LocalTimeTheme
import com.timelane.presentation.task.components.TaskItem
import com.timelane.presentation.task.components.TaskPopup
import com.timelane.domain.model.Task
import com.timelane.domain.model.Event
import kotlinx.coroutines.launch
import com.timelane.presentation.main.DragAndDropController
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    dndController: DragAndDropController,
    onNowClicked: () -> Unit = {},
    showActionPopup: Boolean = false,
    actionLabel: String = "",
    onUndo: () -> Unit = {},
    onPush: () -> Unit = {},
    onPull: () -> Unit = {},
    onOk: () -> Unit = {},
    onInteraction: () -> Unit = {},
    onDeleteAllEvents: () -> Unit = {},
    onDeleteOldEvents: () -> Unit = {},
    onToggleScreenSaver: () -> Unit = {},
    isScreenSaverEnabled: Boolean = false
) {
    val theme = LocalTimeTheme.current
    val tasks by viewModel.tasks.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val fontSizePref by viewModel.fontSize.collectAsState()
    
    // Popup state for task creation
    var showTaskPopup by remember { mutableStateOf(false) }
    var activeParentId by remember { mutableStateOf<Long?>(null) }
    
    // snackbarHostState is passed from parent
    val scope = rememberCoroutineScope()
    
    // Undo tracking
    var deletedTask by remember { mutableStateOf<com.timelane.domain.model.Task?>(null) }
    
    // Copy feedback state
    var showCopiedFeedback by remember { mutableStateOf(false) }
    
    LaunchedEffect(showCopiedFeedback) {
        if (showCopiedFeedback) {
            kotlinx.coroutines.delay(30000) // 30 seconds
            showCopiedFeedback = false
        }
    }
    
    Scaffold(
        snackbarHost = {}, // Global Snackbar is now in MainScreen
        containerColor = Color.Transparent // Transparent for See-Through
    ) { paddingValues ->
        Surface(
            color = Color.Transparent, 
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Box(
                 modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                                onInteraction()
                            }
                        }
                    }
            ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Task List Container (Neumorphism + Glow)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 40.dp, // High spread
                            spotColor = theme.glowPrimary,
                            ambientColor = theme.glowPrimary
                        )
                        .background(
                            color = Color.Black, // Pitch Black
                            shape = androidx.compose.ui.graphics.RectangleShape
                        )
                ) {
                    // Dashboard Header (Studio HUD) moved INSIDE the frosted box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                            .fillMaxWidth()
                    ) {
                       var showMenu by remember { mutableStateOf(false) }
                       
                       // Menu Trigger
                       val moveCompleted by viewModel.moveCompletedToBottom.collectAsState()
                       
                       Box(
                           modifier = Modifier
                               .size(44.dp)
                               .shadow(
                                   elevation = 8.dp,
                                   shape = CircleShape,
                                   spotColor = theme.glowPrimary.copy(alpha = 0.6f),
                                   ambientColor = theme.glowPrimary.copy(alpha = 0.3f)
                               )
                               .background(
                                    color = Color(0xFF0A0A0A), // Neumorphic Black
                                    shape = CircleShape
                                )
                               .border(
                                   width = 1.dp,
                                   brush = Brush.verticalGradient(
                                       colors = listOf(Color.White.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.5f))
                                   ),
                                   shape = CircleShape
                               )
                               .clickable { showMenu = true },
                           contentAlignment = Alignment.Center
                       ) {
                           Icon(
                               imageVector = Icons.Default.Menu,
                               contentDescription = "Menu",
                               tint = Color.White,
                               modifier = Modifier.size(20.dp)
                           )
                           
                           if (showMenu) {
                               // MODAL POPUP (Responsive for landscape)
                               val config = androidx.compose.ui.platform.LocalConfiguration.current
                               val isLandscapeMenu = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                               val menuScrollState = androidx.compose.foundation.rememberScrollState()
                               
                               val menuSpacing = if (isLandscapeMenu) 8.dp else 16.dp
                               val menuPadding = if (isLandscapeMenu) 16.dp else 24.dp
                               val buttonHeight = if (isLandscapeMenu) 44.dp else 56.dp
                               
                               androidx.compose.ui.window.Dialog(
                                   onDismissRequest = { showMenu = false },
                                   properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = !isLandscapeMenu)
                               ) {
                                   Column(
                                       modifier = Modifier
                                           .then(
                                               if (isLandscapeMenu) {
                                                   Modifier
                                                       .fillMaxWidth(0.6f)
                                                       .heightIn(max = config.screenHeightDp.dp * 0.9f)
                                               } else {
                                                   Modifier.width(320.dp)
                                               }
                                           )
                                           .shadow(24.dp, RoundedCornerShape(if (isLandscapeMenu) 24.dp else 32.dp), spotColor = theme.glowPrimary)
                                           .background(Color(0xFF0A0A0A), RoundedCornerShape(if (isLandscapeMenu) 24.dp else 32.dp))
                                           .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(if (isLandscapeMenu) 24.dp else 32.dp))
                                           .padding(menuPadding)
                                           .verticalScroll(menuScrollState)
                                   ) {
                                       Text(
                                           text = "SYSTEM SETTINGS",
                                           color = theme.glowPrimary,
                                           style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                       )
                                       
                                       Spacer(modifier = Modifier.height(menuSpacing))

                                       // 1. Scroll To Now Button
                                       Button(
                                           onClick = {
                                               onNowClicked()
                                               showMenu = false
                                           },
                                           modifier = Modifier.fillMaxWidth().height(buttonHeight),
                                           colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha=0.05f)),
                                           shape = RoundedCornerShape(16.dp)
                                       ) {
                                           Icon(Icons.Default.MyLocation, null, tint = theme.glowPrimary)
                                           Spacer(modifier = Modifier.width(12.dp))
                                           Text("SCROLL TO NOW", color = Color.White, fontSize = if (isLandscapeMenu) 12.sp else 14.sp)
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing))

                                       // 2. Move Completed Switch
                                       SettingsRow(
                                           title = "AUTO-REORDER COMPLETED",
                                           theme = theme,
                                           compact = isLandscapeMenu
                                       ) {
                                           LiquidGlassSwitch(
                                               checked = moveCompleted,
                                               onCheckedChange = { viewModel.setMoveCompletedToBottom(it) },
                                               theme = theme
                                           )
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing))

                                       // 3. HUD Font Size Slider
                                       SettingsRow(
                                           title = "HUD FONT SIZE: ${fontSizePref.toInt()}",
                                           theme = theme,
                                           compact = isLandscapeMenu
                                       ) {
                                           Slider(
                                               value = fontSizePref,
                                               onValueChange = { viewModel.setFontSize(it) },
                                               valueRange = 12f..24f,
                                               colors = SliderDefaults.colors(
                                                   thumbColor = Color.White,
                                                   activeTrackColor = theme.glowPrimary
                                               ),
                                               modifier = if (isLandscapeMenu) Modifier.height(32.dp) else Modifier
                                           )
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing))

                                       // 4. Event Font Size Slider
                                       val eventFontSize by viewModel.eventFontSize.collectAsState()
                                       SettingsRow(
                                           title = "EVENT FONT SIZE: ${eventFontSize.toInt()}",
                                           theme = theme,
                                           compact = isLandscapeMenu
                                       ) {
                                           Slider(
                                               value = eventFontSize,
                                               onValueChange = { viewModel.setEventFontSize(it) },
                                               valueRange = 12f..24f,
                                               colors = SliderDefaults.colors(
                                                   thumbColor = Color.White,
                                                   activeTrackColor = theme.glowPrimary
                                               ),
                                               modifier = if (isLandscapeMenu) Modifier.height(32.dp) else Modifier
                                           )
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing))

                                       // 5. Notification Sounds
                                       SettingsRow(
                                           title = "NOTIFICATION SOUND",
                                           theme = theme,
                                           compact = isLandscapeMenu
                                       ) {
                                           Column {
                                               com.timelane.core.sound.NotificationSound.values().forEach { sound ->
                                                   Row(
                                                       modifier = Modifier
                                                           .fillMaxWidth()
                                                           .clickable { viewModel.updateSound(sound) }
                                                           .padding(vertical = if (isLandscapeMenu) 4.dp else 8.dp),
                                                       verticalAlignment = Alignment.CenterVertically
                                                   ) {
                                                       Text(
                                                           text = sound.label,
                                                           color = if (selectedSound == sound) theme.glowPrimary else Color.White.copy(alpha = 0.6f),
                                                           style = MaterialTheme.typography.labelMedium.copy(
                                                               fontWeight = if (selectedSound == sound) FontWeight.Bold else FontWeight.Normal,
                                                               letterSpacing = 1.sp,
                                                               fontSize = if (isLandscapeMenu) 10.sp else 12.sp
                                                           ),
                                                           modifier = Modifier.weight(1f)
                                                       )
                                                       if (selectedSound == sound) {
                                                           Icon(Icons.Default.Check, null, tint = theme.glowPrimary, modifier = Modifier.size(16.dp))
                                                       }
                                                   }
                                               }
                                           }
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing))

                                       // 6. Delete Old Events Button
                                       Button(
                                           onClick = {
                                               onDeleteOldEvents()
                                               showMenu = false
                                           },
                                           modifier = Modifier.fillMaxWidth().height(buttonHeight),
                                           colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF443333)),
                                           shape = RoundedCornerShape(16.dp)
                                       ) {
                                           Icon(Icons.Default.Delete, null, tint = Color(0xFFFF8866))
                                           Spacer(modifier = Modifier.width(12.dp))
                                           Text("DELETE OLD EVENTS", color = Color(0xFFFF8866), fontSize = if (isLandscapeMenu) 12.sp else 14.sp)
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing / 2))

                                       // 7. Delete All Events Button
                                       Button(
                                           onClick = {
                                               onDeleteAllEvents()
                                               showMenu = false
                                           },
                                           modifier = Modifier.fillMaxWidth().height(buttonHeight),
                                           colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF442222)),
                                           shape = RoundedCornerShape(16.dp)
                                       ) {
                                           Icon(Icons.Default.Delete, null, tint = Color(0xFFFF4444))
                                           Spacer(modifier = Modifier.width(12.dp))
                                           Text("DELETE ALL EVENTS", color = Color(0xFFFF4444), fontSize = if (isLandscapeMenu) 12.sp else 14.sp)
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing))
                                       
                                       // 8. Screen Saver Button
                                       Button(
                                           onClick = {
                                               onToggleScreenSaver()
                                               showMenu = false
                                           },
                                           modifier = Modifier.fillMaxWidth().height(buttonHeight),
                                           colors = ButtonDefaults.buttonColors(
                                               containerColor = if (isScreenSaverEnabled) theme.glowPrimary.copy(alpha=0.3f) else Color.White.copy(alpha=0.1f)
                                           ),
                                           shape = RoundedCornerShape(16.dp)
                                       ) {
                                            // Icon for display (Bedtime/VisibilityOff equivalent)
                                            // Using generic icon if specific one isn't imported, but assuming standard icons are available.
                                            // Since we don't have Bedtime, let's use a visibility related one or generic.
                                            // Actually, let's just use Text for clarity if icon is missing, or simple Check if enabled.
                                           Text(
                                               text = if (isScreenSaverEnabled) "DISABLE SCREEN SAVER" else "SCREEN SAVER MODE", 
                                               color = if (isScreenSaverEnabled) theme.glowPrimary else Color.White, 
                                               fontSize = if (isLandscapeMenu) 12.sp else 14.sp
                                           )
                                       }

                                       Spacer(modifier = Modifier.height(menuSpacing))
                                       
                                       // Close Button
                                       TextButton(
                                           onClick = { showMenu = false },
                                           modifier = Modifier.align(Alignment.CenterHorizontally)
                                       ) {
                                           Text("CLOSE", color = Color.Gray)
                                       }
                                   }
                               }
                           }
                       }
                       
                       Spacer(modifier = Modifier.width(16.dp))
                       
                       Column {
                           if (showActionPopup) {
                               // HUD Feedback (Delete/Undo)
                               Row(
                                   verticalAlignment = Alignment.CenterVertically,
                                   horizontalArrangement = Arrangement.spacedBy(8.dp)
                               ) {
                                   Button(
                                       onClick = { 
                                           onUndo()
                                           onInteraction()
                                       },
                                       colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha=0.1f)),
                                       shape = RoundedCornerShape(8.dp),
                                       modifier = Modifier.height(36.dp)
                                   ) {
                                       Text("UNDO", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                   }
                                   
                                   if (actionLabel.isNotEmpty()) {
                                       Button(
                                           onClick = { 
                                               if (actionLabel == "PULL FUTURE") onPull() else onPush()
                                               onInteraction()
                                           },
                                           colors = ButtonDefaults.buttonColors(containerColor = theme.glowPrimary.copy(alpha=0.2f)),
                                           shape = RoundedCornerShape(8.dp),
                                           modifier = Modifier.height(36.dp)
                                       ) {
                                           Text(actionLabel, color = theme.glowPrimary, style = MaterialTheme.typography.labelSmall)
                                       }
                                   }
                                   
                                   IconButton(onClick = onOk) {
                                       Icon(Icons.Default.Check, "OK", tint = Color.Gray)
                                   }
                               }
                           } else if (showCopiedFeedback) {
                               // ITEM COPIED Feedback
                               Row(
                                   verticalAlignment = Alignment.CenterVertically,
                                   horizontalArrangement = Arrangement.spacedBy(12.dp)
                               ) {
                                   Text(
                                       text = "ITEM COPIED", 
                                       color = theme.glowPrimary, 
                                       style = MaterialTheme.typography.titleMedium.copy(
                                           letterSpacing = 2.sp,
                                           fontWeight = FontWeight.Black
                                       )
                                   )
                                   
                                   Button(
                                       onClick = { 
                                           dndController.clearClipboard()
                                           showCopiedFeedback = false
                                       },
                                       colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha=0.1f)),
                                       shape = RoundedCornerShape(8.dp),
                                       modifier = Modifier.height(32.dp).padding(horizontal = 0.dp),
                                       contentPadding = PaddingValues(horizontal = 12.dp)
                                   ) {
                                       Text("UNDO", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                   }
                               }
                           } else {
                               Text(
                                   text = "TASKS", 
                                   color = Color.White, 
                                   style = MaterialTheme.typography.titleMedium.copy(
                                       letterSpacing = 4.sp,
                                       fontWeight = FontWeight.Black
                                   )
                               )
                           }
                       }
                       
                       Spacer(modifier = Modifier.weight(1f))
                       
                        // Add Directive Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    spotColor = theme.glowPrimary.copy(alpha = 0.9f),
                                    ambientColor = theme.glowPrimary.copy(alpha = 0.5f)
                                )
                                .background(Color(0xFF0A0A0A), CircleShape)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                       colors = listOf(Color.White.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.5f))
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { 
                                    activeParentId = null
                                    showTaskPopup = true 
                                },
                             contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Show Input",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Divider/Glow line below header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, theme.glowPrimary.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                    )

                    val listState = rememberLazyListState()
                    var listPosition by remember { mutableStateOf(Offset.Zero) }

                    LaunchedEffect(dndController, tasks) {
                        dndController.reorderHandler = { task, globalOffset ->
                            val localY = globalOffset.y - listPosition.y
                            val layoutInfo = listState.layoutInfo
                            val items = layoutInfo.visibleItemsInfo
                            
                            val targetItem = items.find { item ->
                                localY >= item.offset && localY <= item.offset + item.size 
                            }
                            
                            if (targetItem != null) {
                                val fromIndex = tasks.indexOfFirst { t -> t.id == task.id }
                                val toIndex = targetItem.index
                                if (fromIndex != -1 && fromIndex != toIndex) {
                                    viewModel.reorderTasks(fromIndex, toIndex)
                                }
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .onGloballyPositioned { listPosition = it.positionInWindow() }
                    ) {
                        if (tasks.isEmpty()) {
                            item {
                                Text(
                                    text = "SYSTEM IDLE. AWAITING INPUT.",
                                    color = theme.textSecondary,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                        } else {
                            itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        when (it) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                deletedTask = task
                                                viewModel.deleteTask(task.id)
                                                true
                                            }
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                dndController.copyTask(task)
                                                showCopiedFeedback = true
                                                false // Don't dismiss
                                            }
                                            else -> false
                                        }
                                    }
                                )

                                @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                                SwipeToDismissBox(
                                    modifier = Modifier.animateItemPlacement(),
                                    state = dismissState,
                                    backgroundContent = {
                                        val color by androidx.compose.animation.animateColorAsState(
                                            targetValue = when (dismissState.targetValue) {
                                                SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF4444).copy(alpha = 0.6f) // Red for delete
                                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF00FF88).copy(alpha = 0.3f) // Subtle green for copy
                                                else -> Color.Transparent
                                            },
                                            label = "SwipeBackground"
                                        ) 
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color, RoundedCornerShape(12.dp)) // Matches TaskItem shape
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
                                        ) {
                                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.White
                                                )
                                            } else if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Copy",
                                                    tint = Color.Black
                                                )
                                            }
                                        }
                                    },
                                    content = {
                                        TaskItem(
                                            task = task,
                                            fontSize = fontSizePref,
                                            dndController = dndController,
                                            onComplete = { viewModel.completeTask(it) },
                                            onDelete = { taskToDelete ->
                                                deletedTask = taskToDelete
                                                viewModel.deleteTask(taskToDelete.id)
                                            },
                                            onCopy = { taskToCopy ->
                                                dndController.copyTask(taskToCopy)
                                                showCopiedFeedback = true
                                            },
                                            onAddSubtask = { parentId ->
                                                activeParentId = parentId
                                                showTaskPopup = true
                                            }
                                        )
                                    },
                                    enableDismissFromStartToEnd = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                } // End of Frosted Column
            }
            
            // Task Creation Popup
            if (showTaskPopup) {
                TaskPopup(
                    onDismiss = { showTaskPopup = false },
                    onAdd = { title ->
                        viewModel.addTask(title, activeParentId)
                        showTaskPopup = false
                    }
                )
            }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    theme: com.timelane.core.theme.TimeThemeColors,
    compact: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(if (compact) 12.dp else 16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(if (compact) 12.dp else 16.dp))
            .padding(if (compact) 10.dp else 16.dp)
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = if (compact) 9.sp else 10.sp
            )
        )
        Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))
        content()
    }
}

@Composable
private fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    theme: com.timelane.core.theme.TimeThemeColors
) {
    val transition = updateTransition(checked, label = "Switch")
    val thumbOffset by transition.animateDp(label = "ThumbOffset") { if (it) 24.dp else 0.dp }
    val glowAlpha by transition.animateFloat(label = "GlowAlpha") { if (it) 0.8f else 0.2f }
    
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(4.dp)
    ) {
        // Track Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (checked) theme.glowPrimary.copy(alpha = 0.15f) else Color.Transparent,
                    RoundedCornerShape(20.dp)
                )
        )
        
        // Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .shadow(
                    elevation = if (checked) 8.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = theme.glowPrimary.copy(alpha = glowAlpha)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (checked) listOf(theme.glowPrimary, theme.glowSecondary) else listOf(Color.White.copy(alpha=0.6f), Color.White.copy(alpha=0.3f))
                    ),
                    shape = CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        )
    }
}
