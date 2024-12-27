plugins {
    alias(libs.plugins.kotlinMultiplatform)
}


kotlin {
    jvm("desktop")

    sourceSets {
        val commonMain by getting

        commonMain.dependencies {
            //Only to access the annotations
            implementation("com.google.dagger:dagger:2.45")
        }
    }
}