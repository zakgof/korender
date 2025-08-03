package com.zakgof.korender.baker

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ltree.LTreeBaker

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Korender Baker"
    ) {
        LTreeBaker()
        // BillboardTreeBaker()
        // RadiantTreeBaker()
    }
}