# GUI

Korender supports on-screen rendering of text and images that can react to touch events (mouse events on desktop), enabling basic interactive widgets. All GUI widgets are declared inside a `Gui` block within a `Frame`:

```kotlin
Frame {
    Gui {
        // widgets here
    }
}
```

## Layout

Widgets are arranged using containers:

- `Row { ... }` — arranges children horizontally
- `Column { ... }` — arranges children vertically
- `Stack { ... }` — stacks children on top of each other
- `Filler()` — consumes remaining space in a row/column; multiple fillers share space equally

```kotlin
Frame {
    Gui {
        Row {
            Text(id = "lt", text = "LEFT TOP", fontResource = "/ubuntu.ttf", height = 30, color = ColorRGBA(0xFFFF8888))
            Filler()
            Text(id = "rt", text = "RIGHT TOP", fontResource = "/ubuntu.ttf", height = 30, color = ColorRGBA(0xFFFF8888))
        }
        Filler()
        Row {
            Filler()
            Image(id = "bottom", imageResource = "/bottom.png", width = 100, height = 100)
            Filler()
        }
    }
}
```

In the above example, "LEFT TOP" appears in the left top corner, "RIGHT TOP" in the right top corner, and the image is centered at the bottom.

## Widgets

### Text

```kotlin
Text(id: String, text: String, style: TextStyle? = null, fontResource: String? = null,
     height: Float? = null, color: ColorRGBA? = null, static: Boolean = false,
     onTouch: TouchHandler = {})
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String | Unique identifier |
| `text` | String | Text to display |
| `style` | TextStyle? | Text style preset (font, color, height) |
| `fontResource` | String? | TTF font file path |
| `height` | Float? | Font height in pixels |
| `color` | ColorRGBA? | Text color |
| `static` | Boolean | Set to true for optimization if text never changes |
| `onTouch` | TouchHandler | Touch event handler |

Convenience overloads accepting `Int` values for `height` are also available.

### Image

```kotlin
Image(id: String, imageResource: String, width: Float, height: Float,
      marginTop: Float = 0f, marginBottom: Float = 0f, marginLeft: Float = 0f, marginRight: Float = 0f,
      onTouch: TouchHandler = {})
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String | Unique identifier |
| `imageResource` | String | Image file path |
| `width` | Float | Width in pixels |
| `height` | Float | Height in pixels |
| `marginTop/Bottom/Left/Right` | Float | Margins around the image |
| `onTouch` | TouchHandler | Touch event handler |

Convenience overloads accepting `Int` values are also available.

### Checkbox

```kotlin
Checkbox(id: String, state: CheckboxState, text: String? = null,
         onChange: (Boolean) -> Unit = {})
```

Uses `CheckboxState(initialState: Boolean = false)` — call `onChange` when toggled.

```kotlin
val checkboxState = CheckboxState(true)

Gui {
    Checkbox(id = "mycheck", state = checkboxState, text = "Enable feature") { checked ->
        // handle change
    }
}
```

### Slider

```kotlin
Slider(id: String, width: Float, height: Float = 48f, state: SliderState,
       onChange: (Float) -> Unit = {})
```

Uses `SliderState(position: Float = 0.5f, min: Float = 0f, max: Float = 1f)`.

```kotlin
val sliderState = SliderState(position = 0.3f, min = 0f, max = 100f)

Gui {
    Slider(id = "myslider", width = 400f, state = sliderState) { value ->
        // handle value change
    }
}
```

Convenience overloads accepting `Int` values for `width` and `height` are also available.

### ProgressBar

```kotlin
ProgressBar(id: String, width: Float, height: Float = 48f, value: Float)
```

Displays a horizontal bar. `value` ranges from 0.0 (empty) to 1.0 (full).

```kotlin
Gui {
    ProgressBar(id = "hpbar", width = 300f, value = 0.75f)
}
```

Convenience overloads accepting `Int` values for `width` and `height` are also available.

### Joystick

```kotlin
Joystick(id: String, state: JoystickState, width: Float)
```

Uses `JoystickState` — read `x` and `y` properties (each -1.0 to 1.0).

```kotlin
val joystickState = JoystickState()

Gui {
    Joystick(id = "stick", state = joystickState, width = 200f)
}

// Read joystick input elsewhere:
val axisX = joystickState.x
val axisY = joystickState.y
```

## Touch handlers

Touch handlers allow you to react to touch events within a widget's screen rectangle. `TouchEvent` contains `type` (UP, DOWN, MOVE), `button` (LEFT, RIGHT, NONE), and coordinates.

Use the `onClick` helper to handle click events:

```kotlin
Image(id = "btn", imageResource = "/button.png", width = 100, height = 100,
    onTouch = { onClick(it) { /* Handle click */ } }
)
```

Keyboard events can be handled at the scope level via `OnKey`:

```kotlin
Korender(resourceLoader = { ... }) {
    OnKey { event ->
        if (event.type == KeyEvent.Type.DOWN) {
            // handle key press
        }
    }
}
```
