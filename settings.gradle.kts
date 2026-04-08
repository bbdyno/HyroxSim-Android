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

rootProject.name = "HyroxSim-Android"

include(
    ":app-mobile",
    ":app-wear",
    ":ui-mobile",
    ":core-model",
    ":core-engine",
    ":core-format",
    ":core-sync",
    ":data-local",
    ":data-healthservices",
    ":data-datalayer",
    ":data-healthconnect",
    ":feature-home-mobile",
    ":feature-builder-mobile",
    ":feature-active-mobile",
    ":feature-history-mobile",
    ":feature-summary-mobile",
    ":feature-home-wear",
    ":feature-active-wear",
    ":feature-history-wear",
    ":feature-summary-wear",
)
