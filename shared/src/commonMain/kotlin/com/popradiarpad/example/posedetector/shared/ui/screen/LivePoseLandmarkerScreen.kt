package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.popradiarpad.example.posedetector.shared.component.LivePoseLandmarkerComponent
import com.popradiarpad.example.posedetector.shared.storage.RealInferenceTimeStorage
import com.popradiarpad.example.posedetector.shared.ui.widget.InferenceTimeChart
import kotlin.math.roundToInt

@Composable
fun LivePoseLandmarkerScreen(component: LivePoseLandmarkerComponent) {
    val slot by component.childSlot.subscribeAsState()
    val sheetComponentOnDismiss = slot.child?.instance?.run { ::onDismiss }

    LivePoseLandmarkerContent(
        showInfoSheet = component::showInfoSheet,
        sheetComponentOnDismiss = sheetComponentOnDismiss,
        onFinish = component.onBack
    )
}

// The component-free internal to make it previewable.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePoseLandmarkerContent(
    modifier: Modifier = Modifier,
    showInfoSheet: () -> Unit,
    sheetComponentOnDismiss: (() -> Unit)?,
    onFinish: () -> Unit
) {
    Scaffold { _ -> // no padding for edge to edge
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LivePoseLandmarkerBackground(
                modifier = Modifier.fillMaxSize()
            )

            if (sheetComponentOnDismiss == null) {
                ButtonColumn(
                    onInfo = showInfoSheet,
                    onFinish = onFinish
                )
            } else {
                ModalBottomSheet(
                    onDismissRequest = sheetComponentOnDismiss,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    InferenceInfo()
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BoxScope.ButtonColumn(
    onInfo: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp),
    ) {
        InfoButton(onClick = onInfo)
        BackButton(onFinish)
    }
}

@Composable
private fun InferenceInfo() {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InferenceTime()
            InferenceTimeChart(modifier = Modifier.fillMaxWidth().height(240.dp))
        }
    }
}

@Composable
private fun InferenceTime() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val inferenceTimeMs by RealInferenceTimeStorage.inferenceTimeMs.collectAsStateWithLifecycle()

        Text("Inference Time")
        Text(inferenceTimeMs?.toMilliSecondsString() ?: "--")
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

// This is a platform independent version of String.format("%.2f ms", ...)
private fun Double.toMilliSecondsString(): String? {
    if (isNaN() || isInfinite()) {
        return null
    }
    val hundredths = (this * 100).roundToInt()
    val integerPart = hundredths / 100
    val fractionalPart = hundredths % 100
    val fractionalString =
        if (fractionalPart < 10) "0$fractionalPart" else fractionalPart.toString()
    return "$integerPart.$fractionalString ms"
}
