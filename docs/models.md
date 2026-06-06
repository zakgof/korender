# Model rendering

Korender supports loading and rendering 3D model files in GLTF (`.gltf`, `.glb`), Wavefront OBJ (`.obj`), and the native Korender scene format (`.kr`). Format is detected by file extension.

To use one, call `Model` within a Frame context:

```kotlin
Frame {
    Model(resource = "model.glb")
}
```

It's possible to select an animation index from the GLTF file (useful when a GLTF embeds multiple animations) and override time used for animating:

```kotlin
Frame {
    Model(resource = "model.glb", time = ..., animation = ...)
}
```

Model parameters:

| Parameter        | Type | Description |
|------------------|------|-------------|
| `resource`       | String | Model file path |
| `transform`      | Transform | Model space transform (default: IDENTITY) |
| `instancing`     | ModelInstancingDeclaration? | Batch instancing declaration |
| `animation`      | Int? | Index of animation to play |
| `onUpdate`       | ((ModelInfo) -> Unit)? | Callback with runtime model data (nodes, animations, cameras) |
| `materialModifier` | BaseMaterialScope.() -> Unit | Material property overrides |

Additionally, instancing is available when multiple model instances need to be rendered:

```kotlin
Frame {
    Model(
        resource = "model.glb",
        instancing = modelInstancing("crowd", 10, true) {
            repeat(10) { i ->
                Instance(
                    transform = ... // i-th instance dynamic position
                )
            }
        }
    )
}
```
