package com.bbdyno.hyroxsim.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenSummary: (String) -> Unit = {},
    vm: HistoryViewModel = hiltViewModel(),
) {
    val rows by vm.summaries.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.history_title),
            color = Color(0xFFFFD700),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(
            color = Color(0xFF222222),
            modifier = Modifier.padding(vertical = 12.dp),
        )

        if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), color = Color(0xFF666666), fontSize = 14.sp)
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows) { s ->
                Surface(
                    onClick = { onOpenSummary(s.id) },
                    color = Color(0xFF0C0C0C),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = s.templateName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${formatDate(s.finishedAtEpochMs)}   ·   ${badge(s.sourceRaw)}",
                            color = Color(0xFF888888),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = stringResource(
                                R.string.history_row_meta_time,
                                formatElapsed(s.totalActiveDurationMs),
                            ) + "   " + stringResource(
                                R.string.history_row_meta_distance,
                                "%.2f".format(s.totalDistanceMeters / 1000),
                            ),
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun badge(sourceRaw: String): String = when (sourceRaw) {
    "watch" -> stringResource(R.string.history_source_watch)
    "garmin" -> stringResource(R.string.history_source_garmin)
    else -> stringResource(R.string.history_source_manual)
}

private fun formatDate(ms: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ms))

private fun formatElapsed(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
