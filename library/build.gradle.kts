import com.vanniktech.maven.publish.DeploymentValidation
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.astridha"
// artifact="smalldecimal"
version = "0.8.8"


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
    //macosX64()
    macosArm64()

    @OptIn(ExperimentalWasmDsl::class)
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
            // implementation(kotlin-test)
            implementation(libs.kotlin.test)
            // implementation(project("libs.kotlin.test"))
        }

    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)

    // remove comment slashes below for really publishing to maven central!
    signAllPublications()

    coordinates(group.toString(), "smalldecimal", version.toString())

    pom {
        name = "Small Decimal"
        description = "KMP Decimal type on a fixed 64bit footprint, " +
                "for 17-18 decimal digits and up to 15 decimal places. " +
                "Fully convenient implementation supporting arithmetical operators and comparators."
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
