import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kapt)

    //https://imperceptiblethoughts.com/shadow/getting-started/
//    id("com.github.johnrengelman.shadow") version "7.1.0"

//    id("com.google.protobuf") version "0.9.4"
}

kotlin {

    jvm("desktop") {
        withJava()
    }

    sourceSets {

        val commonMain by getting
        val desktopMain by getting {
            languageSettings {
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlin.ExperimentalCoroutinesApi")
            }
        }
        //desktopMain.dependsOn(commonMain)

        commonMain.dependencies {
            implementation(compose.components.resources)
        }

        desktopMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation("com.kizitonwose.calendar:compose-multiplatform:2.6.2")

            //https://github.com/gradle/kotlin-dsl-samples/issues/1372#issuecomment-515285784
            configurations.get("kapt").dependencies.add(project(":autoDiscovery"))
//            kapt(project(":autoDiscovery"))
            implementation(project(":autoDiscovery"))
            implementation(project(":autoDiscoveryAnnotations"))

            implementation("com.google.dagger:dagger:2.54")


            configurations.get("kapt").dependencies.add(
                DefaultExternalModuleDependency("com.google.dagger", "dagger-compiler", "2.57.2"))


            configurations.get("kapt").dependencies.add(
                DefaultExternalModuleDependency("org.jetbrains.kotlin","kotlin-metadata-jvm","2.2.0"))

            //implementation(compose.desktop.currentOs)
            println(System.getenv("BUILD_PI"))
            if (System.getenv("BUILD_PI") == "true") {
                implementation(compose.desktop.linux_arm64)
            } else {
                implementation(compose.desktop.currentOs)
            }

            implementation(libs.kotlinx.coroutines.swing)
//            implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

            implementation("commons-io:commons-io:2.14.0")

            implementation("com.squareup.okio:okio:3.4.0")
            implementation( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
            implementation( "com.github.hypfvieh:bluez-dbus:0.1.3")

            implementation("com.fazecast:jSerialComm:2.10.1")

            implementation("com.pi4j:pi4j-core:1.1")

            implementation("com.google.protobuf:protobuf-java:4.28.2")
            implementation("com.google.protobuf:protobuf-kotlin:4.28.2")
            implementation("com.google.protobuf:protobuf-java-util:3.23.0")

            implementation("com.javadocmd:simplelatlng:1.3.1")
            implementation("org.jxmapviewer:jxmapviewer2:2.5")

            implementation("com.ginsberg:cirkle:1.0.1")

            implementation("com.uchuhimo:konf:1.1.2")

            //Web
            val ktor_version = "2.1.0"
            implementation("io.ktor:ktor:${ktor_version}")
            implementation("io.ktor:ktor-server-netty:${ktor_version}")

            implementation("io.ktor:ktor-client-core:${ktor_version}")
            implementation("io.ktor:ktor-client-cio:${ktor_version}")

            implementation("ch.qos.logback:logback-classic:1.4.12")
            implementation("io.ktor:ktor-client-logging:${ktor_version}")

            //Trixnity (https://trixnity.gitlab.io/trixnity/docs/client/create)
            val trixnityVersion = "4.9.2"

            fun trixnity(module: String, version: String = trixnityVersion) =
                "net.folivo:trixnity-$module:$version"

            implementation(trixnity("client"))
            implementation(trixnity("client-repository-exposed"))
            implementation(trixnity("client-media-okio"))

            // So that the Exposed DB for Trixnity writes to a sqlite file
            implementation("org.xerial:sqlite-jdbc:3.44.1.0")

            implementation("io.github.pablichjenkov:daily-agenda-view:1.2.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            
//            desktopTestImplementation("junit:junit:4.13.1")
        }
    }
}

compose.desktop {
    application {
        mainClass = "ca.stefanm.ComposeMain"
        nativeDistributions {
            packageVersion = "1.0.0"
            packageName = "e39Rpi"
            description = "BMW E39 HMI"
            copyright = "© 2021-2025 Stefan Martynkiw."
            vendor = "stefanm.ca"
            targetFormats(
                TargetFormat.Deb
            )
//            includeAllModules = true
            //appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ca.stefanm.ibus.resources"
    generateResClass = always
}