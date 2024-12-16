package com.zakgof.korender.image


import com.zakgof.korender.getPlatform
import com.zakgof.korender.impl.resourceBytes

object Images {
    fun image(resource: String): Image = getPlatform().loadImage(resourceBytes(resource))
}