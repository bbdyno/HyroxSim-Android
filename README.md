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
- `core:domain` — 순수 Kotlin 도메인 (iOS HyroxCore와 1:1)
- `core:persistence` — Room 저장소
- `core:sync-garmin` — Connect IQ SDK 래퍼
- `feature:home` / `feature:active` / `feature:history` / `feature:settings`

## 문서

- [CLAUDE.md](CLAUDE.md) — Claude Code 작업 규칙
- 프로토콜 단일 진실 원본: `../HyroxSim-Garmin/docs/MESSAGE_PROTOCOL.md`
- 도메인 포팅 스펙: `../HyroxSim-Garmin/docs/SPEC.md`
