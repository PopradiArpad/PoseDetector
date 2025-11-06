package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePoseLandmarkerScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp, // When closed, nothing to see from the bottom sheet
        sheetContent = {
            InfoBottomSheet()
        }) {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            LivePoseLandmarkerBackground(
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
            ) {
                InfoButton(onClick = {
                    scope.launch {
                        // No programmatic closing: just swipe down
                        scaffoldState.bottomSheetState.expand()
                    }
                })
                BackButton(onFinish)
            }
        }
    }
}

@Composable
private fun InfoBottomSheet() {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            "This is the info content.",
            modifier = Modifier.padding(top = 48.dp).align(Alignment.Center)
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
    ) {
        Text(
            text = "i",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            fontSize = 30.sp
        )
    }
}

@Composable
expect fun LivePoseLandmarkerBackground(
    modifier: Modifier,
)
