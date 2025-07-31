## Renderables basics

To render objects, add their `Renderable` declarations into the `Frame` context

```kotlin
Frame {
    Renderable(
        base(color = ColorRGBA.Red, metallicFactor = 0f, roughnessFactor = 0.2f),
        mesh = sphere(),
        transform = translate(-2f, -1f, -5f),
        transparent = false
    )
```

A `Renderable` declaration has the following parameters:

- `materialModifiers` - define the surface material properties of a renderable    
- `mesh` - defines the geometry of the object. Internally, a mesh is an indexed list of vertices forming triangles     
- `transform` - model space transformation of the object, such as scaling, rotation, or positioning    
- `transparent` - flag specifying if the object has any transparency