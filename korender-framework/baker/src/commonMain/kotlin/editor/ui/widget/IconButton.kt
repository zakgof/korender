package editor.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import editor.ui.Theme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconButton(icon: DrawableResource, background: Color = Theme.light, onClick: () -> Unit) = Button(
    onClick = { onClick() },
    colors = ButtonDefaults.buttonColors(
        backgroundColor = background
    ),
    modifier = Modifier.size(32.dp),
    contentPadding = PaddingValues(0.dp)
) {
    Image(
        painterResource(icon),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
    )
}