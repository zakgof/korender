package com.zakgof.korender.examples

import androidx.compose.runtime.Composable
import com.zakgof.app.resources.Res
import com.zakgof.korender.Korender
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun GltfExample() = Korender (appResourceLoader = { Res.readBytes(it) }) {
    Frame {
        Scene(gltfResource = "!gltf/box/Box.gltf")
    }
}