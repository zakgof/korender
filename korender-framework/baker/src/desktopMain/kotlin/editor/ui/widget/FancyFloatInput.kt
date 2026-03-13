package editor.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.zakgof.korender.baker.editor.util.sanity
import editor.ui.Theme

@Composable
fun FancyFloatInput(value: Float?, modifier: Modifier = Modifier, validator: (Float) -> Boolean = { true }, onValueChange: (Float) -> Unit) {

    fun isInvalid(text: String): Boolean {
        val float = text.toFloatOrNull()
        return float == null || !validator(float)
    }

    var text by remember { mutableStateOf(value?.sanity() ?: "") }
    val invalid by remember { derivedStateOf { isInvalid(text) } }
    FancyTextInput(
        value = text,
        modifier = modifier.onFocusEvent {
            if (!it.isFocused) {
                text = value.toString()
            }
        },
        onValueChange = {
            text = it
            if (!isInvalid(it)) {
                onValueChange(it.toFloat())
            }
        },
        invalid = invalid
    )
}

@Composable
fun FancyClickToFloatInput(value: Float, textModifier: Modifier = Modifier,  editorModifier: Modifier = Modifier, validator: (Float) -> Boolean = { true }, onValueChange: (Float) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var editing by remember { mutableStateOf(false) }
    var editorWasActive by remember { mutableStateOf(false) }
    Box (
        modifier = Modifier.height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (editing) {
            FancyFloatInput(
                value,
                editorModifier
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                            editing = false
                            true
                        } else {
                            false
                        }
                    }
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            editorWasActive = true
                        } else if (editorWasActive) {
                            editorWasActive = false
                            editing = false
                        }
                    }, validator, onValueChange
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            Text(
                "" + value,
                style = Theme.label.copy(textDecoration = TextDecoration.Underline),
                modifier = textModifier
                    .clickable {
                        editing = true
                    }
            )
        }
    }
}

