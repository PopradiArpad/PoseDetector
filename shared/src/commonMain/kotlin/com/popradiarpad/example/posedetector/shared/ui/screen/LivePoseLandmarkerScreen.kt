package com.popradiarpad.example.posedetector.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.popradiarpad.example.posedetector.shared.util.LogComposition
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

object LivePoseLandmarkerScreen : Screen {
    @Composable
    override fun Content() {
        LogComposition(tag = "LivePoseLandmarkerScreen.Content")

        val navigator = LocalNavigator.currentOrThrow
        LivePoseLandmarkerContent(onFinish = {
            Napier.d("onFinish called, popping navigator")
            navigator.pop()
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePoseLandmarkerContent(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    LogComposition(tag = "LivePoseLandmarkerContent")

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp, // When closed, nothing to see from the bottom sheet
        sheetContent = { InfoBottomSheet() }) {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
//            LivePoseLandmarkerBackground(
//                modifier = Modifier.fillMaxSize()
//            )

            if (scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
                ButtonColumn(
                    onInfo = {
                        scope.launch {
                            // No programmatic closing: just swipe down
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    onFinish = onFinish
                )
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
fun InfoBottomSheet() {
    LogComposition(tag = "InfoBottomSheet")

    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        InferenceTime()
    }
}

@Composable
private fun InferenceTime() {
    LogComposition(tag = "InferenceTime")

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Inference Time",
            modifier = Modifier.padding(16.dp)
        )

//        val inferenceTimeMs by InferenceTimeStorage.inferenceTimeMs.collectAsState()
//
//        Text(
//            text = inferenceTimeMs?.toMilliSecondsString() ?: "--",
//            modifier = Modifier.padding(16.dp)
//        )
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
    val fractionalString = if (fractionalPart < 10) "0$fractionalPart" else fractionalPart.toString()
    return "$integerPart.$fractionalString ms"
}
