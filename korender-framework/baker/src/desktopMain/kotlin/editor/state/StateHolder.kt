package editor.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import com.zakgof.korender.baker.editor.model.Group
import com.zakgof.korender.baker.editor.util.ModelCompiler
import com.zakgof.korender.math.Vec3
import com.zakgof.korender.math.y
import com.zakgof.korender.math.z
import editor.model.BoundingBox
import editor.model.Material
import editor.model.Model
import editor.model.ModelDto
import editor.model.brush.Brush
import editor.model.snap
import editor.state.State.MouseMode
import editor.util.TextureImageCache
import kotlinx.collections.immutable.PersistentMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.File
import kotlin.math.atan
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.tan
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class)
class StateHolder {

    private fun defaultState(mouseMode: MouseMode = MouseMode.CREATOR, modelHash: Int) = State(
        projectionScale = 32f,
        gridScale = 0.5f,
        creator = defaultCreator(grid = 0.5f),
        mouseMode = mouseMode,
        lastSavedModelHash = modelHash
    )

    private fun defaultCreator(center: Vec3 = Vec3.ZERO, grid: Float = state.value.gridScale): BoundingBox {
        val snappedCenter = center.snap(grid)
        val halfSize = Vec3(3f, 1f, 2f) * grid
        return BoundingBox(
            snappedCenter - halfSize,
            snappedCenter + halfSize
        )
    }

    private val _model = MutableStateFlow(Model())
    private val _state = MutableStateFlow(defaultState(MouseMode.CREATOR, System.identityHashCode(_model.value)))

    val state: StateFlow<State> = _state
    val model: StateFlow<Model> = _model

    fun setMouseMode(newMode: State.MouseMode) {
        if (newMode == State.MouseMode.CREATOR) {
            resetCreator()
        }
        _state.update {
            it.copy(mouseMode = newMode)
        }
    }

    fun setGridScale(newScale: Float) = _state.update { it.copy(gridScale = newScale.coerceIn(0.1f, 10000f)) }

    fun setProjectionScale(newScale: Float) {

        _state.update { it.copy(projectionScale = newScale.coerceIn(0.1f, 10000f)) }

        val width = min(UiState.viewSize["top"]!!.first, UiState.viewSize["top"]!!.second)
        val cells = width / (state.value.gridScale * state.value.projectionScale)

        if (cells !in 5f..50f) {
            _state.update { it.copy(gridScale = (width / (30f * state.value.projectionScale)).floor2()) }
        }
    }

    fun setCreator(min: Vec3, max: Vec3) = _state.update {
        it.copy(creator = BoundingBox(min, max))
    }

    fun resetCreator() = _state.update {
        it.copy(creator = defaultCreator(state.value.viewCenter, state.value.gridScale))
    }

    private fun randomBrushColor(seed: Int): Color {
        val r = Random(seed)
        return Color(128 + r.nextInt(127), 128 + r.nextInt(127), 128 + r.nextInt(127))
    }

    fun create() {
        val bb = state.value.creator
        val newBrush = Brush(
            "Brush ${_model.value.brushes.size}",
            randomBrushColor(model.value.brushes.size),
            bb,
            state.value.materialId
        )
        _model.update {
            it.copy(brushes = it.brushes.put(newBrush.id, newBrush))
        }
        _state.update {
            it.copy(
                creator = defaultCreator(state.value.viewCenter),
                selection = setOf(newBrush.id),
                mouseMode = State.MouseMode.SELECT
            )
        }
    }

    fun brushChanged(brush: Brush) {
        _model.update {
            it.copy(brushes = it.brushes.put(brush.id, brush))
        }
    }

    fun setViewCenter(newCenter: Vec3) {
        _state.update { it.copy(viewCenter = newCenter) }
    }

    fun keyDown(key: Key) {
        _state.update { it.copy(pressedKeys = it.pressedKeys + key) }
    }

    fun keyUp(key: Key) {
        _state.update { it.copy(pressedKeys = it.pressedKeys - key) }
    }

    fun frame(dt: Float) {
        if (_state.value.pressedKeys.contains(Key.W)) {
            _state.update { it.copy(camera = it.camera.forward(dt)) }
        }
        if (_state.value.pressedKeys.contains(Key.S)) {
            _state.update { it.copy(camera = it.camera.forward(-dt)) }
        }
        if (_state.value.pressedKeys.contains(Key.A)) {
            _state.update { it.copy(camera = it.camera.right(-dt)) }
        }
        if (_state.value.pressedKeys.contains(Key.D)) {
            _state.update { it.copy(camera = it.camera.right(dt)) }
        }
    }

