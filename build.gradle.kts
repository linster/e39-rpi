plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

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

    id("java")
//    kotlin("jvm") version "2.0.10"
    kotlin("kapt") version "2.0.10"
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.multiplatform") version "2.0.10"
//    id("org.jetbrains.plugin.compose") version "1.7.1"

    //https://imperceptiblethoughts.com/shadow/getting-started/
    id("com.github.johnrengelman.shadow") version "7.1.0"

    id("com.google.protobuf") version "0.9.4"
}

kapt {
    correctErrorTypes = true
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.time.ExperimentalTime"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.ExperimentalUnsignedTypes"
    kotlinOptions.freeCompilerArgs += "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
}

repositories {
    mavenCentral()
    google()
//    flatDir { dir("localJarLibs") }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}


tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ca.stefanm.ComposeMain"
    }
}
