package com.wyldsoft.notes.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyldsoft.notes.PenIconUtils
import kotlinx.coroutines.launch

import com.wyldsoft.notes.editor.EditorState
import com.wyldsoft.notes.pen.PenProfile


@Composable
fun Toolbar(
    editorState: EditorState,
    onPenProfileChanged: (PenProfile) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var selectedProfileIndex by remember { mutableStateOf(0) } // Default to leftmost (index 0)


    // Add a flag to track if we're waiting for panel to close
    var isPanelClosing by remember { mutableStateOf(false) }

    // Add a callback for when panel is fully removed

    // Store 5 profiles
    var profiles by remember {
        mutableStateOf(PenProfile.createDefaultProfiles())
    }

    // Current profile
    val currentProfile = profiles[selectedProfileIndex]

    // Force refresh counter for debugging
    var refreshCounter by remember { mutableStateOf(0) }

    fun forceUIRefresh() {
        refreshCounter++
        scope.launch {
            EditorState.refreshUi.emit(Unit)
        }
        Log.d("Toolbar:", "UI Refresh triggered: $refreshCounter")
    }


    // Listen for drawing events to close panel
    LaunchedEffect(Unit) {
        launch {
            EditorState.forceScreenRefresh.collect {
                println("REFRESH: Force screen refresh requested")
                forceUIRefresh()
            }
        }
    }


    // Handle exclusion rect changes
    LaunchedEffect(editorState.stateExcludeRectsModified) {
        if (editorState.stateExcludeRectsModified) {
            println("Exclusion rects modified - current zones: ${editorState.stateExcludeRects.keys}")
            editorState.stateExcludeRectsModified = false
            if (!isPanelClosing) {  // Only refresh if not waiting for panel to close
                forceUIRefresh()
            }
        }
    }

    // Initialize with default profile
    LaunchedEffect(Unit) {
        onPenProfileChanged(currentProfile)
        EditorState.updatePenProfile(currentProfile)
    }

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main toolbar content - only visible when not collapsed
        if (!editorState.isToolbarCollapsed) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White)
                    .border(1.dp, Color.Gray)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profiles:", color = Color.Black, fontSize = 12.sp)

                // 5 Profile buttons
                profiles.forEachIndexed { index, profile ->
                    ProfileButton(
                        profile = profile,
                        isSelected = selectedProfileIndex == index,
                        onClick = {
                            selectedProfileIndex = index
                            onPenProfileChanged(profile)
                            EditorState.updatePenProfile(profile)
                            Log.d("Toolbar", "Profile selected: $index - ${profile.penType.displayName}")
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Debug info
                Text(
                    text = "Profile: ${selectedProfileIndex + 1} | ${currentProfile.penType.displayName} | Refresh: $refreshCounter",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
        
        // Collapse/Expand button - always visible
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(24.dp)
                .background(Color.White)
                .border(1.dp, Color.Gray)
                .clickable {
                    editorState.isToolbarCollapsed = !editorState.isToolbarCollapsed
                    Log.d("Toolbar", "Toolbar collapsed state: ${editorState.isToolbarCollapsed}")
                    // Force refresh to update exclusion rects
                    forceUIRefresh()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (editorState.isToolbarCollapsed) 
                    Icons.Default.ChevronLeft 
                else 
                    Icons.Default.ChevronRight,
                contentDescription = if (editorState.isToolbarCollapsed) 
                    "Expand toolbar" 
                else 
                    "Collapse toolbar",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProfileButton(
    profile: PenProfile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) profile.strokeColor else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color.Black else Color.Gray
        ),
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Icon(
            imageVector = PenIconUtils.getIconForPenType(profile.penType),
            contentDescription = PenIconUtils.getContentDescriptionForPenType(profile.penType),
            modifier = Modifier.size(24.dp)
        )
    }
}