plugins {
    kotlin("multiplatform") version "1.5.31"
}

apply(plugin = "kotlin-kapt")

repositories {
    google()
    jcenter()
    mavenCentral()
    //maven("https://kotlin.bintray.com/kotlinx")
}

kotlin {

    jvm("jvm") {
        withJava()
    }

    sourceSets {
        val commonMain by sourceSets.getting {
            dependencies {
                implementation(project(":carDefs"))
                implementation(project(":logger"))
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("com.squareup.okio:okio:2.6.0")

                implementation("com.google.dagger:dagger:2.35.1")
            }
        }
        val jvmMain by getting {
            dependencies {

                //Only the JVM version depends on carConduit
                api(project(":carDefs"))
                api(project(":logger"))


                //TODO Android versions will use the framework for decoupled
                //TODO IPC. (Car in Android is a system service. Conduit
                //TODO on android is a client to it).

                api(kotlin("stdlib"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

                api("com.google.dagger:dagger:2.35.1")
                configurations["kapt"].dependencies
                    .add(
                        org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency(
                            "com.google.dagger",
                            "dagger-compiler",
                            "2.35.1"
                        )
                    )

                api( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
                api( "com.github.hypfvieh:bluez-dbus:0.1.3")

                api("com.pi4j:pi4j-core:1.1")
                api("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
            }
        }

        val desktopMain by creating {
            dependsOn(commonMain)
            dependsOn(jvmMain)

            dependencies {
                api(kotlin("stdlib"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.0")
            }
        }

        val jvmTest by getting {
            dependencies {
                //testImplementation("junit:junit:4.12")
                implementation(kotlin("test"))
            }
        }
    }
}