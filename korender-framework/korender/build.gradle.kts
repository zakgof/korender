import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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

// Generated build config
val generatedBuildDir = layout.buildDirectory.dir("generated/kotlin/korenderBuild")

val generateKorenderBuild by tasks.registering {
    outputs.dir(generatedBuildDir)
    doLast {
        val korenderVersion: String by project
        val korenderVersionSuffix: String by project
        val versionValue = korenderVersion + korenderVersionSuffix
        val pkgDir = generatedBuildDir.get().asFile.resolve("com/zakgof/korender")
        pkgDir.mkdirs()
        val outFile = pkgDir.resolve("KorenderBuild.kt")
        outFile.writeText("""package com.zakgof.korender

object KorenderBuild {
    const val version: String = "${versionValue}"
}
""")
    }
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
        freeCompilerArgs.addAll("-Xexpect-actual-classes", "-Xcontext-parameters")
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
        }
    }

    sourceSets {
        val desktopMain by getting
        val commonMain by getting {
            kotlin.srcDir(generatedBuildDir)
        }

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

tasks.matching { it.name.startsWith("compileKotlin") }.configureEach { dependsOn(generateKorenderBuild) }

mavenPublishing {
    val korenderVersion: String by project
    val korenderVersionSuffix: String by project

    publishToMavenCentral()
    signAllPublications()
    coordinates("com.github.zakgof", "korender", korenderVersion + korenderVersionSuffix)

    pom {
        name = "korender"
        description = "Kotlin Multiplatform 3D rendering framework"
        inceptionYear = "2024"
        url = "https://github.com/zakgof/korender"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
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