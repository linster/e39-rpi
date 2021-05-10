import org.jetbrains.compose.compose

plugins {

    if (true) {
        // __KOTLIN_COMPOSE_VERSION__
        kotlin("jvm") version "1.4.31"
        kotlin("kapt") version "1.4.31"
        id("org.jetbrains.compose") version "0.3.2"
    } else {
//        kotlin("jvm") version "1.4.21"
//        kotlin("kapt") version "1.4.21"
//        id("org.jetbrains.compose") version "0.0.0-unmerged-build21"
    }
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("java")
}


//https://imperceptiblethoughts.com/shadow/getting-started/

//group "ca.stefanm"
//version "1.0-SNAPSHOT"

//sourceCompatibility = 1.8

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
    implementation("com.squareup.okio:okio:2.6.0")
    implementation(compose.desktop.currentOs)

    implementation( "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
//    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-flow:1.4.3")

    implementation( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
    implementation( "com.github.hypfvieh:bluez-dbus:0.1.3")

    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")

    implementation("org.jxmapviewer:jxmapviewer2:2.5")


    implementation("com.arkivanov.decompose:decompose:0.2.4")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:0.2.4")
    implementation("com.ginsberg:cirkle:1.0.1")

    api("com.google.dagger:dagger:2.28.1")
    kapt("com.google.dagger:dagger-compiler:2.28.1")

    implementation("com.pi4j:pi4j-core:1.1")

    implementation("com.jakewharton.timber:timber:4.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
    testImplementation("junit:junit:4.12")


    //Web
    implementation("io.ktor:ktor:1.5.1")
    implementation("io.ktor:ktor-server-netty:1.5.1")
    implementation("io.ktor:ktor-html-builder:1.5.1")

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