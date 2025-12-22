package com.zakgof.korender.baker

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.korender
import editor.state.StateHolder
import editor.ui.BrushEditor
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    val holder = remember { StateHolder() }
    val state by holder.state.collectAsState()
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource(Res.drawable.korender),
        title = "Korender Baker" + (state.savePath?.let {" - $it"} ?: "")
    ) {
        // LTreeBaker()
        // BillboardTreeBaker()
        // RadiantTreeBaker()
        BrushEditor(holder)
    }
}