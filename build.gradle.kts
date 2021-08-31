import org.jetbrains.compose.compose

plugins {

    if (true) {
        // __KOTLIN_COMPOSE_VERSION__
        kotlin("jvm") version "1.5.10"
        kotlin("kapt") version "1.5.10"
//        id("org.jetbrains.compose") version "0.3.2"
        id("org.jetbrains.compose") version "0.4.0"
    } else {
//        kotlin("jvm") version "1.4.21"
//        kotlin("kapt") version "1.4.21"
//        id("org.jetbrains.compose") version "0.0.0-unmerged-build21"
    }
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("java")
}

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
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

//
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
    implementation(compose.desktop.currentOs)

    implementation( "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
//    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-flow:1.4.3")

    implementation( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
    implementation( "com.github.hypfvieh:bluez-dbus:0.1.3")

    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")

    implementation("com.javadocmd:simplelatlng:1.3.1")


    implementation("org.jxmapviewer:jxmapviewer2:2.5")

    implementation("com.russhwolf:multiplatform-settings:0.7.7")
    implementation("com.russhwolf:multiplatform-settings-serialization:0.7.7")
    implementation("com.russhwolf:multiplatform-settings-coroutines:0.7.7")


    implementation("com.arkivanov.decompose:decompose:0.2.2")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:0.2.2")
    implementation("com.ginsberg:cirkle:1.0.1")

    api("com.google.dagger:dagger:2.35.1")
    kapt("com.google.dagger:dagger-compiler:2.35.1")

    implementation("com.pi4j:pi4j-core:1.1")

    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
    testImplementation("junit:junit:4.12")


    //Web
    implementation("io.ktor:ktor:1.5.1")
    implementation("io.ktor:ktor-server-netty:1.5.1")
    implementation("io.ktor:ktor-html-builder:1.5.1")

    implementation("io.ktor:ktor-client-core:1.5.1")
    implementation("io.ktor:ktor-client-cio:1.5.1")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.ktor:ktor-client-logging:1.5.1")



}

compose.desktop {
    application {
        mainClass = "ca.stefanm.ibus.gui.GuiMainKt"
    }
}

//compileKotlin {
//    kotlinOptions.jvmTarget = "1.8"
//}
//compileTestKotlin {
//    kotlinOptions.jvmTarget = "1.8"
//}