## Materials and material modifiers

Internally, a material definition in Korender consists of:

- `shader` - a small program running on the GPU to render objects, which is defined by    
  * `vertex shader` - GPU code in GLSL language to process mesh vertices    
  * `fragment shader` - GPU code in GLSL language to process rendered pixels (fragments)    
- `defs` - a set of flags that control shader options    
- `plugins` - customized chunks of code to be injected into shaders    
- `uniforms` - key-value pairs representing shaders' parameters    

Each of the above material components can be modified by a `MaterialModifier`.

Using custom shaders or shader plugins is an advanced topic, however, predefined materials should be sufficient for most use cases.

### Base material

Korender comes with a predefined `base` material supporting a decent lighting set and texturing options. Additionally, several modifiers are available to enable additional features on top of the `base` material:

````kotlin
base(
    color = ColorRGBA(0x203040FF),
    colorTexture = texture("textures/texture.png"),
    metallicFactor = 0.5f,
    roughnessFactor = 0.3f
),
triplanar(
    scale = 0.5f
)
````

#### `base` modifier

Basic rendering with texturing and PBR lighting model

| Uniform name   | Type               | Default value            | Description                                                                                                                           |
|----------------|--------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| color          | ColorRGBA          | ColorRBGA.White          | Base surface color (albedo)                                                                                                           |
| colorTexture   | TextureDeclaration | -                        | Base surface texture (albedo), texel values are multiplied by `color`                                                               |
| metallicFactor | Float              | 0.1f                     | PBR metallic factor: from 0.0 (non-metal) to 1.0 (metal). Metals reflect more light and do not have a diffuse color — their color comes from specular reflections. Non-metals reflect less light and have a diffuse base color. |
| roughnessFactor| Float              | 0.5f                     | PBR roughness factor: from 0.0 (smooth) to 1.0 (rough). Affects how light scatters off the surface. Low roughness = sharp, mirror-like reflections. High roughness = diffuse, blurry reflections.|

#### `triplanar` modifier

Enables triplanar texturing.

Instead of using UV coordinates, triplanar texturing projects the texture onto the surface from three directions: X-axis (side), Y-axis (top), Z-axis (front). The results are blended together based on the surface normal to avoid visible seams.

| Uniform name   | Type               | Default value            | Description                                                                                                                           |
|----------------|--------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| scale          | Float              | 1.0f                     | Controls how large or small the texture appears when projected onto the object. High scale value: texture appears more frequently repeated (smaller features). Low scale value: texture appears stretched or zoomed in (larger features). `scale = 1.0` → Texture covers 1 unit of world space.|

#### `normalTexture` modifier

Enables normal texturing.
A normal texture is a special kind of image used to simulate fine surface detail, such as bumps, wrinkles, or grooves, without increasing the polygon count of a model.

| Uniform name   | Type               | Default value            | Description      |
|----------------|--------------------|--------------------------|------------------|
| normalTexture  | TextureDeclaration | -                        | Normal texture   |

#### `emission` modifier

Enables material emission factor/color. Emission refers to the ability of a surface to emit light, as if it's glowing on its own without needing any external light source.
Emitted light is added to diffuse/directional light reflected from the surface.

| Uniform name   | Type               | Default value            | Description      |
|----------------|--------------------|--------------------------|------------------|
| factor         | ColorRGB           | -                        | Emission factor/color   |

#### `metallicRoughnessTexture` modifier

Enables metallic-roughness texture - a packed texture used in PBR to control how a material reflects light. It stores two properties: metallic and roughness in respectively, B and G channels of the texture.

| Uniform name   | Type               | Default value            | Description      |
|----------------|--------------------|--------------------------|------------------|
| texture        | TextureDeclaration | -                        | Metallic-roughness texture |
