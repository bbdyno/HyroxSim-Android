# HyroxSim-Android Handoff

업데이트: 2026-04-08

## 현재 상태

- `HyroxSim-Android` 작업 공간 생성 완료
- Phase 0 멀티모듈 Gradle 스캐폴드 생성 완료
- Android 저장소 git 초기화 완료
- 앱 모듈: `app-mobile`, `app-wear`
- 공유 모듈: `core-model`, `core-engine`, `core-format`, `core-sync`
- 데이터 모듈: `data-local`, `data-healthservices`, `data-datalayer`, `data-healthconnect`
- 기능 모듈: mobile 5개, wear 4개 실제 화면 코드 반영 완료
- Gradle wrapper 포함
- Phase 1 1차 코어 포팅 완료
  - `core-model`: workout/domain types, segment records, completed workout, presets
  - `core-engine`: engine state machine
  - `core-format`: duration / distance formatter
  - `core-sync`: live state, packet/envelope coder, sync interface
  - Kotlin unit tests 통과
- JDK 17, Android SDK command-line tools, `platforms;android-36`, `build-tools;36.0.0` 설치 완료
- `:app-mobile:assembleDebug`, `:app-wear:assembleDebug` 통과
- mobile 앱:
  - 홈 / 커스텀 builder / 히스토리 / 요약 / 액티브 화면 구현
  - file-backed local persistence 구현
  - phone-origin workout 시작 및 completed workout 저장 구현
  - watch-origin live mirror 수신 및 remote command 송신 구현
- wear 앱:
  - 홈 / 히스토리 / 요약 / 액티브 화면 구현
  - watch-origin workout 시작 및 completed workout 저장 구현
  - phone-origin live mirror 수신 및 command 송신 구현
  - `HealthServicesExerciseSessionManager`, `HealthServicesHeartRateMonitor` 기본 wiring 연결
- 현재 persistence는 계획의 `Room + DataStore`가 아니라 `ObjectOutputStream` 기반 file store 임시 구현

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

1. runtime permission flow 추가
2. watch-origin 센서 수집을 summary/history 지표와 더 밀접하게 보정
3. phone-origin GPS 수집 추가 여부 결정
4. `data-local`을 `Room + DataStore`로 승격할지 판단
5. 실제 paired device 검증 및 edge-case 정리

## 메모

- author / committer는 항상 `bbdyno <della.kimko@gmail.com>`
- 새 파일 헤더의 `Created by`도 항상 `bbdyno`로 유지하고 `Codex` 표기는 남기지 않음
