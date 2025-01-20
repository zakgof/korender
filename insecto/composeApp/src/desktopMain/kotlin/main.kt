
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import city.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Insecto") {
        App()
    }
}