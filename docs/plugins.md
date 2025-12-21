# Plugins

While Korender materials offer a robust set of built-in rendering features, advanced use cases may require custom logic beyond the standard parameters. To support this, Korender utilizes *Shader Plugins*.

Shader Plugins are modular blocks of GLSL code that allow developers to extend the engine's core rendering functionality. These plugins are injected into the pipeline at predefined Extension Points, enabling custom behavior without requiring a full rewrite of the base shaders.

Plugin may introduce their own uniforms to parameterize the material. Also they have access to fragment/vertex properties.

## Extension points

### Vertex shader extensions

| Extension Point | Function signature | Return value |
|---|---|---|
| vposition | vec4 pluginVPosition() | Vertex world space position |
| vnormal | vec3 pluginVNormal() | Vertex world space normal |

Global variables available to vertex plugins:

````glsl
in vec3 pos;     	// vertex attribute
in vec3 normal;  	// vertex attribute
in vec2 tex;     	// vertex attribute
#uniform mat4 model;	// model matrix
````

### Fragment shader extensions

| Extension Point | Function signature | Return value |
|---|---|---|
| position | vec3 pluginPosition() | Overrides world-space fragment position |
| texturing | vec4 pluginTexturing() | Multiplier for texture color |
| normal | vec3 pluginNormal() | Overrides world-space normal |
| albedo | vec4 pluginAlbedo() | Overrides fragment albedo (unlit color) |
| discard | bool pluginDiscard() | Overrides fragment discard test |
| emission | vec3 pluginEmission() | Defines light emission color |
| metallic_roughness | vec2 pluginMetallicRoughness | Overrides material's metallic and roughness factors |
| specular_glossiness | vec2 pluginSpecularGlossiness | Switched material model to specular/glossiness and provides the corresponding factors |
| occlusion | float pluginOcclusion | Overrides occlusion value at fragment |

Global variables available to fragment plugins:

````glsl
in vec3 vpos;		// world position from vertex shader
in vec3 vnormal;	// normal from vertex shader
in vec2 vtex;		// texture coordinates
vec3 position;		// world position (may be modified in the shader)
vec4 albedo;		// fragment albedo (unlit color)
vec3 normal;		// normal (may be modified in the shader)
vec3 emission;		// emission color
float metallic;		// metallic factor
float roughness;	// roughness factor
vec3 look;		// world space vector from camera to fragment
````

Example: 

`texturing.wave.frag`:
````glsl
#uniform float wavescale;

vec4 pluginTexturing() {
    float wave = 0.5 + 0.5 * sin(vtex.x * wavescale) * sin(vtex.y * wavescale); 
    return vec4(wave, 0., 0., 1.);
}
````

````kotlin
Renderable(
  base(),
  plugin("texturing", "shaders/texturing.wave.frag"),
  uniform("wavescale", 10f),
  mesh = ....
...
````
