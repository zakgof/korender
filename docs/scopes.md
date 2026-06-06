### Korender and Frame scopes

To insert Korender rendering viewport into your Compose Multiplatform UI, call the `Korender` function from the parent `@Composable` function:

````kotlin
@Composable
fun App() {
    Column {
        Text("Korender Viewport below !!!")        
        Korender {
          
        }
    }
}
````

Add initialization code into `Korender` scope, per-frame code and renderable objects into `Frame` scope:

````kotlin
Korender {
    // Code here will run once on Korender viewport initialization           
    Frame {
        // Code here will run on every frame
        // Place your renderable declaration functions here
    }
}
````

### Node scope

`Node` creates a child scope for hierarchical scene composition. It can override `resourceLoader`, `transform`, `retentionPolicy`, and `time` for a sub-tree:

```kotlin
Frame {
    Node(transform = translate(5f, 0f, 0f), resourceLoader = { customLoad(it) }) {
        // All resources and transforms here are relative to this node
        Renderable(material = base { color = ColorRGBA.Red }, mesh = sphere())
    }
}
```

`Node` is useful for grouping objects, applying local transforms, or switching resource loaders for different asset packs.