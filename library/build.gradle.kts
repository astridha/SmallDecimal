//import com.android.build.api.dsl.androidLibrary
//import org.gradle.internal.impldep.com.amazonaws.PredefinedClientConfigurations.defaultConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // alias(libs.plugins.kotlinMultiplatform)
    kotlin("multiplatform") version "2.2.21"
    // id("com.android.library")
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    // id("org.jetbrains.kotlin.kapt")
}

group = "io.github.astridha"
//artifact="compact-decimal"
version = "1.0.0"


kotlin {
    // for strict mode
    explicitApi()

    // targets
    jvm()
    //@Suppress("UnstableApiUsage")
    // androidLibrary {
    android {
        namespace = "io.github.astridha.decimal"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
/*
        defaultConfig {
            //applicationId = "io.github.astridha.decimal"
            minSdk = 31
            //targetSdk = 34
            //versionCode = 1
            //versionName = "1.0"

            //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

 */

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    linuxArm64()

    mingwX64()
    macosX64()
    macosArm64()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // ...
        binaries.executable()
    }

    js {
        browser()
        binaries.executable()
    }


    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            //implementation(androidx.test.runner)
            // testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        }

        val wasmJsMain by getting {
            dependencies {
                // Wasm-specific dependencies
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "compact-decimal", version.toString())

    pom {
        name = "decimal Library"
        description = "Multiplatform Decimal library."
        inceptionYear = "2025"
        url = "https://github.com/astridha/decimal/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}
