//
//  HomeMobileFeature.kt
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bbdyno.hyroxsim.android.core.format.DurationFormatter
import com.bbdyno.hyroxsim.android.core.model.CompletedWorkout
import com.bbdyno.hyroxsim.android.core.model.HyroxDivision
import com.bbdyno.hyroxsim.android.core.model.WorkoutTemplate
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxBadge
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxChevron
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxMobileDesign
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxPageIndicator
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxSectionLabel
import com.bbdyno.hyroxsim.android.ui.mobile.HyroxSurfaceCard
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HomeMobileFeatureInfo {
    const val name: String = "feature-home-mobile"
}

private val recentFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("MMM d · HH:mm")
    .withZone(ZoneId.systemDefault())

@Composable
fun HomeMobileScreen(
    templates: List<WorkoutTemplate>,
    recentWorkout: CompletedWorkout?,
    pairedLabel: String,
    onOpenBuilder: () -> Unit,
    onOpenHistory: () -> Unit,
    onSelectRecent: (CompletedWorkout) -> Unit,
    onSelectTemplate: (WorkoutTemplate) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val builtInTemplates = remember(templates) { templates.filter { it.isBuiltIn } }
    val customTemplates = remember(templates) { templates.filterNot { it.isBuiltIn } }
    val pagerState = rememberPagerState(pageCount = { builtInTemplates.size })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "HYROX",
                    style = HyroxMobileDesign.Typography.Title,
                    color = HyroxMobileDesign.Colors.TextPrimary,
                )
                HyroxBadge(
                    text = pairedLabel.uppercase(),
                    containerColor = HyroxMobileDesign.Colors.SurfaceElevated,
                    contentColor = HyroxMobileDesign.Colors.TextSecondary,
                )
            }
        }

        recentWorkout?.let { workout ->
            item {
                HyroxSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectRecent(workout) },
                ) {
                    Text(
                        text = "RECENT",
                        style = HyroxMobileDesign.Typography.Label,
                        color = HyroxMobileDesign.Colors.TextTertiary,
                    )
                    Text(
                        text = workout.division?.shortName ?: workout.templateName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = DurationFormatter.hms(workout.totalDuration),
                        style = HyroxMobileDesign.Typography.MediumNumber,
                        color = HyroxMobileDesign.Colors.Accent,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = recentFormatter.format(workout.finishedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = HyroxMobileDesign.Colors.TextTertiary,
                        )
                        HyroxChevron()
                    }
                }
            }
        }

        if (builtInTemplates.isNotEmpty()) {
            item {
                HyroxSectionLabel("SELECT DIVISION")
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 10.dp,
                ) { page ->
                    PresetDivisionCard(
                        template = builtInTemplates[page],
                        onClick = { onSelectTemplate(builtInTemplates[page]) },
                    )
                }
            }

            item {
                HyroxPageIndicator(
                    pageCount = builtInTemplates.size,
                    currentPage = pagerState.currentPage,
                )
            }
        }

        item {
            HyroxSectionLabel("MY WORKOUTS")
        }

        item {
            HomeActionRow(
                title = "Create Custom Workout",
                subtitle = "Build and save your own HYROX session",
                onClick = onOpenBuilder,
            )
        }

        item {
            HomeActionRow(
                title = "Workout History",
                subtitle = "Review completed efforts and split times",
                onClick = onOpenHistory,
            )
        }

        if (customTemplates.isNotEmpty()) {
            item {
                HyroxSectionLabel("CUSTOM TEMPLATES")
            }

            items(customTemplates, key = { it.id }) { template ->
                CustomTemplateRow(
                    template = template,
                    onClick = { onSelectTemplate(template) },
                )
            }
        }
    }
}

@Composable
private fun PresetDivisionCard(
    template: WorkoutTemplate,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp),
        onClick = onClick,
        shape = RoundedCornerShape(HyroxMobileDesign.Radius.Card),
        color = HyroxMobileDesign.Colors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, HyroxMobileDesign.Colors.Hairline),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(HyroxMobileDesign.Colors.Accent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HyroxBadge(text = divisionBadge(template.division))
                    Text(
                        text = template.division?.shortName ?: template.name,
                        style = HyroxMobileDesign.Typography.CardTitle,
                    )
                    Text(
                        text = template.division?.displayName ?: template.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HyroxMobileDesign.Colors.TextSecondary,
                    )
                }
                Text(
                    text = "${template.stationCount} stations  ·  ~${template.estimatedDurationSeconds / 60} min",
                    style = HyroxMobileDesign.Typography.SmallNumber,
                    color = HyroxMobileDesign.Colors.TextTertiary,
                )
            }
            HyroxChevron(modifier = Modifier.padding(end = 16.dp))
        }
    }
}

@Composable
private fun HomeActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    HyroxSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = HyroxMobileDesign.Colors.TextSecondary,
                )
            }
            HyroxChevron()
        }
    }
}

@Composable
private fun CustomTemplateRow(
    template: WorkoutTemplate,
    onClick: () -> Unit,
) {
    HyroxSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${template.stationCount} stations · ${template.segments.size} segments",
                    style = MaterialTheme.typography.bodySmall,
                    color = HyroxMobileDesign.Colors.TextSecondary,
                )
            }
            HyroxChevron()
        }
    }
}

private fun divisionBadge(division: HyroxDivision?): String =
    when (division) {
        HyroxDivision.MIXED_DOUBLE -> "MIX"
        HyroxDivision.MEN_OPEN_SINGLE,
        HyroxDivision.MEN_OPEN_DOUBLE,
        HyroxDivision.WOMEN_OPEN_SINGLE,
        HyroxDivision.WOMEN_OPEN_DOUBLE,
        -> "OPEN"
        HyroxDivision.MEN_PRO_SINGLE,
        HyroxDivision.MEN_PRO_DOUBLE,
        HyroxDivision.WOMEN_PRO_SINGLE,
        HyroxDivision.WOMEN_PRO_DOUBLE,
        -> "PRO"
        null -> "CUSTOM"
    }
