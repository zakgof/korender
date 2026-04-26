# Korender

**Korender** is a Kotlin Multiplatform 3D graphics rendering engine based on OpenGL / OpenGL ES / WebGL.

Latest version: **0.7.0-SNAPSHOT** (development)    
Latest stable: **0.6.1**    
Github: [https://github.com/zakgof/korender](https://github.com/zakgof/korender)    
License: [Apache-2.0](https://github.com/zakgof/korender?tab=Apache-2.0-1-ov-file#)    
Web demo: [https://zakgof.github.io/projects/korender/wasm](https://zakgof.github.io/projects/korender/wasm)    

#### Supported platforms
| Platform                | 3D API      |
|-------------------------|-------------|
| Desktop (Windows/Linux) | OpenGL 3.3  |
| Android                 | OpenGL ES 3 |
| Web (WebAssembly)       | WebGL 2     |

MacOS support is partial; note that OpenGL is officially deprecated starting macOS 10.14 Mojave.

#### Key Features
- **Declarative DSL**: Compose-like API for building 3D scenes
- **Physically-Based Rendering (PBR)**: Metallic-roughness and specular-glossiness models
- **Lighting**: Directional and point lights with dynamic shadows (VSM, PCF)
- **Texturing**: Full texture support with normal mapping, roughness, metallic, occlusion
- **Advanced Materials**: Triplanar mapping, stochastic sampling, texture arrays
- **Model Loading**: glTF/glb with animations, OBJ (Wavefront)
- **Post-Processing**: Screen-space effects and custom filters
- **Batching & Instancing**: Efficient rendering of many objects
- **Terrain Rendering**: Clipmap-based terrain with heightmaps
- **Deferred Shading**: Experimental deferred rendering pipeline
- **Custom Shaders**: Extensible shader plugin system

