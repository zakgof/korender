package editor.model

import editor.model.brush.Brush
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

data class Model(
    val brushes: PersistentMap<String, Brush> = persistentMapOf(),
    val materials: PersistentMap<String, Material> = persistentMapOf(Material.generic.id to Material.generic)
)
