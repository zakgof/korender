package com.zakgof.korender

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.zakgof.app.resources.Res
import com.zakgof.app.resources.korender32
import com.zakgof.korender.examples.AppExample
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        icon = painterResource(Res.drawable.korender32),
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