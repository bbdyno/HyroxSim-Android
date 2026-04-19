package com.bbdyno.hyroxsim.feature.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbdyno.hyroxsim.core.domain.HyroxDivision
import com.bbdyno.hyroxsim.core.domain.WorkoutTemplate
import com.bbdyno.hyroxsim.core.persistence.repository.WorkoutSummary
import com.bbdyno.hyroxsim.feature.home.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rich home screen matching iOS `HomeViewController`:
 *   1. Recent card (last completed workout, optional)
 *   2. SELECT DIVISION horizontal pager
 *   3. SAVED TEMPLATES section (user-built, optional)
 *   4. MY WORKOUTS actions (Create Custom / History)
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onOpenDetail: (routeKey: String) -> Unit,
    onOpenBuilder: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSummary: (workoutId: String) -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState(initial = HomeUiState(null, emptyList()))

    Box(modifier = modifier.background(Color.Black)) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HeaderRow() }

            ui.mostRecent?.let { recent ->
                item { RecentCard(summary = recent, onClick = { onOpenSummary(recent.id) }) }
            }

            item {
                SectionLabel(
                    title = stringResource(R.string.home_section_select_division),
                    subtitle = stringResource(R.string.home_section_select_division_hint),
                )
            }
            item {
                DivisionPager(
                    onPickDivision = { raw -> onOpenDetail("builtin:$raw") },
                )
            }

            if (ui.savedTemplates.isNotEmpty()) {
                item {
                    SectionLabel(
                        title = stringResource(R.string.home_section_saved_templates),
                        subtitle = stringResource(R.string.home_section_saved_templates_hint),
                    )
                }
                items(ui.savedTemplates, key = { it.id }) { t ->
                    TemplateRow(
                        template = t,
                        onClick = { onOpenDetail(t.id) },
                    )
                }
            }

            item { SectionLabel(stringResource(R.string.home_section_my_workouts), null) }
            item {
                ActionButton(
                    label = stringResource(R.string.home_action_create_custom),
                    subtitle = stringResource(R.string.home_action_create_custom_hint),
                    onClick = onOpenBuilder,
                )
            }
            item {
                ActionButton(
                    label = stringResource(R.string.home_action_history),
                    subtitle = stringResource(R.string.home_action_history_hint),
                    onClick = onOpenHistory,
                )
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_brand_title),
                color = Color(0xFFFFD700),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.home_brand_subtitle),
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun RecentCard(summary: WorkoutSummary, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.home_recent_badge), color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                summary.templateName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                formatElapsed(summary.totalActiveDurationMs),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                formatRelativeDate(summary.finishedAtEpochMs) + " · " +
                    "%.2f km".format(summary.totalDistanceMeters / 1000),
                color = Color(0xFF888888),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DivisionPager(
    onPickDivision: (String) -> Unit,
) {
    val divisions = HyroxDivision.entries.toList()
    val pagerState = rememberPagerState(pageCount = { divisions.size })

    Column {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(280.dp),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(end = 40.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
        ) { pageIndex ->
            val division = divisions[pageIndex]
            Surface(
                onClick = { onPickDivision(division.raw) },
                color = Color(0xFF111111),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(20.dp).fillMaxSize(),
                ) {
                    Column {
                        Text(
                            division.shortName,
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            division.displayName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.home_division_summary), color = Color(0xFFAAAAAA), fontSize = 12.sp)
                        Box(modifier = Modifier.weight(1f))
                        Text(
                            stringResource(R.string.home_division_details),
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
        ) {
            repeat(divisions.size) { index ->
                val color = if (index == pagerState.currentPage) Color(0xFFFFD700) else Color(0xFF333333)
                Surface(
                    color = color,
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .width(if (index == pagerState.currentPage) 18.dp else 6.dp)
                        .height(6.dp),
                ) {}
            }
        }
    }
}

@Composable
private fun TemplateRow(
    template: WorkoutTemplate,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (template.usesRoxZone)
                        stringResource(R.string.home_template_segments_on, template.segments.size)
                    else
                        stringResource(R.string.home_template_segments_off, template.segments.size),
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF555555))
        }
    }
}

@Composable
private fun ActionButton(label: String, subtitle: String?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                subtitle?.let {
                    Text(
                        it,
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF555555))
        }
    }
}

@Composable
private fun SectionLabel(title: String, subtitle: String?) {
    Column {
        HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(bottom = 8.dp))
        Text(title, color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        subtitle?.let {
            Text(
                it,
                color = Color(0xFF666666),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatRelativeDate(ms: Long): String {
    val diffSec = (System.currentTimeMillis() - ms) / 1000
    return when {
        diffSec < 60 -> "just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        diffSec < 604800 -> "${diffSec / 86400}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ms))
    }
}
