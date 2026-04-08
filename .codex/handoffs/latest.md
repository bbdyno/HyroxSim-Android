# HyroxSim-Android Handoff

업데이트: 2026-04-08

## 현재 상태

- `HyroxSim-Android` 작업 공간 생성 완료
- Phase 0 멀티모듈 Gradle 스캐폴드 생성 완료
- 앱 모듈: `app-mobile`, `app-wear`
- 공유 모듈: `core-model`, `core-engine`, `core-format`, `core-sync`
- 데이터 모듈: `data-local`, `data-healthservices`, `data-datalayer`, `data-healthconnect`
- 기능 모듈: mobile 5개, wear 4개 생성 완료
- Gradle wrapper 포함
- Phase 1 1차 코어 포팅 완료
  - `core-model`: workout/domain types, segment records, completed workout, presets
  - `core-engine`: engine state machine
  - `core-format`: duration / distance formatter
  - `core-sync`: live state, packet/envelope coder, sync interface
  - Kotlin unit test 골격 추가
- 로컬 Java / Android SDK 부재로 빌드 검증은 아직 못 함

## 기준 문서

- `docs/PROJECT_PLAN.md`
- `CLAUDE.md`

## 현재 결정

- 별도 Android 프로젝트로 진행
- Kotlin 멀티모듈 구조 채택
- 워치 운동 센서 수집은 `Health Services`
- 폰↔워치 실시간 상태/명령은 `MessageClient`
- 템플릿/완료 운동 같은 durable sync는 `DataClient`
- 영속화는 `Room`, 설정은 `DataStore`
- Galaxy Watch 고급 센서는 필요 시 `Samsung Health Sensor SDK`를 phase 2 이후 옵션으로 추가

## 다음 단계

1. Java 17 + Android SDK 설치 후 `./gradlew tasks` 및 기본 assemble 검증
2. `core-*` 포팅 코드의 compile/runtime 검증 및 Swift parity 차이 보정
3. mobile / wear feature shell을 실제 navigation 구조로 전환
4. `Health Services` vertical slice 구현
5. `Data Layer` transport abstraction 구현

## 메모

- author / committer는 항상 `bbdyno <della.kimko@gmail.com>`
- 새 파일 헤더의 `Created by`도 항상 `bbdyno`로 유지하고 `Codex` 표기는 남기지 않음
