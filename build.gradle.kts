plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kapt) apply false

    //TODO STEFAN where's this go?
//    kotlin("kapt") version "2.0.10"

}

//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
//    }
//}


//kapt {
//    correctErrorTypes = true
//}
//
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.time.ExperimentalTime"
//    kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
//    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.ExperimentalUnsignedTypes"
//    kotlinOptions.freeCompilerArgs += "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
//}
//

// TODO STEFAN can we remove this?
//tasks.withType<Jar> {
//    manifest {
//        attributes["Main-Class"] = "ca.stefanm.ComposeMain"
//    }
//}
