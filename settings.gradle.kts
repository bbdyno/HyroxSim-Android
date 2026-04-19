pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HyroxSim"

include(":app")
include(":core:domain")
include(":core:persistence")
include(":core:sync-garmin")
include(":feature:home")
include(":feature:active")
include(":feature:history")
include(":feature:settings")
include(":feature:builder")
include(":feature:summary")
