# Billboards

A _billboard_ or _impostor_ is a flat 2D object (usually a textured quad) that always faces the camera, no matter where the camera moves. It's a common trick used to represent complex visuals with simple geometry.

To render a billboard, add the `Billboard` functional declaration to your `Frame` context. Additionally, apply the `billboard` material to fine-tune:

````kotlin
Billboard (
    base(colorTexture = texture("textures/sprite.png")),
    billboard(
        position = Vec3(3f, 5f, 7f),
        scale = Vec2(2f, 2f),
        rotation = 0.3f
    ),
    transparent = true
)
````

`billboard` materials supports the following parameters:

| Uniform name | Type  | Default value | Description           |
|--------------|-------|---------------|-----------------------|
| position     | Vec3  | (0, 0, 0)     | World position of billboard's center     |
| scale        | Vec2  | (1, 1)        | Billboard's size (horizontal/vertical)   |
| rotation     | Float | 0.0f          | Rotation (in radians) |
