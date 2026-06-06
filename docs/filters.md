# Post-processing filters

Korender supports post-processing effects that can be applied to the rendered scene. The post-processing pipeline operates as follows:

- The scene is first rendered to a framebuffer, capturing both color and depth as target textures.
- Each post-processing effect renders a full-screen quad using a dedicated shader, utilizing color and depth data from the original scene or the output of the previous effect.
- The final post-processing effect renders directly to the screen.

To define a post-processing effect in your frame, use the `PostProcess` function. It accepts either a `PostProcessingMaterial` or a `PostProcessingEffect`:

```kotlin
Frame {
    PostProcess(blurHorz(radius = 3.0f))
    PostProcess(blurVert(radius = 3.0f))
}
```

`PostProcess` also accepts an optional block to render geometry after the effect:

```kotlin
PostProcess(material = fxaa()) {
    // geometry rendered after FXAA
}
```

Korender includes the following built-in post-processing filters:

| Post-process filter | Signature | Description |
|---------------------|-----------|-------------|
| blur | `blur(radius: Float): PostProcessingEffect` | Gaussian blur as a standalone effect |
| blurHorz | `blurHorz(radius: Float): PostProcessingMaterial` | Horizontal separable Gaussian blur pass |
| blurVert | `blurVert(radius: Float): PostProcessingMaterial` | Vertical separable Gaussian blur pass |
| adjust | `adjust(brightness, contrast, saturation): PostProcessingMaterial` | Adjust brightness, contrast and saturation |
| water | `water(waterColor, transparency, waveScale, waveMagnitude, sky): PostProcessingMaterial` | Water surface with waves (`sky: SkyMaterial` is required) |
| fog | `fog(density, color): PostProcessingMaterial` | Fog effect |
| fxaa | `fxaa(): PostProcessingMaterial` | FXAA anti-aliasing filter |
| customPostProcessingFilter | `customPostProcessingFilter(fragmentShaderFile, block): PostProcessingMaterial` | Custom post-processing shader |
