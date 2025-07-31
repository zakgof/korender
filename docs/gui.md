# GUI

Korender supports on-screen rendering of texts and images. They can react to touch events (mouse events on the desktop), so it's possible to create basic widgets.

## Containers, Elements, and Fillers

Elements can be positioned on the rendering viewport using containers - stacks, columns, and rows - and fillers. A filler consumes the rest of the available horizontal space in a row, or vertical space in a column. If multiple fillers appear in a container, they share the available space equally.

````kotlin
Frame {
    Gui {
        Row {
            Text(text = "LEFT TOP", id = "lt", fontResource = "/ubuntu.ttf", height = 30, color = ColorRBGA(0xFFFF8888))
            Filler
            Text(text = "RIGHT TOP", id = "rt", fontResource = "/ubuntu.ttf", height = 30, color = ColorRBGA(0xFFFF8888))
        }
        Filler
        Row {
            Filler
            Image(imageResource = "/bottom.png", width = 100, height = 100)
            Filler
        }
    }
}
````
In the above example, the text "LEFT TOP" appears in the left top corner, and the text "RIGHT TOP" appears in the right top corner; at the bottom of the screen, the image "/bottom.png" from resources is rendered, centered horizontally.

### Element parameters

Image:

- `imageResource`: image file path
- `width`: width in pixels
- `height`: height in pixels
- `onTouch`: touch event handler

Text:

- `text`: string to display (ASCII only)
- `id`: object used for identification of the element in the tree, must be unique
- `fontResource`: classpath of the ttf file
- `color`: text color
- `onTouch`: touch event handler

### Touch handlers

Touch handlers allow you to react to touch events occurring within the widget's screen rectangle. Touch event contains event type (`Up`, `Down` or `Move`) and event coordinates on screen.
To handle click events, use the `onClick` helper:
````kotlin
Image(
    ....
    onTouch = {
        onClick(it) {
        // Handle click here 
        }
    }
)
```` 
