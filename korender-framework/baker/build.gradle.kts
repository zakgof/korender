plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.zakgof.korender.baker.resources"
    generateResClass = auto
}

kotlin {

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(project(":korender"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.commons.math3)
            implementation("com.github.haifengl:smile-core:5.0.0")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.zakgof.korender.baker.MainKt"
        jvmArgs("--add-exports", "java.desktop/sun.awt=ALL-UNNAMED", "-DprojectRoot=${project.projectDir.absolutePath}")
        nativeApplication {
        }
    }
}
