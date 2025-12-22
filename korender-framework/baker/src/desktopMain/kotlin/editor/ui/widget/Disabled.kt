package editor.ui.widget

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.disabled(disabled: Boolean): Modifier =
    if (!disabled) this
    else
        this
            .alpha(0.5f)
            .focusProperties { canFocus = false }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    }
                }
            }