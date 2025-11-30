# Asset storage

Korender uses [Kotlin Multiplatform resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-usage.html) for storing assets, such as 3d model files and textures.

To let Korender access your assets, provide `appResourceLoader` as a `Korender` composable function's arguments.

```kotlin
@Composable
fun App() = Korender(appResourceLoader = { Res.readBytes(it) }) {
        // ...
        Frame {
            Renderable(
                base(colorTexture = texture("texture/asphalt.jpg")),
```
In the above example, the texture asset is located in the file `commonMain/composeResources/files/texture/asphalt.jpg`

Korender also includes some bundled resources. To access them, start the path with an exclamation mark: `!font/anta.ttf`

# Asset loading and unloading

Korender loads an asset as soon as it's declared in a rendering frame. Asset unloading is managed by retention policy. The default policy is to automatically unload a resource after 10 seconds of not being used.    

Retention policy can be overridden by setting the `retentionPolicy` var in the Korender context:

```kotlin
@Composable
Korender(appResourceLoader = { Res.readBytes(it) }) {
    retentionPolicy = keepForever()
```

#####Available retention policies:    
  - `immediatelyFree()` - unload a resource immediately after it's not used in a frame.    
  - `keepForever()` - do not unload a resource, keep it forever.    
  - `time(seconds: Float)` - unload a resource after specified number of seconds of not being used.    
  - `untilGeneration(generation: Int)` - use generation-based retention: unload all unused resources which generation is less than the specified value; asset generation is specified by the value of the variable `retentionGeneration` at the moment of asset loading.    

