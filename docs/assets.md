# Asset management

Korender uses [Kotlin Multiplatform resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-usage.html) for storing assets, such as 3d model files and textures.

To let Korender access your assets, provide `resourceLoader` as a `Korender` composable function's argument:

```kotlin
@Composable
fun App() = Korender(resourceLoader = { Res.readBytes(it) }) {
    // ...
    Frame {
        Renderable(
            material = base { colorTexture = texture("texture/asphalt.jpg") },
            mesh = quad()
        )
    }
}
```

In the above example, the texture asset is located in the file `commonMain/composeResources/files/texture/asphalt.jpg`. The `resourceLoader` is a `suspend (String) -> ByteArray` — you can also provide a custom implementation to load from network, local filesystem, etc.

You can also override the `resourceLoader` per node in the scene hierarchy using `Node`:

```kotlin
Node(resourceLoader = { myCustomLoader(it) }) {
    // resources used here are loaded via myCustomLoader
}
```

Korender also includes some bundled resources. To access them, start the path with an exclamation mark: `!font/anta.ttf`

# Asset types

Korender supports the following resource types:

**Textures:**

- `texture(resource)` — 2D texture from an image file (png, jpg)
- `texture(id, image)` — 2D texture from an `Image` object
- `textureArray(vararg resources)` — texture array from multiple image files
- `textureArray(id, images)` — texture array from `Image` objects
- `texture3D(id, image)` — 3D volumetric texture from an `Image3D`
- `cubeTexture(resources)` — cube map from 6 image files (one per `CubeTextureSide`)
- `cubeTexture(id, images)` — cube map from `Image` objects
- `textureProbe(name)` — texture from a frame probe (created with `CaptureFrame`)
- `cubeTextureProbe(name)` — cube texture from an environment probe (created with `CaptureEnv`)

Texture declarations can be configured with `TextureFilter` (Nearest, Linear, MipMap), `TextureWrap` (MirroredRepeat, ClampToEdge, Repeat), and anisotropy.

**Images:**

- `loadImage(resource)` — load an image file as an `Image` object (deferred)
- `loadImage(bytes, type)` — load an image from raw bytes
- `createImage(width, height, format)` — create an empty `Image` with the given `PixelFormat`
- `createImage3D(width, height, depth, format)` — create an empty 3D image

**Meshes:**

- `quad()`, `biQuad()`, `cube()`, `sphere()`, `cylinderSide()`, `coneTop()`, `disk()` — procedural shapes
- `obj(file)` — load a Wavefront .obj mesh
- `mesh(id, mesh)` — create declaration from a `Mesh` object
- `customMesh(id, vertexCount, indexCount, ...)` — custom geometry from vertex/index lists
- `pipeMesh(id, segments)` — shape of connected cylinder segments
- `compositeMesh(id, ...)` — combined prototype meshes with instancing
- `heightField(id, ...)` — heightfield mesh from a height function
- `loadMesh(declaration)` — eagerly load a mesh (returns `Deferred<Mesh>`)

**Models (GLTF/GLB/OBJ/KR):**

- `Model(resource, ...)` — load and render a model file. Format detected by extension. Supports animations, instancing, and material modifiers.

**Generic resource loading:**

- `load(resource, mapper)` — load any resource as `ByteArray` and map to a custom object (deferred)

**Loading screen:**

- `OnLoading(force, block)` — display a lightweight scene while resources load. Use only procedural meshes and simple colors to avoid loading dependencies:

```kotlin
Frame {
    OnLoading(force = true) {
        Renderable(
            material = base { color = ColorRGBA.Red },
            mesh = cube()
        )
    }
    // Resources here are loaded while the loading screen displays
    Model(resource = "heavy_scene.glb")
}
```

`force = true` shows the loading screen even if no resources are pending (useful for initial blank frames).

**Environment/frame probes:**
- `CaptureEnv(name, resolution, block)` — capture scene into 6 cube map images
- `CaptureFrame(name, width, height, block)` — capture scene into a frame image

# Asset loading and unloading

Korender loads an asset as soon as it's declared in a rendering frame. Asset unloading is managed by retention policy. The default policy is to automatically unload a resource after 10 seconds of not being used.

Retention policy can be overridden by setting the `retentionPolicy` var in the Korender context:

```kotlin
Korender(resourceLoader = { Res.readBytes(it) }) {
    retentionPolicy = keepForever()
    // ...
    Frame {
        Renderable(material = base { color = ColorRGBA.Red }, mesh = sphere())
    }
}
```

You can also set `retentionGeneration` to track generational policy:

```kotlin
retentionGeneration = 1 // current frame's resources get generation 1
```

##### Available retention policies:
  - `immediatelyFree()` — unload a resource immediately after it's not used in a frame.
  - `keepForever()` — do not unload a resource, keep it forever.
  - `time(seconds: Float)` — unload a resource after specified number of seconds of not being used.
  - `untilGeneration(generation: Int)` — use generation-based retention: unload all unused resources whose generation is less than the specified value; asset generation is specified by `retentionGeneration` at the moment of asset loading.
