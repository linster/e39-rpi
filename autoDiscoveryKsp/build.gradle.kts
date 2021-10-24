plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.31-1.0.0")
    implementation(project(":autoDiscoveryAnnotations"))


    implementation("com.squareup:kotlinpoet:1.9.0")
    //Only to access the annotations
    implementation("com.google.dagger:dagger:2.35.1")
}