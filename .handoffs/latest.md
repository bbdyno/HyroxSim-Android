# Handoff — 2026-04-19

## 현재 상태
안드로이드 앱 처음부터 재구축 완료. 가민 전용(Wear OS 미지원). `./gradlew assembleDebug` 성공.

## 완료 커밋

| 커밋 | 내용 |
|---|---|
| `8fa9da7` | 스캐폴딩 + Gradle 8.10 wrapper + 모듈 구조 + 버전 카탈로그 |
| `49f939f` | core:domain 포팅 + 엔진 단위 테스트 7 PASS |
| `7b4c835` | core:persistence (Room) DAO/Repository/Mapper |
| `c40492b` | core:sync-garmin Connect IQ 래퍼 (v1 프로토콜) |
| `e03e25a` | Compose UI + Hilt DI + 4개 feature 모듈 |

## 빌드
```bash
./gradlew assembleDebug          # 전체 앱 APK
./gradlew :core:domain:test       # 단위 테스트 (엔진 7종)
./gradlew :app:installDebug       # 기기/에뮬레이터 설치
```

필수 사전 조건:
- `local.properties`에 `sdk.dir=...` (gitignore — 로컬 생성 필요)
- JDK 17 (`JAVA_HOME` 설정)
- Android SDK Platform 34, Build Tools 34.0.0

## 모듈 맵
```
app/                    AndroidEntry + MainActivity + Nav + Hilt AppModule
core/domain             (Kotlin-only) 엔진/디비전/세그먼트 + 단위 테스트
core/persistence        (Android) Room DAO + Mapper + Repository
core/sync-garmin        (Android) Connect IQ SDK 래퍼 (Stub + Real 템플릿)
feature/home            Compose 디비전 선택
feature/active          Compose 운동 진행 + ViewModel (500ms 틱)
feature/history         Compose 완료 운동 목록 (Flow)
feature/settings        Compose 가민 페어링 UI
```

## 가민 연동 활성화 절차

1. 가민 개발자 포털에서 **Connect IQ Android SDK (AAR)** 다운로드
2. `libs/ConnectIQ-Android.aar` 로 복사 (gitignore됨)
3. `core/sync-garmin/src/main/kotlin/.../GarminBridge.kt` 하단 `RealGarminBridge` 템플릿 주석 해제
4. `GarminBridge.provide(context)`의 반환을 `StubGarminBridge` → `RealGarminBridge`로 교체
5. Garmin Connect Mobile 앱이 폰에 설치·로그인·워치 페어링 완료 상태 필요

## 구현 완료된 기능

- 31 세그먼트 HYROX 프리셋 자동 생성 (9 디비전 전부)
- WorkoutEngine 상태머신 (start/advance/pause/resume/finish/undo)
- Room 기반 영속 저장 + source 필드 (watch/manual/garmin)
- Garmin v1 프로토콜 메시지 인/디코드 + ACK 패턴
- 하단 탭 네비게이션 (홈/기록/설정)
- Compose 전용 UI (Material 3, 블랙+골드 토큰)
- Hilt DI 통합 (싱글턴 DB, Bridge, Repository)

## 미적용 / 추후 과제

| 항목 | 메모 |
|---|---|
| GPS / HealthConnect 센서 통합 | iOS `CoreLocationAdapter`/`HKWorkoutSession`에 대응. 현재는 수동 세그먼트 전환만 |
| Live Activity / Notification | iOS `HyroxLiveActivityApple` 기능. Android는 Foreground Service + OngoingNotification |
| 워크아웃 빌더 화면 | iOS `TemplateBuilderViewController`. 사용자 커스텀 템플릿 생성 |
| Summary 화면 | 운동 완료 후 상세 리뷰 (세그먼트별 시간/HR/거리) |
| 다국어 리소스 | en/ko/ja/zh-Hans/zh-Hant strings |
| 가민 Real 브릿지 | AAR 드롭 후 템플릿 코드 활성화 (5분 작업) |
| 테스트 범위 확장 | Mapper, Codec 단위 테스트 미작성 |

## 다음 단계 우선순위 (제안)

1. **가민 AAR 드롭 + Real 브릿지 활성화** — 1시간 내 가능
2. **Summary 화면** — 기존 도메인 재사용, 뷰만 추가
3. **GPS / HealthConnect 통합** — 권한 플로우 포함, 3-5일
4. **다국어 리소스** — iOS strings.xml 그대로 포팅

## 참고 파일 (iOS/워치)
- 도메인 원본: `../HyroxSim-iOS/Targets/HyroxCore/Sources/**`
- 메시지 프로토콜 단일 진실 원본: `../HyroxSim-Garmin/docs/MESSAGE_PROTOCOL.md`
- iOS Garmin 통합: `../HyroxSim-iOS/Targets/HyroxSim/Sources/Integration/Garmin/`
