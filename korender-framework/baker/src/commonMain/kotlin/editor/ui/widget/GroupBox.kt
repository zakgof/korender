
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import editor.ui.Theme

@Composable
fun GroupBox(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .padding(start = 2.dp, top = 6.dp, end = 2.dp, bottom = 2.dp)
                .border(1.dp, Theme.medium)
                .padding(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            content()
        }

        Row(
            modifier = Modifier
                .padding(start = 12.dp)
                .background(Theme.background)
        ) {
            Text(
                title,
                fontSize = 10.sp,
                color = Theme.medium,
                modifier = Modifier.background(Theme.background).padding(horizontal = 4.dp)
            )
        }
    }
}