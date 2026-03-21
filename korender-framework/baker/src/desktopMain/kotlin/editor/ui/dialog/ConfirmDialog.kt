package editor.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.zakgof.korender.baker.editor.ui.widget.FancyButton
import com.zakgof.korender.baker.editor.ui.widget.FancyColumn
import editor.ui.Theme

@Composable
fun confirmDialog(title: String, text: String, onConfirm: () -> Unit): () -> Unit {

    var show by remember { mutableStateOf(false) }
    val openDialog = { show = true }

    if (show) {

        DialogWindow(
            title = title,
            onCloseRequest = { show = false },
            state = rememberDialogState(size = DpSize(Dp.Unspecified, Dp.Unspecified))
        ) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            FancyColumn(
                modifier = Modifier.background(Theme.background)
                    .padding(8.dp)
            ) {
                Text(text, style = Theme.label, modifier = Modifier.padding(24.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                                show = false
                                onConfirm()
                                true
                            } else {
                                false
                            }
                        }
                ) {
                    FancyButton(
                        "OK",
                        modifier = Modifier.padding(end = 24.dp)
                            .focusRequester(focusRequester)
                    ) {
                        show = false
                        onConfirm()
                    }
                    FancyButton("Cancel", modifier = Modifier.padding(start = 24.dp)) {
                        show = false
                    }
                }
            }
        }
    }
    return openDialog
}