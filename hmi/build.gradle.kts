import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.findKaptConfiguration

plugins {
    kotlin("multiplatform") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0-rc2"
}

apply(plugin = "kotlin-kapt")

group = "ca.stefanm.ibus"
version = "1.0"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi"
}

repositories {
    google()
    jcenter()
    mavenCentral()
    //maven("https://kotlin.bintray.com/kotlinx")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//    dependsOn("kaptKotlinJvm")
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi"
}

kotlin {

    linuxArm64("rpi") {
    }

    linuxX64("laptop") {

    }

    jvm("jvm") {
        withJava()

        compilations {
            val main by compilations.getting {
                //https://github.com/JetBrains/compose-jb/issues/272#issuecomment-760731693
                configurations {
                    "implementation" {
                        exclude(group = "androidx.compose.animation")
                        exclude(group = "androidx.compose.foundation")
                        exclude(group = "androidx.compose.material")
                        exclude(group = "androidx.compose.runtime")
                        exclude(group = "androidx.compose.ui")
                    }
                }
            }
        }

        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            //kotlinOptions.javaParameters = true
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }

    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)


                //https://stackoverflow.com/questions/62283259/generated-classes-with-kapt-in-metadata-dependency

                println(configurations.asMap.map { it.key }.toString())
                configurations["kapt"].dependencies.add(
                    project(":autoDiscovery")
                )
                implementation(project(":autoDiscovery"))
                implementation(project(":autoDiscoveryAnnotations"))

                api("com.google.dagger:dagger:2.35.1")
//                configurations["kapt"].dependencies.add(
//                    implementation("com.google.dagger:dagger-compiler:2.35.1")
//                )

                configurations["kapt"].dependencies
                    .add(
                        org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency(
                            "com.google.dagger",
                            "dagger-compiler",
                            "2.35.1"
                        )
                    )


                //TODO Next, I think there's something that needs to
                //TODO include the generated sources into this source-set
                //https://kotlinlang.org/docs/mpp-configure-compilations.html#create-a-custom-compilation

                implementation("com.squareup.okio:okio:2.6.0")
                implementation(kotlin("stdlib"))
                implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
                implementation( "com.github.hypfvieh:bluez-dbus:0.1.3")
                implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
                implementation("com.javadocmd:simplelatlng:1.3.1")
                implementation("org.jxmapviewer:jxmapviewer2:2.5")
                implementation("com.uchuhimo:konf:1.1.2")
                implementation("commons-io:commons-io:2.11.0")
                implementation("com.ginsberg:cirkle:1.0.1")
                implementation("com.pi4j:pi4j-core:1.1")
                implementation("com.jakewharton.timber:timber:4.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")


                //Web
                val ktor_version = "1.6.4"
                implementation("io.ktor:ktor:${ktor_version}")
                implementation("io.ktor:ktor-server-netty:${ktor_version}")
                implementation("io.ktor:ktor-html-builder:${ktor_version}")

                implementation("io.ktor:ktor-client-core:${ktor_version}")
                implementation("io.ktor:ktor-client-cio:${ktor_version}")

                implementation("ch.qos.logback:logback-classic:1.2.3")
                implementation("io.ktor:ktor-client-logging:${ktor_version}")


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
//Has to be after the Kotlin block above.
//apply(plugin = "kapt")
compose.desktop {
    application {
        //from(kotlin.targets["rpi"])
        mainClass = "ca.stefanm.ibus.gui.GuiMainKt"

        nativeDistributions {
            packageVersion = "1.0.0"
            packageName = "e39Rpi"
            description = "BMW E39 HMI"
            copyright = "© 2021 Stefan Martynkiw."
            vendor = "stefanm.ca"
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage
            )
            includeAllModules = true
            //appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

        }
    }
}
