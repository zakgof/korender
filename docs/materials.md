## Materials and material modifiers

Internally, a material definition in Korender consists of:

- **Shader** - GPU program to render objects, comprised of:
  - **Vertex Shader** - GLSL code processing mesh vertices
  - **Fragment Shader** - GLSL code processing rendered pixels (fragments)
- **Shader Plugins** - Modular chunks of GLSL code injected into specific stages of the rendering pipeline
- **Uniforms** - Per-material parameters (colors, texture samplers, numeric values)

The material system allows customization at multiple levels, from high-level property modifications to low-level shader plugins.

### Base Material

Korender provides a predefined `base` material supporting PBR (Physically-Based Rendering) with lighting and texturing. Use a lambda block to configure `BaseMaterialScope` properties:

```kotlin
Renderable(
    material = base {
        color = ColorRGBA(0x203040FF)
    },
    mesh = sphere(1.0f)
)
```

Additional features can be enabled by setting optional properties inside the block:

```kotlin
base {
    color = ColorRGBA(1f, 1f, 1f, 1f)
    colorTexture = texture("textures/diffuse.png")
    normalTexture = texture("textures/normal.png")
    metallicFactor = 0.5f
    roughnessFactor = 0.3f
    triplanarScale = 1.0f  // Enable triplanar mapping
}
```

#### Base Material Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `color` | ColorRGBA | White | Base surface color (albedo), multiplied by colorTexture |
| `colorTexture` | TextureDeclaration | - | Base diffuse/albedo texture |
| `metallicFactor` | Float | 0.1 | PBR metallic: 0.0 (non-metal) to 1.0 (fully metal). Metals reflect light as specular only; non-metals have diffuse reflections. |
| `roughnessFactor` | Float | 0.5 | PBR roughness: 0.0 (mirror-smooth) to 1.0 (diffuse). Controls how light scatters from the surface. |
| `alphaCutoff` | Float | 0.5 | Transparency threshold for alpha cutoff (discard fragments below this alpha) |
| `triplanarScale` | Float? | null | Enable triplanar texturing at this world-space scale. When enabled, projects texture from 3 directions instead of using UV coords. |
| `stochasticSharpness` | Float? | null | Enable stochastic texture sampling at this sharpness level. Reduces texture repetition patterns. |
| `colorTextures` | TextureArrayDeclaration? | null | Enable texture array texturing (indexed texture selection per vertex) |
| `normalTexture` | TextureDeclaration | - | Normal map texture for fine surface detail without geometry |
| `metallicRoughnessTexture` | TextureDeclaration | - | Packed texture: R channel = metallic, G channel = roughness |
| `emission` | ColorRGB? | null | Emission color (self-illumination) added to lighting |
| `emissionTexture` | TextureDeclaration | - | Emission texture |
| `occlusionTexture` | TextureDeclaration | - | Ambient occlusion texture (pre-baked shadows) |
| `specularGlossiness` | SpecularGlossiness? | null | Alternative PBR model: specular color and glossiness factors |
| `specularGlossinessTexture` | TextureDeclaration | - | Specular-glossiness packed texture |
| `env` | SkyMaterial? | null | Environment map for reflections |

### Triplanar Mapping

Triplanar texturing projects the texture onto surfaces from three perpendicular directions (X, Y, Z axes) and blends them based on surface normal:

- **Advantages**: No UV unwrapping needed, seamless tiling on any geometry
- **Use cases**: Procedural terrain, rocks, organic shapes
- **Performance**: Higher cost than standard UV texturing

Set `triplanarScale` inside the material block:

```kotlin
base {
    colorTexture = texture("textures/rock.png")
    triplanarScale = 2.0f  // Texture covers 2 world units
}
```

### Stochastic Texture Sampling

Stochastic sampling adds randomized texture offset to reduce visible repetition patterns:

- **Advantages**: More natural appearance for repeated textures
- **Use cases**: Terrain, walls, procedural surfaces
- **Performance**: Slight overhead from randomness computation

Set `stochasticSharpness` inside the material block:

```kotlin
base {
    colorTexture = texture("textures/grass.png")
    stochasticSharpness = 0.5f
}
```

### Texture Arrays

Texture arrays allow indexed selection of multiple textures in a single draw call:

- **Use cases**: Procedural terrain with multiple material types, vertex-colored surface selection
- **Performance**: Efficient for many material variants on many objects

Enable with `colorTextures`:

```kotlin
base {
    colorTextures = textureArray("textures/sand.png", "textures/grass.png", "textures/rock.png")
}
```

### Custom Uniforms and Shader Plugins

Materials support custom uniforms and shader plugins. These are configured inside the material block via `MaterialScope`:

```kotlin
base {
    color = ColorRGBA.White
    float("myCustomFloat", 1.5f)
    vec3("myCustomVec", Vec3(1f, 0f, 0f))
    texture("myCustomTexture", texture("textures/custom.png"))
    plugin(myCustomShaderPlugin)
}
```

### Other Material Types

- `billboard` - camera-facing sprite material with position, scale, rotation, and effects (fire, fireball, smoke)
- `decal` - decal material projected onto surfaces
- `pipe` - material modifier for pipe mesh shapes
- `customMaterial(vertShader, fragShader)` / `customMaterial(vertShader)` - fully custom shader materials
- `customPostProcessingFilter(fragShader)` - custom post-processing filter shader
- Sky materials: `fastCloudSky(...)`, `starrySky(...)`, `cubeSky(cubeTexture)`, `textureSky(texture)`
- Post-processing: `blurHorz(radius)`, `blurVert(radius)`, `adjust(brightness, contrast, saturation)`, `water(...)`, `fog(...)`, `fxaa()`
- Sky material can be assigned to `env` in any base material to enable reflections

### Custom Shaders and Plugins

For advanced rendering, implement custom shader plugins using the `ShaderPlugin` interface. Plugins inject GLSL code into specific pipeline stages identified by `ShaderPluginId`.
