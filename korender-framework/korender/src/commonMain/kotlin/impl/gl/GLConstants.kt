package com.zakgof.korender.impl.gl

object GLConstants {

    /** AccumOp  */
    const val GL_ACCUM: Int = 0x100
    const val GL_LOAD: Int = 0x101
    const val GL_RETURN: Int = 0x102
    const val GL_MULT: Int = 0x103
    const val GL_ADD: Int = 0x104

    /** AlphaFunction  */
    const val GL_NEVER: Int = 0x200
    const val GL_LESS: Int = 0x201
    const val GL_EQUAL: Int = 0x202
    const val GL_LEQUAL: Int = 0x203
    const val GL_GREATER: Int = 0x204
    const val GL_NOTEQUAL: Int = 0x205
    const val GL_GEQUAL: Int = 0x206
    const val GL_ALWAYS: Int = 0x207

    /** AttribMask  */
    const val GL_CURRENT_BIT: Int = 0x1
    const val GL_POINT_BIT: Int = 0x2
    const val GL_LINE_BIT: Int = 0x4
    const val GL_POLYGON_BIT: Int = 0x8
    const val GL_POLYGON_STIPPLE_BIT: Int = 0x10
    const val GL_PIXEL_MODE_BIT: Int = 0x20
    const val GL_LIGHTING_BIT: Int = 0x40
    const val GL_FOG_BIT: Int = 0x80
    const val GL_DEPTH_BUFFER_BIT: Int = 0x100
    const val GL_ACCUM_BUFFER_BIT: Int = 0x200
    const val GL_STENCIL_BUFFER_BIT: Int = 0x400
    const val GL_VIEWPORT_BIT: Int = 0x800
    const val GL_TRANSFORM_BIT: Int = 0x1000
    const val GL_ENABLE_BIT: Int = 0x2000
    const val GL_COLOR_BUFFER_BIT: Int = 0x4000
    const val GL_HINT_BIT: Int = 0x8000
    const val GL_EVAL_BIT: Int = 0x10000
    const val GL_LIST_BIT: Int = 0x20000
    const val GL_TEXTURE_BIT: Int = 0x40000
    const val GL_SCISSOR_BIT: Int = 0x80000
    const val GL_ALL_ATTRIB_BITS: Int = 0xFFFFF

    /** BeginMode  */
    const val GL_POINTS: Int = 0x0
    const val GL_LINES: Int = 0x1
    const val GL_LINE_LOOP: Int = 0x2
    const val GL_LINE_STRIP: Int = 0x3
    const val GL_TRIANGLES: Int = 0x4
    const val GL_TRIANGLE_STRIP: Int = 0x5
    const val GL_TRIANGLE_FAN: Int = 0x6
    const val GL_QUADS: Int = 0x7
    const val GL_QUAD_STRIP: Int = 0x8
    const val GL_POLYGON: Int = 0x9

    /** BlendingFactorDest  */
    const val GL_ZERO: Int = 0
    const val GL_ONE: Int = 1
    const val GL_SRC_COLOR: Int = 0x300
    const val GL_ONE_MINUS_SRC_COLOR: Int = 0x301
    const val GL_SRC_ALPHA: Int = 0x302
    const val GL_ONE_MINUS_SRC_ALPHA: Int = 0x303
    const val GL_DST_ALPHA: Int = 0x304
    const val GL_ONE_MINUS_DST_ALPHA: Int = 0x305

    /** BlendingFactorSrc  */
    const val GL_DST_COLOR: Int = 0x306
    const val GL_ONE_MINUS_DST_COLOR: Int = 0x307
    const val GL_SRC_ALPHA_SATURATE: Int = 0x308

    /** Boolean  */
    const val GL_TRUE: Int = 1
    const val GL_FALSE: Int = 0

    /** ClipPlaneName  */
    const val GL_CLIP_PLANE0: Int = 0x3000
    const val GL_CLIP_PLANE1: Int = 0x3001
    const val GL_CLIP_PLANE2: Int = 0x3002
    const val GL_CLIP_PLANE3: Int = 0x3003
    const val GL_CLIP_PLANE4: Int = 0x3004
    const val GL_CLIP_PLANE5: Int = 0x3005

    /** DataType  */
    const val GL_BYTE: Int = 0x1400
    const val GL_UNSIGNED_BYTE: Int = 0x1401
    const val GL_SHORT: Int = 0x1402
    const val GL_UNSIGNED_SHORT: Int = 0x1403
    const val GL_INT: Int = 0x1404
    const val GL_UNSIGNED_INT: Int = 0x1405
    const val GL_FLOAT: Int = 0x1406
    const val GL_2_BYTES: Int = 0x1407
    const val GL_3_BYTES: Int = 0x1408
    const val GL_4_BYTES: Int = 0x1409
    const val GL_DOUBLE: Int = 0x140A

    /** DrawBufferMode  */
    const val GL_NONE: Int = 0
    const val GL_FRONT_LEFT: Int = 0x400
    const val GL_FRONT_RIGHT: Int = 0x401
    const val GL_BACK_LEFT: Int = 0x402
    const val GL_BACK_RIGHT: Int = 0x403
    const val GL_FRONT: Int = 0x404
    const val GL_BACK: Int = 0x405
    const val GL_LEFT: Int = 0x406
    const val GL_RIGHT: Int = 0x407
    const val GL_FRONT_AND_BACK: Int = 0x408
    const val GL_AUX0: Int = 0x409
    const val GL_AUX1: Int = 0x40A
    const val GL_AUX2: Int = 0x40B
    const val GL_AUX3: Int = 0x40C

    /** ErrorCode  */
    const val GL_NO_ERROR: Int = 0
    const val GL_INVALID_ENUM: Int = 0x500
    const val GL_INVALID_VALUE: Int = 0x501
    const val GL_INVALID_OPERATION: Int = 0x502
    const val GL_STACK_OVERFLOW: Int = 0x503
    const val GL_STACK_UNDERFLOW: Int = 0x504
    const val GL_OUT_OF_MEMORY: Int = 0x505

    /** FeedBackMode  */
    const val GL_2D: Int = 0x600
    const val GL_3D: Int = 0x601
    const val GL_3D_COLOR: Int = 0x602
    const val GL_3D_COLOR_TEXTURE: Int = 0x603
    const val GL_4D_COLOR_TEXTURE: Int = 0x604

    /** FeedBackToken  */
    const val GL_PASS_THROUGH_TOKEN: Int = 0x700
    const val GL_POINT_TOKEN: Int = 0x701
    const val GL_LINE_TOKEN: Int = 0x702
    const val GL_POLYGON_TOKEN: Int = 0x703
    const val GL_BITMAP_TOKEN: Int = 0x704
    const val GL_DRAW_PIXEL_TOKEN: Int = 0x705
    const val GL_COPY_PIXEL_TOKEN: Int = 0x706
    const val GL_LINE_RESET_TOKEN: Int = 0x707

    /** FogMode  */
    const val GL_EXP: Int = 0x800
    const val GL_EXP2: Int = 0x801

    /** FrontFaceDirection  */
    const val GL_CW: Int = 0x900
    const val GL_CCW: Int = 0x901

    /** GetMapTarget  */
    const val GL_COEFF: Int = 0xA00
    const val GL_ORDER: Int = 0xA01
    const val GL_DOMAIN: Int = 0xA02

