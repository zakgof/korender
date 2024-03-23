package com.zakgof.korender.examples

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppExample() {
    val options = listOf(
        Demo("QS", "") { QuickStartExample() },
        Demo("Filter", "") { FilterExample() },
        Demo("Fire", "") { FireExample() },
        Demo("IBB", "") { InstancedBillboardsExample() },
        Demo("IM", "") { InstancedMeshesExample() },
        Demo("Obj", "") { ObjFileExample() },
        Demo("Shadow", "") { ShadowExample() },
        Demo("Tex", "") { TexturingExample() },
        Demo("GUI", "") { GuiExample() },
        Demo("Sky", "") { SkyExample() }

        // "Shadow", "Sky")
    )

    var option by remember { mutableStateOf(options[0]) }
    Column {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier.horizontalScroll(scrollState)
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