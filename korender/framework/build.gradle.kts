import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    id("maven-publish")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
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
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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
