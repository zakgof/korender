package editor.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import editor.ui.Theme
import editor.util.sanity

@Composable
fun FancyFloatInput(value: Float?, modifier: Modifier = Modifier, validator: (Float) -> Boolean = { true }, onValueChange: (Float) -> Unit) {

    fun isInvalid(text: String): Boolean {
        val float = text.toFloatOrNull()
        return float == null || !validator(float)
    }

    var text by remember { mutableStateOf(value?.sanity() ?: "") }
    FancyTextInput(
        value = text,
        modifier = modifier.onFocusEvent {
            if (!it.isFocused) {
                text = value?.sanity() ?: ""
            }
        },
        onValueChange = {8
            text = it
            if (!isInvalid(it)) {
                onValueChange(it.toFloat())
            }
        },
        validator = { !isInvalid(it) }
    )
}

@Composable
fun FancyClickToFloatInput(value: Float, textModifier: Modifier = Modifier,  editorModifier: Modifier = Modifier, validator: (Float) -> Boolean = { true }, onValueChange: (Float) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var editing by remember { mutableStateOf(false) }
    var editorWasActive by remember { mutableStateOf(false) }
    Box (
        modifier = Modifier.height(22.dp),
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
                "" + value.sanity(),
                style = Theme.label.copy(textDecoration = TextDecoration.Underline),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = textModifier
                    .clickable {
                        editing = true
                    }
            )
        }
    }
}

@Composable
fun LabeledFloatInput(label: String, labelW: Dp, value: Float?, validator: (Float) -> Boolean, onValueChanged: (Float) -> Unit) =
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = Theme.label, modifier = Modifier.width(labelW).wrapContentHeight(Alignment.CenterVertically))
        value?.let {
            FancyClickToFloatInput(
                value,
                validator = validator,
                editorModifier = Modifier.width(48.dp),
                textModifier = Modifier.width(36.dp),
                onValueChange = onValueChanged
            )
        } ?: Box(modifier = Modifier.height(22.dp))
    }

