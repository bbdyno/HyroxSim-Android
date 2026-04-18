package com.bbdyno.hyroxsim.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = hiltViewModel(),
) {
    var statusText by remember { mutableStateOf("연결 안 됨") }

    Column(
        modifier = modifier
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "설정",
            color = Color(0xFFFFD700),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(color = Color(0xFF222222))

        Surface(
            color = Color(0xFF0C0C0C),
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("가민 워치 연결", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "가민 워치로 HYROX 운동을 기록하려면:\n" +
                        "• Garmin Connect 앱이 폰에 설치·로그인되어 있어야 합니다\n" +
                        "• 워치가 Garmin Connect에 페어링되어 있어야 합니다\n" +
                        "• Connect IQ Store에서 HyroxSim 워치앱을 설치하세요",
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                )
                Text(
                    text = "상태: ${vm.connectedDeviceName ?: statusText}",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                )
                Button(
                    onClick = {
                        vm.requestDeviceSelection()
                        statusText = "Garmin Connect Mobile 실행됨"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("기기 선택", fontWeight = FontWeight.Bold) }
            }
        }

        Surface(
            color = Color(0xFF0C0C0C),
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("앱 버전", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("1.0.0 (안드로이드 · 가민 전용)",
                    color = Color(0xFF888888), fontSize = 12.sp)
            }
        }
    }
}
