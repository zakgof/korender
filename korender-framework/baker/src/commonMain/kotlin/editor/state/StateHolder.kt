package editor.state

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import com.zakgof.korender.math.Vec3
import editor.model.Brush
import editor.model.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.random.Random

class StateHolder {

    private val defaultCreator = Brush(
        Vec3(-128f, -128f, -64f),
        Vec3(128f, 128f, 64f),
        Color.Red,
        "creator",
        "creator"
    )

    private val _state = MutableStateFlow(State(creatorBrush = defaultCreator))
    private val _model = MutableStateFlow(Model())

    val state: StateFlow<State> = _state
    val model: StateFlow<Model> = _model

    fun setMouseMode(newNode: State.MouseMode) = _state.update { it.copy(mouseMode = newNode) }
    fun setGridScale(newScale: Float) = _state.update { it.copy(gridScale = newScale) }
    fun setProjectionScale(newScale: Float) = _state.update { it.copy(projectionScale = newScale) }
    fun setCreator(min: Vec3, max: Vec3) = _state.update {
        it.copy(
            creatorBrush = Brush(
                min,
                max,
                Color.Red,
                "creator",
                "creator"
            )
        )
    }

    private fun randomBrushColor(seed: Int): Color {
        val r = Random(seed)
        return Color(128 + r.nextInt(127), 128 + r.nextInt(127), 128 + r.nextInt(127))
    }

    fun create() {
        val newBrush = Brush(
            state.value.creatorBrush.min,
            state.value.creatorBrush.max,
            randomBrushColor(model.value.brushes.size),
            "Brush ${_model.value.brushes.size}",
            UUID.randomUUID().toString()
        )
        _model.update {
            it.copy(brushes = it.brushes + newBrush)
        }
        _state.update {
            it.copy(
                creatorBrush = defaultCreator,
                selectedBrush = newBrush,
                mouseMode = State.MouseMode.SELECT
            )
        }
    }

    fun resizeBrush(brush: Brush, min: Vec3, max: Vec3): Brush {
        val newBrush = brush.copy(min = min, max = max)
        _model.update {
            it.copy(brushes = it.brushes.map { el ->
                if (el === brush) newBrush else el
            })
        }
        if (brush === state.value.selectedBrush) {
            _state.update {
                it.copy(selectedBrush = newBrush, clipboard = null)
            }
        }
        return newBrush
    }

    fun selectBrush(newSelection: Brush?) {
        _state.update { it.copy(selectedBrush = newSelection) }
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
        _state.value.selectedBrush?.let { selection -> _state.update { it.copy(clipboard = selection) } }
    }

    fun paste() {
        _state.value.clipboard?.let { clipboard ->
            val newBrush = Brush(
                clipboard.min + _state.value.viewCenter - clipboard.center,
                clipboard.max + _state.value.viewCenter - clipboard.center,
                randomBrushColor(model.value.brushes.size),
                "Brush ${_model.value.brushes.size}",
                UUID.randomUUID().toString()
            )
            _model.update {
                it.copy(brushes = it.brushes + newBrush)
            }
            _state.update {
                it.copy(
                    selectedBrush = newBrush
                )
            }
        }
    }
}