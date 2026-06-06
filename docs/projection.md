### Projection

Korender supports perspective (frustum), orthographic, and logarithmic depth projections.

Set the `projection` property of the Korender context to configure the projection transform. The below example takes into account the viewport aspect ratio:

```kotlin
Frame {
    projection = projection(4f * width / height, 4f, 4f, 10000f, ortho())
}
```

The `projection` function parameters:
- `width` — frustum width at near clipping plane
- `height` — frustum height at near clipping plane
- `near` — distance from camera to near clipping plane
- `far` — distance from camera to far clipping plane
- `mode` — projection mode: `frustum()`, `ortho()`, or `log(c)`

Default projection is `projection(width = 5f * width / height, height = 5f, near = 10f, far = 1000f, frustum())`

Additionally, Korender supports logarithmic depth buffer — a depth buffer technique that stores the logarithm of the depth value instead of the linear depth. It is especially useful in rendering large-scale scenes (e.g., planetary terrain, open-world games) where depth precision becomes problematic.

```kotlin
Frame {
    projection = projection(4f * width / height, 4f, 4f, 10000f, log())
}
```

`log(c = 1.0f)` takes an optional logarithmic depth constant — use higher values for larger depth ranges.

### Camera

Set the `camera` property of the Korender context to set the camera:

```kotlin
Frame {
    camera = camera(Vec3(-2.0f, 5f, 30f), -1.z, 1.y)
}
```

The `camera` function parameters:
- `position` — camera position in world space
- `direction` — camera look direction (will be normalized)
- `up` — up direction (will be normalized)

Default camera is `camera(20.z, -1.z, 1.y)` — positioned at `(0, 0, 20)` looking toward `-Z` with `+Y` up. The shorthand `-1.z` is equivalent to `Vec3(0, 0, -1)` and `1.y` to `Vec3(0, 1, 0)`, provided by Korender's math extensions.
