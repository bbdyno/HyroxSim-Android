# Handoff — 2026-04-19

## 현재 상태
안드로이드 앱 처음부터 재구축 완료 + 가민 SDK 실제 연동까지 완료. `./gradlew assembleDebug` 성공.

## 완료 커밋

| 커밋 | 내용 |
|---|---|
| `8fa9da7` | 스캐폴딩 + Gradle 8.10 wrapper + 모듈 구조 + 버전 카탈로그 |
| `49f939f` | core:domain 포팅 + 엔진 단위 테스트 7 PASS |
| `7b4c835` | core:persistence (Room) DAO/Repository/Mapper |
| `c40492b` | core:sync-garmin Connect IQ 래퍼 스캐폴딩 |
| `e03e25a` | Compose UI + Hilt DI + 4개 feature 모듈 |
| `f6f56b4` | handoff 초안 |
| `9c7a0ce` | **가민 SDK Maven Central 실연결 + RealGarminBridge 구현** |

## 가민 SDK 통합 상태
**✅ 활성화 완료**. Maven Central에 가민이 공식 배포:
```
com.garmin.connectiq:ciq-companion-app-sdk:2.3.0
```

`RealGarminBridge`가 실제 SDK API 사용 중 — 별도 드롭인 불필요.

운영 시 전제조건:
- 사용자 폰에 Garmin Connect Mobile 앱 설치
- Garmin Connect Mobile에서 워치 이미 페어링
- Connect IQ Store에서 HyroxSim 워치앱 설치

## 빌드
```bash
./gradlew assembleDebug          # 전체 앱 APK
./gradlew :core:domain:test       # 단위 테스트 (엔진 7종 PASS)
./gradlew :app:installDebug       # 기기/에뮬레이터 설치
```

사전 조건:
- `local.properties`에 `sdk.dir=...` (gitignore — 로컬 생성 필요)
- JDK 17
- Android SDK Platform 34 + Build Tools 34.0.0

## 모듈 맵
```
app/                    HyroxApplication + MainActivity + Nav + Hilt AppModule
core/domain             (Kotlin-only) 엔진/디비전/세그먼트 + 테스트
core/persistence        (Android) Room DAO + Mapper + Repository
core/sync-garmin        (Android) Connect IQ SDK 실연결 (Real bridge)
feature/home            Compose 디비전 선택 9개
feature/active          Compose 운동 진행 + ViewModel (500ms 틱)
feature/history         Compose 완료 운동 목록 (Flow)
feature/settings        Compose 가민 페어링 UI
```

## 구현 완료된 기능

- 31 세그먼트 HYROX 프리셋 자동 생성 (9 디비전 전부)
- WorkoutEngine 상태머신 (start/advance/pause/resume/finish/undo)
- Room 기반 영속 저장 + source 필드 (watch/manual/garmin)
- Garmin v1 프로토콜 메시지 인/디코드 + ACK 패턴
- **가민 SDK 실제 API 연결** (ConnectIQ/IQDevice/IQApp)
- 하단 탭 네비게이션 (홈/기록/설정)
- Compose 전용 UI (Material 3, 블랙+골드 토큰)
- Hilt DI 통합

## 미적용 / 추후 과제

| 항목 | 메모 |
|---|---|
| GPS / HealthConnect 센서 통합 | iOS `CoreLocationAdapter`/`HKWorkoutSession`에 대응. 현재는 수동 세그먼트 전환만 |
| Live Activity / Notification | iOS `HyroxLiveActivityApple`. Android는 Foreground Service 필요 |
| 워크아웃 빌더 화면 | iOS `TemplateBuilderViewController`. 사용자 커스텀 템플릿 |
| Summary 화면 | 운동 완료 후 상세 리뷰 |
| 다국어 리소스 | en/ko/ja/zh-Hans/zh-Hant strings |
| 테스트 범위 확장 | Mapper, Codec 단위 테스트 |
| 실기기 가민 테스트 | FR265/FR965 기기 필요 |

## 참고 파일
- 도메인 원본: `../HyroxSim-iOS/Targets/HyroxCore/Sources/**`
- 메시지 프로토콜 단일 진실 원본: `../HyroxSim-Garmin/docs/MESSAGE_PROTOCOL.md`
- iOS Garmin 통합: `../HyroxSim-iOS/Targets/HyroxSim/Sources/Integration/Garmin/`
- 가민 SDK GitHub: https://github.com/garmin/connectiq-companion-app-sdk-ios
- 가민 SDK Maven: https://central.sonatype.com/artifact/com.garmin.connectiq/ciq-companion-app-sdk
