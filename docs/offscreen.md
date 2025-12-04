# Offscreen rendering and probes

Instead of rendering to screen, Korender allows to render a scene to an image/texture or to a cube map.

## Render frame to image

Set up a virtual camera and projection and call `captureFrame` - that will return `Deferred<Image>` with the captured image: 

````kotlin
Korender {
    val camera = camera(...)
    val projection = projection(...)
    val deferredImage = captureFrame(1024, 1024, camera, projection) {
        Renderable(...)
    }
    ...
    Frame {
       if (deferredImage.isCompleted) {
           val capturedImage = deferredImage.getCompleted()
           // Use the captured image
    
````

## Render environment to cube images

`captureFrame` samples the environment at the given world-space position and outputs six images aligned with the standard cubemap face orientations. The function returns a `Deferred<CubeTextureImages>`:

````kotlin
Korender {
    val deferredCubeImages = captureEnv(1024, 1f, 1000f) {
        Renderable(...)
    }
    ...
    Frame {
       if (deferredCubeImages.isCompleted) {
           val cubeImages = deferredCubeImages.getCompleted()
           val negativeXSide = cubeImages[CubeTextureSide.NX]
           // Use the captured images
    
````

## Render frame to texture

The functions above retrieve image data from the GPU. If you intend to reuse the rendered scene in subsequent rendering passes, it’s more efficient to keep the data on the GPU - in a texture or cube texture - rather than reading it back.
The following function can be invoked within a Frame context to render the scene directly into a texture:

````kotlin
Frame {
    CaptureFrame("capturedFrame", 256, 256, camera = ..., projection = ...)) {
        Renderable(...)
    }
````
In this example, the scene is rendered into a texture stored in a probe named "capturedFrame". This texture remains on the GPU and can be used in subsequent rendering operations:

````kotlin
Renderable(
   base(colorTexture = textureProbe("capturedFrame"), ...
````
#### Render environment to cube texture

Similarly, environment can be captured without retrieving from GPU to CPU:

````kotlin
Frame {
    CaptureEnv("capturedEnv", 256, position = ..., near = ..., far = ... ) {
        Renderable(...)
    }
````
In this example, the environment is rendered into a cube texture stored in a cube probe named "capturedEnv". This cube texture remains on the GPU and can be used in subsequent rendering operations:

````kotlin
Sky(cubeTextureProbe("capturedEnv"))
````

