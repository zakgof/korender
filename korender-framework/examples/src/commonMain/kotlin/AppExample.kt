package com.zakgof.korender.examples

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AppExample() {
    val options = listOf(
        Demo("Show", "Features showcase") { ShowcaseExample() },
        Demo("Trans", "Transparent objects") { TransparencyExample() },
        Demo("Mesh", "Demonstration of static and dynamic custom meshes") { MeshesExample() },
        Demo("Obj", "Loading a textured obj file") { ObjFileExample() },
        Demo("IBB", "Particles system using instanced billboards") { InstancedBillboardsExample() },
        Demo("IM", "Instanced meshes") { InstancedMeshesExample() },
        Demo("HF", "Heightfield") { HeightFieldExample() },
        Demo("Shadow", "Shadow mapping") { ShadowExample() },
        Demo("Adj", "Post processing saturation adjustment filter") { FilterExample() },
        Demo("Blur", "Post processing two-step blur filter") { BlurExample() },
        Demo("FXAA", "Post processing FXAA filter") { FxaaExample() },
        Demo("FBall", "Fireball effect") { FireBallExample() },
        Demo("Smoke", "Smoke effect") { SmokeExample() },
        Demo("GUI", "On-screen GUI") { GuiExample() },
        Demo("Sky", "Simple sky") { SkyExample() },
        Demo("Plg", "Shader plugin") { ShaderPluginExample() },
        Demo("Gltf", "Gltf scene loading") { GltfExample() }
    )

    var option by remember { mutableStateOf(options[1]) }
    Column {
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            scrollState.scrollBy(-delta)
                        }
                    }
                )
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            options.map {
                Button(onClick = { option = it }) {
                    Text(it.title)
                }
            }
        }
        Text(text = option.description, color = Color.Cyan)
        option.composable()
    }
}

private class Demo(
    val title: String,
    val description: String,
    val composable: @Composable () -> Unit
)