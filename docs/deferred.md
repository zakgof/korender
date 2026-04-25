# Deferred rendering pipeline ![experimental](https://img.shields.io/badge/experimental-BA4040)

Korender support deferred rendering pipeline as an experimental feature.

Deferred rendering is a technique where geometry and material data are rendered first into multiple textures called the G-buffer.
Lighting is then calculated in a separate pass using this stored data, allowing efficient handling of many dynamic lights.
This approach enhances performance in complex scenes and enables various screen-space effects.

To switch to deferred rendering, add the following declaration to the `Frame` context:

````kotlin
    Frame { 
        DeferredShading()
````

Deferred rendering pipeline supports the following effects:

## Shading material modifiers

Additional material modifiers applied during the shading pass

#### `ibl`

Image-based lighting - environment mapping. In forward rendering pipeline IBL can be applied per-object, in deferred shading it's applied globally in screen space via shading material modifier:

````kotlin
    Frame {
        val cubeMap = ...
        DeferredShading {
            Shading(ibl(cubeMap))
        }
````

## Post-shading effects

Screen-space effects applied to the frame after the shading pass

####  `ssr`

Screen-space reflections - applied according to the objects' surface properties (metallic/roughness)

````kotlin
    Frame {
        val cubeMap = ...
        DeferredShading {
            Ssr(
                downsample = 2,
                envTexture = cubeMap
            )
        }
````

####  `bloom`

Screen-space bloom (glow) effect

````kotlin
    Frame {
        val cubeMap = ...
        DeferredShading {
            Bloom(
                amount = 3.0f
            )
        }
````

## Screen-space ambient occlusion

Opt-in ambient occlusion pass for deferred rendering.

````kotlin
    Frame {
        DeferredShading {
            Ssao(blurRadius = 10f)
        }
    }
````

## Decals

````kotlin
    Frame {
        val cubeMap = ...
        DeferredShading {
            Decal(
                base(
                    colorTexture = texture("texture/decal.png"),
                    metallicFactor = 0.2f
                ),
                position = pos, look = look, up = up, size = 1.6f
            )
        }
````
