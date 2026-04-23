package com.zakgof.korender.baker.editor.model

import editor.model.brush.Brush
import editor.model.brush.BrushMesher
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Group (
    val name: String,
    val brushes: Set<Brush>,
    val id: String = Uuid.generateV7().toString()
) {
    val mesh by lazy { BrushMesher.buildGroupMesh(id, brushes) }
}
