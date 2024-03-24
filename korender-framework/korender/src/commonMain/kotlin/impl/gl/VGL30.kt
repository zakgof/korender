package com.zakgof.korender.impl.gl

object VGL30 {

    /** GetTarget  */
    const val GL_MAJOR_VERSION: Int = 0x821B
    const val GL_MINOR_VERSION: Int = 0x821C
    const val GL_NUM_EXTENSIONS: Int = 0x821D
    const val GL_CONTEXT_FLAGS: Int = 0x821E
    const val GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT: Int = 0x1

    /** Renamed tokens.  */
    val GL_COMPARE_REF_TO_TEXTURE: Int = com.zakgof.korender.impl.gl.VGL14.GL_COMPARE_R_TO_TEXTURE
    val GL_CLIP_DISTANCE0: Int = com.zakgof.korender.impl.gl.VGL11.GL_CLIP_PLANE0
    val GL_CLIP_DISTANCE1: Int = com.zakgof.korender.impl.gl.VGL11.GL_CLIP_PLANE1
    val GL_CLIP_DISTANCE2: Int = com.zakgof.korender.impl.gl.VGL11.GL_CLIP_PLANE2
    val GL_CLIP_DISTANCE3: Int = com.zakgof.korender.impl.gl.VGL11.GL_CLIP_PLANE3
    val GL_CLIP_DISTANCE4: Int = com.zakgof.korender.impl.gl.VGL11.GL_CLIP_PLANE4
    val GL_CLIP_DISTANCE5: Int = com.zakgof.korender.impl.gl.VGL11.GL_CLIP_PLANE5
    const val GL_CLIP_DISTANCE6: Int = 0x3006
    const val GL_CLIP_DISTANCE7: Int = 0x3007
    val GL_MAX_CLIP_DISTANCES: Int = com.zakgof.korender.impl.gl.VGL11.GL_MAX_CLIP_PLANES
    val GL_MAX_VARYING_COMPONENTS: Int = com.zakgof.korender.impl.gl.VGL20.GL_MAX_VARYING_FLOATS

    /** Accepted by the `pname` parameters of GetVertexAttribdv, GetVertexAttribfv, GetVertexAttribiv, GetVertexAttribIuiv and GetVertexAttribIiv.  */
    const val GL_VERTEX_ATTRIB_ARRAY_INTEGER: Int = 0x88FD

    /** Returned by the `type` parameter of GetActiveUniform.  */
    const val GL_SAMPLER_1D_ARRAY: Int = 0x8DC0
    const val GL_SAMPLER_2D_ARRAY: Int = 0x8DC1
    const val GL_SAMPLER_1D_ARRAY_SHADOW: Int = 0x8DC3
    const val GL_SAMPLER_2D_ARRAY_SHADOW: Int = 0x8DC4
    const val GL_SAMPLER_CUBE_SHADOW: Int = 0x8DC5
    const val GL_UNSIGNED_INT_VEC2: Int = 0x8DC6
    const val GL_UNSIGNED_INT_VEC3: Int = 0x8DC7
    const val GL_UNSIGNED_INT_VEC4: Int = 0x8DC8
    const val GL_INT_SAMPLER_1D: Int = 0x8DC9
    const val GL_INT_SAMPLER_2D: Int = 0x8DCA
    const val GL_INT_SAMPLER_3D: Int = 0x8DCB
    const val GL_INT_SAMPLER_CUBE: Int = 0x8DCC
    const val GL_INT_SAMPLER_1D_ARRAY: Int = 0x8DCE
    const val GL_INT_SAMPLER_2D_ARRAY: Int = 0x8DCF
    const val GL_UNSIGNED_INT_SAMPLER_1D: Int = 0x8DD1
    const val GL_UNSIGNED_INT_SAMPLER_2D: Int = 0x8DD2
    const val GL_UNSIGNED_INT_SAMPLER_3D: Int = 0x8DD3
    const val GL_UNSIGNED_INT_SAMPLER_CUBE: Int = 0x8DD4
    const val GL_UNSIGNED_INT_SAMPLER_1D_ARRAY: Int = 0x8DD6
    const val GL_UNSIGNED_INT_SAMPLER_2D_ARRAY: Int = 0x8DD7

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_MIN_PROGRAM_TEXEL_OFFSET: Int = 0x8904
    const val GL_MAX_PROGRAM_TEXEL_OFFSET: Int = 0x8905

    /** Accepted by the `mode` parameter of BeginConditionalRender.  */
    const val GL_QUERY_WAIT: Int = 0x8E13
    const val GL_QUERY_NO_WAIT: Int = 0x8E14
    const val GL_QUERY_BY_REGION_WAIT: Int = 0x8E15
    const val GL_QUERY_BY_REGION_NO_WAIT: Int = 0x8E16

    /** Accepted by the `access` parameter of MapBufferRange.  */
    const val GL_MAP_READ_BIT: Int = 0x1
    const val GL_MAP_WRITE_BIT: Int = 0x2
    const val GL_MAP_INVALIDATE_RANGE_BIT: Int = 0x4
    const val GL_MAP_INVALIDATE_BUFFER_BIT: Int = 0x8
    const val GL_MAP_FLUSH_EXPLICIT_BIT: Int = 0x10
    const val GL_MAP_UNSYNCHRONIZED_BIT: Int = 0x20

