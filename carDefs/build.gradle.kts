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
                implementation("com.squareup.okio:okio:2.6.0")

            }
        }

        val jvmMain by sourceSets.getting {
            dependencies {
                api("com.google.dagger:dagger:2.35.1")
            }
        }
    }
}