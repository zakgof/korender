import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

val korenderVersion: String by project

compose.resources {
    publicResClass = true
    packageOfResClass = "com.zakgof.app.resources"
    generateResClass = auto
}

kotlin {
    android {
        namespace = "com.zakgof.korender.examples"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources {
            enable = true
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters")
    }

    sourceSets {
        val desktopMain by getting

        getByName("desktopTest") {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                runtimeOnly(libs.junit.jupiter.engine)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(project(":korender"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.java)
        }
        getByName("wasmJsMain").dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")
}

compose.desktop {
    application {
        mainClass = "com.zakgof.korender.MainKt"
        jvmArgs("--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")
        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "com.zakgof.korender"
            packageVersion = korenderVersion
            modules(
                "java.net.http",
                "jdk.unsupported"
            )
            windows {
                iconFile.set(project.file("korender32.ico"))
            }
            linux {
                iconFile.set(project.file("korender32.png"))
            }
        }
        buildTypes.release.proguard {
            isEnabled = false
        }
    }
}
