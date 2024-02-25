# korender
Simple 3D graphics rendering engine in Kotlin

### Features
- Rendering opaque and transparent objects
- Diffuse, specular and ambient lighting (direction light)
- Projection shadow maps with percentage close soft shadows
- Texturing:
  - uv mapped
  - triplanar
  - aperiodic
  - detail
- Bump mapping
- Custom meshes
- Wavefront .obj model file loading
- Billboards (sprites)
- Batching (instancing)
- Simple heightfield (terrain)
- Textured or shader sky
- On-screen text rendering (HUD)
- Custom shaders support
- Screen-space shaders (filters)
- FXAA
- Simple effects
  - smoke
  - fire
  
### Quickstart
````kotlin
fun main() = korender(LwjglPlatform()) {
  add(SimpleRenderable(
    mesh = Meshes.sphere(2f).build(gpu),
    material = Materials.standard(gpu) {
      colorFile = "/sand.png"
    }
  ))
}
````

### More examples
Find [more advanced examples](https://github.com/zakgof/korender/tree/main/korender/src/examples/kotlin)

### Concepts
- **KorenderContext** - your entry point to access engine features
- **Renderable** - a primitive that can be rendered. A renderable's has a *Mesh* (geometry) and a *Material* (surface properties)
- **Mesh** - is generally an indexed collection of vertices that form triangles. Each vertex has a set of *Attributes*, such as position, normal, texture coordinates, etc.
- **Material** - defines a renderable's surface look; consist of a *Shader* and its *Uniforms* (properties)
- **Shader** - compiled GPU program, consisting of vertex ang fragment shader code
- **Uniforms** - shader's parameters