### Korender and Frame contexts

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

Add initialization code into `Korender` context, per-frame code and renderable objects into `Frame` context:

````kotlin
Korender {
    // Code here will run once on Korender viewport initialization           
    Frame {
        // Code here will run on every frame
        // Place your renderable declaration functions here
    }
}
````