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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakgof.app.resources.Res
import com.zakgof.app.resources.korender32
import com.zakgof.app.resources.menu
import com.zakgof.korender.examples.gltfviewer.GltfLibraryExample
import com.zakgof.korender.examples.infcity.InfiniteCity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

val folders = listOf(
    Folder("Demos", listOf(
        Case("City") { InfiniteCity() },
        Case("Night Earth") { NightEarth() },
    )),
    Folder("Terrain", listOf(
        Case("Heightmap terrain") { HeightmapTerrainExample() },
        Case("Procedural terrain") { ProcTerrainExample() },
    )),

    Folder("Models", listOf(
        Case("OBJ mesh") { ObjMeshExample() },
        Case("GLTF crowd") { GltfCrowdExample() },
        Case("GLTF library") { GltfLibraryExample() },
        Case("Animation blending") { AnimationBlendingExample() },
    )),
    Folder("Lighting", listOf(
        Case("PBR materials") { MetallicRoughnessExample() },
        Case("Shadows") { ShadowExample() },
        Case("Point lights") { LightsExample() },
    )),
    Folder("Texturing", listOf(
        Case("Stochastic Texturing") { StochasticTexturingExample() },
        Case("Detail Texturing") { DetailTexturingExample() },
        Case("Render to texture") { RenderToTextureExample() },
    )),
    Folder("Instancing", listOf(
        Case("Billboards") { InstancedBillboardsExample() },
        Case("Cubes") { InstancedCubesExample() },
    )),
    Folder("Effects", listOf(
        Case("Blur") { BlurExample() },
        Case("Effects") { EffectsExample() },
        Case("Decals") { DecalExample() },
        Case("Bloom") { BloomExample() },
        Case("SSR") { SsrExample() },
        Case("HBAO") { HbaoExample() },
    )),
    Folder("Gui", listOf(
        Case("Widgets") { GuiExample() },
    )),
)

data class Folder(
    val title: String,
    val pages: List<Case>,
)

data class Case(
    val title: String,
    val composable: @Composable () -> Unit,
)

@Composable
fun AppExample() {

    var isExpanded by remember { mutableStateOf(false) }
    val allPages = remember { folders.flatMap { it.pages } }
    var selectedOption by remember { mutableStateOf(allPages.first()) }
    var collapsedFolders by remember { mutableStateOf(emptySet<String>()) }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    val backgroundColor = Color(0xFF181818)
    val selectColor = Color(0xFF808080)
    val textColor = Color(0xFFFFFFFF)
    val folderColor = Color(0xFF808080)

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
                folders.forEach { folder ->
                    val collapsed = folder.title in collapsedFolders
                    Text(
                        modifier = Modifier
                            .clickable {
                                collapsedFolders = if (collapsed) {
                                    collapsedFolders - folder.title
                                } else {
                                    collapsedFolders + folder.title
                                }
                            }
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
                        text = folder.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = folderColor
                    )
                    if (!collapsed) {
                        folder.pages.forEach { option ->
                            Text(
                                modifier = Modifier
                                    .background(color = if (option == selectedOption) selectColor else backgroundColor)
                                    .clickable {
                                        selectedOption = option
                                    }
                                    .padding(start = 28.dp, top = 6.dp, bottom = 6.dp, end = 12.dp),
                                text = option.title,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                    }
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
