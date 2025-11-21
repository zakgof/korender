
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
}

val korenderVersion: String by project

compose.resources {
    publicResClass = true
    packageOfResClass = "com.zakgof.app.resources"
    generateResClass = auto
}

kotlin {

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(project(":korender"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization.core)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        webMain.dependencies {
        }
    }
}

android {
    namespace = "com.zakgof.korender"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.zakgof.korenderexamples"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 6
        versionName = korenderVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = project.properties["keyname"].toString()
            keyPassword = project.properties["keypassword"].toString()
            storeFile = file(project.properties["keystorelocation"].toString())
            storePassword = project.properties["keystorepassword"].toString()
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {

    application {
        mainClass = "com.zakgof.korender.MainKt"

        jvmArgs("--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")

        nativeApplication {
        }

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "com.zakgof.korender"
            packageVersion = korenderVersion
            modules("jdk.unsupported")
            windows {
                iconFile.set(project.file("korender32.ico"))
            }
            linux {
                iconFile.set(project.file("korender32.png"))
            }
        }
    }
}
