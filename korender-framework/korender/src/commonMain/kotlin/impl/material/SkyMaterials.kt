package com.zakgof.korender.impl.material

import com.zakgof.korender.CubeTextureDeclaration
import com.zakgof.korender.MaterialContext
import com.zakgof.korender.TextureDeclaration
import com.zakgof.korender.impl.glgpu.ColorRGBGetter
import com.zakgof.korender.impl.glgpu.FloatGetter
import com.zakgof.korender.impl.glgpu.TextureGetter
import com.zakgof.korender.math.ColorRGB

internal class FastCloudSkyMaterial(
    val density: Float,
    val thickness: Float,
    val scale: Float,
    val rippleAmount: Float,
    val rippleScale: Float,
    val zenithColor: ColorRGB,
    val horizonColor: ColorRGB,
    val cloudLight: Float,
    val cloudDark: Float,
    block: MaterialContext.() -> Unit,
) : InternalSkyMaterial(
    "!shader/plugin/sky.fastcloud.frag",
    "density" to FloatGetter<FastCloudSkyMaterial> { it.density },
    "thickness" to FloatGetter<FastCloudSkyMaterial> { it.thickness },
    "scale" to FloatGetter<FastCloudSkyMaterial> { it.scale },
    "rippleamount" to FloatGetter<FastCloudSkyMaterial> { it.rippleAmount },
    "ripplescale" to FloatGetter<FastCloudSkyMaterial> { it.rippleScale },
    "zenithcolor" to ColorRGBGetter<FastCloudSkyMaterial> { it.zenithColor },
    "horizoncolor" to ColorRGBGetter<FastCloudSkyMaterial> { it.horizonColor },
    "cloudlight" to FloatGetter<FastCloudSkyMaterial> { it.cloudLight },
    "clouddark" to FloatGetter<FastCloudSkyMaterial> { it.cloudDark }
) {
    init {
        block.invoke(this)
    }
}

internal class StarrySkyMaterial(
    val colorness: Float,
    val density: Float,
    val speed: Float,
    val size: Float,
    block: MaterialContext.() -> Unit,
) : InternalSkyMaterial(
    "!shader/plugin/sky.starry.frag",
    "colorness" to FloatGetter<StarrySkyMaterial> { it.colorness },
    "density" to FloatGetter<StarrySkyMaterial> { it.density },
    "speed" to FloatGetter<StarrySkyMaterial> { it.speed },
    "size" to FloatGetter<StarrySkyMaterial> { it.size },
) {
    init {
        block.invoke(this)
    }
}

internal class CubeSkyMaterial(
    val cubeTexture: CubeTextureDeclaration,
    block: MaterialContext.() -> Unit,
) : InternalSkyMaterial(
    "!shader/plugin/sky.cube.frag",
    "cubeTexture" to TextureGetter<CubeSkyMaterial> { it.cubeTexture }
) {
    init {
        block.invoke(this)
    }
}

internal class TextureSkyMaterial(
    val texture: TextureDeclaration,
    block: MaterialContext.() -> Unit,
) : InternalSkyMaterial(
    "!shader/plugin/sky.texture.frag",
    "texture" to TextureGetter<TextureSkyMaterial> { it.texture }
) {
    init {
        block.invoke(this)
    }
}