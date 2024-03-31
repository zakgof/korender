package com.zakgof.korender.image


import com.zakgof.korender.getPlatform
import com.zakgof.korender.impl.material.Image
import com.zakgof.korender.impl.resourceStream

object Images {

    fun image(resource: String): Image = getPlatform().loadImage(resourceStream(resource))
}