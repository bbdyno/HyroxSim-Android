plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.bbdyno.hyroxsim.sync.garmin"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore)

    // Connect IQ Android SDK (AAR). Dropped into ../../libs/ by the developer.
    // See libs/README.md for provisioning instructions.
    implementation(fileTree(mapOf("dir" to "../../libs", "include" to listOf("*.aar"))))
}
