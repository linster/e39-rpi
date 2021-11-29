plugins {
    kotlin("multiplatform") version "1.5.31"
}

kotlin {

    jvm("jvm") {
        withJava()
    }

    sourceSets {
        val commonMain by sourceSets.getting {

        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

                implementation("com.jakewharton.timber:timber:4.7.1")
            }
        }
    }
}