    /** GetTarget  */
    const val GL_CURRENT_COLOR: Int = 0xB00
    const val GL_CURRENT_INDEX: Int = 0xB01
    const val GL_CURRENT_NORMAL: Int = 0xB02
    const val GL_CURRENT_TEXTURE_COORDS: Int = 0xB03
    const val GL_CURRENT_RASTER_COLOR: Int = 0xB04
    const val GL_CURRENT_RASTER_INDEX: Int = 0xB05
    const val GL_CURRENT_RASTER_TEXTURE_COORDS: Int = 0xB06
    const val GL_CURRENT_RASTER_POSITION: Int = 0xB07
    const val GL_CURRENT_RASTER_POSITION_VALID: Int = 0xB08
    const val GL_CURRENT_RASTER_DISTANCE: Int = 0xB09
    const val GL_POINT_SMOOTH: Int = 0xB10
    const val GL_POINT_SIZE: Int = 0xB11
    const val GL_POINT_SIZE_RANGE: Int = 0xB12
    const val GL_POINT_SIZE_GRANULARITY: Int = 0xB13
    const val GL_LINE_SMOOTH: Int = 0xB20
    const val GL_LINE_WIDTH: Int = 0xB21
    const val GL_LINE_WIDTH_RANGE: Int = 0xB22
    const val GL_LINE_WIDTH_GRANULARITY: Int = 0xB23
    const val GL_LINE_STIPPLE: Int = 0xB24
    const val GL_LINE_STIPPLE_PATTERN: Int = 0xB25
    const val GL_LINE_STIPPLE_REPEAT: Int = 0xB26
    const val GL_LIST_MODE: Int = 0xB30
    const val GL_MAX_LIST_NESTING: Int = 0xB31
    const val GL_LIST_BASE: Int = 0xB32
    const val GL_LIST_INDEX: Int = 0xB33
    const val GL_POLYGON_MODE: Int = 0xB40
    const val GL_POLYGON_SMOOTH: Int = 0xB41
    const val GL_POLYGON_STIPPLE: Int = 0xB42
    const val GL_EDGE_FLAG: Int = 0xB43
    const val GL_CULL_FACE: Int = 0xB44
    const val GL_CULL_FACE_MODE: Int = 0xB45
    const val GL_FRONT_FACE: Int = 0xB46
    const val GL_LIGHTING: Int = 0xB50
    const val GL_LIGHT_MODEL_LOCAL_VIEWER: Int = 0xB51
    const val GL_LIGHT_MODEL_TWO_SIDE: Int = 0xB52
    const val GL_LIGHT_MODEL_AMBIENT: Int = 0xB53
    const val GL_SHADE_MODEL: Int = 0xB54
    const val GL_COLOR_MATERIAL_FACE: Int = 0xB55
    const val GL_COLOR_MATERIAL_PARAMETER: Int = 0xB56
    const val GL_COLOR_MATERIAL: Int = 0xB57
    const val GL_FOG: Int = 0xB60
    const val GL_FOG_INDEX: Int = 0xB61
    const val GL_FOG_DENSITY: Int = 0xB62
    const val GL_FOG_START: Int = 0xB63
    const val GL_FOG_END: Int = 0xB64
    const val GL_FOG_MODE: Int = 0xB65
    const val GL_FOG_COLOR: Int = 0xB66
    const val GL_DEPTH_RANGE: Int = 0xB70
    const val GL_DEPTH_TEST: Int = 0xB71
    const val GL_DEPTH_WRITEMASK: Int = 0xB72
    const val GL_DEPTH_CLEAR_VALUE: Int = 0xB73
    const val GL_DEPTH_FUNC: Int = 0xB74
    const val GL_ACCUM_CLEAR_VALUE: Int = 0xB80
    const val GL_STENCIL_TEST: Int = 0xB90
    const val GL_STENCIL_CLEAR_VALUE: Int = 0xB91
    const val GL_STENCIL_FUNC: Int = 0xB92
    const val GL_STENCIL_VALUE_MASK: Int = 0xB93
    const val GL_STENCIL_FAIL: Int = 0xB94
    const val GL_STENCIL_PASS_DEPTH_FAIL: Int = 0xB95
    const val GL_STENCIL_PASS_DEPTH_PASS: Int = 0xB96
    const val GL_STENCIL_REF: Int = 0xB97
    const val GL_STENCIL_WRITEMASK: Int = 0xB98
    const val GL_MATRIX_MODE: Int = 0xBA0
    const val GL_NORMALIZE: Int = 0xBA1
    const val GL_VIEWPORT: Int = 0xBA2
    const val GL_MODELVIEW_STACK_DEPTH: Int = 0xBA3
    const val GL_PROJECTION_STACK_DEPTH: Int = 0xBA4
    const val GL_TEXTURE_STACK_DEPTH: Int = 0xBA5
    const val GL_MODELVIEW_MATRIX: Int = 0xBA6
    const val GL_PROJECTION_MATRIX: Int = 0xBA7
    const val GL_TEXTURE_MATRIX: Int = 0xBA8
    const val GL_ATTRIB_STACK_DEPTH: Int = 0xBB0
    const val GL_CLIENT_ATTRIB_STACK_DEPTH: Int = 0xBB1
    const val GL_ALPHA_TEST: Int = 0xBC0
    const val GL_ALPHA_TEST_FUNC: Int = 0xBC1
    const val GL_ALPHA_TEST_REF: Int = 0xBC2
    const val GL_DITHER: Int = 0xBD0
    const val GL_BLEND_DST: Int = 0xBE0
    const val GL_BLEND_SRC: Int = 0xBE1
    const val GL_BLEND: Int = 0xBE2
    const val GL_LOGIC_OP_MODE: Int = 0xBF0
    const val GL_INDEX_LOGIC_OP: Int = 0xBF1
    const val GL_LOGIC_OP: Int = 0xBF1
    const val GL_COLOR_LOGIC_OP: Int = 0xBF2
    const val GL_AUX_BUFFERS: Int = 0xC00
    const val GL_DRAW_BUFFER: Int = 0xC01
    const val GL_READ_BUFFER: Int = 0xC02
    const val GL_SCISSOR_BOX: Int = 0xC10
    const val GL_SCISSOR_TEST: Int = 0xC11
    const val GL_INDEX_CLEAR_VALUE: Int = 0xC20
    const val GL_INDEX_WRITEMASK: Int = 0xC21
    const val GL_COLOR_CLEAR_VALUE: Int = 0xC22
    const val GL_COLOR_WRITEMASK: Int = 0xC23
    const val GL_INDEX_MODE: Int = 0xC30
    const val GL_RGBA_MODE: Int = 0xC31
    const val GL_DOUBLEBUFFER: Int = 0xC32
    const val GL_STEREO: Int = 0xC33
    const val GL_RENDER_MODE: Int = 0xC40
    const val GL_PERSPECTIVE_CORRECTION_HINT: Int = 0xC50
    const val GL_POINT_SMOOTH_HINT: Int = 0xC51
    const val GL_LINE_SMOOTH_HINT: Int = 0xC52
    const val GL_POLYGON_SMOOTH_HINT: Int = 0xC53
    const val GL_FOG_HINT: Int = 0xC54
    const val GL_TEXTURE_GEN_S: Int = 0xC60
    const val GL_TEXTURE_GEN_T: Int = 0xC61
    const val GL_TEXTURE_GEN_R: Int = 0xC62
    const val GL_TEXTURE_GEN_Q: Int = 0xC63
    const val GL_PIXEL_MAP_I_TO_I: Int = 0xC70
    const val GL_PIXEL_MAP_S_TO_S: Int = 0xC71
    const val GL_PIXEL_MAP_I_TO_R: Int = 0xC72
    const val GL_PIXEL_MAP_I_TO_G: Int = 0xC73
    const val GL_PIXEL_MAP_I_TO_B: Int = 0xC74
    const val GL_PIXEL_MAP_I_TO_A: Int = 0xC75
    const val GL_PIXEL_MAP_R_TO_R: Int = 0xC76
    const val GL_PIXEL_MAP_G_TO_G: Int = 0xC77
    const val GL_PIXEL_MAP_B_TO_B: Int = 0xC78
    const val GL_PIXEL_MAP_A_TO_A: Int = 0xC79
    const val GL_PIXEL_MAP_I_TO_I_SIZE: Int = 0xCB0
    const val GL_PIXEL_MAP_S_TO_S_SIZE: Int = 0xCB1
    const val GL_PIXEL_MAP_I_TO_R_SIZE: Int = 0xCB2
    const val GL_PIXEL_MAP_I_TO_G_SIZE: Int = 0xCB3
    const val GL_PIXEL_MAP_I_TO_B_SIZE: Int = 0xCB4
    const val GL_PIXEL_MAP_I_TO_A_SIZE: Int = 0xCB5
    const val GL_PIXEL_MAP_R_TO_R_SIZE: Int = 0xCB6
    const val GL_PIXEL_MAP_G_TO_G_SIZE: Int = 0xCB7
    const val GL_PIXEL_MAP_B_TO_B_SIZE: Int = 0xCB8
    const val GL_PIXEL_MAP_A_TO_A_SIZE: Int = 0xCB9
    const val GL_UNPACK_SWAP_BYTES: Int = 0xCF0
    const val GL_UNPACK_LSB_FIRST: Int = 0xCF1
    const val GL_UNPACK_ROW_LENGTH: Int = 0xCF2
    const val GL_UNPACK_SKIP_ROWS: Int = 0xCF3
    const val GL_UNPACK_SKIP_PIXELS: Int = 0xCF4
    const val GL_UNPACK_ALIGNMENT: Int = 0xCF5
    const val GL_PACK_SWAP_BYTES: Int = 0xD00
    const val GL_PACK_LSB_FIRST: Int = 0xD01
    const val GL_PACK_ROW_LENGTH: Int = 0xD02
    const val GL_PACK_SKIP_ROWS: Int = 0xD03
    const val GL_PACK_SKIP_PIXELS: Int = 0xD04
    const val GL_PACK_ALIGNMENT: Int = 0xD05
    const val GL_MAP_COLOR: Int = 0xD10
    const val GL_MAP_STENCIL: Int = 0xD11
    const val GL_INDEX_SHIFT: Int = 0xD12
    const val GL_INDEX_OFFSET: Int = 0xD13
    const val GL_RED_SCALE: Int = 0xD14
    const val GL_RED_BIAS: Int = 0xD15
    const val GL_ZOOM_X: Int = 0xD16
    const val GL_ZOOM_Y: Int = 0xD17
    const val GL_GREEN_SCALE: Int = 0xD18
    const val GL_GREEN_BIAS: Int = 0xD19
    const val GL_BLUE_SCALE: Int = 0xD1A
    const val GL_BLUE_BIAS: Int = 0xD1B
    const val GL_ALPHA_SCALE: Int = 0xD1C
    const val GL_ALPHA_BIAS: Int = 0xD1D
    const val GL_DEPTH_SCALE: Int = 0xD1E
    const val GL_DEPTH_BIAS: Int = 0xD1F
    const val GL_MAX_EVAL_ORDER: Int = 0xD30
    const val GL_MAX_LIGHTS: Int = 0xD31
    const val GL_MAX_CLIP_PLANES: Int = 0xD32
    const val GL_MAX_TEXTURE_SIZE: Int = 0xD33
    const val GL_MAX_PIXEL_MAP_TABLE: Int = 0xD34
    const val GL_MAX_ATTRIB_STACK_DEPTH: Int = 0xD35
    const val GL_MAX_MODELVIEW_STACK_DEPTH: Int = 0xD36
    const val GL_MAX_NAME_STACK_DEPTH: Int = 0xD37
    const val GL_MAX_PROJECTION_STACK_DEPTH: Int = 0xD38
    const val GL_MAX_TEXTURE_STACK_DEPTH: Int = 0xD39
    const val GL_MAX_VIEWPORT_DIMS: Int = 0xD3A
    const val GL_MAX_CLIENT_ATTRIB_STACK_DEPTH: Int = 0xD3B
    const val GL_SUBPIXEL_BITS: Int = 0xD50
    const val GL_INDEX_BITS: Int = 0xD51
    const val GL_RED_BITS: Int = 0xD52
    const val GL_GREEN_BITS: Int = 0xD53
    const val GL_BLUE_BITS: Int = 0xD54
    const val GL_ALPHA_BITS: Int = 0xD55
    const val GL_DEPTH_BITS: Int = 0xD56
    const val GL_STENCIL_BITS: Int = 0xD57
    const val GL_ACCUM_RED_BITS: Int = 0xD58
    const val GL_ACCUM_GREEN_BITS: Int = 0xD59
    const val GL_ACCUM_BLUE_BITS: Int = 0xD5A
    const val GL_ACCUM_ALPHA_BITS: Int = 0xD5B
    const val GL_NAME_STACK_DEPTH: Int = 0xD70
    const val GL_AUTO_NORMAL: Int = 0xD80
    const val GL_MAP1_COLOR_4: Int = 0xD90
    const val GL_MAP1_INDEX: Int = 0xD91
    const val GL_MAP1_NORMAL: Int = 0xD92
    const val GL_MAP1_TEXTURE_COORD_1: Int = 0xD93
    const val GL_MAP1_TEXTURE_COORD_2: Int = 0xD94
    const val GL_MAP1_TEXTURE_COORD_3: Int = 0xD95
    const val GL_MAP1_TEXTURE_COORD_4: Int = 0xD96
    const val GL_MAP1_VERTEX_3: Int = 0xD97
    const val GL_MAP1_VERTEX_4: Int = 0xD98
    const val GL_MAP2_COLOR_4: Int = 0xDB0
    const val GL_MAP2_INDEX: Int = 0xDB1
    const val GL_MAP2_NORMAL: Int = 0xDB2
    const val GL_MAP2_TEXTURE_COORD_1: Int = 0xDB3
    const val GL_MAP2_TEXTURE_COORD_2: Int = 0xDB4
    const val GL_MAP2_TEXTURE_COORD_3: Int = 0xDB5
    const val GL_MAP2_TEXTURE_COORD_4: Int = 0xDB6
    const val GL_MAP2_VERTEX_3: Int = 0xDB7
    const val GL_MAP2_VERTEX_4: Int = 0xDB8
    const val GL_MAP1_GRID_DOMAIN: Int = 0xDD0
    const val GL_MAP1_GRID_SEGMENTS: Int = 0xDD1
    const val GL_MAP2_GRID_DOMAIN: Int = 0xDD2
    const val GL_MAP2_GRID_SEGMENTS: Int = 0xDD3
    const val GL_TEXTURE_1D: Int = 0xDE0
    const val GL_TEXTURE_2D: Int = 0xDE1
    const val GL_FEEDBACK_BUFFER_POINTER: Int = 0xDF0
    const val GL_FEEDBACK_BUFFER_SIZE: Int = 0xDF1
    const val GL_FEEDBACK_BUFFER_TYPE: Int = 0xDF2
    const val GL_SELECTION_BUFFER_POINTER: Int = 0xDF3
    const val GL_SELECTION_BUFFER_SIZE: Int = 0xDF4

