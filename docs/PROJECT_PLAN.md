# HyroxSim-Android 상세 작업 계획

업데이트: 2026-04-08

현재 진행:
- Phase 0 멀티모듈 Gradle 스캐폴드 생성 완료
- Phase 1 1차 코어 포팅 완료
- mobile / wear 앱 기본 흐름 구현 완료
- JDK 17 / Android SDK 설치 후 core test + 양쪽 `assembleDebug` 검증 완료
- 현재 local persistence는 `Room + DataStore` 대신 file-backed 임시 구현

## 1. 목표

기존 `HyroxSim` iOS + Apple Watch 앱과 기능적으로 동등한 Android + Wear OS 앱을 별도 프로젝트로 구축한다.

핵심 목표:
- Android 폰 앱과 Galaxy Watch / Wear OS 워치 앱 제공
- 워치 시작 → 폰 실시간 미러, 폰 시작 → 워치 미러 둘 다 지원
- 양방향 명령 `advance / pause / resume / end` 지원
- 템플릿, 완료 운동, 히스토리, 요약 화면까지 parity 확보

## 2. 범위 정의

### MVP 포함

| 영역 | 모바일 | 워치 |
|---|---|---|
| 홈 / 프리셋 | O | O |
| 커스텀 운동 빌더 | O | X |
| 액티브 운동 | O | O |
| 실시간 미러 | O | O |
| 양방향 원격 명령 | O | O |
| 히스토리 / 요약 | O | O |
| completed workout 동기화 | O | O |

### MVP 제외

- Garmin 연동
- 서버 백엔드 / 계정 시스템
- 클라우드 히스토리 동기화
- 타일 / 컴플리케이션 / 워치페이스
- Samsung 전용 고급 센서 최적화

## 3. 기능 parity 기준

기존 iOS 코드 기준으로 Android에서도 다음 흐름을 맞춘다.

### 폰 시작 운동

1. 폰에서 템플릿 선택 후 운동 시작
2. 폰 로컬 엔진이 세그먼트 진행과 화면 상태를 관리
3. 워치가 미러 화면을 열고 현재 운동 상태를 표시
4. 워치가 가능하면 심박을 폰으로 릴레이
5. 워치에서 `pause/resume/advance/end` 명령을 보내면 폰 운동에 반영

### 워치 시작 운동

1. 워치에서 템플릿 선택 후 운동 시작
2. 워치가 운동 세션의 authoritative source
3. 워치에서 GPS + HR을 수집
4. 폰이 실시간 mirror 화면을 표시
5. 폰에서 명령을 보내면 워치 운동에 반영
6. 종료 후 completed workout을 폰에 동기화

## 4. 권장 기술 선택

### UI

- Mobile: `Jetpack Compose`
- Wear: `Compose for Wear OS`

근거:
- Google 공식 문서에서 Wear OS용 Jetpack Compose와 Material 3 구성을 제공함
- 워치 전용 네비게이션, screen size, rotary input 패턴을 공식적으로 지원

### 워치 운동 센서 / 세션

- 1차 선택: `Health Services ExerciseClient`
- 2차 옵션: `Samsung Health Sensor SDK`를 Galaxy Watch 전용 강화 경로로 추가

근거:
- Health Services `ExerciseClient`는 운동 시작/일시정지/재개/종료와 exercise capabilities 조회를 공식 지원
- 공식 compatibility 문서상 exercise type에 따라 필요한 센서만 켜고, outdoor run에서 watch GPS location을 제공함
- Samsung SDK는 Galaxy Watch4+에서 raw/processed 센서 접근이 가능하지만 emulator 미지원이므로 기본 경로로 두기엔 리스크가 큼

### 폰↔워치 통신

- 실시간 상태/명령: `MessageClient`
- durable 동기화: `DataClient`
- 대용량 파일이 필요해지면 `ChannelClient`

근거:
- Google 공식 문서상 `MessageClient`는 RPC/one-way request에 적합하지만 persistence/retry가 없음
- `DataClient`의 `DataItem`은 nearby devices 간 persistent sync에 적합하고 offline read/write도 가능

### 로컬 저장소

- Phone: `Room` + `DataStore`
- Wear: `Room` + `DataStore`

역할:
- `Room`: templates, completed workouts, segment records
- `DataStore`: user preferences, last selected division, debug flags

### 선택하지 않는 것

- Kotlin Multiplatform: 지금 단계에서는 불필요
- Flutter / React Native: 센서/워치 통합, 배터리, background exercise 경로가 복잡해짐
- Samsung Health Sensor SDK 우선 설계: Galaxy 외 Wear OS 확장성을 해침

## 5. 제안 아키텍처

### 저장소 구조

```text
HyroxSim-Android/
├── app-mobile
├── app-wear
├── core-model
├── core-engine
├── core-format
├── core-sync
├── data-local
├── data-healthservices
├── data-datalayer
├── data-healthconnect
├── feature-home-mobile
├── feature-builder-mobile
├── feature-active-mobile
├── feature-history-mobile
├── feature-summary-mobile
├── feature-home-wear
├── feature-active-wear
├── feature-history-wear
└── feature-summary-wear
```

