package com.zakgof.korender.baker.editor.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Group (
    val name: String,
    val brushIds: Set<String>,
    val id: String = Uuid.generateV7().toString()
)
