package com.bbdyno.hyroxsim.feature.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bbdyno.hyroxsim.core.domain.HyroxDivision

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuilderRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    vm: BuilderViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsState()
    val saved by vm.savedTemplates.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "커스텀 운동",
            color = Color(0xFFFFD700),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(color = Color(0xFF222222))

        OutlinedTextField(
            value = ui.name,
            onValueChange = vm::onNameChanged,
            label = { Text("이름") },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0C0C0C),
                unfocusedContainerColor = Color(0xFF0C0C0C),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFAAAAAA),
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Text("디비전", color = Color(0xFFAAAAAA), fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HyroxDivision.entries.forEach { d ->
                FilterChip(
                    selected = ui.division == d,
                    onClick = { vm.onDivisionChanged(d) },
                    label = { Text(d.shortName) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF0C0C0C),
                        selectedContainerColor = Color(0xFFFFD700),
                        labelColor = Color.White,
                        selectedLabelColor = Color.Black,
                    ),
                )
            }
        }

        Surface(
            color = Color(0xFF0C0C0C),
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ROX Zone 사용", fontWeight = FontWeight.SemiBold)
                    Text(
                        "각 Run↔Station 전환에 ROX Zone 구간 삽입",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                    )
                }
                Switch(
                    checked = ui.usesRoxZone,
                    onCheckedChange = vm::onRoxZoneToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFFFFD700),
                    ),
                )
            }
        }

        Button(
            onClick = vm::onSave,
            enabled = !ui.saving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (ui.saving) "저장 중..." else "저장 + 가민 동기화", fontWeight = FontWeight.Bold)
        }

        ui.savedMessage?.let { msg ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg, color = Color(0xFFAAAAAA), fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = vm::clearMessage) { Text("닫기", color = Color(0xFFFFD700)) }
            }
        }

        if (saved.isNotEmpty()) {
            HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(top = 12.dp))
            Text("저장된 템플릿", color = Color(0xFFAAAAAA), fontSize = 14.sp)
            saved.forEach { t ->
                Surface(
                    color = Color(0xFF0C0C0C),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${t.segments.size}세그 · ROX ${if (t.usesRoxZone) "on" else "off"}",
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                            )
                        }
                        TextButton(onClick = { vm.delete(t) }) {
                            Text("삭제", color = Color(0xFFFF3B30))
                        }
                    }
                }
            }
        }
    }
}