### 모듈 책임

| 모듈 | 역할 |
|---|---|
| `core-model` | 도메인 모델, enum, value object |
| `core-engine` | 운동 상태 머신, 세그먼트 전환, HR/거리 ingest |
| `core-format` | duration/pace/distance formatter |
| `core-sync` | `LiveWorkoutState`, `WorkoutCommand`, `HeartRateRelay`, packet codec |
| `data-local` | Room entities/dao/repository |
| `data-healthservices` | Wear OS 운동 세션, HR/GPS adapter |
| `data-datalayer` | phone/watch message + durable sync adapter |
| `data-healthconnect` | phone 쪽 Health Connect read/write 옵션 |
| `app-mobile`, `app-wear` | 앱 엔트리, navigation, DI |

### Android 쪽 authoritative boundaries

- 운동 엔진은 `core-engine`에 두고 pure Kotlin으로 유지
- 플랫폼 센서는 adapter로 주입
- 폰/워치 동기화 payload는 `core-sync`에서 공통 정의
- UI는 상태 모델만 소비

## 6. iOS 코드와의 매핑

기존 iOS `HyroxCore`에서 Android로 직접 포팅할 우선 대상:

| iOS 소스 | Android 대상 |
|---|---|
| `Targets/HyroxCore/Sources/Models/*` | `core-model` |
| `Targets/HyroxCore/Sources/Engine/*` | `core-engine` |
| `Targets/HyroxCore/Sources/Formatters/*` | `core-format` |
| `Targets/HyroxCore/Sources/Sync/*` | `core-sync` |

그대로 가져가지 않는 것:

| iOS 소스 | 이유 |
|---|---|
| `HyroxPersistenceApple` | SwiftData 전용 |
| `HyroxLiveActivityApple` | ActivityKit 전용 |
| `WatchConnectivity` 구현체 | Android에서는 Data Layer로 대체 |
| `HealthKit` 구현체 | Wear OS Health Services로 대체 |

## 7. 동기화 설계

### 실시간 패킷

Android에서도 iOS와 동일한 개념의 payload를 유지한다.

- `WorkoutOrigin`
- `LiveWorkoutState`
- `WorkoutCommand`
- `HeartRateRelay`
- `LiveSyncPacket`

### 전송 채널 분리

| 종류 | 기술 | 이유 |
|---|---|---|
| live state | `MessageClient` | low latency, 실시간 화면 반영 |
| remote command | `MessageClient` | RPC 성격 |
| template sync | `DataClient` | persistent sync 필요 |
| completed workout sync | `DataClient` | durable delivery 필요 |
| large export | `ChannelClient` | 100KB 이상 대비 |

### 권장 authoritative model

- watch-origin workout: 워치 authoritative
- phone-origin workout: 폰 authoritative
- mirror side는 UI + remote command + optional sensor relay만 담당

이 구조가 현재 iOS 구현과 가장 가깝다.

## 8. 센서 전략

### watch-origin workout

- `ExerciseClient`로 운동 세션 시작
- HR, distance, pace, location 수집
- outdoor run 세그먼트에만 location 사용
- station 세그먼트에서는 GPS 소비를 줄이는 방향으로 상태 처리

### phone-origin workout

- 폰은 자체 운동 엔진과 GPS를 사용
- 워치는 미러 모드로 진입
- 워치 HR 릴레이는 phase 1 spike 후 최종 결정

주의:
- 이 경로는 iOS와 동일 parity를 만들 수 있지만, 워치에서 HR만 켜는 최소 workout session 설계가 배터리와 UX에 직접 영향
- 여기서 `Health Services`만으로 충분한지 먼저 검증하고, 부족하면 `Samsung Health Sensor SDK`를 Galaxy 전용 옵션으로 추가

## 9. MVP 구현 순서

### Phase 0. 프로젝트 부트스트랩

산출물:
- Android Studio 멀티모듈 프로젝트
- `app-mobile`, `app-wear`, `core-*` 모듈
- Compose / Compose Wear OS / Hilt / Room / DataStore 기본 설정

완료 조건:
- 폰 앱과 워치 앱이 각각 실행
- paired emulator 또는 실제 Galaxy Watch 연결 가능

현재 상태:
- 스캐폴드 생성 완료
- wrapper 포함
- 실제 실행 검증은 환경 설치 후 진행 필요

### Phase 1. 코어 포팅

산출물:
- Kotlin `WorkoutTemplate`, `WorkoutSegment`, `SegmentRecord`, `CompletedWorkout`
- Kotlin `WorkoutEngine`
- formatter / preset / sync packet 정의

완료 조건:
- iOS `HyroxKitTests`와 동등한 코어 단위 테스트 작성
- 기본 HYROX preset 생성 가능

### Phase 2. Mobile 앱 기본 화면

산출물:
- 홈 / 프리셋 선택 / 템플릿 상세 / 빌더 / 로컬 히스토리
- Room persistence

완료 조건:
- 폰 단독 운동을 시작하기 전까지의 흐름이 동작

### Phase 3. Wear 앱 기본 화면

