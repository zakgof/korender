package editor.model

import com.zakgof.korender.baker.editor.model.Group
import editor.model.brush.Brush
import editor.model.brush.BrushMesh
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

data class Model(
    val brushes: PersistentMap<String, Brush> = persistentMapOf(),
    val invisibleBrushes: Set<String> = setOf(),
    val groups: PersistentMap<String, Group> = persistentMapOf(),
    val brushGroups: PersistentMap<String, String> = persistentMapOf(),
    val materials: PersistentMap<String, Material> = persistentMapOf(Material.generic.id to Material.generic)
) {
    fun brushMeshes(): List<BrushMesh> =
        brushes.values.filter { brush -> !invisibleBrushes.contains(brush.id) && !groups.containsKey(brush.id) }
            .map { brush -> brush.mesh } + groups.values.map { group -> group.mesh }
}
