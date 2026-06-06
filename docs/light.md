# Lights

Korender supports 3 types of light sources:

- ambient light
- directional lights (with optional shadow cascades)
- point lights

All types of lights can be declared dynamically in the Frame context:

```kotlin
Frame {
    AmbientLight(ColorRGB.white(0.25f))
    PointLight(Vec3(100f, 20f, 100f), ColorRGB.Green)
    DirectionalLight(Vec3(1f, -1f, 2f), ColorRGB.white(1.0f))
}
```

`AmbientLight(color: ColorRGB)` — fills all surfaces with a uniform base color.

`DirectionalLight(direction: Vec3, color: ColorRGB, block: ShadowScope.() -> Unit)` — light from a distant source with parallel rays. The `block` parameter accepts optional shadow cascade declarations.

`PointLight(position: Vec3, color: ColorRGB, attenuationLinear: Float, attenuationQuadratic: Float)` — light radiating from a point with distance-based attenuation. Default attenuation is `linear = 0.1f`, `quadratic = 0.01f`.

# Shadows

Shadows are supported for directional lights. Korender supports multiple cascades of shadow maps with automatic blending of maps within overlapping cascades. The supported mapping techniques are:

- `hard()` — straightforward shadow mapping with no shadow edge anti-aliasing.
- `softwarePcf(samples, blurRadius, bias)` — soft shadows with blurring filter using Vogel disk sampling
- `hardwarePcf(samples, blurRadius, bias)` — percentage-close filtering on GPU provided by OpenGL implementation
- `vsm(blurRadius)` — variance shadow mapping

```kotlin
Frame {
    DirectionalLight(Vec3(1f, -1f, 0.3f), ColorRGB.white(5.0f)) {
        Cascade(mapSize = 1024, near = 4f,  far =  12f, algorithm = softwarePcf(samples = 16, blurRadius = 0.01f))
        Cascade(mapSize = 1024, near = 10f, far =  30f, algorithm = vsm(blurRadius = 0.01f))
        Cascade(mapSize = 1024, near = 25f, far = 100f, algorithm = vsm(blurRadius = 0.02f))
    }
}
```

`Cascade(mapSize, near, far, fixedYRange, algorithm)` — defines a shadow cascade. `fixedYRange` is an optional pair for occluder Y-range optimization.
