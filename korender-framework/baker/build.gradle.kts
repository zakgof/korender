plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.zakgof.korender.baker.resources"
    generateResClass = auto
}

kotlin {

    jvm("desktop")

    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters")
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(project(":korender"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.commons.math3)
            implementation("com.github.haifengl:smile-core:4.4.0")
            implementation(libs.kotlinxcollections.immutable)
            implementation(libs.kotlinx.serialization.cbor)
            implementation(compose.materialIconsExtended)
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
            "--add-exports=java.base/sun.misc=ALL-UNNAMED",
            "--add-exports=java.desktop/sun.awt=ALL-UNNAMED"
        )
        mainClass = "com.zakgof.korender.baker.MainKt"
        nativeApplication {
        }
        nativeDistributions {
            jvmArgs += listOf(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-exports=java.base/sun.misc=ALL-UNNAMED",
                "--add-exports=java.desktop/sun.awt=ALL-UNNAMED"
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
