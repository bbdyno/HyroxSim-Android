# HyroxSim-Android — Claude Code 규칙

## 프로젝트 개요

HYROX 경기 시뮬레이션 앱의 Android + Wear OS 버전.
기능 목표는 기존 `HyroxSim` iOS/watchOS 앱과 동일하다.

## 세션 Handoff

- 최신 handoff: `.codex/handoffs/latest.md`
- 날짜별 스냅샷: `.codex/handoffs/YYYY-MM-DD.md`
- 새 세션은 초기 분석 전에 최신 handoff를 먼저 읽고 이어서 작업
- handoff에는 절대경로 대신 repo-relative path를 기록

## 커밋 / 작성자 규칙

- author / committer는 항상 `bbdyno <della.kimko@gmail.com>`
- `Co-Authored-By: Claude` 붙이지 않음
- 커밋 메시지는 한국어
- 작업 단위별로 커밋 분리

## 파일 헤더 형식

모든 Kotlin / Java 파일의 헤더:

```kotlin
//
//  FileName.kt
//  ModuleName
//
//  Created by bbdyno on M/D/YY.
//
```

- 새로 만들거나 수정하는 파일 헤더의 `Created by`는 항상 `bbdyno`
- `Codex` 이름을 파일 헤더에 쓰지 않음

## 아키텍처 원칙

- Kotlin 멀티모듈 구조를 기본으로 함
- 워치 센서 수집은 Wear OS `Health Services` 우선
- 폰↔워치 실시간 통신은 Wear OS `Data Layer API`
- 영속화는 `Room`, 설정은 `DataStore`
- UI는 `Jetpack Compose`, 워치는 `Compose for Wear OS`
- `Samsung Health Sensor SDK`는 Galaxy 전용 강화 옵션으로만 취급하고 초기 핵심 경로는 Google 공식 스택으로 유지

## 초기 범위

- 폰: 홈 / 프리셋 / 커스텀 빌더 / 액티브 운동 / 워치 미러 / 히스토리 / 요약
- 워치: 홈 / 시작 확인 / 액티브 운동 / 폰 미러 / 히스토리 / 요약
- 양방향 원격 명령과 completed workout 동기화

## 문서

- 구현 전 설계 기준 문서: `docs/PROJECT_PLAN.md`
- 현재 Phase 0 상태는 `.codex/handoffs/latest.md` 기준
