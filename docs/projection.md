### Projection
Korender supports perspective (frustum) and orthographic projections.
Set the `projection` property of Korender context to set the projection transform. The below example takes into account the viewport aspect ratio:

````kotlin
Frame {
    projection = projection(4f * width / height, 4f, 4f, 10000f, ortho())
````
Default projection is `projection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f, frustum())`

Additionally, Korender supports logarithmic depth buffer - a depth buffer technique that stores the logarithm of the depth value instead of the linear depth. It is especially useful in rendering large-scale scenes (e.g., planetary terrain, open-world games) where depth precision becomes problematic.
````kotlin
Frame {
    projection = projection(4f * width / height, 4f, 4f, 10000f, log())
````

### Camera
Set the `camera` property of the Korender context to set the camera:

````kotlin
Frame {
    camera = camera(Vec3(-2.0f, 5f, 30f), -1.z, 1.y)
````
Default camera is `camera(20.z, -1.z, 1.y)`