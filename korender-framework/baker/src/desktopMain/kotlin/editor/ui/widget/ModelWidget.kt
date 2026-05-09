package com.zakgof.korender.baker.editor.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import editor.model.entity.EntityModel
import editor.ui.Theme

@Composable
fun EntityWidget(entityModel: EntityModel, selected: Boolean, onClick: () -> Unit) {
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
            text = entityModel.name,
            fontSize = 14.sp,
            color = Theme.light,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
//        material.colorTexture?.let {
//            Image(
//                bitmap = TextureImageCache.compose(material.colorTexture),
//                contentDescription = null,
//                colorFilter = ColorFilter.tint(
//                    color = material.baseColor,
//                    blendMode = BlendMode.Modulate
//                ),
//                modifier = Modifier.size(36.dp, 36.dp)
//            )
//        } ?: Box(
//            modifier = Modifier.size(36.dp, 36.dp)
//                .background(material.baseColor)
//        )

    }

}