package gl

object VGL14 {

    /** Accepted by the `pname` parameter of TexParameteri, TexParameterf, TexParameteriv, TexParameterfv, GetTexParameteriv, and GetTexParameterfv.  */
    const val GL_GENERATE_MIPMAP: Int = 0x8191

    /** Accepted by the `target` parameter of Hint, and by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_GENERATE_MIPMAP_HINT: Int = 0x8192

    /** Accepted by the `sfactor` and `dfactor` parameters of BlendFunc.  */
    const val GL_CONSTANT_COLOR: Int = 0x8001
    const val GL_ONE_MINUS_CONSTANT_COLOR: Int = 0x8002
    const val GL_CONSTANT_ALPHA: Int = 0x8003
    const val GL_ONE_MINUS_CONSTANT_ALPHA: Int = 0x8004

    /** Accepted by the `mode` parameter of BlendEquation.  */
    const val GL_FUNC_ADD: Int = 0x8006
    const val GL_MIN: Int = 0x8007
    const val GL_MAX: Int = 0x8008

    /** Accepted by the `mode` parameter of BlendEquation.  */
    const val GL_FUNC_SUBTRACT: Int = 0x800A
    const val GL_FUNC_REVERSE_SUBTRACT: Int = 0x800B

    /** Accepted by the `internalFormat` parameter of TexImage1D, TexImage2D, CopyTexImage1D and CopyTexImage2D.  */
    const val GL_DEPTH_COMPONENT16: Int = 0x81A5
    const val GL_DEPTH_COMPONENT24: Int = 0x81A6
    const val GL_DEPTH_COMPONENT32: Int = 0x81A7

    /** Accepted by the `pname` parameter of GetTexLevelParameterfv and GetTexLevelParameteriv.  */
    const val GL_TEXTURE_DEPTH_SIZE: Int = 0x884A

    /** Accepted by the `pname` parameter of TexParameterf, TexParameteri, TexParameterfv, TexParameteriv, GetTexParameterfv, and GetTexParameteriv.  */
    const val GL_DEPTH_TEXTURE_MODE: Int = 0x884B

    /** Accepted by the `pname` parameter of TexParameterf, TexParameteri, TexParameterfv, TexParameteriv, GetTexParameterfv, and GetTexParameteriv.  */
    const val GL_TEXTURE_COMPARE_MODE: Int = 0x884C
    const val GL_TEXTURE_COMPARE_FUNC: Int = 0x884D

    /**
     * Accepted by the `param` parameter of TexParameterf, TexParameteri, TexParameterfv, and TexParameteriv when the `pname` parameter is
     * TEXTURE_COMPARE_MODE.
     */
    const val GL_COMPARE_R_TO_TEXTURE: Int = 0x884E

    /** Accepted by the `pname` parameter of Fogi and Fogf.  */
    const val GL_FOG_COORDINATE_SOURCE: Int = 0x8450

    /** Accepted by the `param` parameter of Fogi and Fogf.  */
    const val GL_FOG_COORDINATE: Int = 0x8451
    const val GL_FRAGMENT_DEPTH: Int = 0x8452

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_CURRENT_FOG_COORDINATE: Int = 0x8453
    const val GL_FOG_COORDINATE_ARRAY_TYPE: Int = 0x8454
    const val GL_FOG_COORDINATE_ARRAY_STRIDE: Int = 0x8455

    /** Accepted by the `pname` parameter of GetPointerv.  */
    const val GL_FOG_COORDINATE_ARRAY_POINTER: Int = 0x8456

    /** Accepted by the `array` parameter of EnableClientState and DisableClientState.  */
    const val GL_FOG_COORDINATE_ARRAY: Int = 0x8457

    /** Accepted by the `pname` parameter of PointParameterfARB, and the `pname` of Get.  */
    const val GL_POINT_SIZE_MIN: Int = 0x8126
    const val GL_POINT_SIZE_MAX: Int = 0x8127
    const val GL_POINT_FADE_THRESHOLD_SIZE: Int = 0x8128
    const val GL_POINT_DISTANCE_ATTENUATION: Int = 0x8129

    /**
     * Accepted by the `cap` parameter of Enable, Disable, and IsEnabled, and by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev.
     */
    const val GL_COLOR_SUM: Int = 0x8458

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_CURRENT_SECONDARY_COLOR: Int = 0x8459
    const val GL_SECONDARY_COLOR_ARRAY_SIZE: Int = 0x845A
    const val GL_SECONDARY_COLOR_ARRAY_TYPE: Int = 0x845B
    const val GL_SECONDARY_COLOR_ARRAY_STRIDE: Int = 0x845C

    /** Accepted by the `pname` parameter of GetPointerv.  */
    const val GL_SECONDARY_COLOR_ARRAY_POINTER: Int = 0x845D

    /** Accepted by the `array` parameter of EnableClientState and DisableClientState.  */
    const val GL_SECONDARY_COLOR_ARRAY: Int = 0x845E

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_BLEND_DST_RGB: Int = 0x80C8
    const val GL_BLEND_SRC_RGB: Int = 0x80C9
    const val GL_BLEND_DST_ALPHA: Int = 0x80CA
    const val GL_BLEND_SRC_ALPHA: Int = 0x80CB

    /** Accepted by the `sfail`, `dpfail`, and `dppass` parameter of StencilOp.  */
    const val GL_INCR_WRAP: Int = 0x8507
    const val GL_DECR_WRAP: Int = 0x8508

    /** Accepted by the `target` parameters of GetTexEnvfv, GetTexEnviv, TexEnvi, TexEnvf, Texenviv, and TexEnvfv.  */
    const val GL_TEXTURE_FILTER_CONTROL: Int = 0x8500

    /**
     * When the `target` parameter of GetTexEnvfv, GetTexEnviv, TexEnvi, TexEnvf, TexEnviv, and TexEnvfv is TEXTURE_FILTER_CONTROL, then the value of
     * `pname` may be.
     */
    const val GL_TEXTURE_LOD_BIAS: Int = 0x8501

    /** Accepted by the `pname` parameters of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_MAX_TEXTURE_LOD_BIAS: Int = 0x84FD

    /**
     * Accepted by the `param` parameter of TexParameteri and TexParameterf, and by the `params` parameter of TexParameteriv and TexParameterfv,
     * when their `pname` parameter is TEXTURE_WRAP_S, TEXTURE_WRAP_T, or TEXTURE_WRAP_R.
     */
    const val GL_MIRRORED_REPEAT: Int = 0x8370

    var gl: IGL14? = null
}
