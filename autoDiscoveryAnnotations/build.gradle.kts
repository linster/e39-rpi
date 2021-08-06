plugins {
    kotlin("jvm")
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
}