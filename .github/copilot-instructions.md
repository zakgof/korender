# Korender Copilot Instructions

Korender is a **Kotlin Multiplatform 3D rendering engine** that integrates with Jetbrains Compose Multiplatform. It provides a declarative API for creating 3D graphics that runs on Desktop (Windows/Linux), Android, and Web (WebAssembly).

## Quick Start Commands

### Build & Run
- **Desktop (JVM)**: `./gradlew :korender-framework:examples:run`
- **Web (WASM)**: `./gradlew :korender-framework:examples:wasmJsBrowserDevelopmentRun`
- **Web (production build)**: `./gradlew :korender-framework:examples:wasmJsBrowserDistribution`
- **Android**: `./gradlew :korender-framework:examples:installRelease`

### Build Core Library
- `./gradlew :korender-framework:korender:build`

### Clean Build
- `./gradlew clean :korender-framework:korender:build`

**Note**: The `baker` module is a shader baking utility; it's not typically built as part of the main development cycle.

## Architecture Overview

### Project Structure
```
korender-framework/
├── korender/           # Core rendering engine library (public & impl code)
├── examples/           # Showcase/demo application
└── baker/              # Shader baking utility
```

### Public API vs Implementation

**Korender uses a clean public/internal split**:
- **Public API**: Top-level files in `commonMain/kotlin/` (e.g., `Korender.kt`, `Meshes.kt`, `Materials.kt`, `Events.kt`)
- **Internal Implementation**: Everything under `impl/` folder is implementation detail

**Key distinction**: The `Platform.kt` file uses `expect`/`actual` declarations. Platform-specific implementations are in `desktopMain`, `androidMain`, `webMain` folders with corresponding `Platform.kt` files.

### Multiplatform Source Sets
- **commonMain**: Shared code across all platforms (declarative API, math utilities, business logic)
- **desktopMain**: JVM-specific (LWJGL OpenGL bindings)
- **androidMain**: Android-specific (OpenGL ES bindings)
- **webMain**: JavaScript/WebAssembly-specific (WebGL bindings)

Code in `commonMain` must not reference platform-specific APIs. Use `expect`/`actual` pattern for platform differences.

### Core Modules

#### `impl/engine/`
- **Engine.kt**: Main rendering orchestration
- **Renderer.kt**: Frame rendering pipeline
- **RenderContext.kt**: Per-frame render state management
- **Loader.kt**: Async asset loading

#### `impl/material/`
- **Shaders.kt**: Shader compilation and management
- **Materials.kt**: Material definitions and PBR implementation
- **ShaderPlugin.kt**: Extensible shader pipeline (decorators for shader functionality)
- **SkyMaterials.kt**, **PostProcessingMaterials.kt**, **BillboardEffects.kt**: Specialized material types

#### `impl/geometry/`
- **Meshes.kt**: Mesh data structures
- **ObjLoader.kt**, **GltfLoader.kt**: Model file importers
- **MeshAttributes.kt**: Vertex attribute management

#### `impl/gl/`
Low-level OpenGL abstractions (GL.kt, GLProgram.kt, GLShader.kt, GLTexture.kt, etc.). These are common to all platforms but backend-specific.

#### `impl/glgpu/`
High-level GPU resource wrappers (GlGpuShader, GlGpuMesh, GlGpuTexture, etc.) - the primary interface for GPU operations.

### Shader Plugin System

The engine uses a plugin-based shader architecture. Each `ShaderPluginId` (e.g., `TEXTURING`, `NORMAL`, `METALLIC_ROUGHNESS`) corresponds to a shader pass that modifies rendering. Plugins are composed to build complete shaders. See `impl/material/ShaderPlugin.kt`.

### Declarative DSL Scopes

Korender's API is built on nested scope receivers:
- **KorenderScope**: Top-level 3D viewport
- **FrameScope**: Per-frame rendering context (inside a Frame { })
- **PipeMeshScope**: Material assignment context
- **ShadowScope**: Shadow map rendering
- **DeferredShadingScope**: Deferred rendering pipeline (experimental)

These provide type-safe, context-aware DSL for building scenes.

## Key Conventions

