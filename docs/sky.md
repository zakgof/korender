# Sky rendering

Korender supports several ways to render sky. Declare them inside a `Frame` using the `Sky` function:

```kotlin
Sky(material: SkyMaterial)
```

## Cubemap sky

Provide 6 textures to construct a skybox cube map:

```kotlin
Frame {
    Sky(cubeSky(cubeTexture(mapOf(
        CubeTextureSide.NX to "textures/skybox-nx.jpg",
        CubeTextureSide.NY to "textures/skybox-ny.jpg",
        CubeTextureSide.NZ to "textures/skybox-nz.jpg",
        CubeTextureSide.PX to "textures/skybox-px.jpg",
        CubeTextureSide.PY to "textures/skybox-py.jpg",
        CubeTextureSide.PZ to "textures/skybox-pz.jpg"
    ))))
}
```

Alternatively, create a cube texture from `Image` objects via `cubeTexture(id, images)`.

## Single-texture sky

Supply a single texture that will be projected onto the sky dome, accounting for projection distortion:

```kotlin
Frame {
    Sky(textureSky(texture("textures/sky.jpg")))
}
```

## Procedural sky

Choose from several predefined parameterized procedural sky presets:

```kotlin
Frame {
    Sky(fastCloudSky(thickness = 12.0f, rippleAmount = 0.4f))
}
```

### Presets:

#### `fastCloudSky`

Procedural day sky with clouds (returns `SkyMaterial`):

| Parameter    | Type    | Default        | Description                             |
|--------------|---------|----------------|-----------------------------------------|
| density      | Float   | 3.0f           | Cloud density, 1-5                      |
| thickness    | Float   | 10.0f          | Cloud thickness, 0-20                   |
| scale        | Float   | 1.0f           | Cloud scale, 0.1-10                     |
| rippleAmount | Float   | 0.3f           | Cloud high-frequency ripple amount, 0-1 |
| rippleScale  | Float   | 4.0f           | Cloud ripple frequency multiplier, 1-10 |
| zenithColor  | ColorRGB| ColorRGB(0x3F6FC3) | Sky color at zenith                 |
| horizonColor | ColorRGB| ColorRGB(0xB8CAE9) | Sky color at horizon                |
| cloudLight   | Float   | 1.0f           | Max lightness of clouds                 |
| cloudDark    | Float   | 0.7f           | Min lightness of clouds                 |
| block        | MaterialScope.() -> Unit | {} | Optional material customization   |

#### `starrySky`

Procedural night sky with colorful stars (returns `SkyMaterial`):

| Parameter | Type    | Default | Description                  |
|-----------|---------|---------|------------------------------|
| colorness | Float   | 0.8f    | Star color intensity         |
| density   | Float   | 20.0f   | Star density (amount)        |
| speed     | Float   | 1.0f    | Star motion speed factor     |
| size      | Float   | 15.0f   | Star radius factor           |
| block     | MaterialScope.() -> Unit | {} | Optional material customization |