    /** Accepted by the `pname` parameter of GetBufferParameteriv.  */
    const val GL_BUFFER_ACCESS_FLAGS: Int = 0x911F
    const val GL_BUFFER_MAP_LENGTH: Int = 0x9120
    const val GL_BUFFER_MAP_OFFSET: Int = 0x9121

    /** Accepted by the `target` parameter of ClampColor and the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_CLAMP_VERTEX_COLOR: Int = 0x891A
    const val GL_CLAMP_FRAGMENT_COLOR: Int = 0x891B
    const val GL_CLAMP_READ_COLOR: Int = 0x891C

    /** Accepted by the `clamp` parameter of ClampColor.  */
    const val GL_FIXED_ONLY: Int = 0x891D

    /**
     * Accepted by the `internalformat` parameter of TexImage1D, TexImage2D, TexImage3D, CopyTexImage1D, CopyTexImage2D, and RenderbufferStorage, and
     * returned in the `data` parameter of GetTexLevelParameter and GetRenderbufferParameteriv.
     */
    const val GL_DEPTH_COMPONENT32F: Int = 0x8CAC
    const val GL_DEPTH32F_STENCIL8: Int = 0x8CAD

    /**
     * Accepted by the `type` parameter of DrawPixels, ReadPixels, TexImage1D, TexImage2D, TexImage3D, TexSubImage1D, TexSubImage2D, TexSubImage3D, and
     * GetTexImage.
     */
    const val GL_FLOAT_32_UNSIGNED_INT_24_8_REV: Int = 0x8DAD

    /** Accepted by the `value` parameter of GetTexLevelParameter.  */
    const val GL_TEXTURE_RED_TYPE: Int = 0x8C10
    const val GL_TEXTURE_GREEN_TYPE: Int = 0x8C11
    const val GL_TEXTURE_BLUE_TYPE: Int = 0x8C12
    const val GL_TEXTURE_ALPHA_TYPE: Int = 0x8C13
    const val GL_TEXTURE_LUMINANCE_TYPE: Int = 0x8C14
    const val GL_TEXTURE_INTENSITY_TYPE: Int = 0x8C15
    const val GL_TEXTURE_DEPTH_TYPE: Int = 0x8C16

    /** Returned by the `params` parameter of GetTexLevelParameter.  */
    const val GL_UNSIGNED_NORMALIZED: Int = 0x8C17

    /** Accepted by the `internalFormat` parameter of TexImage1D, TexImage2D, and TexImage3D.  */
    const val GL_RGBA32F: Int = 0x8814
    const val GL_RGB32F: Int = 0x8815
    const val GL_RGBA16F: Int = 0x881A
    const val GL_RGB16F: Int = 0x881B

    /** Accepted by the `internalformat` parameter of TexImage1D, TexImage2D, TexImage3D, CopyTexImage1D, CopyTexImage2D, and RenderbufferStorage.  */
    const val GL_R11F_G11F_B10F: Int = 0x8C3A

    /**
     * Accepted by the `type` parameter of DrawPixels, ReadPixels, TexImage1D, TexImage2D, GetTexImage, TexImage3D, TexSubImage1D, TexSubImage2D,
     * TexSubImage3D, GetHistogram, GetMinmax, ConvolutionFilter1D, ConvolutionFilter2D, ConvolutionFilter3D, GetConvolutionFilter, SeparableFilter2D,
     * GetSeparableFilter, ColorTable, ColorSubTable, and GetColorTable.
     */
    const val GL_UNSIGNED_INT_10F_11F_11F_REV: Int = 0x8C3B

    /** Accepted by the `internalformat` parameter of TexImage1D, TexImage2D, TexImage3D, CopyTexImage1D, CopyTexImage2D, and RenderbufferStorage.  */
    const val GL_RGB9_E5: Int = 0x8C3D

    /**
     * Accepted by the `type` parameter of DrawPixels, ReadPixels, TexImage1D, TexImage2D, GetTexImage, TexImage3D, TexSubImage1D, TexSubImage2D,
     * TexSubImage3D, GetHistogram, GetMinmax, ConvolutionFilter1D, ConvolutionFilter2D, ConvolutionFilter3D, GetConvolutionFilter, SeparableFilter2D,
     * GetSeparableFilter, ColorTable, ColorSubTable, and GetColorTable.
     */
    const val GL_UNSIGNED_INT_5_9_9_9_REV: Int = 0x8C3E

    /** Accepted by the `pname` parameter of GetTexLevelParameterfv and GetTexLevelParameteriv.  */
    const val GL_TEXTURE_SHARED_SIZE: Int = 0x8C3F

    /**
     * Accepted by the `target` parameter of BindFramebuffer, CheckFramebufferStatus, FramebufferTexture{1D|2D|3D}, FramebufferRenderbuffer, and
     * GetFramebufferAttachmentParameteriv.
     */
    const val GL_FRAMEBUFFER: Int = 0x8D40
    const val GL_READ_FRAMEBUFFER: Int = 0x8CA8
    const val GL_DRAW_FRAMEBUFFER: Int = 0x8CA9

    /**
     * Accepted by the `target` parameter of BindRenderbuffer, RenderbufferStorage, and GetRenderbufferParameteriv, and returned by
     * GetFramebufferAttachmentParameteriv.
     */
    const val GL_RENDERBUFFER: Int = 0x8D41