    /** GetTextureParameter  */
    const val GL_TEXTURE_WIDTH: Int = 0x1000
    const val GL_TEXTURE_HEIGHT: Int = 0x1001
    const val GL_TEXTURE_INTERNAL_FORMAT: Int = 0x1003
    const val GL_TEXTURE_COMPONENTS: Int = 0x1003
    const val GL_TEXTURE_BORDER_COLOR: Int = 0x1004
    const val GL_TEXTURE_BORDER: Int = 0x1005

    /** HintMode  */
    const val GL_DONT_CARE: Int = 0x1100
    const val GL_FASTEST: Int = 0x1101
    const val GL_NICEST: Int = 0x1102

    /** LightName  */
    const val GL_LIGHT0: Int = 0x4000
    const val GL_LIGHT1: Int = 0x4001
    const val GL_LIGHT2: Int = 0x4002
    const val GL_LIGHT3: Int = 0x4003
    const val GL_LIGHT4: Int = 0x4004
    const val GL_LIGHT5: Int = 0x4005
    const val GL_LIGHT6: Int = 0x4006
    const val GL_LIGHT7: Int = 0x4007

    /** LightParameter  */
    const val GL_AMBIENT: Int = 0x1200
    const val GL_DIFFUSE: Int = 0x1201
    const val GL_SPECULAR: Int = 0x1202
    const val GL_POSITION: Int = 0x1203
    const val GL_SPOT_DIRECTION: Int = 0x1204
    const val GL_SPOT_EXPONENT: Int = 0x1205
    const val GL_SPOT_CUTOFF: Int = 0x1206
    const val GL_CONSTANT_ATTENUATION: Int = 0x1207
    const val GL_LINEAR_ATTENUATION: Int = 0x1208
    const val GL_QUADRATIC_ATTENUATION: Int = 0x1209

