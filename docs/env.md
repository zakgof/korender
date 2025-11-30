# Environment mapping

Korender supports environment mapping (via cube maps).
When enabled, rendered objects incorporate distant lighting from the environment map, following Korender’s physically-based rendering (PBR) model for realistic illumination.

## Cube map creation
There a several ways to create a cube map:

- from 6 textures images:

````kotlin
Frame {
    val cubeMap = cubeTexture("env", mapOf(
        NX to "textures/env-nx.jpg",
        NY to "textures/env-ny.jpg",
        NZ to "textures/env-nz.jpg",
        PX to "textures/env-px.jpg",
        PY to "textures/env-py.jpg",
        PZ to "textures/env-pz.jpg"
    ))
````

- capture env probe from a scene:

````kotlin
Frame {
    CaptureEnv("probe1", 1024) {
        // Render capture scene here
    }
    val cubeMap = cubeTextureProbe("probe1")
````

## Environment rendering

To render an object with environment mapping, add the `ibl` material modifier to a `Renderable` declaration:

````kotlin
Frame {
    val cubeMap = ...
    Renderable(
        base(...),
        ibl(cubeMap),
````

It's also possible to use a sky material modifier as the ibl source:

````kotlin
Frame {
    Renderable(
        base(...),
        ibl(fastCloudSky()),
````


