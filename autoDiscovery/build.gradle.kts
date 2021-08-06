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

dependencies {
    implementation( "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.squareup:kotlinpoet:1.9.0")
    implementation(project(":autoDiscoveryAnnotations"))
}