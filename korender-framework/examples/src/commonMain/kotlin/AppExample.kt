package com.zakgof.korender.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakgof.app.resources.Res
import com.zakgof.app.resources.korender32
import com.zakgof.app.resources.menu
import com.zakgof.korender.examples.gltfviewer.GltfLibraryExample
import com.zakgof.korender.examples.infcity.InfiniteCity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

val pages = listOf(

    Case("Heightmap terrain") { HeightmapTerrainExample() },
    Case("Procedural terrain") { ProcTerrainExample() },
    Case("City demo") { InfiniteCity() },
    Case("PBR materials") { MetallicRoughnessExample() },
    Case("OBJ mesh") { ObjMeshExample() },
    Case("GLTF crowd") { GltfCrowdExample() },
    Case("GLTF library") { GltfLibraryExample() },
    Case("Shadows") { ShadowExample() },
    Case("Stochastic Texturing") { StochasticTexturingExample() },
    Case("Render to texture") { RenderToTextureExample() },
    Case("Point lights") { LightsExample() },
    Case("GUI") { GuiExample() },
    Case("Particles / Billboards") { InstancedBillboardsExample() },
    Case("Particles / Cubes") { InstancedCubesExample() },
    Case("Blur") { BlurExample() },
    Case("Effects") { EffectsExample() },
    Case("Decals") { DecalExample() },
    Case("Bloom") { BloomExample() },
    Case("SSR") { SsrExample() },
    Case("HBAO") { HbaoExample() },

    /*
    Case("Texture arrays") { TextureArrayExample() },
    Case("Multiple viewports") { MultipleViewportsExample() },
    Case("Sky") { SkyExample() },
    Case("Transparency") { TransparencyExample() },
    Case("Meshes") { MeshesExample() },
    Case("FXAA") { FxaaExample() },
    Case("CSM") { CSMExample() },
    Case("Env") { CaptureEnvExample() },
    Case("Capture frame") { CaptureFrameExample() },
    Case("Shapes") { BasicShapesExample() },
     */
)

@Composable
fun AppExample() {

    var isExpanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(pages.first()) }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    val backgroundColor = Color(0xFF181818)
    val selectColor = Color(0xFF808080)
    val textColor = Color(0xFFFFFFFF)

    @Composable
    fun MenuImage() = Image(
        painter = painterResource(Res.drawable.menu),
        contentDescription = "Expand/collapse",
        modifier = Modifier.padding(8.dp).size(24.dp).clickable {
            isExpanded = !isExpanded
        }
    )

    Row(
        modifier = Modifier.background(backgroundColor)
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
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
            if (isExpanded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.korender32),
                        contentDescription = "Korender",
                        modifier = Modifier.padding(4.dp).size(20.dp)
                    )
                    Text(
                        text = "Korender",
                        fontSize = 16.sp,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    MenuImage()
                }
                pages.map { option ->
                    Text(
                        modifier = Modifier
                            .background(color = if (option == selectedOption) selectColor else backgroundColor)
                            .clickable {
                                selectedOption = option
                            }
                            .padding(12.dp, 6.dp),
                        text = option.title,
                        fontSize = 12.sp,
                        color = textColor
                    )
                }
            } else {
                MenuImage()
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            selectedOption.composable()
        }
    }
}

data class Case(
    val title: String,
    val composable: @Composable () -> Unit,
)
