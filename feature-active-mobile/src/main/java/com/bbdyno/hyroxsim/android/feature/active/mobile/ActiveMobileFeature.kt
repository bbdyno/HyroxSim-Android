//
//  ActiveMobileFeature.kt
//  feature-active-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.active.mobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbdyno.hyroxsim.android.core.sync.LiveWorkoutState
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxMobileDesign

object ActiveMobileFeatureInfo {
    const val name: String = "feature-active-mobile"
}

@Composable
fun ActiveMobileScreen(
    state: LiveWorkoutState,
    modeLabel: String,
    onAdvance: () -> Unit,
    onPauseResume: () -> Unit,
    onEnd: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val accentColor = accentColor(state)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HyroxMobileDesign.Colors.Background),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GpsStatusLabel(state = state)

            Text(
                text = state.segmentLabel.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                textAlign = TextAlign.Center,
            )

            state.segmentSubLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HyroxMobileDesign.Colors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = state.segmentElapsedText,
                style = HyroxMobileDesign.Typography.LargeNumber.copy(fontSize = 64.sp),
                color = HyroxMobileDesign.Colors.TextPrimary,
            )

            Text(
                text = state.totalElapsedText,
                style = HyroxMobileDesign.Typography.MediumNumber,
                color = HyroxMobileDesign.Colors.TextPrimary.copy(alpha = 0.6f),
            )

            Text(
                text = modeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = HyroxMobileDesign.Colors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            if (state.stationNameText != null || state.stationTargetText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MetricPanel(
                        label = "STATION",
                        value = state.stationNameText ?: "—",
                        modifier = Modifier.weight(1f),
                    )
                    MetricPanel(
                        label = "TARGET",
                        value = state.stationTargetText ?: "—",
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                MetricPanel(
                    label = "PACE",
                    value = state.paceText,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricPanel(
                    label = "DISTANCE",
                    value = state.distanceText,
                    modifier = Modifier.weight(1f),
                )
                MetricPanel(
                    label = "HEART",
                    value = state.heartRateText,
                    valueColor = if (state.heartRateText != "—") HyroxMobileDesign.Colors.Destructive else HyroxMobileDesign.Colors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (state.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            CircularControlButton(
                text = if (state.isPaused) "▶" else "❚❚",
                onClick = onPauseResume,
                enabled = !state.isFinished,
            )

            NextButton(
                text = if (state.isLastSegment) "FINISH" else "NEXT",
                highlighted = state.isLastSegment,
                onClick = onAdvance,
                enabled = !state.isFinished,
            )

            CircularControlButton(
                text = "■",
                onClick = onEnd,
                enabled = true,
            )
        }
    }
}

@Composable
private fun GpsStatusLabel(
    state: LiveWorkoutState,
) {
    val text = when {
        !state.gpsActive -> "GPS OFF"
        state.gpsStrong -> "GPS FAIR"
        else -> "SEARCHING..."
    }
    val color = when {
        !state.gpsActive -> HyroxMobileDesign.Colors.TextTertiary
        state.gpsStrong -> HyroxMobileDesign.Colors.Accent
        else -> HyroxMobileDesign.Colors.TextSecondary
    }

    Text(
        text = text,
        style = HyroxMobileDesign.Typography.Label,
        color = color,
    )
}

@Composable
private fun MetricPanel(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = HyroxMobileDesign.Colors.TextPrimary,
) {
    Surface(
        modifier = modifier,
        color = HyroxMobileDesign.Colors.Surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, HyroxMobileDesign.Colors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = HyroxMobileDesign.Typography.Label,
                color = HyroxMobileDesign.Colors.TextTertiary,
            )
            Text(
                text = value,
                style = HyroxMobileDesign.Typography.MediumNumber,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun CircularControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Black.copy(alpha = 0.3f),
            contentColor = Color.White,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun NextButton(
    text: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (highlighted) {
                HyroxMobileDesign.Colors.Accent.copy(alpha = 0.4f)
            } else {
                Color.White.copy(alpha = 0.16f)
            },
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.08f),
            disabledContentColor = Color.White.copy(alpha = 0.35f),
        ),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.4f)),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

private fun accentColor(state: LiveWorkoutState): Color =
    when (state.accentKindRaw) {
        "RUN" -> HyroxMobileDesign.Colors.RunAccent
        "ROX_ZONE" -> HyroxMobileDesign.Colors.RoxZoneAccent
        else -> HyroxMobileDesign.Colors.Accent
    }
