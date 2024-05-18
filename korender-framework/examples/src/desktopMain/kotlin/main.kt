package com.zakgof.korender

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.zakgof.korender.examples.AppExample

fun main() = application {
    Window(
        icon = painterResource("/korender32.png"),
        onCloseRequest = ::exitApplication,
        title = "Korender Demo"
    ) {
        MaterialTheme(colors = darkColors()) {
            Scaffold {
                AppExample()
            }
        }
    }
}