    /** Accepted by the `internalformat` parameter of RenderbufferStorage.  */
    const val GL_STENCIL_INDEX1: Int = 0x8D46
    const val GL_STENCIL_INDEX4: Int = 0x8D47
    const val GL_STENCIL_INDEX8: Int = 0x8D48
    const val GL_STENCIL_INDEX16: Int = 0x8D49

    /** Accepted by the `pname` parameter of GetRenderbufferParameteriv.  */
    const val GL_RENDERBUFFER_WIDTH: Int = 0x8D42
    const val GL_RENDERBUFFER_HEIGHT: Int = 0x8D43
    const val GL_RENDERBUFFER_INTERNAL_FORMAT: Int = 0x8D44
    const val GL_RENDERBUFFER_RED_SIZE: Int = 0x8D50
    const val GL_RENDERBUFFER_GREEN_SIZE: Int = 0x8D51
    const val GL_RENDERBUFFER_BLUE_SIZE: Int = 0x8D52
    const val GL_RENDERBUFFER_ALPHA_SIZE: Int = 0x8D53
    const val GL_RENDERBUFFER_DEPTH_SIZE: Int = 0x8D54
    const val GL_RENDERBUFFER_STENCIL_SIZE: Int = 0x8D55
    const val GL_RENDERBUFFER_SAMPLES: Int = 0x8CAB

    /** Accepted by the `pname` parameter of GetFramebufferAttachmentParameteriv.  */
    const val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE: Int = 0x8CD0
    const val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME: Int = 0x8CD1
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL: Int = 0x8CD2
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE: Int = 0x8CD3
    const val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER: Int = 0x8CD4
    const val GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING: Int = 0x8210
    const val GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE: Int = 0x8211
    const val GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE: Int = 0x8212
    const val GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE: Int = 0x8213
    const val GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE: Int = 0x8214
    const val GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE: Int = 0x8215
    const val GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE: Int = 0x8216
    const val GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE: Int = 0x8217

    /** Returned in `params` by GetFramebufferAttachmentParameteriv.  */
    const val GL_FRAMEBUFFER_DEFAULT: Int = 0x8218
    const val GL_INDEX: Int = 0x8222

    /** Accepted by the `attachment` parameter of FramebufferTexture{1D|2D|3D}, FramebufferRenderbuffer, and GetFramebufferAttachmentParameteriv.  */
    const val GL_COLOR_ATTACHMENT0: Int = 0x8CE0
    const val GL_COLOR_ATTACHMENT1: Int = 0x8CE1
    const val GL_COLOR_ATTACHMENT2: Int = 0x8CE2
    const val GL_COLOR_ATTACHMENT3: Int = 0x8CE3
    const val GL_COLOR_ATTACHMENT4: Int = 0x8CE4
    const val GL_COLOR_ATTACHMENT5: Int = 0x8CE5
    const val GL_COLOR_ATTACHMENT6: Int = 0x8CE6
    const val GL_COLOR_ATTACHMENT7: Int = 0x8CE7
    const val GL_COLOR_ATTACHMENT8: Int = 0x8CE8
    const val GL_COLOR_ATTACHMENT9: Int = 0x8CE9
    const val GL_COLOR_ATTACHMENT10: Int = 0x8CEA
    const val GL_COLOR_ATTACHMENT11: Int = 0x8CEB
    const val GL_COLOR_ATTACHMENT12: Int = 0x8CEC
    const val GL_COLOR_ATTACHMENT13: Int = 0x8CED
    const val GL_COLOR_ATTACHMENT14: Int = 0x8CEE
    const val GL_COLOR_ATTACHMENT15: Int = 0x8CEF
    const val GL_COLOR_ATTACHMENT16: Int = 0x8CF0
    const val GL_COLOR_ATTACHMENT17: Int = 0x8CF1
    const val GL_COLOR_ATTACHMENT18: Int = 0x8CF2
    const val GL_COLOR_ATTACHMENT19: Int = 0x8CF3
    const val GL_COLOR_ATTACHMENT20: Int = 0x8CF4
    const val GL_COLOR_ATTACHMENT21: Int = 0x8CF5
    const val GL_COLOR_ATTACHMENT22: Int = 0x8CF6
    const val GL_COLOR_ATTACHMENT23: Int = 0x8CF7
    const val GL_COLOR_ATTACHMENT24: Int = 0x8CF8
    const val GL_COLOR_ATTACHMENT25: Int = 0x8CF9
    const val GL_COLOR_ATTACHMENT26: Int = 0x8CFA
    const val GL_COLOR_ATTACHMENT27: Int = 0x8CFB
    const val GL_COLOR_ATTACHMENT28: Int = 0x8CFC
    const val GL_COLOR_ATTACHMENT29: Int = 0x8CFD
    const val GL_COLOR_ATTACHMENT30: Int = 0x8CFE
    const val GL_COLOR_ATTACHMENT31: Int = 0x8CFF
    const val GL_DEPTH_ATTACHMENT: Int = 0x8D00
    const val GL_STENCIL_ATTACHMENT: Int = 0x8D20
    const val GL_DEPTH_STENCIL_ATTACHMENT: Int = 0x821A

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_MAX_SAMPLES: Int = 0x8D57

