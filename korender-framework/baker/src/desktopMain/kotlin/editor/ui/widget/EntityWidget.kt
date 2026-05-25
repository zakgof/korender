package editor.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakgof.korender.baker.resources.Res
import com.zakgof.korender.baker.resources.cube
import editor.cache.KorenderCache
import editor.model.entity.EntityModel
import editor.ui.Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun EntityWidget(entityModel: EntityModel, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(1.dp)
    )
    {
        val image by KorenderCache.modelSnap(entityModel).collectAsState()
        Image(
            painter = image?.let { BitmapPainter(it) } ?: painterResource(Res.drawable.cube),
            contentDescription = null,
            modifier = Modifier.size(36.dp, 36.dp)
        )
        Text(
            modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = entityModel.name,
            fontSize = 14.sp,
            color = Theme.light,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }

}