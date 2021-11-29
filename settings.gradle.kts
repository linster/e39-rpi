pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "rpi"
include(":hmi")
include(":car")
include(":carConduit")
include(":logger")
include(":autoDiscoveryAnnotations")
include(":autoDiscovery")
