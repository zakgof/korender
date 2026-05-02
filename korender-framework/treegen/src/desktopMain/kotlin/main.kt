package com.zakgof.korender.baker

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.zakgof.korender.treegen.resources.Res
import com.zakgof.korender.treegen.resources.korender
import ltree.LTreeBaker
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        icon = painterResource(Res.drawable.korender),
        title = "Korender TreeGen",
        onCloseRequest = ::exitApplication,
    ) {
        LTreeBaker()
    }
}