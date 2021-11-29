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

            }
        }
        val jvmMain by getting {
            dependencies {

                //Only the JVM version depends on carConduit
                implementation(project(":carConduit"))

                //TODO Android versions will use the framework for decoupled
                //TODO IPC. (Car in Android is a system service. Conduit
                //TODO on android is a client to it).

                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
            }
        }
    }
}