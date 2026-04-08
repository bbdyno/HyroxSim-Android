plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.bbdyno.hyroxsim.android.mobile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bbdyno.hyroxsim.android.mobile"
        minSdk = libs.versions.minSdkMobile.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":ui-mobile"))
    implementation(project(":core-model"))
    implementation(project(":core-engine"))
    implementation(project(":core-format"))
    implementation(project(":core-sync"))
    implementation(project(":data-local"))
    implementation(project(":data-datalayer"))
    implementation(project(":data-healthconnect"))
    implementation(project(":feature-home-mobile"))
    implementation(project(":feature-builder-mobile"))
    implementation(project(":feature-active-mobile"))
    implementation(project(":feature-history-mobile"))
    implementation(project(":feature-summary-mobile"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
