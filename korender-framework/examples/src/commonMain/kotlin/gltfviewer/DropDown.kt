package com.zakgof.korender.examples.gltfviewer

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
                }
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
