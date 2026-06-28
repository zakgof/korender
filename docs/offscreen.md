# Offscreen rendering and probes

Instead of rendering to screen, Korender allows rendering a scene to an image/texture or to a cube map.

## Render frame to image (CPU readback)

Call `captureFrame` on the Korender scope — it returns `Deferred<Image>` with the captured pixels:

```kotlin
Korender(resourceLoader = { ... }) {
    val deferredImage = captureFrame(1024, 1024) {
        camera = camera(Vec3(...), ...)
        projection = projection(...)
        Renderable(...)
    }
    ...
    Frame {
        if (deferredImage.isCompleted) {
            val capturedImage = deferredImage.getCompleted()
            // Use the captured image
        }
    }
}
```

`captureFrame(width: Int, height: Int, block: FrameScope.() -> Unit): Deferred<Image>`

## Render environment to cube images (CPU readback)

`captureEnv` samples the environment at the given world-space position and outputs six images aligned with standard cubemap face orientations:

```kotlin
Korender(resourceLoader = { ... }) {
    val deferredCubeImages = captureEnv(1024) {
        Renderable(...)
    }
    ...
    Frame {
        if (deferredCubeImages.isCompleted) {
            val cubeImages = deferredCubeImages.getCompleted()
            val negativeXSide = cubeImages[CubeTextureSide.NX]
        }
    }
}
```

`captureEnv(resolution: Int, block: FrameScope.() -> Unit): Deferred<CubeTextureImages>`

## Render frame to texture (GPU-only)

The functions above retrieve image data from the GPU to CPU. If you intend to reuse the rendered scene in subsequent rendering passes, it's more efficient to keep the data on the GPU — in a texture or cube texture — rather than reading it back.

Use `CaptureFrame` inside a Frame context to render directly into a GPU texture:

```kotlin
Frame {
    CaptureFrame("capturedFrame", 256, 256) {
        camera = camera(...)
        projection = projection(...)
        Renderable(...)
    }
}
```

The scene is rendered into a probe named "capturedFrame". This texture stays on the GPU and can be used in subsequent rendering:

```kotlin
Renderable(
    material = base { colorTexture = textureProbe("capturedFrame") },
    mesh = sphere()
)
```

### Render environment to cube texture

Similarly, environment can be captured without readback to CPU:

```kotlin
Frame {
    CaptureEnv("capturedEnv", 256) {
        Renderable(...)
    }
}
```

The cube texture is stored in a cube probe named "capturedEnv" and stays on the GPU:

```kotlin
Sky(cubeTextureProbe("capturedEnv"))
```

Or as environment map for PBR reflections:

```kotlin
base {
    env = cubeSky(cubeTextureProbe("capturedEnv"))
}
```
