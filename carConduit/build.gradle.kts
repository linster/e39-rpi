plugins {
    kotlin("multiplatform") version "1.5.31"
}

apply(plugin = "kotlin-kapt")

kotlin {

    jvm("jvm") {
        withJava()
    }

    sourceSets {
        val commonMain by sourceSets.getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation(project(":logger"))
                implementation(project(":carDefs"))

                implementation("com.squareup.okio:okio:2.6.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation(project(":logger"))

                implementation( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
                implementation( "com.github.hypfvieh:bluez-dbus:0.1.3")

                implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")

                configurations["kapt"].dependencies
                    .add(
                        org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency(
                            "com.google.dagger",
                            "dagger-compiler",
                            "2.35.1"
                        )
                    )
            }
        }
    }
}