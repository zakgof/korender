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

        Demo("QS", "") { QuickStartExample() },
        Demo("Mesh", "") { MeshesExample() },
        Demo("Obj", "") { ObjFileExample() },
        Demo("IBB", "") { InstancedBillboardsExample() },
        Demo("IM", "") { InstancedMeshesExample() },
        Demo("HF", "") { HeightFieldExample() },
        Demo("Tex", "") { TexturingExample() },
        Demo("Shadow", "") { ShadowExample() },
        Demo("Filter", "") { FilterExample() },
        Demo("Fire", "") { FireExample() },
        Demo("GUI", "") { GuiExample() },
        Demo("Sky", "") { SkyExample() }
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
        option.composable()
    }
}

class Demo(val title: String, val description: String, val composable: @Composable () -> Unit)