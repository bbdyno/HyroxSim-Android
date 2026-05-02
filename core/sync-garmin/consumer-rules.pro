# Connect IQ SDK uses reflection on listener interfaces and parcelable
# device/app classes. R8 in any consuming module would otherwise rename
# them and silently break message delivery in release builds.
-keep class com.garmin.android.connectiq.** { *; }
-keep interface com.garmin.android.connectiq.** { *; }
-dontwarn com.garmin.android.connectiq.**
