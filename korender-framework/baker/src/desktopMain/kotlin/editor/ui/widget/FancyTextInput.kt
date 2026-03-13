package editor.ui.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import editor.ui.Theme

@Composable
fun FancyTextInput(value: String, modifier: Modifier = Modifier, validator: (String) -> Boolean = { true }, onValueChange: (String) -> Unit) {

    var text by remember(value) { mutableStateOf(value) }
    val invalid by remember { derivedStateOf { !validator(text) } }

    val border = if (invalid) Color.Red else Theme.medium
    BasicTextField(
        value = text,
        cursorBrush = SolidColor(border),
        modifier = modifier
            .padding(2.dp)
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .padding(4.dp)
            .onFocusEvent {
                if (!it.isFocused) {
                    text = value
                }
            },
        onValueChange = {
            text = it
            if (validator(it)) {
                onValueChange(it)
            }
        },
        textStyle = TextStyle(
            fontSize = 12.sp,
            color = Theme.light
        )
    )
}

@Composable
fun FancyClickToTextInput(value: String, textModifier: Modifier = Modifier, editorModifier: Modifier = Modifier, validator: (String) -> Boolean = { true }, onValueChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var editing by remember { mutableStateOf(false) }
    var editorWasActive by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (editing) {
            FancyTextInput(
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
                value,
                style = Theme.label.copy(textDecoration = TextDecoration.Underline),
                modifier = textModifier.clickable {
                    editing = true
                })
        }
    }
}


