package editor.ui

import androidx.compose.runtime.Composable
import com.zakgof.korender.Korender
import com.zakgof.korender.baker.resources.Res
import editor.state.StateHolder

@Composable
fun KorenderView(holder: StateHolder) {
    Korender({ Res.readBytes(it) }) {

    }
}