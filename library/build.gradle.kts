//import com.android.build.api.dsl.androidLibrary
//import org.gradle.internal.impldep.com.amazonaws.PredefinedClientConfigurations.defaultConfig
import org.gradle.api.problems.internal.GradleCoreProblemGroup.compilation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // alias(libs.plugins.kotlinMultiplatform)
    kotlin("multiplatform") version "2.3.0"
    // id("com.android.library")
    alias(libs.plugins.android.kotlin.multiplatform.library)

    //alias(libs.plugins.vanniktech.mavenPublish)
    id("com.vanniktech.maven.publish") version "0.35.0"
    // id("org.jetbrains.kotlin.kapt")
}

group = "io.github.astridha"
//artifact="decimal"
version = "0.5.0"


kotlin {
    // for strict mode
    explicitApi()

    // targets
    jvm()
    //@Suppress("UnstableApiUsage")
    // androidLibrary {
    androidLibrary {
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
            minSdk = 31
            aarMetadata {
                minCompileSdk = 29
            }
            //targetSdk = 34
            //versionCode = 1
            //versionName = "0.5"

            //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

 */
        compilerOptions {
            jvmTarget.set(
                JvmTarget.JVM_11
            )
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
        nodejs()
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

    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "decimal", version.toString())

    pom {
        name = "Decimal Library"
        description = "Slim Decimal Type on a 64bit footprint."
        inceptionYear = "2026"
        url = "https://github.com/astridha/decimal/"
        licenses {
            license {
                name = "MIT License"
                url = "https://www.opensource.org/licenses/mit-license.php"
                distribution = "https://www.opensource.org/licenses/mit-license.php"
            }
        }
        developers {
            developer {
                id = "astridha"
                name = "Astrid Hanssen"
                url = "https://github.com/astridha/"
                email = "github@astrid-hanssen.de"
                organization = "astrid"
                organizationUrl = "https://github.com/astridha/"
            }
        }
        scm {
            url = "https://github.com/astridha/decimal/"
            connection = "scm:git:git://github.com/astridha/decimal.git"
            developerConnection = "scm:git:ssh://git@github.com/astridha/decimal/"
        }
    }
}
