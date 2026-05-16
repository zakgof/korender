package editor.model

import editor.model.brush.Brush
import editor.model.brush.Group
import editor.model.entity.EntityInstance
import editor.model.entity.EntityModel
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

data class Model(
    val brushes: PersistentMap<String, Brush> = persistentMapOf(),
    val invisibleBrushes: Set<String> = setOf(),
    val groups: PersistentMap<String, Group> = persistentMapOf(),
    val brushGroups: PersistentMap<String, String> = persistentMapOf(),
    val materials: PersistentMap<String, Material> = persistentMapOf(Material.generic.id to Material.generic),
    val entityInstances: PersistentMap<String, EntityInstance> = persistentMapOf(),
    val entityModels: PersistentMap<String, EntityModel> = persistentMapOf(),
)