산출물:
- 홈 / 시작 확인 / 액티브 운동 / 히스토리 / 요약
- 템플릿 동기화 수신

완료 조건:
- 워치에서 preset 선택 후 로컬로 운동 시작 가능

### Phase 4. 워치 운동 세션

산출물:
- `Health Services` exercise session
- HR / distance / pace / GPS adapter
- 세그먼트 전환 UI

완료 조건:
- 워치 단독 운동 기록 가능
- completed workout 생성 및 로컬 저장 가능

### Phase 5. 실시간 미러링

산출물:
- phone-origin live mirror
- watch-origin live mirror
- 양방향 `advance/pause/resume/end`

완료 조건:
- 실시간 상태가 두 화면에 반영
- mirror side에서 명령 전송 가능

### Phase 6. durable sync

산출물:
- template sync
- completed workout sync
- history reconciliation

완료 조건:
- 오프라인/지연 연결 후에도 template/workout 데이터가 복구

### Phase 7. 배터리 / 안정화

산출물:
- sensor batching / throttling 점검
- reconnect / duplicate delivery / idempotency 처리
- 실제 Galaxy Watch 테스트

완료 조건:
- 장시간 운동 중 배터리/연결 문제의 주요 케이스 정리

## 10. 예상 공수

1인 기준 대략:

| 단계 | 예상 |
|---|---|
| Phase 0 | 1~2일 |
| Phase 1 | 3~5일 |
| Phase 2 | 4~6일 |
| Phase 3 | 3~5일 |
| Phase 4 | 5~8일 |
| Phase 5 | 5~8일 |
| Phase 6 | 3~5일 |
| Phase 7 | 4~6일 |

총합:
- MVP 최소 4주
- 실제 디바이스 안정화 포함 5~7주

## 11. 테스트 전략

### 단위 테스트

- `WorkoutEngine`
- formatter
- preset 생성
- sync packet codec
- repository mapper

### 통합 테스트

- Room persistence
- Data Layer payload encode/decode
- Health Services mock / fake session

### E2E

- 폰 시작 → 워치 미러
- 워치 시작 → 폰 미러
- 명령 round-trip
- completed workout sync
- reconnect after temporary disconnect

### 실제 디바이스 검증

- Galaxy Watch 실제 기기 필수
- Samsung Health Sensor SDK를 쓰는 경우 emulator 미지원

## 12. 주요 리스크

### A. phone-origin workout에서 watch HR relay

가장 큰 기술 리스크다.

이유:
- 워치가 운동 authoritative source가 아닐 때도 HR 세션을 열어야 할 수 있음
- 배터리 소모와 UX 영향이 큼

대응:
- Phase 0~1 사이에 spike 구현
- `Health Services`만으로 가능한지 먼저 검증
- 부족하면 Samsung SDK 추가 여부 결정

### B. Data Layer 연결 품질

- `MessageClient`는 persistence/retry가 없음
- 따라서 live state에는 적합하지만 history/template sync에는 부적합

대응:
- live와 durable sync 채널을 분리
- completed workout/template는 `DataClient` 사용

### C. Wear OS 기기 capability 편차

- Health Services capability는 기기별로 다를 수 있음
- optional data types가 다를 수 있음

대응:
- 런타임 capability probing 필수
- unsupported metric은 graceful degradation

### D. Galaxy 전용 기능의 종속성 증가

- Samsung SDK를 일찍 넣으면 타 Wear OS 호환성이 약해짐

대응:
- core path는 Google 공식 스택 유지
- Samsung SDK는 adapter layer 뒤에 격리

## 13. 바로 시작할 구현 체크리스트

1. Android Studio 멀티모듈 프로젝트 생성
2. phone / wear 앱 모듈 생성
3. shared Kotlin modules 생성
4. iOS `HyroxCore` 모델/엔진 포팅
5. Room schema 설계
6. Data Layer transport abstraction 설계
7. Wear `ExerciseClient` vertical slice 구현
8. phone-origin / watch-origin state machine 테스트 작성

## 14. 공식 문서 근거

- Wear OS Data Layer API overview  
  https://developer.android.com/training/wearables/data/overview
- Wear OS Data Layer client type 선택 가이드  
  https://developer.android.com/training/wearables/data/client-types
- Health Services `ExerciseClient` 가이드  
  https://developer.android.com/health-and-fitness/health-services/active-data
- Health Services compatibility 가이드  
  https://developer.android.com/health-and-fitness/health-services/compatibility
- Health Connect 시작 가이드  
  https://developer.android.com/health-and-fitness/health-connect/get-started
- Jetpack Compose on Wear OS  
  https://developer.android.com/training/wearables/compose
- Samsung Health Sensor SDK  
  https://developer.samsung.com/health/sensor/guide/introduction.html

## 15. 결정 요약

- 이 프로젝트는 충분히 현실적이다
- Garmin보다 Android + Galaxy Watch 쪽이 구현 경로가 더 명확하다
- 초기 MVP는 Google 공식 스택만으로 설계하고, Galaxy 전용 강화는 후순위가 맞다
- 다음 실제 작업은 설계가 아니라 스캐폴딩과 core port부터 들어가는 것이 가장 효율적이다
