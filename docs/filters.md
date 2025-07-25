# Post-processing filters

Korender supports post-processing effects that can be applied to the rendered scene. The post-processing pipeline operates as follows:

- The scene is first rendered to a framebuffer, capturing both color and depth as target textures.
- Each post-processing effect renders a full-screen quad using a dedicated shader, utilizing color and depth data from the original scene or the output of the previous effect.
- The final post-processing effect renders directly to the screen.

To define a post-processing effect in your frame, use the `PostProcess` function with one or more material modifiers that specify the effect’s shader.

````kotlin
Frame {
    PostProcess(blurHorz(radius = 3.0f))
    PostProcess(blurVert(radius = 3.0f))
````

Korender includes the following built-in post-processing material modifiers:

| Post-process filter | Description                                |
|---------------------|--------------------------------------------|
| blurHorz            | Horizontal separable blur pass             |
| blurVert            | Vertical separable blur pass               |
| adjust              | Adjust brightness, contract and saturation |
| water               | Water effect                               |
| fog                 | Fog                                        |
| fxaa                | FXAA anti-aliasing filter                  |