    /** Returned by CheckFramebufferStatus().  */
    const val GL_FRAMEBUFFER_COMPLETE: Int = 0x8CD5
    const val GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT: Int = 0x8CD6
    const val GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: Int = 0x8CD7
    const val GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER: Int = 0x8CDB
    const val GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER: Int = 0x8CDC
    const val GL_FRAMEBUFFER_UNSUPPORTED: Int = 0x8CDD
    const val GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE: Int = 0x8D56
    const val GL_FRAMEBUFFER_UNDEFINED: Int = 0x8219

    /** Accepted by the `pname` parameters of GetIntegerv, GetFloatv,  and GetDoublev.  */
    const val GL_FRAMEBUFFER_BINDING: Int = 0x8CA6
    const val GL_DRAW_FRAMEBUFFER_BINDING: Int = 0x8CA6
    const val GL_READ_FRAMEBUFFER_BINDING: Int = 0x8CAA
    const val GL_RENDERBUFFER_BINDING: Int = 0x8CA7
    const val GL_MAX_COLOR_ATTACHMENTS: Int = 0x8CDF
    const val GL_MAX_RENDERBUFFER_SIZE: Int = 0x84E8

    /** Returned by GetError().  */
    const val GL_INVALID_FRAMEBUFFER_OPERATION: Int = 0x506

    /**
     * Accepted by the `format` parameter of DrawPixels, ReadPixels, TexImage1D, TexImage2D, TexImage3D, TexSubImage1D, TexSubImage2D, TexSubImage3D, and
     * GetTexImage, by the `type` parameter of CopyPixels, by the `internalformat` parameter of TexImage1D, TexImage2D, TexImage3D, CopyTexImage1D,
     * CopyTexImage2D, and RenderbufferStorage, and returned in the `data` parameter of GetTexLevelParameter and GetRenderbufferParameteriv.
     */
    const val GL_DEPTH_STENCIL: Int = 0x84F9

    /**
     * Accepted by the `type` parameter of DrawPixels, ReadPixels, TexImage1D, TexImage2D, TexImage3D, TexSubImage1D, TexSubImage2D, TexSubImage3D, and
     * GetTexImage.
     */
    const val GL_UNSIGNED_INT_24_8: Int = 0x84FA

    /**
     * Accepted by the `internalformat` parameter of TexImage1D, TexImage2D, TexImage3D, CopyTexImage1D, CopyTexImage2D, and RenderbufferStorage, and
     * returned in the `data` parameter of GetTexLevelParameter and GetRenderbufferParameteriv.
     */
    const val GL_DEPTH24_STENCIL8: Int = 0x88F0

    /** Accepted by the `value` parameter of GetTexLevelParameter.  */
    const val GL_TEXTURE_STENCIL_SIZE: Int = 0x88F1

    /**
     * Accepted by the `type` parameter of DrawPixels, ReadPixels, TexImage1D, TexImage2D, TexImage3D, GetTexImage, TexSubImage1D, TexSubImage2D,
     * TexSubImage3D, GetHistogram, GetMinmax, ConvolutionFilter1D, ConvolutionFilter2D, GetConvolutionFilter, SeparableFilter2D, GetSeparableFilter,
     * ColorTable, ColorSubTable, and GetColorTable.
     *
     *
     * Accepted by the `type` argument of VertexPointer, NormalPointer, ColorPointer, SecondaryColorPointer, FogCoordPointer, TexCoordPointer, and
     * VertexAttribPointer.
     */
    const val GL_HALF_FLOAT: Int = 0x140B

    /** Accepted by the `internalFormat` parameter of TexImage1D, TexImage2D, and TexImage3D.  */
    const val GL_RGBA32UI: Int = 0x8D70
    const val GL_RGB32UI: Int = 0x8D71
    const val GL_RGBA16UI: Int = 0x8D76
    const val GL_RGB16UI: Int = 0x8D77
    const val GL_RGBA8UI: Int = 0x8D7C
    const val GL_RGB8UI: Int = 0x8D7D
    const val GL_RGBA32I: Int = 0x8D82
    const val GL_RGB32I: Int = 0x8D83
    const val GL_RGBA16I: Int = 0x8D88
    const val GL_RGB16I: Int = 0x8D89
    const val GL_RGBA8I: Int = 0x8D8E
    const val GL_RGB8I: Int = 0x8D8F

    /** Accepted by the `format` parameter of TexImage1D, TexImage2D, TexImage3D, TexSubImage1D, TexSubImage2D, TexSubImage3D, DrawPixels and ReadPixels.  */
    const val GL_RED_INTEGER: Int = 0x8D94
    const val GL_GREEN_INTEGER: Int = 0x8D95
    const val GL_BLUE_INTEGER: Int = 0x8D96
    const val GL_ALPHA_INTEGER: Int = 0x8D97
    const val GL_RGB_INTEGER: Int = 0x8D98
    const val GL_RGBA_INTEGER: Int = 0x8D99
    const val GL_BGR_INTEGER: Int = 0x8D9A
    const val GL_BGRA_INTEGER: Int = 0x8D9B

    /** Accepted by the `target` parameter of TexParameteri, TexParameteriv, TexParameterf, TexParameterfv, GenerateMipmap, and BindTexture.  */
    const val GL_TEXTURE_1D_ARRAY: Int = 0x8C18
    const val GL_TEXTURE_2D_ARRAY: Int = 0x8C1A

