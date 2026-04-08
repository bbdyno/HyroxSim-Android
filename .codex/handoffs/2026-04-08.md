# HyroxSim-Android Handoff

업데이트: 2026-04-08

## 세션 시작 방법

- 새 세션은 반드시 이 문서부터 읽고 시작
- 작업 디렉토리는 `HyroxSim-Android` 루트 기준
- 추천 시작 문구:
  - `먼저 최신 handoff 문서 읽고 바로 이어서 진행해`

## 현재 HEAD / 저장소 상태

- 저장소: `HyroxSim-Android`는 별도 git repo
- 현재 브랜치: `main`
- 직전 커밋:
  - `6e9f714` `Make Android apps build locally`
  - `ac91fc7` `Bootstrap Android multi-module workspace`
- 작업 트리: 이 문서 시점 기준 최신 parity 작업 반영 상태

## 현재 구현된 것

- 멀티모듈 구조:
  - 앱: `app-mobile`, `app-wear`
  - 코어: `core-model`, `core-engine`, `core-format`, `core-sync`
  - 데이터: `data-local`, `data-healthservices`, `data-datalayer`, `data-healthconnect`
  - 기능: mobile 5개, wear 4개
- 코어 포팅:
  - HYROX preset / template / segment / completed workout 모델 포팅 완료
  - `WorkoutEngine` 상태 머신 포팅 완료
  - formatter / sync payload / packet codec 포팅 완료
- mobile 앱:
  - 홈 화면에서 템플릿 목록 표시
  - 템플릿 상세 화면에서 코스 확인 후 시작 / 커스터마이즈 가능
  - 세그먼트 편집형 builder에서 run / Rox Zone / station 추가, 재정렬, 수정 가능
  - history / summary / active workout 화면 동작
  - summary에서 zone / split / station breakdown 표시
  - phone-origin workout 시작 전 location runtime permission 요청
  - phone-origin run 세그먼트에서 GPS location 수집
  - phone-origin workout 시작 가능
  - completed workout 저장 가능
  - watch-origin mirror state 수신 가능
  - watch로 `advance / pause / resume / end` command 보내기 가능
- wear 앱:
  - 홈 / 시작 확인 / history / summary / active 화면 동작
  - watch-origin workout 시작 가능
  - watch-origin workout 및 HR relay 진입 시 sensor/location runtime permission 요청
  - completed workout 저장 및 phone sync 가능
  - phone-origin mirror state 수신 가능
  - phone으로 command 송신 가능
  - `HealthServicesExerciseSessionManager` 기본 연결 완료
  - `HealthServicesHeartRateMonitor` 기본 연결 완료
- 동기화:
  - `WearDataLayerSyncCoordinator` 사용
  - template / completed workout durable sync
  - live state / command / heart-rate relay realtime sync
- 로컬 저장:
  - `Room + DataStore` 기반 local persistence 사용
  - 기존 `hyrox-workout-library.bin` 파일이 있으면 1회 자동 마이그레이션

## 검증 완료

- JDK 17 설치 완료
- Android SDK command-line tools 설치 완료
- SDK 패키지 설치 완료:
  - `platform-tools`
  - `platforms;android-36`
  - `build-tools;36.0.0`
- 검증 명령:
  - `./gradlew --no-daemon --console=plain :app-mobile:assembleDebug`
  - `./gradlew --no-daemon --console=plain :app-wear:assembleDebug`
  - `./gradlew --no-daemon --console=plain :core-model:test :core-engine:test :core-format:test :core-sync:test :app-mobile:assembleDebug :app-wear:assembleDebug`
- 위 명령은 모두 통과한 상태

## 중요 파일

- mobile 앱 진입점: `app-mobile/src/main/java/com/bbdyno/hyroxsim/android/mobile/MainActivity.kt`
- wear 앱 진입점: `app-wear/src/main/java/com/bbdyno/hyroxsim/android/wear/MainActivity.kt`
- local persistence: `data-local/src/main/java/com/bbdyno/hyroxsim/android/data/local/LocalWorkoutLibrary.kt`
- watch exercise adapter: `data-healthservices/src/main/java/com/bbdyno/hyroxsim/android/data/healthservices/HealthServicesExerciseSessionManager.kt`
- HR relay adapter: `data-healthservices/src/main/java/com/bbdyno/hyroxsim/android/data/healthservices/HealthServicesHeartRateMonitor.kt`
- data layer sync: `data-datalayer/src/main/java/com/bbdyno/hyroxsim/android/data/datalayer/WearDataLayerSyncCoordinator.kt`

## iOS 대비 현재 차이

- 아직 iOS와 화면이 1:1 parity는 아님
- UIKit / SwiftUI 기준의 세밀한 시각 디자인까지 맞춘 것은 아님
- 실제 paired Android phone + Galaxy Watch 실기기 검증 미완

## 다음 우선순위

1. watch-origin 센서 수집을 summary/history 지표와 더 밀접하게 보정
2. 실제 paired Android phone + Galaxy Watch 실기기 검증 및 edge-case 정리
3. 시각 parity를 더 밀고 싶으면 active / design token / chart polish 순으로 강화

## 환경 메모

- 현재 로컬에서 사용한 JDK:
  - `/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- 현재 로컬 Android SDK:
  - `$HOME/Library/Android/sdk`
- 새 세션에서 build가 안 되면 위 두 경로부터 확인

## 규칙

- author / committer는 항상 `bbdyno <della.kimko@gmail.com>`
- 새 파일 헤더의 `Created by`도 항상 `bbdyno`
- 파일 헤더에는 도구명 대신 `bbdyno`만 사용
- 커밋 메시지는 한국어 원칙이지만 이미 남긴 3개 Android 초기 커밋은 영문 메시지 상태
