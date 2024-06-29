# ![Korender](doc/korender32.png) korender
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.zakgof/korender/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.zakgof/korender)

Kotlin Multiplatform 3D graphics rendering engine based on OpenGL / OpenGL ES.

Korender uses declarative approach that seamlessly integrates into Compose Multiplatform UI. 
Same Korender code runs on all supported platforms.

### Supported platforms
- Desktop (Windows/Linux) - based on LWJGL
- Android - based on OpenGL ES API

### Features
- Rendering opaque and transparent objects
- Diffuse, specular and ambient lighting (directional light)
- Projection shadow maps with percentage close soft shadows
- Texturing:
  - uv mapped
  - triplanar
  - detail
- Bump mapping
- Predefined and custom meshes
- Wavefront .obj model file loading
- Billboards (sprites)
- Batching (instancing)
- Simple heightfield (terrain)
- Textured or shader sky
- On-screen basic GUI
- Custom shaders support
- Multi-pass rendering and screen-space shaders (filters)
- Simple effects
  - smoke
  - fire
  - water

### Quick start

- Check out QuickStart application available at https://github.com/zakgof/korender/tree/main/quickstart

or, create an application from scratch:

- Generate a new KMP application using [Kotlin Multiplatform Wizard](https://kmp.jetbrains.com/). Select Android and Desktop platforms.
- Add Korender dependency `com.github.zakgof:korender:0.1.0`
- Add the following code to commonMain:
 
````kotlin
@Composable
fun App() = Korender {
    Frame {
        Renderable(
            options(StandardMaterialOption.Color),
            standardUniforms {
                color = Color(1.0f, 0.2f, 1.0f, 0.5f + 0.5f * sin(frameInfo.time))
            },
            mesh = sphere(2.0f),
            transform = Transform().translate(sin(frameInfo.time).y)
        )
    }
}
````
 - Run on desktop: `.\gradlew composeApp:run`
 - Result:

![Korender](doc/quickstart.jpg)

### Examples showcase app

- JVM Desktop Windows: https://github.com/zakgof/korender/releases/download/0.1.0/korender-demo-0.1.0.zip
- Android APK: https://github.com/zakgof/korender/releases/download/0.1.0/korender-demo-0.1.0.apk

[Examples source code](https://github.com/zakgof/korender/tree/main/korender-framework/examples/src/commonMain/kotlin)

### Further reading
Explore the [Korender Wiki](https://github.com/zakgof/korender/wiki)