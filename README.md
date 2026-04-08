# HyroxSim-Android

HYROX 시뮬레이션 앱의 Android + Wear OS 버전 작업 공간입니다.

현재 상태:
- Phase 0 멀티모듈 Gradle 스캐폴드 생성 완료
- `app-mobile`, `app-wear`, `core-*`, `data-*`, `feature-*` 모듈 생성 완료
- 상세 계획은 `docs/PROJECT_PLAN.md`에 정리
- 로컬 Java / Android SDK가 없어 실제 빌드 검증은 아직 못 함

바로 볼 문서:
- `docs/PROJECT_PLAN.md`
- `CLAUDE.md`
- `.codex/handoffs/latest.md`

예상 초기 명령:
- `./gradlew -v`
- `./gradlew tasks`
- `./gradlew :app-mobile:assembleDebug`
- `./gradlew :app-wear:assembleDebug`

목표:
- Android 폰 앱 + Galaxy Watch / Wear OS 워치 앱 지원
- 기존 iOS/watchOS 앱과 기능 동등성 확보
- 코어 도메인/엔진/동기화 스키마를 Kotlin 멀티모듈로 재구성