    /** ListMode  */
    const val GL_COMPILE: Int = 0x1300
    const val GL_COMPILE_AND_EXECUTE: Int = 0x1301

    /** LogicOp  */
    const val GL_CLEAR: Int = 0x1500
    const val GL_AND: Int = 0x1501
    const val GL_AND_REVERSE: Int = 0x1502
    const val GL_COPY: Int = 0x1503
    const val GL_AND_INVERTED: Int = 0x1504
    const val GL_NOOP: Int = 0x1505
    const val GL_XOR: Int = 0x1506
    const val GL_OR: Int = 0x1507
    const val GL_NOR: Int = 0x1508
    const val GL_EQUIV: Int = 0x1509
    const val GL_INVERT: Int = 0x150A
    const val GL_OR_REVERSE: Int = 0x150B
    const val GL_COPY_INVERTED: Int = 0x150C
    const val GL_OR_INVERTED: Int = 0x150D
    const val GL_NAND: Int = 0x150E
    const val GL_SET: Int = 0x150F

    /** MaterialParameter  */
    const val GL_EMISSION: Int = 0x1600
    const val GL_SHININESS: Int = 0x1601
    const val GL_AMBIENT_AND_DIFFUSE: Int = 0x1602
    const val GL_COLOR_INDEXES: Int = 0x1603

    /** MatrixMode  */
    const val GL_MODELVIEW: Int = 0x1700
    const val GL_PROJECTION: Int = 0x1701
    const val GL_TEXTURE: Int = 0x1702

    /** PixelCopyType  */
    const val GL_COLOR: Int = 0x1800
    const val GL_DEPTH: Int = 0x1801
    const val GL_STENCIL: Int = 0x1802

    /** PixelFormat  */
    const val GL_COLOR_INDEX: Int = 0x1900
    const val GL_STENCIL_INDEX: Int = 0x1901
    const val GL_DEPTH_COMPONENT: Int = 0x1902
    const val GL_RED: Int = 0x1903
    const val GL_GREEN: Int = 0x1904
    const val GL_BLUE: Int = 0x1905
    const val GL_ALPHA: Int = 0x1906
    const val GL_RGB: Int = 0x1907
    const val GL_RGBA: Int = 0x1908
    const val GL_LUMINANCE: Int = 0x1909
    const val GL_LUMINANCE_ALPHA: Int = 0x190A

    /** PixelType  */
    const val GL_BITMAP: Int = 0x1A00

    /** PolygonMode  */
    const val GL_POINT: Int = 0x1B00
    const val GL_LINE: Int = 0x1B01
    const val GL_FILL: Int = 0x1B02

    /** RenderingMode  */
    const val GL_RENDER: Int = 0x1C00
    const val GL_FEEDBACK: Int = 0x1C01
    const val GL_SELECT: Int = 0x1C02

    /** ShadingModel  */
    const val GL_FLAT: Int = 0x1D00
    const val GL_SMOOTH: Int = 0x1D01

    /** StencilOp  */
    const val GL_KEEP: Int = 0x1E00
    const val GL_REPLACE: Int = 0x1E01
    const val GL_INCR: Int = 0x1E02
    const val GL_DECR: Int = 0x1E03

    /** StringName  */
    const val GL_VENDOR: Int = 0x1F00
    const val GL_RENDERER: Int = 0x1F01
    const val GL_VERSION: Int = 0x1F02
    const val GL_EXTENSIONS: Int = 0x1F03

    /** TextureCoordName  */
    const val GL_S: Int = 0x2000
    const val GL_T: Int = 0x2001
    const val GL_R: Int = 0x2002
    const val GL_Q: Int = 0x2003

    /** TextureEnvMode  */
    const val GL_MODULATE: Int = 0x2100
    const val GL_DECAL: Int = 0x2101

    /** TextureEnvParameter  */
    const val GL_TEXTURE_ENV_MODE: Int = 0x2200
    const val GL_TEXTURE_ENV_COLOR: Int = 0x2201

    /** TextureEnvTarget  */
    const val GL_TEXTURE_ENV: Int = 0x2300

    /** TextureGenMode  */
    const val GL_EYE_LINEAR: Int = 0x2400
    const val GL_OBJECT_LINEAR: Int = 0x2401
    const val GL_SPHERE_MAP: Int = 0x2402

    /** TextureGenParameter  */
    const val GL_TEXTURE_GEN_MODE: Int = 0x2500
    const val GL_OBJECT_PLANE: Int = 0x2501
    const val GL_EYE_PLANE: Int = 0x2502

    /** TextureMagFilter  */
    const val GL_NEAREST: Int = 0x2600
    const val GL_LINEAR: Int = 0x2601

    /** TextureMinFilter  */
    const val GL_NEAREST_MIPMAP_NEAREST: Int = 0x2700
    const val GL_LINEAR_MIPMAP_NEAREST: Int = 0x2701
    const val GL_NEAREST_MIPMAP_LINEAR: Int = 0x2702
    const val GL_LINEAR_MIPMAP_LINEAR: Int = 0x2703

    /** TextureParameterName  */
    const val GL_TEXTURE_MAG_FILTER: Int = 0x2800
    const val GL_TEXTURE_MIN_FILTER: Int = 0x2801
    const val GL_TEXTURE_WRAP_S: Int = 0x2802
    const val GL_TEXTURE_WRAP_T: Int = 0x2803

    /** TextureWrapMode  */
    const val GL_CLAMP: Int = 0x2900
    const val GL_REPEAT: Int = 0x2901

    /** ClientAttribMask  */
    const val GL_CLIENT_PIXEL_STORE_BIT: Int = 0x1
    const val GL_CLIENT_VERTEX_ARRAY_BIT: Int = 0x2
    const val GL_CLIENT_ALL_ATTRIB_BITS: Int = -0x1

    /** polygon_offset  */
    const val GL_POLYGON_OFFSET_FACTOR: Int = 0x8038
    const val GL_POLYGON_OFFSET_UNITS: Int = 0x2A00
    const val GL_POLYGON_OFFSET_POINT: Int = 0x2A01
    const val GL_POLYGON_OFFSET_LINE: Int = 0x2A02
    const val GL_POLYGON_OFFSET_FILL: Int = 0x8037