### Naming
- **Public API**: Uppercase first letter (e.g., `Korender`, `Meshes`, `Materials`)
- **Internal classes**: Prefix with context (e.g., `DefaultFrameScope`, `GlGpuMesh`, `DefaultCamera`)
- **Math types**: Uppercase (e.g., `Vec3`, `Mat4`, `Quaternion`)
- **impl/* classes**: Usually prefixed with capability/backend (e.g., `GlGpuFrameBuffer`, `ObjLoader`)

### Scope Receivers (DSL Building)
The codebase uses Kotlin scope receivers extensively for DSL creation. A function with `context(ScopeType)` can access properties/functions from that scope. Familiarize yourself with:
- `context(KorenderScope)` in top-level API functions
- `context(FrameScope)` in per-frame operations
- `context(ResourceScope)` for asset management

### Platform Specifics with expect/actual

When you need platform-specific code:
1. Define `expect` in `commonMain` (e.g., `Platform.kt`)
2. Implement `actual` in each platform (e.g., `desktopMain/Platform.kt`)
3. Use conditional compilation if needed: `@OptIn(ExperimentalWasmDsl::class)` for WASM-specific builds

### Compose Integration

Korender is a `@Composable` function. State management follows Compose conventions:
- Use `remember` for frame-local state
- Use `LaunchedEffect` for side effects
- Rendering is recomposable but typically stable (same inputs = same output)

## Build System Details

### Gradle Configuration
- **Kotlin 2.3.0**, Kotlin Multiplatform, Jetbrains Compose Multiplatform
- **JVM Target**: 17
- **Android**: API 24+ (minSdk), API 34+ (compileSdk)
- **WASM**: Experimental support via `@OptIn(ExperimentalWasmDsl::class)`

### Important Gradle Properties
- `korenderVersion = 0.7.0` and `korenderVersionSuffix = -SNAPSHOT` (in gradle.properties)
- `development=true` enables development-mode builds
- Maven Central publication configured in `korender/build.gradle.kts`

### Common Build Tasks
- `build`: Full build for all targets
- `compileKotlin*`: Compile for specific target (e.g., `compileKotlinDesktop`)
- `test`: Run tests (note: currently no tests in this beta library)

## Testing

No formal test framework is currently in use. The project relies on:
- **Visual testing**: Run examples on each platform
- **Smoke tests**: Deploy to GitHub Pages (WASM) and test live
- **Manual testing**: Interactive verification of rendering features

## CI/CD Pipeline

GitHub Actions workflows in `.github/workflows/`:
- **deploy-wasm.yml**: Triggered on `release` branch push. Builds WASM, deploys to GitHub Pages
- **deploy-mkdocs.yml**: Deploys MkDocs documentation

These require JDK 17 and Gradle. Artifacts are cached.

## Documentation

- **README.md**: Quick start, feature list, running examples locally
- **docs/**: MkDocs source (published to wiki)
- **mkdocs.yml**: MkDocs configuration
- **Examples**: See `korender-framework/examples/src/commonMain/kotlin/` for working code patterns

## Debugging Tips

### Common Issues

**Issue**: WASM build fails with shader compilation errors
- **Cause**: Shader code may not be compatible with WebGL 2
- **Fix**: Validate shaders in `impl/material/Shaders.kt` for GLSL ES compliance

**Issue**: Desktop build fails with LWJGL linking
- **Cause**: Missing platform-specific natives or JVM toolchain mismatch
- **Fix**: Ensure JDK 17 is used. Check `gradle.properties` for JVM args

**Issue**: Resource loading fails
- **Cause**: `ResourceLoader` not provided or returns null
- **Fix**: Pass a valid `resourceLoader` lambda to `Korender {}` block

### Logging & Debugging
- Enable Gradle logging: `./gradlew --debug`
- Check shader debug info: `impl/material/ShaderDebugInfo.kt`
- Inspect GPU state: `impl/engine/GlState.kt`

## Dependencies to Know

- **Compose Multiplatform**: UI framework (Korender embeds as @Composable)
- **LWJGL 3**: Desktop OpenGL bindings
- **Kotlinx Serialization**: JSON/CBOR for asset metadata
- **Kotlinx Coroutines**: Async resource loading
- **Kotlin Reflect**: Used for platform detection on JVM

## Performance Notes

- **Instancing**: Implemented via `impl/context/Instancing.kt` for batched rendering
- **Shadow Mapping**: VSM (Variance Shadow Maps) and PCF (Percentage-Closer Filter) supported
- **Screen-Space Effects**: Post-processing available (see `PostProcessingMaterials.kt`)
- **Deferred Shading**: Available but marked experimental

Resource pooling and reuse are handled by `impl/engine/Inventory.kt` and `ResultKeeper.kt`.

## File Organization Reference

For quick navigation:
- **Public API**: Look in root of `src/commonMain/kotlin/` (no `impl/` prefix)
- **Platform behavior**: Check `*Main/kotlin/Platform.kt` (desktopMain, androidMain, webMain)
- **Rendering pipeline**: Start in `impl/engine/Engine.kt`, trace to `Renderer.kt`
- **Shaders**: Look in `impl/material/`
- **Math**: Check `math/` folder for Vec3, Mat4, Transform, etc.
- **3D models**: Geometry loaders in `impl/geometry/`