    fun copy() {
        _state.update { it.copy(clipboard = selectedBrushes()) }
    }

    fun cut() {
        val brushes = selectedBrushes()
        _model.update {
            it.copy(
                brushes = it.brushes.removeAll(state.value.selection)
            )
        }
        _state.update {
            it.copy(
                clipboard = brushes,
                selection = setOf()
            )
        }
    }

    private fun selectedBrushes(): Set<Brush> = model.value.brushes.values.filter { brush -> state.value.selection.contains(brush.id) }.toSet()

    @OptIn(ExperimentalUuidApi::class)
    fun paste() {
        if (state.value.clipboard.isEmpty())
            return
        val clipboardBoundingBox = state.value.clipboard
            .map { it.bb }
            .reduce(BoundingBox::merge)
        val offset = state.value.viewCenter - clipboardBoundingBox.center
        val newBrushes = state.value.clipboard.mapIndexed { index, brush ->
            brush.copy(
                name = generateBrushName(brush.name),
                id = Uuid.generateV7().toHexDashString(),
                projectionColor = randomBrushColor(model.value.brushes.size + index).toArgb(),
                faces = brush.faces.map {
                    it.copy(plane = it.plane.translate(offset))
                }
            )
        }
        _model.update { it.copy(brushes = it.brushes.putAll(newBrushes.associateBy { nb -> nb.id })) }
        _state.update { it.copy(selection = newBrushes.map { nb -> nb.id }.toSet()) }
    }

    fun deleteSelected() {
        val groupIds = state.value.selection.mapNotNull { model.value.brushGroups[it] }
        _model.update {
            it.copy(
                brushes = it.brushes.removeAll(state.value.selection),
                brushGroups = it.brushGroups.removeAll(state.value.selection),
                groups = it.groups.removeAll(groupIds)
            )
        }
        _state.update {
            it.copy(selection = setOf())
        }
    }

