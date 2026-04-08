//
//  TemplateDetailMobileFeature.kt
//  feature-home-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.feature.home.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.format.DistanceFormatter
import com.bbdyno.hyroxsim.android.core.model.SegmentType
import com.bbdyno.hyroxsim.android.core.model.WorkoutSegment
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxBadge
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxDivider
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxMobileDesign
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxPrimaryButton
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxSecondaryButton
import kotlin.math.roundToInt

@Composable
fun TemplateDetailMobileScreen(
    template: WorkoutTemplate,
    onStartPhoneWorkout: () -> Unit,
    onCustomizeTemplate: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = template.division?.displayName ?: template.name,
                        style = HyroxMobileDesign.Typography.Headline,
                    )
                    Text(
                        text = "${template.stationCount} stations · ${DistanceFormatter.short(template.totalRunDistanceMeters)} run · ~${(template.estimatedDurationSeconds / 60.0).roundToInt()} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HyroxMobileDesign.Colors.TextSecondary,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(6.dp)) }

            item {
                Text(
                    text = "COURSE",
                    style = HyroxMobileDesign.Typography.Section,
                    color = HyroxMobileDesign.Colors.Accent,
                )
            }

            item {
                HyroxDivider(color = HyroxMobileDesign.Colors.AccentDim)
            }

            itemsIndexed(template.segments, key = { _, segment -> segment.id }) { index, segment ->
                SegmentDetailRow(
                    segment = segment,
                    stationOrdinal = template.segments
                        .take(index + 1)
                        .count { it.type == SegmentType.STATION },
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 20.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HyroxPrimaryButton(
                text = "Start Workout",
                onClick = onStartPhoneWorkout,
                modifier = Modifier.fillMaxWidth(),
            )
            HyroxSecondaryButton(
                text = "Customize",
                onClick = onCustomizeTemplate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SegmentDetailRow(
    segment: WorkoutSegment,
    stationOrdinal: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (segment.type == SegmentType.STATION) {
            HyroxBadge(
                text = "%02d".format(stationOrdinal),
                modifier = Modifier.width(28.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = segmentAccent(segment),
                            shape = RoundedCornerShape(99.dp),
                        )
                        .width(8.dp)
                        .height(8.dp),
                )
            }
        }

        Text(
            text = segmentTitle(segment),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = HyroxMobileDesign.Colors.TextPrimary.copy(alpha = if (segment.type == SegmentType.ROX_ZONE) 0.4f else 1f),
            fontWeight = if (segment.type == SegmentType.STATION) FontWeight.Bold else FontWeight.Normal,
        )

        segmentDetail(segment)?.let { detail ->
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = HyroxMobileDesign.Colors.TextTertiary,
            )
        }
    }
}

private fun segmentAccent(segment: WorkoutSegment): Color =
    when (segment.type) {
        SegmentType.RUN -> HyroxMobileDesign.Colors.RunAccent
        SegmentType.ROX_ZONE -> HyroxMobileDesign.Colors.RoxZoneAccent
        SegmentType.STATION -> HyroxMobileDesign.Colors.Accent
    }

private fun segmentTitle(segment: WorkoutSegment): String =
    when (segment.type) {
        SegmentType.RUN -> "Running"
        SegmentType.ROX_ZONE -> "Rox Zone"
        SegmentType.STATION -> segment.stationKind?.displayName ?: "Station"
    }

private fun segmentDetail(segment: WorkoutSegment): String? =
    when (segment.type) {
        SegmentType.RUN -> segment.distanceMeters?.let(DistanceFormatter::short)
        SegmentType.ROX_ZONE -> null
        SegmentType.STATION -> buildString {
            segment.stationTarget?.formatted?.let(::append)
            segment.weightKg?.let { weight ->
                if (isNotEmpty()) append(" · ")
                append(weight.roundToInt())
                append("kg")
                segment.weightNote?.takeIf { it.isNotBlank() }?.let { note ->
                    append(" ")
                    append(note)
                }
            }
        }.ifBlank { null }
    }
