package com.zakgof.korender.baker.editor.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakgof.korender.math.ColorRGBA
import editor.model.Material
import editor.ui.Theme
import editor.ui.widget.toCompose
import editor.util.TextureImageCache

@Composable
fun MaterialWidget(material: Material, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            onClick()
        }
            .padding(1.dp)
    )
    {
        Text(
            modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = material.name,
            fontSize = 14.sp,
            color = Theme.light,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        material.colorTexture?.let {
            Image(
                bitmap = TextureImageCache.compose(material.colorTexture.path),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = ColorRGBA(material.baseColor).toCompose(),
                    blendMode = BlendMode.Modulate
                ),
                modifier = Modifier.size(36.dp, 36.dp)
            )
        } ?: Box(
            modifier = Modifier.size(36.dp, 36.dp)
                .background(ColorRGBA(material.baseColor).toCompose())
        )

    }

}