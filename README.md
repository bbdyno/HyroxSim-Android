# HyroxSim-Android

HYROX 시뮬레이션 앱의 Android + Wear OS 버전 작업 공간입니다.

현재 상태:
- Phase 0 멀티모듈 Gradle 스캐폴드 생성 완료
- Phase 1 코어 포팅 완료
- mobile / wear 앱 흐름 구현 완료
- `app-mobile`, `app-wear`, `core-*`, `data-*`, `feature-*` 모듈 실제 코드 반영 완료
- 로컬 파일 기반 템플릿 / 히스토리 저장 구현 완료
- 폰 시작 / 워치 시작 / 양방향 mirror command 기본 연결 완료
- 상세 계획은 `docs/PROJECT_PLAN.md`에 정리
- 로컬 JDK 17 + Android SDK 기준 `assembleDebug` 및 core test 검증 완료

바로 볼 문서:
- `docs/PROJECT_PLAN.md`
- `CLAUDE.md`
- `.codex/handoffs/latest.md`

예상 초기 명령:
- `./gradlew -v`
- `./gradlew tasks`
- `./gradlew :core-model:test :core-engine:test :core-format:test :core-sync:test`
- `./gradlew :app-mobile:assembleDebug`
- `./gradlew :app-wear:assembleDebug`

목표:
- Android 폰 앱 + Galaxy Watch / Wear OS 워치 앱 지원
- 기존 iOS/watchOS 앱과 기능 동등성 확보
- 코어 도메인/엔진/동기화 스키마를 Kotlin 멀티모듈로 재구성
