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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import kotlin.math.roundToInt

@Composable
fun TemplateDetailMobileScreen(
    template: WorkoutTemplate,
    onStartPhoneWorkout: () -> Unit,
    onCustomizeTemplate: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        template.division?.displayName ?: template.name,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(if (template.isBuiltIn) "Built-in" else "Custom")
                            },
                        )
                        template.division?.let { division ->
                            AssistChip(onClick = {}, label = { Text(division.shortName) })
                        }
                    }
                    Text(
                        "${template.stationCount} stations • ${DistanceFormatter.short(template.totalRunDistanceMeters)} run • ~${(template.estimatedDurationSeconds / 60.0).roundToInt()} min",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onStartPhoneWorkout) {
                            Text("Start Workout")
                        }
                        OutlinedButton(onClick = onCustomizeTemplate) {
                            Text("Customize")
                        }
                    }
                }
            }
        }

        item {
            Text("Course", style = MaterialTheme.typography.titleLarge)
        }

        itemsIndexed(template.segments, key = { _, segment -> segment.id }) { index, segment ->
            SegmentDetailCard(
                segment = segment,
                stationOrdinal = template.segments
                    .take(index + 1)
                    .count { it.type == SegmentType.STATION },
            )
        }
    }
}

@Composable
private fun SegmentDetailCard(
    segment: WorkoutSegment,
    stationOrdinal: Int,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (segment.type == SegmentType.STATION) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "%02d".format(stationOrdinal),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("•", style = MaterialTheme.typography.titleMedium, color = segmentAccent(segment))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(segmentTitle(segment), style = MaterialTheme.typography.titleMedium)
                segmentDetail(segment)?.let { detail ->
                    Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun segmentAccent(segment: WorkoutSegment): Color =
    when (segment.type) {
        SegmentType.RUN -> MaterialTheme.colorScheme.tertiary
        SegmentType.ROX_ZONE -> MaterialTheme.colorScheme.secondary
        SegmentType.STATION -> MaterialTheme.colorScheme.primary
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
        SegmentType.ROX_ZONE -> "Transition"
        SegmentType.STATION -> buildString {
            segment.stationTarget?.formatted?.let(::append)
            segment.weightKg?.let { weight ->
                if (isNotEmpty()) append(" • ")
                append(weight.roundToInt())
                append(" kg")
                segment.weightNote?.takeIf { it.isNotBlank() }?.let {
                    append(" ")
                    append(it)
                }
            }
        }.ifBlank { null }
    }
