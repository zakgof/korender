# Instancing

To improve rendering performance, Korender supports `Renderable` and `Billboard` instancing (batching) - a technique when multiple objects are rendered within one call from a single GPU buffer.

### Instanced Renderables

To render multiple Renderables in a batch, add an `instancing` parameter when declaring a `Renderable` in a `Frame` context. Set the required number of `Instance`s with appropriate transforms:

````kotlin
Frame { 
    Renderable(
        base(color = Red),
        mesh = cube(0.3f),
        instancing = instancing(
              id = "cubes",
              count = 3,
              dynamic = true
        ) {
             Instance.translate(1.x)
             Instance.translate(2.x)
             Instance.translate(3.x)
        }
    )
}
````
Assure that the number of instances does not exceed the declared `count` used to initialize the buffers. If the instances never change, set the `dynamic` flag to `false` - this will improve the performance by reading the instances and passing them to the GPU only once. `instancing` requires a unique `id` for mesh identification.

### Instanced Billboards

Similarly, billboards can be instanced too:

````kotlin
Billboard(
    base(colorTexture = texture("texture/splat.png")), 
    transparent = true, 
    instancing = billboardInstancing(
        id = "particles",
        count = 3,
        dynamic = false
    ) {
        Instance(pos = 1.x, scale = Vec2(2f, 2f))
        Instance(pos = 2.x, scale = Vec2(3f, 2f))
        Instance(pos = 3.x, scale = Vec2(2f, 3f))
    }
)
````
For each `Instance`, define `pos`, `scale`, and `rotation` of the billboard. The `transparent` flag should be enabled if the billboards have transparency to sort the instances back to front.