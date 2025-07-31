## Meshes

There are predefined helper functions for common shapes:

- `quad(1.0f, 2.0f)` - quad in *xy* plane with half-side 1.0 along *x* and 2.0 along *y* axis
- `cube(0.5f)` - cube with half-side 0.5
- `sphere(1.0f)` - sphere with radius 1.0
- `disk(1.0f)` - disk with radius 1.0
- `coneTop(1.0f, 2.0f)` - conical surface with height 1.0 and radius 1.0
- `cylinderSide(1.0f, 2.0f)` - cylindrical surface with height 1.0 and radius 1.0
- `obj("models/file.obj")` - loads a mesh from a Wavefront .obj file `models/file.obj`

It's also possible to generate custom meshes via the `customMesh` helper function:

````kotlin
customMesh("road", 4, 6) {
    pos(-0.5f, 0f, 0f).normal(1.y).tex(0f, 0f)
    pos(-0.5f, 0f, 32f).normal(1.y).tex(0f, 32f)
    pos(0.5f, 0f, 32f).normal(1.y).tex(1f, 32f)
    pos(0.5f, 0f, 0f).normal(1.y).tex(1f, 0f)
    index(0, 1, 2, 0, 2, 3)
}
````

Note that if `dynamic` attribute is set to `true`, the initializer block of the mesh if called every frame and mesh data is updated in GPU dynamically.

