rootProject.name = "e39-RPi"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://kotlin.bintray.com/kotlinx")
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven { url = uri(path = "https://jitpack.io") }
    }
}

include(":composeApp")
include(":autoDiscoveryAnnotations")
include(":autoDiscovery")
