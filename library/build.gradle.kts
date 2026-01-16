import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.3.0"
    alias(libs.plugins.android.kotlin.multiplatform.library)
    id("com.vanniktech.maven.publish") version "0.35.0"
    id("maven-publish")
}

group = "io.github.astridha"
// artifact="smalldecimal"
version = "0.6.0"


kotlin {
    // for strict mode
    explicitApi()

    // targets
    jvm()
    //@Suppress("UnstableApiUsage")
    // androidLibrary {
    androidLibrary {
        namespace = "io.github.astridha.smalldecimal"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

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

    // remove comment slashes below for publishing to maven central!
    // signAllPublications()

    coordinates(group.toString(), "smalldecimal", version.toString())

    pom {
        name = "Small Decimal Library"
        description = "Small Decimal Type on a 64bit footprint."
        inceptionYear = "2026"
        url = "https://github.com/astridha/smalldecimal/"
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
            url = "https://github.com/astridha/smalldecimal/"
            connection = "scm:git:git://github.com/astridha/smalldecimal.git"
            developerConnection = "scm:git:ssh://git@github.com/astridha/smalldecimal/"
        }
    }
}