    /** Accepted by the `target` parameter of TexImage3D, TexSubImage3D, CopyTexSubImage3D, CompressedTexImage3D, and CompressedTexSubImage3D.  */
    const val GL_PROXY_TEXTURE_2D_ARRAY: Int = 0x8C1B

    /**
     * Accepted by the `target` parameter of TexImage2D, TexSubImage2D, CopyTexImage2D, CopyTexSubImage2D, CompressedTexImage2D, and
     * CompressedTexSubImage2D.
     */
    const val GL_PROXY_TEXTURE_1D_ARRAY: Int = 0x8C19

    /** Accepted by the `pname` parameter of GetBooleanv, GetDoublev, GetIntegerv and GetFloatv.  */
    const val GL_TEXTURE_BINDING_1D_ARRAY: Int = 0x8C1C
    const val GL_TEXTURE_BINDING_2D_ARRAY: Int = 0x8C1D
    const val GL_MAX_ARRAY_TEXTURE_LAYERS: Int = 0x88FF

    /**
     * Accepted by the `internalformat` parameter of TexImage2D, CopyTexImage2D, and CompressedTexImage2D and the `format` parameter of
     * CompressedTexSubImage2D.
     */
    const val GL_COMPRESSED_RED_RGTC1: Int = 0x8DBB
    const val GL_COMPRESSED_SIGNED_RED_RGTC1: Int = 0x8DBC
    const val GL_COMPRESSED_RG_RGTC2: Int = 0x8DBD
    const val GL_COMPRESSED_SIGNED_RG_RGTC2: Int = 0x8DBE

    /** Accepted by the `internalFormat` parameter of TexImage1D, TexImage2D, TexImage3D, CopyTexImage1D, and CopyTexImage2D.  */
    const val GL_R8: Int = 0x8229
    const val GL_R16: Int = 0x822A
    const val GL_RG8: Int = 0x822B
    const val GL_RG16: Int = 0x822C
    const val GL_R16F: Int = 0x822D
    const val GL_R32F: Int = 0x822E
    const val GL_RG16F: Int = 0x822F
    const val GL_RG32F: Int = 0x8230
    const val GL_R8I: Int = 0x8231
    const val GL_R8UI: Int = 0x8232
    const val GL_R16I: Int = 0x8233
    const val GL_R16UI: Int = 0x8234
    const val GL_R32I: Int = 0x8235
    const val GL_R32UI: Int = 0x8236
    const val GL_RG8I: Int = 0x8237
    const val GL_RG8UI: Int = 0x8238
    const val GL_RG16I: Int = 0x8239
    const val GL_RG16UI: Int = 0x823A
    const val GL_RG32I: Int = 0x823B
    const val GL_RG32UI: Int = 0x823C
    const val GL_RG: Int = 0x8227
    const val GL_COMPRESSED_RED: Int = 0x8225
    const val GL_COMPRESSED_RG: Int = 0x8226

    /** Accepted by the `format` parameter of TexImage3D, TexImage2D, TexImage3D, TexSubImage1D, TexSubImage2D, TexSubImage3D, and ReadPixels.  */
    const val GL_RG_INTEGER: Int = 0x8228

    /**
     * Accepted by the `target` parameters of BindBuffer, BufferData, BufferSubData, MapBuffer, UnmapBuffer, GetBufferSubData, GetBufferPointerv,
     * BindBufferRange, BindBufferOffset and BindBufferBase.
     */
    const val GL_TRANSFORM_FEEDBACK_BUFFER: Int = 0x8C8E

    /** Accepted by the `param` parameter of GetIntegeri_v and GetBooleani_v.  */
    const val GL_TRANSFORM_FEEDBACK_BUFFER_START: Int = 0x8C84
    const val GL_TRANSFORM_FEEDBACK_BUFFER_SIZE: Int = 0x8C85

    /**
     * Accepted by the `param` parameter of GetIntegeri_v and GetBooleani_v, and by the `pname` parameter of GetBooleanv,
     * GetDoublev, GetIntegerv, and GetFloatv.
     */
    const val GL_TRANSFORM_FEEDBACK_BUFFER_BINDING: Int = 0x8C8F

    /** Accepted by the `bufferMode` parameter of TransformFeedbackVaryings.  */
    const val GL_INTERLEAVED_ATTRIBS: Int = 0x8C8C
    const val GL_SEPARATE_ATTRIBS: Int = 0x8C8D

    /** Accepted by the `target` parameter of BeginQuery, EndQuery, and GetQueryiv.  */
    const val GL_PRIMITIVES_GENERATED: Int = 0x8C87
    const val GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN: Int = 0x8C88

    /**
     * Accepted by the `cap` parameter of Enable, Disable, and IsEnabled, and by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev.
     */
    const val GL_RASTERIZER_DISCARD: Int = 0x8C89

    /** Accepted by the `pname` parameter of GetBooleanv, GetDoublev, GetIntegerv, and GetFloatv.  */
    const val GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS: Int = 0x8C8A
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS: Int = 0x8C8B
    const val GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS: Int = 0x8C80

