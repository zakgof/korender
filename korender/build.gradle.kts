plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.zakgof"
version = "0.0.1-SNAPSHOT"


repositories {
    mavenCentral()
}

sourceSets {
    val examples by creating {
        kotlin {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }
}

val examplesImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val examplesRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

dependencies {

    val lwjglVersion = "3.3.3"
    val lwjglNatives = "natives-windows"

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    // implementation("org.lwjgl", "lwjgl-assimp")
    implementation("org.lwjgl", "lwjgl-glfw")
    // implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    // implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    // runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    // runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    // runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)

    implementation("de.javagl:obj:0.4.0")

}

kotlin {
    jvmToolchain(19)
}