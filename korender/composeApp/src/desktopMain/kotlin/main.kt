package com.zakgof.korender

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "korender") {
        AppExample()
    }
}