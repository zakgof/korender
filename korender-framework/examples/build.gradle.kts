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

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    sourceSets {
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
            implementation("androidx.camera:camera-core:1.6.1")
            implementation("androidx.camera:camera-lifecycle:1.6.1")
            implementation("androidx.camera:camera-camera2:1.6.1")
            implementation("dev.icerock.moko:permissions-camera:0.20.1")
            implementation("dev.icerock.moko:permissions-compose:0.20.1")
            implementation("androidx.activity:activity-compose:1.10.1")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
            implementation("androidx.compose.ui:ui")
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
