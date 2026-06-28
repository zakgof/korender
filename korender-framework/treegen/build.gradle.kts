plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.zakgof.korender.treegen.resources"
    generateResClass = auto
}

kotlin {

    jvm("desktop")

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(project(":korender"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.commons.math3)
            implementation(libs.kotlinxcollections.immutable)
            implementation(libs.kotlinx.serialization.cbor)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        jvmArgs += listOf(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        )
        mainClass = "com.zakgof.korender.baker.MainKt"
        nativeDistributions {
            jvmArgs += listOf(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
            )
            windows {
                includeAllModules = true
            }
        }
        buildTypes.release.proguard {
            isEnabled = false
        }
    }
}
