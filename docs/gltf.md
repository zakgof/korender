# GLTF

Korender supports loading and rendering GLTF/GLB files, including meshes, animations, and textured materials.
To use one, simply call `Gltf` within a Frame context:

````kotlin
Frame {
    Gltf(resource = "model.glb")
````

It's possibly to select an animation index from the GLTF file (useful when a GLTF embeds multiple animations) and override time used for animating:

````kotlin
Frame {
    Gltf(resource = "model.glb", time = ..., animation = ...)
````

Additionally, instancing is available when a number of model instances needs to be rendered:

````kotlin
Frame {
    Gltf(
        resource = "model.glb",
        instancing = gltfInstancing("crowd", 10, true) {
            repeat(10) { i ->
                Instance(
                    transform = ... // i-th instance dynamic position
                )
            }
        }
    )

````


