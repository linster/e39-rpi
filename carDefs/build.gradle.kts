plugins {
    kotlin("multiplatform") version "1.5.31"
}
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
            }
        }
    }
}