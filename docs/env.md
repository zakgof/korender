# Environment mapping

Korender supports environment mapping (via cube maps or sky materials).
When enabled, rendered objects incorporate distant lighting from the environment map, following Korender's physically-based rendering (PBR) model for realistic illumination.

## Cube map creation

There are several ways to create a cube map:

- from 6 texture image files:

```kotlin
Frame {
    val cubeMap = cubeTexture(mapOf(
        CubeTextureSide.NX to "textures/env-nx.jpg",
        CubeTextureSide.NY to "textures/env-ny.jpg",
        CubeTextureSide.NZ to "textures/env-nz.jpg",
        CubeTextureSide.PX to "textures/env-px.jpg",
        CubeTextureSide.PY to "textures/env-py.jpg",
        CubeTextureSide.PZ to "textures/env-pz.jpg"
    ))
}
```

- capture env probe from a scene:

```kotlin
Frame {
    CaptureEnv("probe1", 1024) {
        // Render capture scene here
    }
    val cubeMap = cubeTextureProbe("probe1")
}
```

## Environment rendering

To render an object with environment mapping, set the `env` property in a material block. The `env` property accepts any `SkyMaterial` — use `cubeSky(cubeMap)` to wrap a cube texture:

```kotlin
Frame {
    val cubeMap = ...
    Renderable(
        material = base {
            color = ColorRGBA.White
            env = cubeSky(cubeMap)
        },
        mesh = sphere()
    )
}
```

It's also possible to use a procedural sky material as the env source:

```kotlin
Frame {
    Renderable(
        material = base {
            color = ColorRGBA.White
            env = fastCloudSky()
        },
        mesh = sphere()
    )
}
```