    /** texture  */
    const val GL_ALPHA4: Int = 0x803B
    const val GL_ALPHA8: Int = 0x803C
    const val GL_ALPHA12: Int = 0x803D
    const val GL_ALPHA16: Int = 0x803E
    const val GL_LUMINANCE4: Int = 0x803F
    const val GL_LUMINANCE8: Int = 0x8040
    const val GL_LUMINANCE12: Int = 0x8041
    const val GL_LUMINANCE16: Int = 0x8042
    const val GL_LUMINANCE4_ALPHA4: Int = 0x8043
    const val GL_LUMINANCE6_ALPHA2: Int = 0x8044
    const val GL_LUMINANCE8_ALPHA8: Int = 0x8045
    const val GL_LUMINANCE12_ALPHA4: Int = 0x8046
    const val GL_LUMINANCE12_ALPHA12: Int = 0x8047
    const val GL_LUMINANCE16_ALPHA16: Int = 0x8048
    const val GL_INTENSITY: Int = 0x8049
    const val GL_INTENSITY4: Int = 0x804A
    const val GL_INTENSITY8: Int = 0x804B
    const val GL_INTENSITY12: Int = 0x804C
    const val GL_INTENSITY16: Int = 0x804D
    const val GL_R3_G3_B2: Int = 0x2A10
    const val GL_RGB4: Int = 0x804F
    const val GL_RGB5: Int = 0x8050
    const val GL_RGB8: Int = 0x8051
    const val GL_RGB10: Int = 0x8052
    const val GL_RGB12: Int = 0x8053
    const val GL_RGB16: Int = 0x8054
    const val GL_RGBA2: Int = 0x8055
    const val GL_RGBA4: Int = 0x8056
    const val GL_RGB5_A1: Int = 0x8057
    const val GL_RGBA8: Int = 0x8058
    const val GL_RGB10_A2: Int = 0x8059
    const val GL_RGBA12: Int = 0x805A
    const val GL_RGBA16: Int = 0x805B
    const val GL_TEXTURE_RED_SIZE: Int = 0x805C
    const val GL_TEXTURE_GREEN_SIZE: Int = 0x805D
    const val GL_TEXTURE_BLUE_SIZE: Int = 0x805E
    const val GL_TEXTURE_ALPHA_SIZE: Int = 0x805F
    const val GL_TEXTURE_LUMINANCE_SIZE: Int = 0x8060
    const val GL_TEXTURE_INTENSITY_SIZE: Int = 0x8061
    const val GL_PROXY_TEXTURE_1D: Int = 0x8063
    const val GL_PROXY_TEXTURE_2D: Int = 0x8064

    /** texture_object  */
    const val GL_TEXTURE_PRIORITY: Int = 0x8066
    const val GL_TEXTURE_RESIDENT: Int = 0x8067
    const val GL_TEXTURE_BINDING_1D: Int = 0x8068
    const val GL_TEXTURE_BINDING_2D: Int = 0x8069

    /** vertex_array  */
    const val GL_VERTEX_ARRAY: Int = 0x8074
    const val GL_NORMAL_ARRAY: Int = 0x8075
    const val GL_COLOR_ARRAY: Int = 0x8076
    const val GL_INDEX_ARRAY: Int = 0x8077
    const val GL_TEXTURE_COORD_ARRAY: Int = 0x8078
    const val GL_EDGE_FLAG_ARRAY: Int = 0x8079
    const val GL_VERTEX_ARRAY_SIZE: Int = 0x807A
    const val GL_VERTEX_ARRAY_TYPE: Int = 0x807B
    const val GL_VERTEX_ARRAY_STRIDE: Int = 0x807C
    const val GL_NORMAL_ARRAY_TYPE: Int = 0x807E
    const val GL_NORMAL_ARRAY_STRIDE: Int = 0x807F
    const val GL_COLOR_ARRAY_SIZE: Int = 0x8081
    const val GL_COLOR_ARRAY_TYPE: Int = 0x8082
    const val GL_COLOR_ARRAY_STRIDE: Int = 0x8083
    const val GL_INDEX_ARRAY_TYPE: Int = 0x8085
    const val GL_INDEX_ARRAY_STRIDE: Int = 0x8086
    const val GL_TEXTURE_COORD_ARRAY_SIZE: Int = 0x8088
    const val GL_TEXTURE_COORD_ARRAY_TYPE: Int = 0x8089
    const val GL_TEXTURE_COORD_ARRAY_STRIDE: Int = 0x808A
    const val GL_EDGE_FLAG_ARRAY_STRIDE: Int = 0x808C
    const val GL_VERTEX_ARRAY_POINTER: Int = 0x808E
    const val GL_NORMAL_ARRAY_POINTER: Int = 0x808F
    const val GL_COLOR_ARRAY_POINTER: Int = 0x8090
    const val GL_INDEX_ARRAY_POINTER: Int = 0x8091
    const val GL_TEXTURE_COORD_ARRAY_POINTER: Int = 0x8092
    const val GL_EDGE_FLAG_ARRAY_POINTER: Int = 0x8093
    const val GL_V2F: Int = 0x2A20
    const val GL_V3F: Int = 0x2A21
    const val GL_C4UB_V2F: Int = 0x2A22
    const val GL_C4UB_V3F: Int = 0x2A23
    const val GL_C3F_V3F: Int = 0x2A24
    const val GL_N3F_V3F: Int = 0x2A25
    const val GL_C4F_N3F_V3F: Int = 0x2A26
    const val GL_T2F_V3F: Int = 0x2A27
    const val GL_T4F_V4F: Int = 0x2A28
    const val GL_T2F_C4UB_V3F: Int = 0x2A29
    const val GL_T2F_C3F_V3F: Int = 0x2A2A
    const val GL_T2F_N3F_V3F: Int = 0x2A2B
    const val GL_T2F_C4F_N3F_V3F: Int = 0x2A2C
    const val GL_T4F_C4F_N3F_V4F: Int = 0x2A2D

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

    /** Accepted by the `internalformat` parameter of TexImage1D, TexImage2D, TexImage3D, CopyTexImage1D, and CopyTexImage2D.  */
    const val GL_COMPRESSED_ALPHA: Int = 0x84E9
    const val GL_COMPRESSED_LUMINANCE: Int = 0x84EA
    const val GL_COMPRESSED_LUMINANCE_ALPHA: Int = 0x84EB
    const val GL_COMPRESSED_INTENSITY: Int = 0x84EC
    const val GL_COMPRESSED_RGB: Int = 0x84ED
    const val GL_COMPRESSED_RGBA: Int = 0x84EE

    /** Accepted by the `target` parameter of Hint and the `value` parameter of GetIntegerv, GetBooleanv, GetFloatv, and GetDoublev.  */
    const val GL_TEXTURE_COMPRESSION_HINT: Int = 0x84EF

    /** Accepted by the `value` parameter of GetTexLevelParameter.  */
    const val GL_TEXTURE_COMPRESSED_IMAGE_SIZE: Int = 0x86A0
    const val GL_TEXTURE_COMPRESSED: Int = 0x86A1

    /** Accepted by the `value` parameter of GetIntegerv, GetBooleanv, GetFloatv, and GetDoublev.  */
    const val GL_NUM_COMPRESSED_TEXTURE_FORMATS: Int = 0x86A2
    const val GL_COMPRESSED_TEXTURE_FORMATS: Int = 0x86A3

    /** Accepted by the `param` parameters of TexGend, TexGenf, and TexGeni when `pname` parameter is TEXTURE_GEN_MODE.  */
    const val GL_NORMAL_MAP: Int = 0x8511
    const val GL_REFLECTION_MAP: Int = 0x8512

