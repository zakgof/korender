package com.zakgof.korender.gl

object VGL12 {

    /** Aliases for smooth points and lines.  */
    const val GL_ALIASED_POINT_SIZE_RANGE: Int = 0x846D
    const val GL_ALIASED_LINE_WIDTH_RANGE: Int = 0x846E
    const val GL_SMOOTH_POINT_SIZE_RANGE: Int = 0xB12
    const val GL_SMOOTH_POINT_SIZE_GRANULARITY: Int = 0xB13
    const val GL_SMOOTH_LINE_WIDTH_RANGE: Int = 0xB22
    const val GL_SMOOTH_LINE_WIDTH_GRANULARITY: Int = 0xB23

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_TEXTURE_BINDING_3D: Int = 0x806A

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev, and by the `pname` parameter of PixelStore.  */
    const val GL_PACK_SKIP_IMAGES: Int = 0x806B
    const val GL_PACK_IMAGE_HEIGHT: Int = 0x806C
    const val GL_UNPACK_SKIP_IMAGES: Int = 0x806D
    const val GL_UNPACK_IMAGE_HEIGHT: Int = 0x806E

    /**
     * Accepted by the `cap` parameter of Enable, Disable, and IsEnabled, by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev, and by the `target` parameter of TexImage3D, GetTexImage, GetTexLevelParameteriv, GetTexLevelParameterfv, GetTexParameteriv, and
     * GetTexParameterfv.
     */
    const val GL_TEXTURE_3D: Int = 0x806F

    /** Accepted by the `target` parameter of TexImage3D, GetTexLevelParameteriv, and GetTexLevelParameterfv.  */
    const val GL_PROXY_TEXTURE_3D: Int = 0x8070

    /** Accepted by the `pname` parameter of GetTexLevelParameteriv and GetTexLevelParameterfv.  */
    const val GL_TEXTURE_DEPTH: Int = 0x8071

    /** Accepted by the `pname` parameter of TexParameteriv, TexParameterfv, GetTexParameteriv, and GetTexParameterfv.  */
    const val GL_TEXTURE_WRAP_R: Int = 0x8072

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_MAX_3D_TEXTURE_SIZE: Int = 0x8073

    /** Accepted by the `format` parameter of DrawPixels, GetTexImage, ReadPixels, TexImage1D, and TexImage2D.  */
    const val GL_BGR: Int = 0x80E0
    const val GL_BGRA: Int = 0x80E1

    /**
     * Accepted by the `type` parameter of DrawPixels, ReadPixels, TexImage1D, TexImage2D, GetTexImage, TexImage3D, TexSubImage1D, TexSubImage2D,
     * TexSubImage3D, GetHistogram, GetMinmax, ConvolutionFilter1D, ConvolutionFilter2D, ConvolutionFilter3D, GetConvolutionFilter, SeparableFilter2D,
     * SeparableFilter3D, GetSeparableFilter, ColorTable, GetColorTable, TexImage4D, and TexSubImage4D.
     */
    const val GL_UNSIGNED_BYTE_3_3_2: Int = 0x8032
    const val GL_UNSIGNED_BYTE_2_3_3_REV: Int = 0x8362
    const val GL_UNSIGNED_SHORT_5_6_5: Int = 0x8363
    const val GL_UNSIGNED_SHORT_5_6_5_REV: Int = 0x8364
    const val GL_UNSIGNED_SHORT_4_4_4_4: Int = 0x8033
    const val GL_UNSIGNED_SHORT_4_4_4_4_REV: Int = 0x8365
    const val GL_UNSIGNED_SHORT_5_5_5_1: Int = 0x8034
    const val GL_UNSIGNED_SHORT_1_5_5_5_REV: Int = 0x8366
    const val GL_UNSIGNED_INT_8_8_8_8: Int = 0x8035
    const val GL_UNSIGNED_INT_8_8_8_8_REV: Int = 0x8367
    const val GL_UNSIGNED_INT_10_10_10_2: Int = 0x8036
    const val GL_UNSIGNED_INT_2_10_10_10_REV: Int = 0x8368

    /**
     * Accepted by the `cap` parameter of Enable, Disable, and IsEnabled, and by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev.
     */
    const val GL_RESCALE_NORMAL: Int = 0x803A

    /** Accepted by the `pname` parameter of LightModel*, and also by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_LIGHT_MODEL_COLOR_CONTROL: Int = 0x81F8

    /** Accepted by the `param` parameter of LightModel* when `pname` is  LIGHT_MODEL_COLOR_CONTROL.  */
    const val GL_SINGLE_COLOR: Int = 0x81F9
    const val GL_SEPARATE_SPECULAR_COLOR: Int = 0x81FA

    /**
     * Accepted by the `param` parameter of TexParameteri and TexParameterf, and by the `params` parameter of TexParameteriv and TexParameterfv,
     * when their `pname` parameter is TEXTURE_WRAP_S, TEXTURE_WRAP_T, or TEXTURE_WRAP_R.
     */
    const val GL_CLAMP_TO_EDGE: Int = 0x812F

    /** Accepted by the `pname` parameter of TexParameteri, TexParameterf, TexParameteriv, TexParameterfv, GetTexParameteriv, and GetTexParameterfv.  */
    const val GL_TEXTURE_MIN_LOD: Int = 0x813A
    const val GL_TEXTURE_MAX_LOD: Int = 0x813B
    const val GL_TEXTURE_BASE_LEVEL: Int = 0x813C
    const val GL_TEXTURE_MAX_LEVEL: Int = 0x813D

    /** Recommended maximum amounts of vertex and index data.  */
    const val GL_MAX_ELEMENTS_VERTICES: Int = 0x80E8
    const val GL_MAX_ELEMENTS_INDICES: Int = 0x80E9

    var gl: IGL12? = null
}
