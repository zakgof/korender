package com.zakgof.korender.baker.editor.model.entity

import com.zakgof.korender.math.Transform
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class EntityInstance (
    val name: String,
    val modelId: EntityModel,
    val transform: Transform,
    val id: String = Uuid.generateV7().toHexDashString(),
)