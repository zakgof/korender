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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AppExample() {
    val options = listOf(
        Demo("QS", "QuickStart: simple sphere with dynamic color and position") { QuickStartExample() },
        Demo("Mesh", "Demonstration of static and dynamic custom meshes") { MeshesExample() },
        Demo("Obj", "Loading a textured obj file") { ObjFileExample() },
        Demo("IBB", "Particles system using instanced billboards") { InstancedBillboardsExample() },
        Demo("IM", "Instanced meshes") { InstancedMeshesExample() },
        Demo("HF", "Heightfield") { HeightFieldExample() },
        Demo("Shadow", "Shadow mapping") { ShadowExample() },
        Demo("Filter", "Custom filter") { FilterExample() },
        Demo("Fire", "Simple fire effect") { FireExample() },
        Demo("FBall", "Fireball effect") { FireBallExample() },
        Demo("GUI", "On-screen GUI") { GuiExample() },
        Demo("Sky", "Simple sky") { SkyExample() }
    )

    var option by remember { mutableStateOf(options[0]) }
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
        Text(text = option.description)
        option.composable()
    }
}

private class Demo(val title: String, val description: String, val composable: @Composable () -> Unit)