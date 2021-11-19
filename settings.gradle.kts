pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "rpi"
include(":hmi")
include(":autoDiscoveryAnnotations")
include(":autoDiscovery")
