//import org.jetbrains.compose.compose
//import org.jetbrains.kotlin.daemon.common.configureDaemonJVMOptions

plugins {

//    kotlin("multiplatform") version "1.6.0"
        // __KOTLIN_COMPOSE_VERSION__
//        kotlin("jvm") version "1.5.31"
//    kotlin("kapt") version "1.6.0"

//    id("org.jetbrains.compose") version "1.0.0-beta5"

//    id("com.github.johnrengelman.shadow") version "7.1.0"
    //id("java")
}

//kapt {
//    correctErrorTypes = true
//}


//https://imperceptiblethoughts.com/shadow/getting-started/

//group "ca.stefanm"
//version "1.0-SNAPSHOT"

//sourceCompatibility = 1.8

//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
//    kotlinOptions.freeCompilerArgs += "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi"
//}

repositories {
    mavenCentral()
    google()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}




dependencies {
//    kapt(project(":autoDiscovery"))
//    implementation(project(":autoDiscovery"))
//    implementation(project(":autoDiscoveryAnnotations"))
//
//    implementation("com.squareup.okio:okio:2.6.0")
//    implementation(compose.desktop.currentOs)
//
//    implementation( "org.jetbrains.kotlin:kotlin-stdlib")
//    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
//
//    implementation( "com.github.hypfvieh:dbus-java-osgi:3.2.3")
//    implementation( "com.github.hypfvieh:bluez-dbus:0.1.3")
//
//    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
//
//    implementation("com.javadocmd:simplelatlng:1.3.1")
//    implementation("org.jxmapviewer:jxmapviewer2:2.5")
//
//    implementation("com.uchuhimo:konf:1.1.2")
//    implementation("commons-io:commons-io:2.11.0")
//
//    implementation("com.ginsberg:cirkle:1.0.1")
//
//    api("com.google.dagger:dagger:2.35.1")
////    kapt("com.google.dagger:dagger-compiler:2.35.1")
//
//    implementation("com.pi4j:pi4j-core:1.1")
//
//    implementation("com.jakewharton.timber:timber:4.7.1")
//
//    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
//    testImplementation("junit:junit:4.12")
//
//
//    //Web
//    val ktor_version = "1.6.4"
//    implementation("io.ktor:ktor:${ktor_version}")
//    implementation("io.ktor:ktor-server-netty:${ktor_version}")
//    implementation("io.ktor:ktor-html-builder:${ktor_version}")
//
//    implementation("io.ktor:ktor-client-core:${ktor_version}")
//    implementation("io.ktor:ktor-client-cio:${ktor_version}")
//
//    implementation("ch.qos.logback:logback-classic:1.2.3")
//    implementation("io.ktor:ktor-client-logging:${ktor_version}")
//
//

}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ca.stefanm.ComposeMain"
    }
}
