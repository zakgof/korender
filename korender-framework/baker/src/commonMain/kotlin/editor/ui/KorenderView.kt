package editor.ui

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import editor.model.StateModel

@Composable
fun KorenderView(stateModel: StateModel) {
    Korender({ Res.readBytes(it) }) {

    }
}