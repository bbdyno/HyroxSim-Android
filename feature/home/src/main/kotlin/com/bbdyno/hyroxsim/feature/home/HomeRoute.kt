package com.bbdyno.hyroxsim.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bbdyno.hyroxsim.core.domain.HyroxDivision

/**
 * Home landing — HYROX 디비전 9종 리스트. 선택 시 운동 시작.
 * iOS 앱의 HomeViewController 레이아웃을 Compose로 재구성.
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onStartWorkout: (divisionRaw: String) -> Unit,
) {
    Column(
        modifier = modifier
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = "HyroxSim",
            color = Color(0xFFFFD700),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "HYROX 디비전을 선택하세요",
            color = Color(0xFFAAAAAA),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        HorizontalDivider(color = Color(0xFF222222))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            items(HyroxDivision.entries.toList()) { division ->
                DivisionCard(
                    division = division,
                    onClick = { onStartWorkout(division.raw) },
                )
            }
        }
    }
}

@Composable
private fun DivisionCard(
    division: HyroxDivision,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = division.displayName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = division.shortName + " · 8 stations",
                color = Color(0xFF888888),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
