import com.vanniktech.maven.publish.DeploymentValidation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)

    id("com.vanniktech.maven.publish") version "0.36.0"
    // id("maven-publish")
}

group = "io.github.astridha"
// artifact="smalldecimal"
version = "0.8.5"


kotlin {
    // for strict mode
    explicitApi()

    // targets
    jvm()

     android {
        namespace = "io.github.astridha.smalldecimal"
        // defined in libs.versions.toml:
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget.set(
                JvmTarget.JVM_17
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
            // none!
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // implementation(androidx.test.runner)
            // testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        }

    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)

    // remove comment slashes below for really publishing to maven central!
    signAllPublications()

    coordinates(group.toString(), "smalldecimal", version.toString())

    pom {
        name = "KMP Small Decimal Library"
        description = "Everyday Decimal type on a 64bit footprint with up to 15 decimal places."
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
