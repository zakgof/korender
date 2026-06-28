# Plugins

While Korender materials offer a robust set of built-in rendering features, advanced use cases may require custom logic beyond the standard parameters. To support this, Korender utilizes *Shader Plugins*.

Shader Plugins are modular blocks of GLSL code that allow developers to extend the engine's core rendering functionality. These plugins are injected into the pipeline at predefined Extension Points, enabling custom behavior without requiring a full rewrite of the base shaders.

Plugins may introduce their own uniforms to parameterize the material and have access to fragment/vertex properties.

## Defining a plugin

Use `shaderPlugin(id, file)` to load a GLSL plugin file and assign it to a pipeline stage:

```kotlin
val myPlugin = shaderPlugin(ShaderPluginId.TEXTURING, "shaders/texturing.wave.frag")
```

Apply it inside a material block via `MaterialScope.plugin`:

```kotlin
Renderable(
    material = base {
        plugin(myPlugin)
        float("wavescale", 10f)
    },
    mesh = sphere()
)
```

Custom uniforms are set using `float`, `int`, `vec2`, `vec3`, or `texture` methods on `MaterialScope`.

## Extension points

### Vertex shader extensions

| Extension Point | Function signature | Return value |
|---|---|---|
| `VPOSITION` | `vec4 pluginVPosition()` | Vertex world space position |
| `VNORMAL` | `vec3 pluginVNormal()` | Vertex world space normal |
| `VPROJECTION` | `vec4 pluginVProjection()` | Overrides clip-space position |

Global variables available to vertex plugins:

```glsl
in vec3 pos;        // vertex attribute
in vec3 normal;     // vertex attribute
in vec2 tex;        // vertex attribute
#uniform mat4 model;    // model matrix
```

### Fragment shader extensions

| Extension Point | Function signature | Return value |
|---|---|---|
| `TEXSOURCE` | `vec4 pluginTexSource()` | Source texture sample before texturing |
| `TEXTURING` | `vec4 pluginTexturing()` | Multiplier for texture color |
| `POSITION` | `vec3 pluginPosition()` | Overrides world-space fragment position |
| `NORMAL` | `vec3 pluginNormal()` | Overrides world-space normal |
| `ALBEDO` | `vec4 pluginAlbedo()` | Overrides fragment albedo (unlit color) |
| `DISCARD` | `bool pluginDiscard()` | Overrides fragment discard test |
| `EMISSION` | `vec3 pluginEmission()` | Defines light emission color |
| `METALLIC_ROUGHNESS` | `vec2 pluginMetallicRoughness()` | Overrides metallic/roughness factors |
| `SPECULAR_GLOSSINESS` | `vec2 pluginSpecularGlossiness()` | Switches to specular/glossiness PBR model |
| `OCCLUSION` | `float pluginOcclusion()` | Overrides occlusion value |
| `OUTPUT` | `vec4 pluginOutput()` | Final output color override |
| `SECSKY` | `vec3 pluginSecSky()` | Secondary sky reflection color |
| `SKY` | `vec3 pluginSky()` | Primary sky color |
| `TERRAIN` | `vec4 pluginTerrain()` | Terrain-specific color override |
| `DEPTH` | `float pluginDepth()` | Overrides depth value |

Global variables available to fragment plugins:

```glsl
in vec3 vpos;       // world position from vertex shader
in vec3 vnormal;    // normal from vertex shader
in vec2 vtex;       // texture coordinates
vec3 position;      // world position (may be modified in the shader)
vec4 albedo;        // fragment albedo (unlit color)
vec3 normal;        // normal (may be modified in the shader)
vec3 emission;      // emission color
float metallic;     // metallic factor
float roughness;    // roughness factor
vec3 look;          // world space vector from camera to fragment
```

## Example

`shaders/texturing.wave.frag`:

```glsl
#uniform float wavescale;

vec4 pluginTexturing() {
    float wave = 0.5 + 0.5 * sin(vtex.x * wavescale) * sin(vtex.y * wavescale);
    return vec4(wave, 0., 0., 1.);
}
```

```kotlin
// Load plugin at the scope level (Korender or Frame scope)
val wavePlugin = shaderPlugin(ShaderPluginId.TEXTURING, "shaders/texturing.wave.frag")

// Use in material
Renderable(
    material = base {
        plugin(wavePlugin)
        float("wavescale", 10f)
    },
    mesh = sphere()
)
```
