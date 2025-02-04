# ![Korender](doc/korender32.png) korender
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.zakgof/korender/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.zakgof/korender)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-278ec7.svg?logo=kotlin)](http://kotlinlang.org)

![wasm](https://img.shields.io/badge/wasm-624FE8)
![android](https://img.shields.io/badge/android-136F63)
![windows](https://img.shields.io/badge/windows-3F88C5)
![linux](https://img.shields.io/badge/linux-FFBA08)

Kotlin Multiplatform 3D graphics rendering engine based on OpenGL / OpenGL ES / WebGL.

Korender uses declarative approach that seamlessly integrates 3D viewport into Compose Multiplatform UI. 
Same Korender code runs on all supported platforms.

````kotlin
@Composable
fun App() = Korender(appResourceLoader = { Res.readBytes(it) }) {
  camera = camera(Vec3(0f, 5f, 30f), -1.z, 1.y)
  Frame {
    Pass {
      DirectionalLight(Vec3(1f, -1f, -1f))
      Sky(fastCloudSky())
      Renderable(
        standart {
          baseColor = Green
          pbr.metallic = 0.3f
          pbr.roughness = 0.5f
        },
        mesh = sphere(2f),
        transform = translate(-0.5f.y)
      )
    }
    Pass {
      Screen(water(), fastCloudSky())
      Billboard(fire { yscale = 10f; xscale = 2f }, position = 6.y, transparent = true)
      Gui {
        Filler()
        Text(text = "FPS ${frameInfo.avgFps}", height = 50, color = Red, fontResource = "font/orbitron.ttf", id = "fps")
      }
    }
  }
}
````
![Korender](doc/quickstart.jpg)

Live web demo: https://zakgof.github.io/projects/korender/wasm/

Korender is BETA - APIs may change without notice.

### Supported platforms
| Platform                                            | 3D API      |
|-----------------------------------------------------|-------------|
| Desktop (Windows/Linux)                             | OpenGL 3.3  |
| Android                                             | OpenGL ES 3 |
| Web ![new](https://img.shields.io/badge/new-FF4040) | WebGL 2     |
|                                                     |             |

### Examples showcase app

- Web live demo https://zakgof.github.io/projects/korender/wasm/
- JVM Desktop Windows: https://github.com/zakgof/korender/releases/download/0.3.0/korender-demo-0.3.0.zip
- Android APK: https://github.com/zakgof/korender/releases/download/0.3.0/korender-demo-0.3.0.apk

[Examples source code](https://github.com/zakgof/korender/tree/main/korender-framework/examples/src/commonMain/kotlin)

### Features
- Physically Based Rendering (PBR) metallic-roughness model ![new](https://img.shields.io/badge/new-FF4040)
- Rendering opaque and transparent objects
- Directional and point lights
- Projection shadow maps with percentage close soft shadows
- Texturing with normal mapping support
- Predefined and custom meshes
- Wavefront .obj and .gtlf/.glb file loading ![new](https://img.shields.io/badge/new-FF4040)
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
- Add Korender dependency `com.github.zakgof:korender:0.3.0`
- Add the above code to commonMain
- Run on desktop: `.\gradlew composeApp:run`


### Further reading
Explore the [Korender Wiki](https://github.com/zakgof/korender/wiki)