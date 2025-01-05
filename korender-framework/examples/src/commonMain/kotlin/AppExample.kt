package com.zakgof.korender.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AppExample() {
    val options = listOf(
        Demo("Feature showcase") { ShowcaseExample() },
        Demo("PBR metallic/roughness") { MetallicRoughnessExample() },
        Demo("Transparency") { TransparencyExample() },
        Demo("Dynamic meshes") { MeshesExample() },
        Demo(".obj file") { ObjFileExample() },
        Demo(".gltf scene") { GltfExample() },
        Demo("Point lights") { LightsExample() },
        Demo("Instanced billboards") { InstancedBillboardsExample() },
        Demo("Instanced meshes") { InstancedMeshesExample() },
        Demo("Shadow mapping") { ShadowExample() },
        Demo("Blur filter") { BlurExample() },
        Demo("FXAA filter") { FxaaExample() },
        Demo("Fireball effect") { FireBallExample() },
        Demo("Smoke effect") { SmokeExample() },
        Demo("GUI") { GuiExample() },
        Demo("Sky") { SkyExample() }
    )

    var selectedOption by remember { mutableStateOf(options.first()) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val backgroundColor = Color(0xFF181818)
    val selectColor = Color(0xFF808080)
    val textColor = Color(0xFFFFFFFF)
    Row (modifier = Modifier.background(backgroundColor)) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            scrollState.scrollBy(-delta)
                        }
                    }
                )
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Korender Demo",
                fontSize = 18.sp,
                color = textColor
            )
            options.map { option ->
                Text(
                    modifier = Modifier
                        .background(color = if (option == selectedOption) selectColor else backgroundColor)
                        .clickable { selectedOption = option }
                        .padding(12.dp, 6.dp),
                    text = option.title,
                    fontSize = 12.sp,
                    color = textColor
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            selectedOption.composable()
        }
    }
}

private data class Demo(
    val title: String,
    val composable: @Composable () -> Unit
)