    fun rotateSelectionModes() {
        _state.update {
            val modes = State.SelectionMode.entries
            it.copy(selectionMode = modes[(modes.indexOf(it.selectionMode) + 1) % modes.size])
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selection = setOf()) }
    }

    fun selectBrushes(brushIds: Set<String>, flip: Boolean, allGroup: Boolean) {
        _state.update { state ->
            val newSelection = if (flip) {
                val s = LinkedHashSet(state.selection)
                for (brushId in brushIds) {
                    if (brushId in state.selection)
                        s -= brushId
                    else
                        s += brushId
                }
                s
            } else {
                brushIds
            }
            val result = if (allGroup) {
                newSelection +
                        newSelection.mapNotNull { model.value.brushGroups[it] }
                            .flatMap { model.value.groups[it]!!.brushIds }
                            .toSet()
            } else {
                newSelection -
                        newSelection.mapNotNull { model.value.brushGroups[it] }
                            .map { model.value.groups[it]!!.brushIds }
                            .filter { !newSelection.containsAll(it) }
                            .flatten()
                            .toSet()
            }
            state.copy(selection = result)
        }
    }

    fun selectMaterial(material: Material) {
        _state.update { it.copy(materialId = material.id) }
    }

    fun createMaterial() {
        val material = Material(name = generateMaterialName())
        addMaterial(material)
    }

    fun addMaterial(material: Material) {
        _model.update { it.copy(materials = it.materials.put(material.id, material)) }
        selectMaterial(material)
    }

    fun updateMaterial(material: Material) {
        val oldMaterial = model.value.materials[state.value.materialId]!!
        TextureImageCache.dispose(oldMaterial.colorTexture?.path ?: "")
        _model.update {
            it.copy(materials = it.materials.put(material.id, material))
        }
    }

    fun deleteMaterial() {
        val oldMaterial = model.value.materials[state.value.materialId]!!
        TextureImageCache.dispose(oldMaterial.colorTexture?.path ?: "")
        // TODO : need to replace existing material references to Generic !
        _model.update {
            it.copy(materials = it.materials.remove(state.value.materialId))
        }
        _state.update {
            it.copy(materialId = Material.generic.id)
        }
    }

    fun applyMaterialToSelection() {
        val materialId = state.value.materialId
        modifySelectedBrushes { brush -> brush.copy(faces = brush.faces.map { it.copy(materialId = materialId) }) }
    }

    fun newProject() {
        _model.update { Model() }
        _state.update { defaultState(State.MouseMode.CREATOR, System.identityHashCode(_model.value)) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadProject(path: String) {
        val bytes = File(path).readBytes()
        val modelDto: ModelDto = Cbor.decodeFromByteArray(bytes)
        _model.update { modelDto.toModel() }
        _state.update { defaultState(MouseMode.SELECT, System.identityHashCode(_model.value)) }
        resetViews()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun saveProject(path: String) {
        val bytes = Cbor.encodeToByteArray(ModelDto(model.value))
        File(path).writeBytes(bytes)
        _state.update { it.copy(
            savePath = path,
            lastSavedModelHash = System.identityHashCode(model.value)
        ) }
    }

    fun applyTexturingUScaleToSelection(uScale: Float) {
        modifySelectedBrushes { brush -> brush.copy(faces = brush.faces.map { it.copy(texturing = it.texturing.copy(u = it.texturing.u.copy(scale = uScale))) }) }
    }

    fun applyTexturingUOffsetToSelection(uOffset: Float) {
        modifySelectedBrushes { brush -> brush.copy(faces = brush.faces.map { it.copy(texturing = it.texturing.copy(u = it.texturing.u.copy(offset = uOffset))) }) }
    }

    fun applyTexturingVScaleToSelection(vScale: Float) {
        modifySelectedBrushes { brush -> brush.copy(faces = brush.faces.map { it.copy(texturing = it.texturing.copy(v = it.texturing.v.copy(scale = vScale))) }) }
    }

    fun applyTexturingVOffsetToSelection(vOffset: Float) {
        modifySelectedBrushes { brush -> brush.copy(faces = brush.faces.map { it.copy(texturing = it.texturing.copy(v = it.texturing.v.copy(offset = vOffset))) }) }
    }

    fun applyTexturingWorldScaleToSelection(newValue: Boolean) {
        modifySelectedBrushes { brush -> brush.copy(faces = brush.faces.map { it.copy(texturing = it.texturing.copy(worldScale = newValue)) }) }
    }

    fun setLastTextureDir(directory: String) {
        _state.update { it.copy(lastTextureDir = directory) }
    }

    fun zoomOnSelection() {
        if (state.value.selection.isNotEmpty()) {
            val bb = selectedBrushes()
                .map { it.bb }
                .reduce(BoundingBox::merge)

            _state.update { it.copy(viewCenter = bb.center) }
            // TODO: zoom too ?
        }
    }

    fun selectViaRay(look: Vec3, flip: Boolean) {
        val brush = model.value.brushes.values.firstOrNull { brush ->
            brush.intersectRayBrush(state.value.camera.position, look) != null
        }
        brush?.let {
            selectBrushes(setOf(brush.id), flip, true)
        } ?: clearSelection()
    }

    fun carve(): Boolean {
        val by = selectedBrushes()
        val target = model.value.brushes.values - by
        val carving = Brush.carve(target, by.first(), state.value.materialId)

        if (carving.isEmpty())
            return false

        var brushes = model.value.brushes
        var groups = model.value.groups
        var brushGroups = model.value.brushGroups

        carving.forEach { (old, new) ->
            val newBrushIds = new.map { it.id }.toSet()
            brushes = brushes.putAll(new.associateBy { it.id }).remove(old.id)
            val oldGroupId = brushGroups[old.id]
            if (oldGroupId != null) {
                val oldGroup = groups[oldGroupId]!!
                groups = groups.put(oldGroupId, oldGroup.copy(brushIds = oldGroup.brushIds + newBrushIds - old.id))
                brushGroups = brushGroups.putAll(newBrushIds.associateWith { oldGroupId }).remove(old.id)
            } else {
                val newGroup = Group(old.name, newBrushIds)
                groups = groups.put(newGroup.id, newGroup)
                brushGroups = brushGroups.putAll(newBrushIds.associateWith { newGroup.id })
            }
        }

        _model.update {
            it.copy(
                brushes = brushes,
                groups = groups,
                brushGroups = brushGroups
            )
        }
        return true
    }


    private fun modifySelectedBrushes(mutator: (Brush) -> Brush) {
        val selection = state.value.selection
        if (selection.isEmpty()) return
        _model.update { model ->
            var brushes = model.brushes
            model.brushes.forEach { (id, brush) ->
                if (id in selection) brushes = brushes.put(id, mutator(brush))
            }
            model.copy(brushes = brushes)
        }
    }

    private fun generateMaterialName(): String {
        val existing = model.value.materials.values.map { it.name }
            .filter { it.startsWith("Material") }
            .toSet()
        var i = 0
        while ("Material $i" in existing) {
            i++
        }
        return "Material $i"
    }

    private fun generateBrushName(name: String): String {
        val parts = Regex("^(.*?) (\\d+)$").matchEntire(name)?.groupValues
        val base = parts?.get(1) ?: name
        val num = parts?.get(2)?.toInt() ?: 0

        val existing = model.value.brushes.values.map { it.name }
            .filter { it.startsWith(base) }
            .toSet()
        var i = num
        while ("$base $i" in existing) {
            i++
        }
        return "$base $i"
    }

    fun dryRun() {
        val sceneModel = ModelCompiler.compile(model.value)
        _state.update { it.copy(lastCompiledSceneModel = sceneModel) }
    }

    fun selectAll() {
        _state.update { it.copy(selection = model.value.brushes.keys) }
    }

    fun setCamera(position: Vec3, direction: Vec3, up: Vec3) {
        _state.update { it.copy(camera = State.Camera(position, direction, up)) }
    }

    fun resetViews() {

        if (model.value.brushes.isEmpty()) {
            _state.update {
                it.copy(
                    projectionScale = defaultState(State.MouseMode.SELECT, System.identityHashCode(_model.value)).projectionScale,
                    viewCenter = Vec3.ZERO
                )
            }

        } else {

            val bb = model.value.brushes.values
                .map { it.bb }
                .reduce(BoundingBox::merge)


            val widthX = min(UiState.viewSize["top"]!!.first, UiState.viewSize["front"]!!.first)
            val widthY = min(UiState.viewSize["left"]!!.second, UiState.viewSize["front"]!!.second)
            val widthZ = min(UiState.viewSize["left"]!!.first, UiState.viewSize["top"]!!.second)

            val minX = widthX / bb.size.x
            val minY = widthY / bb.size.y
            val minZ = widthZ / bb.size.z

            val scale = (minOf(minX, minY, minZ) * 0.9f).floorSig(2)
            val grid = (minOf(widthX, widthY, widthZ) / (30f * scale)).floor2()

            val aspect = UiState.viewSize["korender"]!!.first / UiState.viewSize["korender"]!!.second
            val fovY = 2f * atan(5f * 0.5f / 10f)

            val dY = (bb.size.y * 0.5f) / tan(fovY * 0.5f)
            val dX = (bb.size.x * 0.5f) / (tan(fovY * 0.5f) * aspect)
            val distance = maxOf(dX, dY, bb.max.z - bb.center.z + 10f)

            val cameraPos = bb.center + distance.z

            _state.update {
                it.copy(
                    projectionScale = scale,
                    gridScale = grid,
                    viewCenter = bb.center,
                    camera = State.Camera(cameraPos, -1.z, 1.y)
                )
            }
        }

    }

    fun Float.floorSig(digits: Int): Float {
        val s = 10f.pow(floor(log10(this)).toInt() - (digits - 1))
        return floor(this / s) * s
    }

    fun Float.floor2(): Float {
        val bits = toBits()
        val exp = (bits ushr 23) and 0xff
        return Float.fromBits(exp shl 23)
    }

    fun viewResized(name: String, width: Int, height: Int) {
        UiState.viewSize[name] = width to height
    }

    fun groupSelection() {
        val brushIds = state.value.selection
        val existingGroupsIds = brushIds.mapNotNull { model.value.brushGroups[it] }
            .toSet()
        val existingGroupBrushIds = existingGroupsIds.mapNotNull { model.value.groups[it] }
            .flatMap { it.brushIds }
            .toSet()
        val newGroup = Group("Group", brushIds)
        val newBrushGroupMappings = brushIds.associateWith { newGroup.id }
        _model.update {
            it.copy(
                groups = it.groups.removeAll(existingGroupsIds).put(newGroup.id, newGroup),
                brushGroups = it.brushGroups.removeAll(existingGroupBrushIds).putAll(newBrushGroupMappings)
            )
        }
    }

    fun ungroupSelection() {
        val brushIds = state.value.selection
        val groupsIds = brushIds.mapNotNull { model.value.brushGroups[it] }
        _model.update {
            it.copy(
                groups = it.groups.removeAll(groupsIds),
                brushGroups = it.brushGroups.removeAll(brushIds)
            )
        }
    }

    fun renameGroup(groupId: String, newName: String) {
        _model.update { it.copy(groups = it.groups.put(groupId, it.groups[groupId]!!.copy(name = newName))) }
    }

    fun hideSelection() {
        _model.update { it.copy(invisibleBrushes = it.invisibleBrushes + state.value.selection) }
        clearSelection()
    }

    fun unhideSelection() {
        _model.update { it.copy(invisibleBrushes = it.invisibleBrushes - state.value.selection) }
    }
}

fun <K, V> PersistentMap<K, V>.removeAll(keys: Collection<K>): PersistentMap<K, V> {
    var res = this
    keys.forEach {
        res = res.remove(it)
    }
    return res
}
