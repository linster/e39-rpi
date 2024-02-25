import org.jetbrains.compose.compose
import org.jetbrains.kotlin.daemon.common.configureDaemonJVMOptions


buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
    }
}
plugins {

    if (true) {
        // __KOTLIN_COMPOSE_VERSION__
        kotlin("jvm") version "1.8.20" //TODO https://github.com/Kotlin/kotlinx.serialization/issues/1417#issuecomment-1021545236
        kotlin("kapt") version "1.8.20" //TODO update to 1.6.20+ to get sealed interface seriaization working.
//        id("org.jetbrains.compose") version "0.3.2"
        id("org.jetbrains.compose") version "1.4.0"
    } else {
//        kotlin("jvm") version "1.4.21"
//        kotlin("kapt") version "1.4.21"
//        id("org.jetbrains.compose") version "0.0.0-unmerged-build21"
    }
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("java")

//    id("com.squareup.sqldelight") version "1.5.5"

    id("com.google.protobuf") version "0.9.4"
}


//sqldelight {
//    database("NavigationDb") {
//        packageName = "ca.stefanm.e39.navigation.db"
//        //sourceFolders = listOf("db")
//        schemaOutputDirectory = file("build/dbs")
//        //dependency(project(":OtherProject"))
//        dialect = "sqlite:3.24"
//        verifyMigrations = true
//    }
//}

kapt {
    correctErrorTypes = true
}


//https://imperceptiblethoughts.com/shadow/getting-started/

//group "ca.stefanm"
//version "1.0-SNAPSHOT"

//sourceCompatibility = 1.8

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi"
}

repositories {
    mavenCentral()
    google()
//    flatDir { dir("localJarLibs") }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}



//jar {
//    manifest {
//        attributes 'Main-Class': 'ca.stefanm.ibus.MainKt'
//    }
//}



dependencies {


    kapt(project(":autoDiscovery"))
    implementation(project(":autoDiscovery"))
    implementation(project(":autoDiscoveryAnnotations"))

    implementation("com.squareup.okio:okio:2.6.0")

    println(System.getenv("BUILD_PI"))
    if (System.getenv("BUILD_PI") == "true") {
        implementation(compose.desktop.linux_arm64)
    } else {
        implementation(compose.desktop.currentOs)
    }
    implementation( "org.jetbrains.kotlin:kotlin-stdlib")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

    implementation( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
    implementation( "com.github.hypfvieh:bluez-dbus:0.1.3")

//    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
    implementation("com.fazecast:jSerialComm:2.10.1")


    implementation("com.google.protobuf:protobuf-java:3.23.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.23.0")
    implementation("com.google.protobuf:protobuf-java-util:3.23.0")

    implementation("com.javadocmd:simplelatlng:1.3.1")
    implementation("org.jxmapviewer:jxmapviewer2:2.5")

    implementation("com.uchuhimo:konf:1.1.2")
    implementation("commons-io:commons-io:2.11.0")

    implementation("com.ginsberg:cirkle:1.0.1")

    api("com.google.dagger:dagger:2.45")
    kapt("com.google.dagger:dagger-compiler:2.45")

    implementation("com.pi4j:pi4j-core:1.1")

    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
    testImplementation("junit:junit:4.12")


//    implementation("com.squareup.sqldelight:sqlite-driver:1.5.5")
//    implementation("com.squareup.sqldelight:coroutines-extensions-jvm:1.5.5")

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

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ca.stefanm.ComposeMain"
    }
}
compose.desktop {
    application {
//        mainClass = "ca.stefanm.ibus.gui.GuiMainKt"
        mainClass = "ca.stefanm.ComposeMain"


        nativeDistributions {
            packageVersion = "1.0.0"
            packageName = "e39Rpi"
            description = "BMW E39 HMI"
            copyright = "© 2021 Stefan Martynkiw."
            vendor = "stefanm.ca"
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            includeAllModules = true
            //appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

        }
    }
}