    /**
     * When the `pname` parameter of TexGendv, TexGenfv, and TexGeniv is TEXTURE_GEN_MODE, then the array `params` may also contain NORMAL_MAP
     * or REFLECTION_MAP. Accepted by the `cap` parameter of Enable, Disable, IsEnabled, and by the `pname` parameter of GetBooleanv,
     * GetIntegerv, GetFloatv, and GetDoublev, and by the `target` parameter of BindTexture, GetTexParameterfv, GetTexParameteriv, TexParameterf,
     * TexParameteri, TexParameterfv, and TexParameteriv.
     */
    const val GL_TEXTURE_CUBE_MAP: Int = 0x8513

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_TEXTURE_BINDING_CUBE_MAP: Int = 0x8514

    /**
     * Accepted by the `target` parameter of GetTexImage, GetTexLevelParameteriv, GetTexLevelParameterfv, TexImage2D, CopyTexImage2D, TexSubImage2D, and
     * CopySubTexImage2D.
     */
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_X: Int = 0x8515
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_X: Int = 0x8516
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Y: Int = 0x8517
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Y: Int = 0x8518
    const val GL_TEXTURE_CUBE_MAP_POSITIVE_Z: Int = 0x8519
    const val GL_TEXTURE_CUBE_MAP_NEGATIVE_Z: Int = 0x851A

    /** Accepted by the `target` parameter of GetTexLevelParameteriv, GetTexLevelParameterfv, GetTexParameteriv, and TexImage2D.  */
    const val GL_PROXY_TEXTURE_CUBE_MAP: Int = 0x851B

    /** Accepted by the `pname` parameter of GetBooleanv, GetDoublev, GetIntegerv, and GetFloatv.  */
    const val GL_MAX_CUBE_MAP_TEXTURE_SIZE: Int = 0x851C

    /**
     * Accepted by the `cap` parameter of Enable, Disable, and IsEnabled, and by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev.
     */
    const val GL_MULTISAMPLE: Int = 0x809D
    const val GL_SAMPLE_ALPHA_TO_COVERAGE: Int = 0x809E
    const val GL_SAMPLE_ALPHA_TO_ONE: Int = 0x809F
    const val GL_SAMPLE_COVERAGE: Int = 0x80A0

    /** Accepted by the `mask` parameter of PushAttrib.  */
    const val GL_MULTISAMPLE_BIT: Int = 0x20000000

    /** Accepted by the `pname` parameter of GetBooleanv, GetDoublev, GetIntegerv, and GetFloatv.  */
    const val GL_SAMPLE_BUFFERS: Int = 0x80A8
    const val GL_SAMPLES: Int = 0x80A9
    const val GL_SAMPLE_COVERAGE_VALUE: Int = 0x80AA
    const val GL_SAMPLE_COVERAGE_INVERT: Int = 0x80AB

    /** Accepted by the `texture` parameter of ActiveTexture and MultiTexCoord.  */
    const val GL_TEXTURE0: Int = 0x84C0
    const val GL_TEXTURE1: Int = 0x84C1
    const val GL_TEXTURE2: Int = 0x84C2
    const val GL_TEXTURE3: Int = 0x84C3
    const val GL_TEXTURE4: Int = 0x84C4
    const val GL_TEXTURE5: Int = 0x84C5
    const val GL_TEXTURE6: Int = 0x84C6
    const val GL_TEXTURE7: Int = 0x84C7
    const val GL_TEXTURE8: Int = 0x84C8
    const val GL_TEXTURE9: Int = 0x84C9
    const val GL_TEXTURE10: Int = 0x84CA
    const val GL_TEXTURE11: Int = 0x84CB
    const val GL_TEXTURE12: Int = 0x84CC
    const val GL_TEXTURE13: Int = 0x84CD
    const val GL_TEXTURE14: Int = 0x84CE
    const val GL_TEXTURE15: Int = 0x84CF
    const val GL_TEXTURE16: Int = 0x84D0
    const val GL_TEXTURE17: Int = 0x84D1
    const val GL_TEXTURE18: Int = 0x84D2
    const val GL_TEXTURE19: Int = 0x84D3
    const val GL_TEXTURE20: Int = 0x84D4
    const val GL_TEXTURE21: Int = 0x84D5
    const val GL_TEXTURE22: Int = 0x84D6
    const val GL_TEXTURE23: Int = 0x84D7
    const val GL_TEXTURE24: Int = 0x84D8
    const val GL_TEXTURE25: Int = 0x84D9
    const val GL_TEXTURE26: Int = 0x84DA
    const val GL_TEXTURE27: Int = 0x84DB
    const val GL_TEXTURE28: Int = 0x84DC
    const val GL_TEXTURE29: Int = 0x84DD
    const val GL_TEXTURE30: Int = 0x84DE
    const val GL_TEXTURE31: Int = 0x84DF

    /** Accepted by the `pname` parameter of GetBooleanv, GetDoublev, GetIntegerv, and GetFloatv.  */
    const val GL_ACTIVE_TEXTURE: Int = 0x84E0
    const val GL_CLIENT_ACTIVE_TEXTURE: Int = 0x84E1
    const val GL_MAX_TEXTURE_UNITS: Int = 0x84E2

    /** Accepted by the `params` parameter of TexEnvf, TexEnvi, TexEnvfv, and TexEnviv when the `pname` parameter value is TEXTURE_ENV_MODE.  */
    const val GL_COMBINE: Int = 0x8570

    /** Accepted by the `pname` parameter of TexEnvf, TexEnvi, TexEnvfv, and TexEnviv when the `target` parameter value is TEXTURE_ENV.  */
    const val GL_COMBINE_RGB: Int = 0x8571
    const val GL_COMBINE_ALPHA: Int = 0x8572
    const val GL_SOURCE0_RGB: Int = 0x8580
    const val GL_SOURCE1_RGB: Int = 0x8581
    const val GL_SOURCE2_RGB: Int = 0x8582
    const val GL_SOURCE0_ALPHA: Int = 0x8588
    const val GL_SOURCE1_ALPHA: Int = 0x8589
    const val GL_SOURCE2_ALPHA: Int = 0x858A
    const val GL_OPERAND0_RGB: Int = 0x8590
    const val GL_OPERAND1_RGB: Int = 0x8591
    const val GL_OPERAND2_RGB: Int = 0x8592
    const val GL_OPERAND0_ALPHA: Int = 0x8598
    const val GL_OPERAND1_ALPHA: Int = 0x8599
    const val GL_OPERAND2_ALPHA: Int = 0x859A
    const val GL_RGB_SCALE: Int = 0x8573

    /**
     * Accepted by the `params` parameter of TexEnvf, TexEnvi, TexEnvfv, and TexEnviv when the `pname` parameter value is COMBINE_RGB or
     * COMBINE_ALPHA.
     */
    const val GL_ADD_SIGNED: Int = 0x8574
    const val GL_INTERPOLATE: Int = 0x8575
    const val GL_SUBTRACT: Int = 0x84E7

    /**
     * Accepted by the `params` parameter of TexEnvf, TexEnvi, TexEnvfv, and TexEnviv when the `pname` parameter value is SOURCE0_RGB,
     * SOURCE1_RGB, SOURCE2_RGB, SOURCE0_ALPHA, SOURCE1_ALPHA, or SOURCE2_ALPHA.
     */
    const val GL_CONSTANT: Int = 0x8576
    const val GL_PRIMARY_COLOR: Int = 0x8577
    const val GL_PREVIOUS: Int = 0x8578