    /** Accepted by the `pname` parameter of GetProgramiv.  */
    const val GL_TRANSFORM_FEEDBACK_VARYINGS: Int = 0x8C83
    const val GL_TRANSFORM_FEEDBACK_BUFFER_MODE: Int = 0x8C7F
    const val GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH: Int = 0x8C76

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_VERTEX_ARRAY_BINDING: Int = 0x85B5

    /**
     * Accepted by the `cap` parameter of Enable, Disable, and IsEnabled, and by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev.
     */
    const val GL_FRAMEBUFFER_SRGB: Int = 0x8DB9

    var gl: com.zakgof.korender.impl.gl.IGL30? = null

    /**
     * Generate mipmaps for a specified texture target.
     *
     * @param target the target to which the texture whose mimaps to generate is bound. One of:<br></br><table><tr><td>[TEXTURE_1D][GL11.GL_TEXTURE_1D]</td><td>[TEXTURE_2D][GL11.GL_TEXTURE_2D]</td><td>[TEXTURE_3D][GL12.GL_TEXTURE_3D]</td><td>[TEXTURE_1D_ARRAY][GL30C.GL_TEXTURE_1D_ARRAY]</td><td>[TEXTURE_2D_ARRAY][GL30C.GL_TEXTURE_2D_ARRAY]</td><td>[TEXTURE_CUBE_MAP][GL13.GL_TEXTURE_CUBE_MAP]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glGenerateMipmap)
     */
    fun glGenerateMipmap(target: Int) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glGenerateMipmap(target)
    }

    /**
     * Generates framebuffer object names.
     *
     * @see [Reference Page](http://docs.gl/gl4/glGenFramebuffers)
     */
    fun glGenFramebuffers(): Int {
        return com.zakgof.korender.impl.gl.VGL30.gl!!.glGenFramebuffers()
    }

    /**
     * Attaches a level of a 2D texture object as a logical buffer to the currently bound framebuffer object.
     *
     * @param target     the framebuffer target. One of:<br></br><table><tr><td>[FRAMEBUFFER][GL30C.GL_FRAMEBUFFER]</td><td>[READ_FRAMEBUFFER][GL30C.GL_READ_FRAMEBUFFER]</td><td>[DRAW_FRAMEBUFFER][GL30C.GL_DRAW_FRAMEBUFFER]</td></tr></table>
     * @param attachment the attachment point of the framebuffer. One of:<br></br><table><tr><td>[COLOR_ATTACHMENT0][GL30C.GL_COLOR_ATTACHMENT0]</td><td>[COLOR_ATTACHMENT1][GL30C.GL_COLOR_ATTACHMENT1]</td><td>[COLOR_ATTACHMENT2][GL30C.GL_COLOR_ATTACHMENT2]</td><td>[COLOR_ATTACHMENT3][GL30C.GL_COLOR_ATTACHMENT3]</td></tr><tr><td>[COLOR_ATTACHMENT4][GL30C.GL_COLOR_ATTACHMENT4]</td><td>[COLOR_ATTACHMENT5][GL30C.GL_COLOR_ATTACHMENT5]</td><td>[COLOR_ATTACHMENT6][GL30C.GL_COLOR_ATTACHMENT6]</td><td>[COLOR_ATTACHMENT7][GL30C.GL_COLOR_ATTACHMENT7]</td></tr><tr><td>[COLOR_ATTACHMENT8][GL30C.GL_COLOR_ATTACHMENT8]</td><td>[COLOR_ATTACHMENT9][GL30C.GL_COLOR_ATTACHMENT9]</td><td>[COLOR_ATTACHMENT10][GL30C.GL_COLOR_ATTACHMENT10]</td><td>[COLOR_ATTACHMENT11][GL30C.GL_COLOR_ATTACHMENT11]</td></tr><tr><td>[COLOR_ATTACHMENT12][GL30C.GL_COLOR_ATTACHMENT12]</td><td>[COLOR_ATTACHMENT13][GL30C.GL_COLOR_ATTACHMENT13]</td><td>[COLOR_ATTACHMENT14][GL30C.GL_COLOR_ATTACHMENT14]</td><td>[COLOR_ATTACHMENT15][GL30C.GL_COLOR_ATTACHMENT15]</td></tr><tr><td>[COLOR_ATTACHMENT16][GL30C.GL_COLOR_ATTACHMENT16]</td><td>[COLOR_ATTACHMENT17][GL30C.GL_COLOR_ATTACHMENT17]</td><td>[COLOR_ATTACHMENT18][GL30C.GL_COLOR_ATTACHMENT18]</td><td>[COLOR_ATTACHMENT19][GL30C.GL_COLOR_ATTACHMENT19]</td></tr><tr><td>[COLOR_ATTACHMENT20][GL30C.GL_COLOR_ATTACHMENT20]</td><td>[COLOR_ATTACHMENT21][GL30C.GL_COLOR_ATTACHMENT21]</td><td>[COLOR_ATTACHMENT22][GL30C.GL_COLOR_ATTACHMENT22]</td><td>[COLOR_ATTACHMENT23][GL30C.GL_COLOR_ATTACHMENT23]</td></tr><tr><td>[COLOR_ATTACHMENT24][GL30C.GL_COLOR_ATTACHMENT24]</td><td>[COLOR_ATTACHMENT25][GL30C.GL_COLOR_ATTACHMENT25]</td><td>[COLOR_ATTACHMENT26][GL30C.GL_COLOR_ATTACHMENT26]</td><td>[COLOR_ATTACHMENT27][GL30C.GL_COLOR_ATTACHMENT27]</td></tr><tr><td>[COLOR_ATTACHMENT28][GL30C.GL_COLOR_ATTACHMENT28]</td><td>[COLOR_ATTACHMENT29][GL30C.GL_COLOR_ATTACHMENT29]</td><td>[COLOR_ATTACHMENT30][GL30C.GL_COLOR_ATTACHMENT30]</td><td>[COLOR_ATTACHMENT31][GL30C.GL_COLOR_ATTACHMENT31]</td></tr><tr><td>[DEPTH_ATTACHMENT][GL30C.GL_DEPTH_ATTACHMENT]</td><td>[STENCIL_ATTACHMENT][GL30C.GL_STENCIL_ATTACHMENT]</td><td>[DEPTH_STENCIL_ATTACHMENT][GL30C.GL_DEPTH_STENCIL_ATTACHMENT]</td></tr></table>
     * @param textarget  the type of texture
     * @param texture    the texture object to attach to the framebuffer attachment point named by `attachment`
     * @param level      the mipmap level of `texture` to attach
     *
     * @see [Reference Page](http://docs.gl/gl4/glFramebufferTexture2D)
     */
    fun glFramebufferTexture2D(
        target: Int,
        attachment: Int,
        textarget: Int,
        texture: Int,
        level: Int
    ) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glFramebufferTexture2D(target, attachment, textarget, texture, level)
    }

    /**
     * Generates renderbuffer object names.
     *
     * @see [Reference Page](http://docs.gl/gl4/glGenRenderbuffers)
     */
    fun glGenRenderbuffers(): Int {
        return com.zakgof.korender.impl.gl.VGL30.gl!!.glGenRenderbuffers()
    }

    /**
     * Establishes data storage, format and dimensions of a renderbuffer object's image.
     *
     * @param target         the target of the allocation. Must be:<br></br><table><tr><td>[RENDERBUFFER][GL30C.GL_RENDERBUFFER]</td></tr></table>
     * @param internalformat the internal format to use for the renderbuffer object's image. Must be a color-renderable, depth-renderable, or stencil-renderable format.
     * @param width          the width of the renderbuffer, in pixels
     * @param height         the height of the renderbuffer, in pixels
     *
     * @see [Reference Page](http://docs.gl/gl4/glRenderbufferStorage)
     */
    fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glRenderbufferStorage(target, internalformat, width, height)
    }

    /**
     * Attaches a renderbuffer as a logical buffer to the currently bound framebuffer object.
     *
     * @param target             the framebuffer target. One of:<br></br><table><tr><td>[FRAMEBUFFER][GL30C.GL_FRAMEBUFFER]</td><td>[READ_FRAMEBUFFER][GL30C.GL_READ_FRAMEBUFFER]</td><td>[DRAW_FRAMEBUFFER][GL30C.GL_DRAW_FRAMEBUFFER]</td></tr></table>
     * @param attachment         the attachment point of the framebuffer. One of:<br></br><table><tr><td>[COLOR_ATTACHMENT0][GL30C.GL_COLOR_ATTACHMENT0]</td><td>[COLOR_ATTACHMENT1][GL30C.GL_COLOR_ATTACHMENT1]</td><td>[COLOR_ATTACHMENT2][GL30C.GL_COLOR_ATTACHMENT2]</td><td>[COLOR_ATTACHMENT3][GL30C.GL_COLOR_ATTACHMENT3]</td></tr><tr><td>[COLOR_ATTACHMENT4][GL30C.GL_COLOR_ATTACHMENT4]</td><td>[COLOR_ATTACHMENT5][GL30C.GL_COLOR_ATTACHMENT5]</td><td>[COLOR_ATTACHMENT6][GL30C.GL_COLOR_ATTACHMENT6]</td><td>[COLOR_ATTACHMENT7][GL30C.GL_COLOR_ATTACHMENT7]</td></tr><tr><td>[COLOR_ATTACHMENT8][GL30C.GL_COLOR_ATTACHMENT8]</td><td>[COLOR_ATTACHMENT9][GL30C.GL_COLOR_ATTACHMENT9]</td><td>[COLOR_ATTACHMENT10][GL30C.GL_COLOR_ATTACHMENT10]</td><td>[COLOR_ATTACHMENT11][GL30C.GL_COLOR_ATTACHMENT11]</td></tr><tr><td>[COLOR_ATTACHMENT12][GL30C.GL_COLOR_ATTACHMENT12]</td><td>[COLOR_ATTACHMENT13][GL30C.GL_COLOR_ATTACHMENT13]</td><td>[COLOR_ATTACHMENT14][GL30C.GL_COLOR_ATTACHMENT14]</td><td>[COLOR_ATTACHMENT15][GL30C.GL_COLOR_ATTACHMENT15]</td></tr><tr><td>[COLOR_ATTACHMENT16][GL30C.GL_COLOR_ATTACHMENT16]</td><td>[COLOR_ATTACHMENT17][GL30C.GL_COLOR_ATTACHMENT17]</td><td>[COLOR_ATTACHMENT18][GL30C.GL_COLOR_ATTACHMENT18]</td><td>[COLOR_ATTACHMENT19][GL30C.GL_COLOR_ATTACHMENT19]</td></tr><tr><td>[COLOR_ATTACHMENT20][GL30C.GL_COLOR_ATTACHMENT20]</td><td>[COLOR_ATTACHMENT21][GL30C.GL_COLOR_ATTACHMENT21]</td><td>[COLOR_ATTACHMENT22][GL30C.GL_COLOR_ATTACHMENT22]</td><td>[COLOR_ATTACHMENT23][GL30C.GL_COLOR_ATTACHMENT23]</td></tr><tr><td>[COLOR_ATTACHMENT24][GL30C.GL_COLOR_ATTACHMENT24]</td><td>[COLOR_ATTACHMENT25][GL30C.GL_COLOR_ATTACHMENT25]</td><td>[COLOR_ATTACHMENT26][GL30C.GL_COLOR_ATTACHMENT26]</td><td>[COLOR_ATTACHMENT27][GL30C.GL_COLOR_ATTACHMENT27]</td></tr><tr><td>[COLOR_ATTACHMENT28][GL30C.GL_COLOR_ATTACHMENT28]</td><td>[COLOR_ATTACHMENT29][GL30C.GL_COLOR_ATTACHMENT29]</td><td>[COLOR_ATTACHMENT30][GL30C.GL_COLOR_ATTACHMENT30]</td><td>[COLOR_ATTACHMENT31][GL30C.GL_COLOR_ATTACHMENT31]</td></tr><tr><td>[DEPTH_ATTACHMENT][GL30C.GL_DEPTH_ATTACHMENT]</td><td>[STENCIL_ATTACHMENT][GL30C.GL_STENCIL_ATTACHMENT]</td><td>[DEPTH_STENCIL_ATTACHMENT][GL30C.GL_DEPTH_STENCIL_ATTACHMENT]</td></tr></table>
     * @param renderbuffertarget the renderbuffer target. Must be:<br></br><table><tr><td>[RENDERBUFFER][GL30C.GL_RENDERBUFFER]</td></tr></table>
     * @param renderbuffer       the name of an existing renderbuffer object of type `renderbuffertarget` to attach
     *
     * @see [Reference Page](http://docs.gl/gl4/glFramebufferRenderbuffer)
     */
    fun glFramebufferRenderbuffer(
        target: Int,
        attachment: Int,
        renderbuffertarget: Int,
        renderbuffer: Int
    ) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)
    }

    /**
     * Specifies a list of color buffers to be drawn into.
     *
     * @see [Reference Page](http://docs.gl/gl4/glDrawBuffers)
     */
    fun glDrawBuffers(buf: Int) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glDrawBuffers(buf)
    }

    /**
     * Deletes framebuffer objects.
     *
     * @see [Reference Page](http://docs.gl/gl4/glDeleteFramebuffers)
     */
    fun glDeleteFramebuffers(framebuffer: Int) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glDeleteFramebuffers(framebuffer)
    }

    /**
     * Deletes renderbuffer objects.
     *
     * @see [Reference Page](http://docs.gl/gl4/glDeleteRenderbuffers)
     */
    fun glDeleteRenderbuffers(renderbuffer: Int) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glDeleteRenderbuffers(renderbuffer)
    }

    /**
     * Binds a framebuffer to a framebuffer target.
     *
     * @param target      the framebuffer target of the binding operation. One of:<br></br><table><tr><td>[FRAMEBUFFER][GL30C.GL_FRAMEBUFFER]</td><td>[READ_FRAMEBUFFER][GL30C.GL_READ_FRAMEBUFFER]</td><td>[DRAW_FRAMEBUFFER][GL30C.GL_DRAW_FRAMEBUFFER]</td></tr></table>
     * @param framebuffer the name of the framebuffer object to bind
     *
     * @see [Reference Page](http://docs.gl/gl4/glBindFramebuffer)
     */
    fun glBindFramebuffer(target: Int, framebuffer: Int) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glBindFramebuffer(target, framebuffer)
    }

    /**
     * Binds a renderbuffer to a renderbuffer target.
     *
     * @param target       the renderbuffer target of the binding operation. Must be:<br></br><table><tr><td>[RENDERBUFFER][GL30C.GL_RENDERBUFFER]</td></tr></table>
     * @param renderbuffer the name of the renderbuffer object to bind
     *
     * @see [Reference Page](http://docs.gl/gl4/glBindRenderbuffer)
     */
    fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        com.zakgof.korender.impl.gl.VGL30.gl!!.glBindRenderbuffer(target, renderbuffer)
    }

    /**
     * Checks the completeness status of a framebuffer.
     *
     * @param target the target of the framebuffer completeness check. One of:<br></br><table><tr><td>[FRAMEBUFFER][GL30C.GL_FRAMEBUFFER]</td><td>[READ_FRAMEBUFFER][GL30C.GL_READ_FRAMEBUFFER]</td><td>[DRAW_FRAMEBUFFER][GL30C.GL_DRAW_FRAMEBUFFER]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glCheckFramebufferStatus)
     */
    fun glCheckFramebufferStatus(target: Int): Int {
        return com.zakgof.korender.impl.gl.VGL30.gl!!.glCheckFramebufferStatus(target)
    }
}
