# ![Korender](doc/korender32.png) korender
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.zakgof/korender/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.zakgof/korender)

Kotlin Multiplatform 3D graphics rendering engine based on OpenGL / OpenGL ES.

### Quick start

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

Quickstart application code is also available at https://github.com/zakgof/korender/quickstart

![Korender](doc/quickstart.jpg)

### Supported platforms
- Desktop (Windows/Linux) - based on LWJGL 
- Android - based on OpenGL ES

### Features
- Rendering opaque and transparent objects
- Diffuse, specular and ambient lighting (directional light)
- Projection shadow maps with percentage close soft shadows
- Texturing:
  - uv mapped
  - triplanar
  - detail
- Bump mapping
- Custom meshes
- Wavefront .obj model file loading
- Billboards (sprites)
- Batching (instancing)
- Simple heightfield (terrain)
- Textured or shader sky
- On-screen basic GUI
- Custom shaders support
- Screen-space shaders (filters)
- Simple effects
  - smoke
  - fire
  - water

### Demo app

- JVM Desktop Windows: https://github.com/zakgof/korender/releases/download/0.0.3/korender-demo-0.0.3.apk
- Android APK: https://github.com/zakgof/korender/releases/download/0.0.3/korender-demo-0.0.3.apk

### Examples
Find [more advanced examples](https://github.com/zakgof/korender/tree/main/korender-framework/examples/src/commonMain/kotlin)

### Wiki

### Concepts
- **KorenderContext** - your entry point to access engine features
- **Renderable** - a primitive that can be rendered. A renderable's has a *Mesh* (geometry) and a *Material* (surface properties)
- **Mesh** - is generally an indexed collection of vertices that form triangles. Each vertex has a set of *Attributes*, such as position, normal, texture coordinates, etc.
- **Material** - defines a renderable's surface look; consist of a *Shader* and its *Uniforms* (properties)
- **Shader** - compiled GPU program, consisting of vertex ang fragment shader code
- **Uniforms** - shader's parameters