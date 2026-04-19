# HyroxSim-Android

HYROX 경기 시뮬레이터 안드로이드 앱. **가민 워치 전용** (Wear OS 미지원).

iOS 앱과 동일 기능 + 가민 Connect IQ 워치앱(`HyroxSim-Garmin`)과 페어링.

## 빌드

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test                # 단위 테스트
```

Android Studio Koala+ (2024.1+) 권장.

## 가민 연동

`libs/ConnectIQ-Android.aar`를 Garmin 개발자 포털에서 다운받아 드롭인 필요. 자세한 절차는 `libs/README.md` 참조.

## 모듈 구조

- `app` — Application, Activity, Navigation
- `core:domain` — 순수 Kotlin 도메인 (iOS HyroxCore와 1:1). `PacePlanner` + 번들된 `resources/pace/pace_planner.json`(201개 대회 레이스 데이터) 포함
- `core:persistence` — Room 저장소
- `core:sensors` — FusedLocation + HealthConnect
- `core:sync-garmin` — Connect IQ SDK 래퍼. `GarminGoalSyncService.SyncResult` 로 페어링 상태별 결과 구분
- `feature:home` / `feature:active` / `feature:builder` / `feature:goalsetup` / `feature:history` / `feature:settings` / `feature:summary`

## GoalSetup (데이터 기반 페이스 플래너)

iOS `PacePlannerViewController` 파리티:
- 목표 완주 시간 → 디비전별 5분 버킷 보간 → 티어(APEX/PRO/EXPERT/STRONG/SOLID/STEADY/RISING/STARTER) + "Top X% of N athletes" 표시
- 8-lap 러닝은 실측 fatigue curve 기반 adaptive 분배
- 스테이션별(SkiErg/Sled Push/Sled Pull/… Wall Balls) 평균을 버킷에서 추출해 표시
- 홈 `SELECT DIVISION` 카드에서 GOAL 버튼, `SAVED TEMPLATES` 카드에서 Goal 버튼으로 진입
- 빌트인 프리셋은 `builtin:<divisionRaw>` 라우트 키로 로딩 (DB 저장 없음)

## 페어링 게이트

`GarminBridge.isPaired` — 워치가 연결되고 CIQ 앱이 트래킹 중일 때만 true.
`GarminGoalSyncService.sendGoal()` 은 `SyncResult.{Sent, NotPaired, Failed}` 반환.
GoalSetup UI는 저장 후 메시지로 동기화 결과를 알림 ("Saved + synced to Garmin" vs "Saved (watch not paired)").

## 문서

- [CLAUDE.md](CLAUDE.md) — Claude Code 작업 규칙
- 프로토콜 단일 진실 원본: `../HyroxSim-Garmin/docs/MESSAGE_PROTOCOL.md`
- 도메인 포팅 스펙: `../HyroxSim-Garmin/docs/SPEC.md`
