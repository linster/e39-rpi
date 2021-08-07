plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("java")
}

//group 'ca.stefanm'
//version 'unspecified'

repositories {
    mavenCentral()
    google()
    maven("https://kotlin.bintray.com/kotlinx")
}

//Witchcraft and sorcery to get around
//      Symbol is declared in module 'jdk.compiler' which does not export package 'com.sun.tools.javac.code'
//https://github.com/gradle/gradle/issues/15538
tasks {
    withType<JavaCompile> {
        options.fork(mapOf(Pair("jvmArgs", listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))))
    }
}


dependencies {


    //Only to access the annotations
    implementation("com.google.dagger:dagger:2.35.1")

    implementation( "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation(project(":autoDiscoveryAnnotations"))
}