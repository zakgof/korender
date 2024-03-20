import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    id("maven-publish")
    id("signing")
}

val libraryVersion = "0.0.2-SNAPSHOT"
val libraryGroup = "com.github.zakgof"

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
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.obj)
        }
        desktopMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(compose.desktop.currentOs)

            implementation(libs.lwjgl)
            implementation(libs.lwjgl.opengl)
            implementation(libs.lwjgl.jawt)
            implementation(libs.lwjgl3.awt)

            runtimeOnly("org.lwjgl:lwjgl:3.3.3") {
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

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply { load(it) }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar)
        groupId = libraryGroup
        version = libraryVersion
        pom {
            name.set("korender")
            description.set("Kotlin Multiplatform 3D rendering framework")
            url.set("https://github.com/zakgof/korender")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("zakgof")
                    name.set("Oleksandr Zakusylo")
                    email.set("zakgof@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/zakgof/korender")
                connection.set("scm:git:https://github.com/zakgof/korender.git")
                developerConnection.set("scm:git:https://github.com/zakgof/korender.git")
            }
        }
    }
}

signing {
    if (getExtraString("signing.keyId") != null) {
        sign(publishing.publications)
    }
}

//https://github.com/gradle/gradle/issues/26132
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(signingTasks)
}
