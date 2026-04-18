# libs/

외부에서 받은 `.aar` / `.jar`를 드롭하는 위치. gitignore됨.

## ConnectIQ-Android.aar (가민 워치 연동)

### 필요성
`core:sync-garmin` 모듈의 Kotlin 코드는 `ConnectIQ Android SDK`를 참조합니다. 이 AAR이 없으면 빌드가 실패합니다.

### 다운로드 절차

1. https://developer.garmin.com/connect-iq/overview/ 가민 개발자 계정 로그인
2. "Connect IQ Mobile SDK (Android)" 최신 버전 다운로드
3. 압축 해제 → `ConnectIQ-Android.aar` 파일을 이 디렉토리에 복사
4. `./gradlew :core:sync-garmin:build` 로 빌드 확인

### 의존성 등록 방식

`core/sync-garmin/build.gradle.kts`에서 `fileTree`로 자동 픽업:

```kotlin
implementation(fileTree("../../libs") { include("*.aar") })
```

### 주의사항
- 가민 재배포 라이선스 이슈 피하기 위해 repo에 커밋 금지
- 팀 CI에서는 Maven 사내 저장소 또는 Secrets 아티팩트 스토어로 주입
