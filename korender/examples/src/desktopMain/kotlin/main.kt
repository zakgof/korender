package com.zakgof.korender

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.zakgof.korender.examples.AppExample

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "korender") {
        AppExample()
    }
}