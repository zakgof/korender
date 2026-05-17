pluginManagement {
    repositories {
        // maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "korender-root"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":korender")
include(":examples")
include(":examples-android")
include(":baker")
include(":treegen")
