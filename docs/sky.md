# Sky rendering

Korender supports several ways to render sky:

## Cubemap sky

Provide 6 textures to construct a skybox cube map:
````kotlin
Frame {
    Sky(cubeSky(cubeTexture("sky", mapOf(
        NX to "textures/skybox-nx.jpg",
        NY to "textures/skybox-ny.jpg",
        NZ to "textures/skybox-nz.jpg",
        PX to "textures/skybox-px.jpg",
        PY to "textures/skybox-py.jpg",
        PZ to "textures/skybox-pz.jpg"
    ))))
````

## Single-texture sky

Supply a single texture that will be projected onto the sky dome, accounting for projection distortion
````kotlin
Frame {
    Sky(textureSky("textures/sky.jpg"))
````

## Procedural sky

Choose from several predefined parameterized procedural sky presets:

````kotlin
Frame {
    Sky(fastCloudSky(thickness = 12.0f, rippleAmount = 0.4f))

````

### Presets:

#### `fastCloudSky`

Procedural day sky with clouds, supported parameters:

| Uniform name | Type  | Default value   | Description                             |
|--------------|-------|-----------------|-----------------------------------------|
| density      | Float | 3.0f            | Cloud density, 1-5                      |
| thickness    | Float | 10.0f           | Cloud thickness, 0-20                   |
| scale        | Float | 1.0f            | Cloud scale, 0.1-10                     |
| rippleAmount | Float | 0.3f            | Cloud high-frequency ripple amount, 0-1 |
| rippleScale  | Float | 4.0f            | Cloud ripple frequency multiplier, 1-10 |
| zenithColor  | Color | Color(0x3F6FC3) | Sky color at zenith                     |
| horizonColor | Color | Color(0xB8CAE9) | Sky color at horizon                    |
| cloudLight   | Float | 1.0f            | Max lightness of clouds                 |
| cloudDark    | Float | 0.7f            | Min lightness of clouds                 |


#### `starrySky`

Procedural night sky with colorful stars:

| Uniform name | Type  | Default value               | Description                             |
|--------------|-------|-----------------------------|-----------------------------------------|
| colorness    | Float | 0.8f                        | Star color intensity                    |
| density      | Float | 20.0f                       | Star density (amount)                   |
| speed        | Float | 1.0f                        | Star motion speed factor                |
| size         | Float | 15.0f                       | Star radius factor                      |
