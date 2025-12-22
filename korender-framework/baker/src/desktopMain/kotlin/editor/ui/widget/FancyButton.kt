package com.zakgof.korender.baker.editor.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import editor.ui.Theme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun FancyButton(text: String? = null, icon: DrawableResource? = null, background: Color = Theme.light, onClick: () -> Unit) = Button(
    onClick = { onClick() },
    colors = ButtonDefaults.buttonColors(
        backgroundColor = background
    ),
    modifier = Modifier.height(24.dp),
    contentPadding = PaddingValues(4.dp)
) {
    icon?.let {
        Image(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
    text?.let {
        Text(text, color = Theme.dark, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
    }
}