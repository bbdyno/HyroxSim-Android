# HyroxSim-Android — Claude Code 규칙

## 프로젝트 개요

HYROX 경기 시뮬레이터 안드로이드 앱. **가민 워치 전용** — Wear OS 미지원.
형제 프로젝트:
- `../HyroxSim-iOS` — iOS 메인 앱 (Apple Watch + Garmin)
- `../HyroxSim-Garmin` — Connect IQ 워치 앱 (Monkey C)

## 언어 / 툴체인

- **Kotlin 2.0+**
- **Jetpack Compose** (Material 3)
- **Android Gradle Plugin 8.5+**
- **minSdk 26 (Android 8.0)**, **targetSdk 34 (Android 14)**
- **Hilt** for DI, **Room** for persistence, **Coroutines + Flow**
- **KSP** for Room/Hilt compile

## 모듈 구조

```
app/                            # Application entry + navigation
core/
├── domain/                     # 순수 Kotlin — 도메인 모델 + WorkoutEngine
├── persistence/                # Room 기반 저장소
└── sync-garmin/                # Connect IQ Android SDK 래퍼
feature/
├── home/                       # 디비전/템플릿 선택
├── active/                     # 운동 진행
├── history/                    # 완료 운동 목록
└── settings/                   # 가민 페어링 등
```

## 디자인 토큰

iOS 동일 블랙+골드 스킴.

```kotlin
// core/domain/Tokens.kt
object HyroxColors {
    val Background = Color(0xFF000000)
    val Accent = Color(0xFFFFD700)   // gold
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFAAAAAA)

    val Run = Color(0xFF007AFF)      // systemBlue
    val RoxZone = Color(0xFFFF9500)  // systemOrange
    val Station = Color(0xFFFFD700)
}
```

## 파일 헤더

Kotlin 파일은 헤더 주석을 **생략**합니다 (Kotlin 관례). 라이선스 고지는 LICENSE 파일로.

## 커밋 규칙

- **커밋 메시지: 한국어**
- author/committer: `bbdyno <della.kimko@gmail.com>`
- `Co-Authored-By: Claude` 붙이지 않음
- 작업 단위별 분리

## 가민 통합 원칙

- **Connect IQ Android SDK (AAR)** 는 `libs/` 아래에 수동 드롭인 (gitignore됨)
- Garmin Connect Mobile 앱이 폰에 설치·워치 페어링되어 있어야 작동
- 메시지 프로토콜: `../HyroxSim-Garmin/docs/MESSAGE_PROTOCOL.md` 가 단일 진실 원본
- enum raw value는 iOS Swift enum 자동 camelCase 값과 **동일 문자열** 유지 (`menOpenSingle` 등)

## 도메인 원칙

- `core/domain`은 **AndroidX / 플랫폼 API 의존 금지** (순수 Kotlin)
- `WorkoutEngine`은 시간을 외부 주입 (`Instant` 파라미터), 내부에서 `Clock.systemUTC()` 직접 호출 금지
- 값 타입은 `data class`, `@JvmInline value class`, sealed class 적극 사용

## Wear OS 미지원

iOS의 `HyroxSimWatch`에 해당하는 안드로이드 워치 앱은 **만들지 않음**. 유일한 워치 통합 경로는 Garmin.

## 주의사항

- HealthConnect는 **기기/OS 지원 확인 필수** — 미지원 기기 fallback 처리
- GPS는 `FusedLocationProviderClient`. 백그라운드 위치는 현재 미사용 (배터리)
- Compose Navigation으로 단일 Activity + 다중 Composable 구조
- 로컬 DB 마이그레이션은 Room `AutoMigration` 또는 Migration 수동 작성
