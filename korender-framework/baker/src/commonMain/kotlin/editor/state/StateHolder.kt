package editor.state

import com.zakgof.korender.math.Vec3
import editor.model.Brush
import editor.model.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class StateHolder {
    private val _state = MutableStateFlow(State())
    private val _model = MutableStateFlow(Model())
    val state: StateFlow<State> = _state
    val model: StateFlow<Model> = _model

    fun setMouseMode(newNode: State.MouseMode) = _state.update { it.copy(mouseMode = newNode) }
    fun setGridScale(newScale: Float) = _state.update { it.copy(gridScale = newScale) }
    fun setProjectionScale(newScale: Float) = _state.update { it.copy(projectionScale = newScale) }
    fun setCreator(min: Vec3, max: Vec3) = _state.update { it.copy(creatorBrush = Brush(min, max)) }
    fun create() {
        _model.update { it.copy(brushes = it.brushes + state.value.creatorBrush) }
        _state.update { it.copy(
            creatorBrush = Brush(Vec3(-128f, -128f, -64f), Vec3(128f, 128f, 64f)),
            mouseMode = State.MouseMode.SELECT
        ) }
    }
}