package com.zakgof.korender.examples.gltfviewer

import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> FixedItemsDropdown(
    title: String,
    items: List<T>,
    label: (T) -> String,
    selected: T?,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    selected?.let {
        ExposedDropdownMenuBox(
            modifier = Modifier.width(200.dp),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                value = label(selected),
                onValueChange = {},
                readOnly = true,
                label = { Text(title) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.White,
                    backgroundColor = Color.DarkGray
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                items.forEach { item ->
                    DropdownMenuItem(onClick = {
                        onSelected(item)
                        expanded = false
                    }) {
                        Text(text = label(item))
                    }
                }
            }
        }
    }
}