    /** Accepted by the `params` parameter of TexEnvf, TexEnvi, TexEnvfv, and TexEnviv when the `pname` parameter value is COMBINE_RGB_ARB.  */
    const val GL_DOT3_RGB: Int = 0x86AE
    const val GL_DOT3_RGBA: Int = 0x86AF

    /**
     * Accepted by the `param` parameter of TexParameteri and TexParameterf, and by the `params` parameter of TexParameteriv and TexParameterfv,
     * when their `pname` parameter is TEXTURE_WRAP_S, TEXTURE_WRAP_T, or TEXTURE_WRAP_R.
     */
    const val GL_CLAMP_TO_BORDER: Int = 0x812D

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_TRANSPOSE_MODELVIEW_MATRIX: Int = 0x84E3
    const val GL_TRANSPOSE_PROJECTION_MATRIX: Int = 0x84E4
    const val GL_TRANSPOSE_TEXTURE_MATRIX: Int = 0x84E5
    const val GL_TRANSPOSE_COLOR_MATRIX: Int = 0x84E6

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

    /** New token names.  */
    const val GL_FOG_COORD_SRC: Int = 0x8450
    const val GL_FOG_COORD: Int = 0x8451
    const val GL_CURRENT_FOG_COORD: Int = 0x8453
    const val GL_FOG_COORD_ARRAY_TYPE: Int = 0x8454
    const val GL_FOG_COORD_ARRAY_STRIDE: Int = 0x8455
    const val GL_FOG_COORD_ARRAY_POINTER: Int = 0x8456
    const val GL_FOG_COORD_ARRAY: Int = 0x8457
    const val GL_FOG_COORD_ARRAY_BUFFER_BINDING: Int = 0x889D
    const val GL_SRC0_RGB: Int = 0x8580
    const val GL_SRC1_RGB: Int = 0x8581
    const val GL_SRC2_RGB: Int = 0x8582
    const val GL_SRC0_ALPHA: Int = 0x8588
    const val GL_SRC1_ALPHA: Int = 0x8589
    const val GL_SRC2_ALPHA: Int = 0x858A

    /**
     * Accepted by the `target` parameters of BindBuffer, BufferData, BufferSubData, MapBuffer, UnmapBuffer, GetBufferSubData,
     * GetBufferParameteriv, and GetBufferPointerv.
     */
    const val GL_ARRAY_BUFFER: Int = 0x8892
    const val GL_ELEMENT_ARRAY_BUFFER: Int = 0x8893

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_ARRAY_BUFFER_BINDING: Int = 0x8894
    const val GL_ELEMENT_ARRAY_BUFFER_BINDING: Int = 0x8895
    const val GL_VERTEX_ARRAY_BUFFER_BINDING: Int = 0x8896
    const val GL_NORMAL_ARRAY_BUFFER_BINDING: Int = 0x8897
    const val GL_COLOR_ARRAY_BUFFER_BINDING: Int = 0x8898
    const val GL_INDEX_ARRAY_BUFFER_BINDING: Int = 0x8899
    const val GL_TEXTURE_COORD_ARRAY_BUFFER_BINDING: Int = 0x889A
    const val GL_EDGE_FLAG_ARRAY_BUFFER_BINDING: Int = 0x889B
    const val GL_SECONDARY_COLOR_ARRAY_BUFFER_BINDING: Int = 0x889C
    const val GL_FOG_COORDINATE_ARRAY_BUFFER_BINDING: Int = 0x889D
    const val GL_WEIGHT_ARRAY_BUFFER_BINDING: Int = 0x889E

    /** Accepted by the `pname` parameter of GetVertexAttribiv.  */
    const val GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING: Int = 0x889F

    /** Accepted by the `usage` parameter of BufferData.  */
    const val GL_STREAM_DRAW: Int = 0x88E0
    const val GL_STREAM_READ: Int = 0x88E1
    const val GL_STREAM_COPY: Int = 0x88E2
    const val GL_STATIC_DRAW: Int = 0x88E4
    const val GL_STATIC_READ: Int = 0x88E5
    const val GL_STATIC_COPY: Int = 0x88E6
    const val GL_DYNAMIC_DRAW: Int = 0x88E8
    const val GL_DYNAMIC_READ: Int = 0x88E9
    const val GL_DYNAMIC_COPY: Int = 0x88EA

    /** Accepted by the `access` parameter of MapBuffer.  */
    const val GL_READ_ONLY: Int = 0x88B8
    const val GL_WRITE_ONLY: Int = 0x88B9
    const val GL_READ_WRITE: Int = 0x88BA

    /** Accepted by the `pname` parameter of GetBufferParameteriv.  */
    const val GL_BUFFER_SIZE: Int = 0x8764
    const val GL_BUFFER_USAGE: Int = 0x8765
    const val GL_BUFFER_ACCESS: Int = 0x88BB
    const val GL_BUFFER_MAPPED: Int = 0x88BC

    /** Accepted by the `pname` parameter of GetBufferPointerv.  */
    const val GL_BUFFER_MAP_POINTER: Int = 0x88BD

    /** Accepted by the `target` parameter of BeginQuery, EndQuery, and GetQueryiv.  */
    const val GL_SAMPLES_PASSED: Int = 0x8914

    /** Accepted by the `pname` parameter of GetQueryiv.  */
    const val GL_QUERY_COUNTER_BITS: Int = 0x8864
    const val GL_CURRENT_QUERY: Int = 0x8865

    /** Accepted by the `pname` parameter of GetQueryObjectiv and GetQueryObjectuiv.  */
    const val GL_QUERY_RESULT: Int = 0x8866
    const val GL_QUERY_RESULT_AVAILABLE: Int = 0x8867

    /** Accepted by the `name` parameter of GetString.  */
    const val GL_SHADING_LANGUAGE_VERSION: Int = 0x8B8C

    /** Accepted by the `pname` parameter of GetInteger.  */
    const val GL_CURRENT_PROGRAM: Int = 0x8B8D

    /** Accepted by the `pname` parameter of GetShaderiv.  */
    const val GL_SHADER_TYPE: Int = 0x8B4F
    const val GL_DELETE_STATUS: Int = 0x8B80
    const val GL_COMPILE_STATUS: Int = 0x8B81
    const val GL_LINK_STATUS: Int = 0x8B82
    const val GL_VALIDATE_STATUS: Int = 0x8B83
    const val GL_INFO_LOG_LENGTH: Int = 0x8B84
    const val GL_ATTACHED_SHADERS: Int = 0x8B85
    const val GL_ACTIVE_UNIFORMS: Int = 0x8B86
    const val GL_ACTIVE_UNIFORM_MAX_LENGTH: Int = 0x8B87
    const val GL_ACTIVE_ATTRIBUTES: Int = 0x8B89
    const val GL_ACTIVE_ATTRIBUTE_MAX_LENGTH: Int = 0x8B8A
    const val GL_SHADER_SOURCE_LENGTH: Int = 0x8B88

    /** Returned by the `type` parameter of GetActiveUniform.  */
    const val GL_FLOAT_VEC2: Int = 0x8B50
    const val GL_FLOAT_VEC3: Int = 0x8B51
    const val GL_FLOAT_VEC4: Int = 0x8B52
    const val GL_INT_VEC2: Int = 0x8B53
    const val GL_INT_VEC3: Int = 0x8B54
    const val GL_INT_VEC4: Int = 0x8B55
    const val GL_BOOL: Int = 0x8B56
    const val GL_BOOL_VEC2: Int = 0x8B57
    const val GL_BOOL_VEC3: Int = 0x8B58
    const val GL_BOOL_VEC4: Int = 0x8B59
    const val GL_FLOAT_MAT2: Int = 0x8B5A
    const val GL_FLOAT_MAT3: Int = 0x8B5B
    const val GL_FLOAT_MAT4: Int = 0x8B5C
    const val GL_SAMPLER_1D: Int = 0x8B5D
    const val GL_SAMPLER_2D: Int = 0x8B5E
    const val GL_SAMPLER_3D: Int = 0x8B5F
    const val GL_SAMPLER_CUBE: Int = 0x8B60
    const val GL_SAMPLER_1D_SHADOW: Int = 0x8B61
    const val GL_SAMPLER_2D_SHADOW: Int = 0x8B62

