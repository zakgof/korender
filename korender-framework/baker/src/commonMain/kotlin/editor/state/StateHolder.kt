package editor.state

import androidx.compose.ui.graphics.Color
import com.zakgof.korender.math.Vec3
import editor.model.Brush
import editor.model.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class StateHolder {

    private val defaultCreator = Brush(Vec3(-128f, -128f, -64f), Vec3(128f, 128f, 64f), Color.Red)

    private val _state = MutableStateFlow(State(creatorBrush = defaultCreator))
    private val _model = MutableStateFlow(Model())

    val state: StateFlow<State> = _state
    val model: StateFlow<Model> = _model

    fun setMouseMode(newNode: State.MouseMode) = _state.update { it.copy(mouseMode = newNode) }
    fun setGridScale(newScale: Float) = _state.update { it.copy(gridScale = newScale) }
    fun setProjectionScale(newScale: Float) = _state.update { it.copy(projectionScale = newScale) }
    fun setCreator(min: Vec3, max: Vec3) = _state.update { it.copy(creatorBrush = Brush(min, max, Color.Red)) }

    private fun randomBrushColor(seed: Int): Color {
        val r = Random(seed)
        return Color(128 + r.nextInt(127), 128 + r.nextInt(127), 128 + r.nextInt(127))
    }

    fun create() {
        val newBrush = Brush(
            state.value.creatorBrush.min,
            state.value.creatorBrush.max,
            randomBrushColor(model.value.brushes.size)
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
                it.copy(selectedBrush = newBrush)
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
}