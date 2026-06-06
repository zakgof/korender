# Custom shaders

Korender allows replacing the default vertex and/or fragment shaders with custom GLSL code for full control over rendering.

## Custom material with custom vertex and fragment shaders

```kotlin
customMaterial(vertShaderFile: String, fragShaderFile: String, block: MaterialScope.() -> Unit): Material
```

Use this when you need complete control over both vertex and fragment processing:

```kotlin
Frame {
    val myMaterial = customMaterial("shaders/my.vert", "shaders/my.frag") {
        float("myParam", 1.5f)
        vec3("myColor", Vec3(1f, 0f, 0f))
        texture("myTex", texture("textures/diffuse.png"))
    }
    Renderable(material = myMaterial, mesh = sphere())
}
```

The `block` configures custom uniforms via `MaterialScope` (`float`, `int`, `vec2`, `vec3`, `texture`, `plugin`, `flags`).

## Custom material with custom vertex shader only

```kotlin
customMaterial(vertShaderFile: String, block: BaseMaterialScope.() -> Unit): Material
```

Use a custom vertex shader while keeping Korender's standard PBR fragment shader. This is useful for vertex animation, deformation, or custom vertex transformations:

```kotlin
Frame {
    val animMaterial = customMaterial("shaders/wave.vert") {
        color = ColorRGBA.Red
        metallicFactor = 0.5f
        roughnessFactor = 0.3f
        colorTexture = texture("textures/diffuse.png")
    }
    Renderable(material = animMaterial, mesh = sphere())
}
```

The `block` configures all `BaseMaterialScope` properties (color, colorTexture, metallicFactor, roughnessFactor, etc.) plus custom uniforms.

## Custom post-processing filter

```kotlin
customPostProcessingFilter(fragmentShaderFile: String, block: PostProcessMaterialScope.() -> Unit = {}): PostProcessingMaterial
```

Create a custom post-processing effect:

```kotlin
Frame {
    PostProcess(customPostProcessingFilter("shaders/myFilter.frag") {
        float("intensity", 0.5f)
    })
}
```

## Shader plugin system

For less invasive customization, use `shaderPlugin` to inject GLSL code into specific stages of the standard pipeline without writing full shaders. See [Plugins](plugins.md) for details.
