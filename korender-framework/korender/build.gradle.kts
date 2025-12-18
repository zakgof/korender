import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.net.URI

plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.zakgof.korender.resources"
    generateResClass = always
}

kotlin {

    jvm("desktop")
    androidLibrary {
        namespace = "com.zakgof.korender"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        androidResources.enable = true
    }
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
        }
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.material)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlin.reflect)
            implementation(compose.desktop.currentOs)

            implementation(libs.lwjgl)
            implementation(libs.lwjgl.opengl)
            implementation(libs.lwjgl.jawt)
            implementation(libs.lwjgl3.awt.get().toString()) {
                exclude(group = "org.lwjgl")
            }
            listOf("windows", "linux", "macos", "macos-arm64").forEach {
                runtimeOnly(dependencies.variantOf(libs.lwjgl) { classifier("natives-$it") })
                runtimeOnly(dependencies.variantOf(libs.lwjgl.opengl) { classifier("natives-$it") })
            }
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

val korenderVersion: String by project
val korenderVersionSuffix: String by project

version = korenderVersion + korenderVersionSuffix

mavenPublishing {

    publishToMavenCentral()
    publishing {
        repositories {
            maven {
                name = "WorldMandia"
                url = URI.create(
                    (version.toString().endsWith("SNAPSHOT")
                        .let { if (it) "https://repo.worldmandia.cc/snapshots" else "https://repo.worldmandia.cc/releases" })
                )

                credentials {
                    password = System.getenv("password")
                    username = System.getenv("username")
                }
            }
        }
    }
    signAllPublications()
    coordinates("com.github.zakgof", "korender", version.toString())

    pom {
        name = "korender"
        description = "Kotlin Multiplatform 3D rendering framework"
        inceptionYear = "2024"
        url = "https://github.com/zakgof/korender"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "zakgof"
                name = "Oleksandr Zakusylo"
                url = "https://github.com/zakgof"
            }
        }
        scm {
            url = "https://github.com/zakgof/korender"
            connection = "scm:git:https://github.com/zakgof/korender.git"
            developerConnection = "scm:git:https://github.com/zakgof/korender.git"
        }
    }
}