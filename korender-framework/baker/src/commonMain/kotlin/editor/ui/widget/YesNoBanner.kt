
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.minus
import com.zakgof.korender.baker.resources.plus
import editor.ui.Theme
import editor.ui.widget.IconButton

@Composable
fun YesNoBanner(text: String, onNo: () -> Unit = {}, onYes: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Theme.background)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = CenterVertically
    ) {

        Text(
            modifier = Modifier.weight(1f),
            text = text,
            textAlign = TextAlign.Center,
            color = Theme.light,
            fontSize = 14.sp
        )
        IconButton(Res.drawable.plus) { onYes() }
        IconButton(Res.drawable.minus) { onNo() }
    }
}