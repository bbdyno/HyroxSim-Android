# libs/

로컬 바이너리 드롭 위치. gitignore됨.

## ConnectIQ Android SDK

~~AAR을 여기에 드롭하세요~~ — **더 이상 불필요**.

가민이 Connect IQ Android SDK를 **Maven Central**에 공식 배포하면서(`com.garmin.connectiq:ciq-companion-app-sdk`), `core/sync-garmin/build.gradle.kts`가 직접 의존성을 선언합니다:

```kotlin
api(libs.garmin.connectiq)   // 2.3.0
```

참고: https://central.sonatype.com/artifact/com.garmin.connectiq/ciq-companion-app-sdk

이 폴더는 만약 미래에 Maven Central에 없는 별도 바이너리 (예: OEM 파트너 확장)를 써야 할 때를 위해 예약해둡니다.
