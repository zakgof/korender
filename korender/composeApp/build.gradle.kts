import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.obj)
        }
        desktopMain.dependencies {
            runtimeOnly(libs.kotlin.reflect)
            implementation(compose.desktop.currentOs)

            implementation(libs.lwjgl)
            implementation(libs.lwjgl.glfw)
            implementation(libs.lwjgl.opengl)
            implementation(libs.lwjgl.jawt)
            implementation(libs.lwjgl3.awt)

            runtimeOnly("org.lwjgl:lwjgl:3.3.3") {
                artifact {
                    classifier = "natives-windows"
                }
            }
            runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.3") {
                artifact {
                    classifier = "natives-windows"
                }
            }
            runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.3") {
                artifact {
                    classifier = "natives-windows"
                }
            }
        }
    }
}

android {
    namespace = "com.zakgof.korender"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.zakgof.korender"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "com.zakgof.korender.MainKt"

        jvmArgs("--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "com.zakgof.korender"
            packageVersion = "0.0.1"
        }
    }
}
