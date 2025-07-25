# Lights

Korender supports 3 types of light sources:

- ambient light
- directional lights
- point lights

All types of lights can be declared dynamically in the Frame context:

````kotlin
Frame {
    AmbientLight(white(0.25f))
    PointLight(Vec3(100f, 20f, 100f), ColorRGB.Green)
    DirectionalLight(Vec3(1f, -1f, 2f), white(1.0f))
````

# Shadows

Shadows are supported for directional lights. Korender supports multiple cascades of shadow maps with automatic blending of maps within overlapping cascades. The supported mapping techniques are:

- Hard shadows - straightforward shadow mapping with no shadow edge anti-aliasing.
- Software PCF shadows - soft shadows with blurring filter using Vogel disk sampling
- Hardware PCF shadows - percentage-close filtering on GPU provided by OpenGL implementation
- VSM - variance shadow mapping

````kotlin
Frame {
    DirectionalLight(Vec3(1f, -1f, 0.3f), white(5.0f)) { 
        Cascade(mapSize = 1024, near = 4f,  far =  12f, algorithm = softwarePcf(samples = 16, blurRadius = 0.01f))
        Cascade(mapSize = 1024, near = 10f, far =  30f, algorithm = vsm(blurRadius = 0.01f))
        Cascade(mapSize = 1024, near = 25f, far = 100f, algorithm = vsm(blurRadius = 0.02f))
    }
````