    /** Accepted by the `type` argument of CreateShader and returned by the `params` parameter of GetShaderiv.  */
    const val GL_VERTEX_SHADER: Int = 0x8B31

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_MAX_VERTEX_UNIFORM_COMPONENTS: Int = 0x8B4A
    const val GL_MAX_VARYING_FLOATS: Int = 0x8B4B
    const val GL_MAX_VERTEX_ATTRIBS: Int = 0x8869
    const val GL_MAX_TEXTURE_IMAGE_UNITS: Int = 0x8872
    const val GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int = 0x8B4C
    const val GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS: Int = 0x8B4D
    const val GL_MAX_TEXTURE_COORDS: Int = 0x8871

    /**
     * Accepted by the `cap` parameter of Disable, Enable, and IsEnabled, and by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev.
     */
    const val GL_VERTEX_PROGRAM_POINT_SIZE: Int = 0x8642
    const val GL_VERTEX_PROGRAM_TWO_SIDE: Int = 0x8643

    /** Accepted by the `pname` parameter of GetVertexAttrib{dfi}v.  */
    const val GL_VERTEX_ATTRIB_ARRAY_ENABLED: Int = 0x8622
    const val GL_VERTEX_ATTRIB_ARRAY_SIZE: Int = 0x8623
    const val GL_VERTEX_ATTRIB_ARRAY_STRIDE: Int = 0x8624
    const val GL_VERTEX_ATTRIB_ARRAY_TYPE: Int = 0x8625
    const val GL_VERTEX_ATTRIB_ARRAY_NORMALIZED: Int = 0x886A
    const val GL_CURRENT_VERTEX_ATTRIB: Int = 0x8626

    /** Accepted by the `pname` parameter of GetVertexAttribPointerv.  */
    const val GL_VERTEX_ATTRIB_ARRAY_POINTER: Int = 0x8645

    /** Accepted by the `type` argument of CreateShader and returned by the `params` parameter of GetShaderiv.  */
    const val GL_FRAGMENT_SHADER: Int = 0x8B30

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_MAX_FRAGMENT_UNIFORM_COMPONENTS: Int = 0x8B49

    /** Accepted by the `target` parameter of Hint and the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_FRAGMENT_SHADER_DERIVATIVE_HINT: Int = 0x8B8B

    /** Accepted by the `pname` parameters of GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_MAX_DRAW_BUFFERS: Int = 0x8824
    const val GL_DRAW_BUFFER0: Int = 0x8825
    const val GL_DRAW_BUFFER1: Int = 0x8826
    const val GL_DRAW_BUFFER2: Int = 0x8827
    const val GL_DRAW_BUFFER3: Int = 0x8828
    const val GL_DRAW_BUFFER4: Int = 0x8829
    const val GL_DRAW_BUFFER5: Int = 0x882A
    const val GL_DRAW_BUFFER6: Int = 0x882B
    const val GL_DRAW_BUFFER7: Int = 0x882C
    const val GL_DRAW_BUFFER8: Int = 0x882D
    const val GL_DRAW_BUFFER9: Int = 0x882E
    const val GL_DRAW_BUFFER10: Int = 0x882F
    const val GL_DRAW_BUFFER11: Int = 0x8830
    const val GL_DRAW_BUFFER12: Int = 0x8831
    const val GL_DRAW_BUFFER13: Int = 0x8832
    const val GL_DRAW_BUFFER14: Int = 0x8833
    const val GL_DRAW_BUFFER15: Int = 0x8834

    /**
     * Accepted by the `cap` parameter of Enable, Disable, and IsEnabled, by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and
     * GetDoublev, and by the `target` parameter of TexEnvi, TexEnviv, TexEnvf, TexEnvfv, GetTexEnviv, and GetTexEnvfv.
     */
    const val GL_POINT_SPRITE: Int = 0x8861

    /**
     * When the `target` parameter of TexEnvf, TexEnvfv, TexEnvi, TexEnviv, GetTexEnvfv, or GetTexEnviv is POINT_SPRITE, then the value of
     * `pname` may be.
     */
    const val GL_COORD_REPLACE: Int = 0x8862

    /** Accepted by the `pname` parameter of PointParameter{if}v.  */
    const val GL_POINT_SPRITE_COORD_ORIGIN: Int = 0x8CA0

    /** Accepted by the `param` parameter of PointParameter{if}v.  */
    const val GL_LOWER_LEFT: Int = 0x8CA1
    const val GL_UPPER_LEFT: Int = 0x8CA2

    /** Accepted by the `pname` parameter of GetBooleanv, GetIntegerv, GetFloatv, and GetDoublev.  */
    const val GL_BLEND_EQUATION_RGB: Int = 0x8009
    const val GL_BLEND_EQUATION_ALPHA: Int = 0x883D

    /** Accepted by the `pname` parameter of GetIntegerv.  */
    const val GL_STENCIL_BACK_FUNC: Int = 0x8800
    const val GL_STENCIL_BACK_FAIL: Int = 0x8801
    const val GL_STENCIL_BACK_PASS_DEPTH_FAIL: Int = 0x8802
    const val GL_STENCIL_BACK_PASS_DEPTH_PASS: Int = 0x8803
    const val GL_STENCIL_BACK_REF: Int = 0x8CA3
    const val GL_STENCIL_BACK_VALUE_MASK: Int = 0x8CA4
    const val GL_STENCIL_BACK_WRITEMASK: Int = 0x8CA5

    /** GetTarget  */
    const val GL_MAJOR_VERSION: Int = 0x821B
    const val GL_MINOR_VERSION: Int = 0x821C
    const val GL_NUM_EXTENSIONS: Int = 0x821D
    const val GL_CONTEXT_FLAGS: Int = 0x821E
    const val GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT: Int = 0x1

    /** Renamed tokens.  */
    val GL_COMPARE_REF_TO_TEXTURE: Int = GL_COMPARE_R_TO_TEXTURE
    val GL_CLIP_DISTANCE0: Int = GL_CLIP_PLANE0
    val GL_CLIP_DISTANCE1: Int = GL_CLIP_PLANE1
    val GL_CLIP_DISTANCE2: Int = GL_CLIP_PLANE2
    val GL_CLIP_DISTANCE3: Int = GL_CLIP_PLANE3
    val GL_CLIP_DISTANCE4: Int = GL_CLIP_PLANE4
    val GL_CLIP_DISTANCE5: Int = GL_CLIP_PLANE5
    const val GL_CLIP_DISTANCE6: Int = 0x3006
    const val GL_CLIP_DISTANCE7: Int = 0x3007
    val GL_MAX_CLIP_DISTANCES: Int = GL_MAX_CLIP_PLANES
    val GL_MAX_VARYING_COMPONENTS: Int = GL_MAX_VARYING_FLOATS

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

    const val GL_TEXTURE_MAX_ANISOTROPY: Int = 0x84FE
    const val GL_MAX_TEXTURE_MAX_ANISOTROPY: Int = 0x84FF

    const val GL_TEXTURE_CUBE_MAP_SEAMLESS: Int = 0x884F

}