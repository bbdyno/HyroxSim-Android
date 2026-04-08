plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bbdyno.hyroxsim.android.data.local"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdkMobile.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.androidx.room.compiler)
}
