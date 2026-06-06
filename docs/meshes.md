## Meshes

There are predefined helper functions for common shapes:

- `quad(halfSideX, halfSideY)` - z-axis facing quad with given half-dimensions
- `biQuad(halfSideX, halfSideY)` - two-sided quad visible from both sides
- `cube(halfSide)` - cube with given half-edge
- `sphere(radius, slices, sectors)` - sphere with given radius and tessellation
- `cylinderSide(height, radius, sectors)` - cylindrical surface without bases
- `coneTop(height, radius, sectors)` - conical surface without base
- `disk(radius, sectors)` - flat disk in xz plane
- `obj("models/file.obj")` - loads a mesh from a Wavefront .obj file
- `pipeMesh(id, segments, dynamic, block)` - shape consisting of multiple connected cylinders (pipes)

It's also possible to generate custom meshes via the `customMesh` helper function:

```kotlin
customMesh("road", 4, 6) {
    pos(Vec3(-0.5f, 0f, 0f), Vec3(-0.5f, 0f, 32f), Vec3(0.5f, 0f, 32f), Vec3(0.5f, 0f, 0f))
    tex(Vec2(0f, 0f), Vec2(0f, 32f), Vec2(1f, 32f), Vec2(1f, 0f))
    index(0, 1, 2, 0, 2, 3)
}
```

Additional `MeshInitializer` methods for advanced geometry:

- `normal(vararg Vec3)` - vertex normals for lighting
- `attr(attribute, vararg values)` - custom per-vertex attributes
- `attrBytes(attribute, rawBytes)` - custom attributes from raw byte data
- `attrSet(attribute, index, value)` - set a single vertex attribute
- `indexBytes(rawBytes)` - indices from raw byte data
- `embed(prototype, transform, colorTexIndex)` - embed another mesh with optional transform

Other mesh declaration methods:

- `mesh(id, mesh)` - create a mesh declaration from an existing `Mesh` object
- `compositeMesh(id, prototypes, attributes, instancingParameters, dynamic, block)` - combine multiple prototype meshes into a single instanced mesh
- `heightField(id, cellsX, cellsZ, cellWidth, height)` - heightfield mesh from a height function
- `loadMesh(meshDeclaration)` - eagerly load a mesh from a declaration (returns `Deferred<Mesh>`)

Note that if `dynamic` is set to `true` for `customMesh` or `pipeMesh`, the mesh data can be updated each frame.
