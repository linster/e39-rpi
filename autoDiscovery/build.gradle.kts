plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kapt)
}


//plugins {
//    kotlin("jvm")
//    kotlin("kapt")
//    id("java")
//}

kotlin {
    jvm("desktop") { withJava() }

    sourceSets {
        val commonMain by getting

        commonMain.dependencies {
            //Only to access the annotations
            implementation("com.google.dagger:dagger:2.54")
            implementation("com.squareup:kotlinpoet:1.12.0")
            implementation(project(":autoDiscoveryAnnotations"))
        }
    }
}

//Witchcraft and sorcery to get around
//      Symbol is declared in module 'jdk.compiler' which does not export package 'com.sun.tools.javac.code'
//https://github.com/gradle/gradle/issues/15538
//tasks {
//    withType<JavaCompile> {
//        options.fork(mapOf(Pair("jvmArgs", listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))))
//    }
//}


//dependencies {
//
//
//    //Only to access the annotations
//    implementation("com.google.dagger:dagger:2.45")
//
//    implementation( "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("com.squareup:kotlinpoet:1.12.0")
//    implementation(project(":autoDiscoveryAnnotations"))
//}