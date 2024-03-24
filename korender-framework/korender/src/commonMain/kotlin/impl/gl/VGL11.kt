package com.zakgof.korender.impl.gl

import java.nio.ByteBuffer

object VGL11 {

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


    var gl: com.zakgof.korender.impl.gl.IGL11? = null

    /**
     * Constructs a sequence of geometric primitives by successively transferring elements for `count` vertices to the GL.
     * The i<sup>th</sup> element transferred by `DrawElements` will be taken from element `indices[i]` (if no element array buffer is bound), or
     * from the element whose index is stored in the currently bound element array buffer at offset `indices + i`.
     *
     * @param mode    the kind of primitives being constructed. One of:<br></br><table><tr><td>[POINTS][GL11C.GL_POINTS]</td><td>[LINE_STRIP][GL11C.GL_LINE_STRIP]</td><td>[LINE_LOOP][GL11C.GL_LINE_LOOP]</td><td>[LINES][GL11C.GL_LINES]</td><td>[TRIANGLE_STRIP][GL11C.GL_TRIANGLE_STRIP]</td><td>[TRIANGLE_FAN][GL11C.GL_TRIANGLE_FAN]</td></tr><tr><td>[TRIANGLES][GL11C.GL_TRIANGLES]</td><td>[LINES_ADJACENCY][GL32.GL_LINES_ADJACENCY]</td><td>[LINE_STRIP_ADJACENCY][GL32.GL_LINE_STRIP_ADJACENCY]</td><td>[TRIANGLES_ADJACENCY][GL32.GL_TRIANGLES_ADJACENCY]</td><td>[TRIANGLE_STRIP_ADJACENCY][GL32.GL_TRIANGLE_STRIP_ADJACENCY]</td><td>[PATCHES][GL40.GL_PATCHES]</td></tr></table>
     * @param count   the number of vertices to transfer to the GL
     * @param type    indicates the type of index values in `indices`. One of:<br></br><table><tr><td>[UNSIGNED_BYTE][GL11C.GL_UNSIGNED_BYTE]</td><td>[UNSIGNED_SHORT][GL11C.GL_UNSIGNED_SHORT]</td><td>[UNSIGNED_INT][GL11C.GL_UNSIGNED_INT]</td></tr></table>
     * @param indices the index values
     *
     * @see [Reference Page](http://docs.gl/gl4/glDrawElements)
     */
    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glDrawElements(mode, count, type, indices)
    }

    /**
     * Enables the specified OpenGL state.
     *
     * @param target the OpenGL state to enable
     *
     * @see [Reference Page](http://docs.gl/gl4/glEnable)
     */
    fun glEnable(target: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glEnable(target)
    }

    /**
     * Binds the a texture to a texture target.
     *
     *
     * While a texture object is bound, GL operations on the target to which it is bound affect the bound object, and queries of the target to which it is
     * bound return state from the bound object. If texture mapping of the dimensionality of the target to which a texture object is bound is enabled, the
     * state of the bound texture object directs the texturing operation.
     *
     * @param target  the texture target. One of:<br></br><table><tr><td>[TEXTURE_1D][GL11C.GL_TEXTURE_1D]</td><td>[TEXTURE_2D][GL11C.GL_TEXTURE_2D]</td><td>[TEXTURE_1D_ARRAY][GL30.GL_TEXTURE_1D_ARRAY]</td><td>[TEXTURE_RECTANGLE][GL31.GL_TEXTURE_RECTANGLE]</td><td>[TEXTURE_CUBE_MAP][GL13.GL_TEXTURE_CUBE_MAP]</td></tr><tr><td>[TEXTURE_3D][GL12.GL_TEXTURE_3D]</td><td>[TEXTURE_2D_ARRAY][GL30.GL_TEXTURE_2D_ARRAY]</td><td>[TEXTURE_CUBE_MAP_ARRAY][GL40.GL_TEXTURE_CUBE_MAP_ARRAY]</td><td>[TEXTURE_BUFFER][GL31.GL_TEXTURE_BUFFER]</td><td>[TEXTURE_2D_MULTISAMPLE][GL32.GL_TEXTURE_2D_MULTISAMPLE]</td></tr><tr><td>[TEXTURE_2D_MULTISAMPLE_ARRAY][GL32.GL_TEXTURE_2D_MULTISAMPLE_ARRAY]</td></tr></table>
     * @param texture the texture object to bind
     *
     * @see [Reference Page](http://docs.gl/gl4/glBindTexture)
     */
    fun glBindTexture(target: Int, texture: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glBindTexture(target, texture)
    }

    // --- [ glTexParameterf ] ---
    /**
     * Float version of [TexParameteri][.glTexParameteri].
     *
     * @param target the texture target
     * @param pname  the parameter to set
     * @param param  the parameter value
     *
     * @see [Reference Page](http://docs.gl/gl4/glTexParameterf)
     */
    fun glTexParameterf(target: Int, pname: Int, param: Float) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glTexParameterf(target, pname, param)
    }

    /**
     * Deletes texture objects. After a texture object is deleted, it has no contents or dimensionality, and its name is again unused. If a texture that is
     * currently bound to any of the target bindings of [BindTexture][.glBindTexture] is deleted, it is as though [BindTexture][.glBindTexture] had been executed with the
     * same target and texture zero. Additionally, special care must be taken when deleting a texture if any of the images of the texture are attached to a
     * framebuffer object.
     *
     *
     * Unused names in textures that have been marked as used for the purposes of [GenTextures][.glGenTextures] are marked as unused again. Unused names in textures are
     * silently ignored, as is the name zero.
     *
     * @see [Reference Page](http://docs.gl/gl4/glDeleteTextures)
     */
    fun glDeleteTextures(texture: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glDeleteTextures(texture)
    }

    /**
     * Sets the integer value of a pixel store parameter.
     *
     * @param pname the pixel store parameter to set. One of:<br></br><table><tr><td>[UNPACK_SWAP_BYTES][GL11C.GL_UNPACK_SWAP_BYTES]</td><td>[UNPACK_LSB_FIRST][GL11C.GL_UNPACK_LSB_FIRST]</td><td>[UNPACK_ROW_LENGTH][GL11C.GL_UNPACK_ROW_LENGTH]</td></tr><tr><td>[UNPACK_SKIP_ROWS][GL11C.GL_UNPACK_SKIP_ROWS]</td><td>[UNPACK_SKIP_PIXELS][GL11C.GL_UNPACK_SKIP_PIXELS]</td><td>[UNPACK_ALIGNMENT][GL11C.GL_UNPACK_ALIGNMENT]</td></tr><tr><td>[UNPACK_IMAGE_HEIGHT][GL12.GL_UNPACK_IMAGE_HEIGHT]</td><td>[UNPACK_SKIP_IMAGES][GL12.GL_UNPACK_SKIP_IMAGES]</td><td>[UNPACK_COMPRESSED_BLOCK_WIDTH][GL42.GL_UNPACK_COMPRESSED_BLOCK_WIDTH]</td></tr><tr><td>[UNPACK_COMPRESSED_BLOCK_HEIGHT][GL42.GL_UNPACK_COMPRESSED_BLOCK_HEIGHT]</td><td>[UNPACK_COMPRESSED_BLOCK_DEPTH][GL42.GL_UNPACK_COMPRESSED_BLOCK_DEPTH]</td><td>[UNPACK_COMPRESSED_BLOCK_SIZE][GL42.GL_UNPACK_COMPRESSED_BLOCK_SIZE]</td></tr></table>
     * @param param the parameter value
     *
     * @see [Reference Page](http://docs.gl/gl4/glPixelStorei)
     */
    fun glPixelStorei(pname: Int, param: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glPixelStorei(pname, param)
    }

    /**
     * Returns n previously unused texture names in textures. These names are marked as used, for the purposes of GenTextures only, but they acquire texture
     * state and a dimensionality only when they are first bound, just as if they were unused.
     *
     * @see [Reference Page](http://docs.gl/gl4/glGenTextures)
     */
    fun glGenTextures(): Int {
        return com.zakgof.korender.impl.gl.VGL11.gl!!.glGenTextures()
    }

    // --- [ glBlendFunc ] ---
    /**
     * Specifies the weighting factors used by the blend equation, for both RGB and alpha functions and for all draw buffers.
     *
     * @param sfactor the source weighting factor. One of:<br></br><table><tr><td>[ZERO][GL11C.GL_ZERO]</td><td>[ONE][GL11C.GL_ONE]</td><td>[SRC_COLOR][GL11C.GL_SRC_COLOR]</td><td>[ONE_MINUS_SRC_COLOR][GL11C.GL_ONE_MINUS_SRC_COLOR]</td><td>[DST_COLOR][GL11C.GL_DST_COLOR]</td></tr><tr><td>[ONE_MINUS_DST_COLOR][GL11C.GL_ONE_MINUS_DST_COLOR]</td><td>[SRC_ALPHA][GL11C.GL_SRC_ALPHA]</td><td>[ONE_MINUS_SRC_ALPHA][GL11C.GL_ONE_MINUS_SRC_ALPHA]</td><td>[DST_ALPHA][GL11C.GL_DST_ALPHA]</td><td>[ONE_MINUS_DST_ALPHA][GL11C.GL_ONE_MINUS_DST_ALPHA]</td></tr><tr><td>[CONSTANT_COLOR][GL14.GL_CONSTANT_COLOR]</td><td>[ONE_MINUS_CONSTANT_COLOR][GL14.GL_ONE_MINUS_CONSTANT_COLOR]</td><td>[CONSTANT_ALPHA][GL14.GL_CONSTANT_ALPHA]</td><td>[ONE_MINUS_CONSTANT_ALPHA][GL14.GL_ONE_MINUS_CONSTANT_ALPHA]</td><td>[SRC_ALPHA_SATURATE][GL11C.GL_SRC_ALPHA_SATURATE]</td></tr><tr><td>[SRC1_COLOR][GL33.GL_SRC1_COLOR]</td><td>[ONE_MINUS_SRC1_COLOR][GL33.GL_ONE_MINUS_SRC1_COLOR]</td><td>[SRC1_ALPHA][GL15.GL_SRC1_ALPHA]</td><td>[ONE_MINUS_SRC1_ALPHA][GL33.GL_ONE_MINUS_SRC1_ALPHA]</td></tr></table>
     * @param dfactor the destination weighting factor
     *
     * @see [Reference Page](http://docs.gl/gl4/glBlendFunc)
     */
    fun glBlendFunc(sfactor: Int, dfactor: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glBlendFunc(sfactor, dfactor)
    }

    /**
     * Masks the writing of depth values to the depth buffer. In the initial state, the depth buffer is enabled for writing.
     *
     * @param flag whether depth values are written or not.
     *
     * @see [Reference Page](http://docs.gl/gl4/glDepthMask)
     */
    fun glDepthMask(flag: Boolean) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glDepthMask(flag)
    }

    /**
     * Specifies which polygon faces are culled if [CULL_FACE][GL11C.GL_CULL_FACE] is enabled. Front-facing polygons are rasterized if either culling is disabled or the
     * CullFace mode is [BACK][GL11C.GL_BACK] while back-facing polygons are rasterized only if either culling is disabled or the CullFace mode is
     * [FRONT][GL11C.GL_FRONT]. The initial setting of the CullFace mode is [BACK][GL11C.GL_BACK]. Initially, culling is disabled.
     *
     * @param mode the CullFace mode. One of:<br></br><table><tr><td>[FRONT][GL11C.GL_FRONT]</td><td>[BACK][GL11C.GL_BACK]</td><td>[FRONT_AND_BACK][GL11C.GL_FRONT_AND_BACK]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glCullFace)
     */
    fun glCullFace(mode: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glCullFace(mode)
    }

    /**
     * Specifies a two-dimensional texture image.
     *
     * @param target         the texture target. One of:<br></br><table><tr><td>[TEXTURE_2D][GL11C.GL_TEXTURE_2D]</td><td>[TEXTURE_1D_ARRAY][GL30.GL_TEXTURE_1D_ARRAY]</td><td>[TEXTURE_RECTANGLE][GL31.GL_TEXTURE_RECTANGLE]</td><td>[TEXTURE_CUBE_MAP][GL13.GL_TEXTURE_CUBE_MAP]</td></tr><tr><td>[PROXY_TEXTURE_2D][GL11C.GL_PROXY_TEXTURE_2D]</td><td>[PROXY_TEXTURE_1D_ARRAY][GL30.GL_PROXY_TEXTURE_1D_ARRAY]</td><td>[PROXY_TEXTURE_RECTANGLE][GL31.GL_PROXY_TEXTURE_RECTANGLE]</td><td>[PROXY_TEXTURE_CUBE_MAP][GL13.GL_PROXY_TEXTURE_CUBE_MAP]</td></tr></table>
     * @param level          the level-of-detail number
     * @param internalformat the texture internal format. One of:<br></br><table><tr><td>[RED][GL11C.GL_RED]</td><td>[RG][GL30.GL_RG]</td><td>[RGB][GL11C.GL_RGB]</td><td>[RGBA][GL11C.GL_RGBA]</td><td>[DEPTH_COMPONENT][GL11C.GL_DEPTH_COMPONENT]</td><td>[DEPTH_STENCIL][GL30.GL_DEPTH_STENCIL]</td></tr><tr><td>[R8][GL30.GL_R8]</td><td>[R8_SNORM][GL31.GL_R8_SNORM]</td><td>[R16][GL30.GL_R16]</td><td>[R16_SNORM][GL31.GL_R16_SNORM]</td><td>[RG8][GL30.GL_RG8]</td><td>[RG8_SNORM][GL31.GL_RG8_SNORM]</td></tr><tr><td>[RG16][GL30.GL_RG16]</td><td>[RG16_SNORM][GL31.GL_RG16_SNORM]</td><td>[R3_G3_B2][GL11C.GL_R3_G3_B2]</td><td>[RGB4][GL11C.GL_RGB4]</td><td>[RGB5][GL11C.GL_RGB5]</td><td>[RGB565][GL41.GL_RGB565]</td></tr><tr><td>[RGB8][GL11C.GL_RGB8]</td><td>[RGB8_SNORM][GL31.GL_RGB8_SNORM]</td><td>[RGB10][GL11C.GL_RGB10]</td><td>[RGB12][GL11C.GL_RGB12]</td><td>[RGB16][GL11C.GL_RGB16]</td><td>[RGB16_SNORM][GL31.GL_RGB16_SNORM]</td></tr><tr><td>[RGBA2][GL11C.GL_RGBA2]</td><td>[RGBA4][GL11C.GL_RGBA4]</td><td>[RGB5_A1][GL11C.GL_RGB5_A1]</td><td>[RGBA8][GL11C.GL_RGBA8]</td><td>[RGBA8_SNORM][GL31.GL_RGBA8_SNORM]</td><td>[RGB10_A2][GL11C.GL_RGB10_A2]</td></tr><tr><td>[RGB10_A2UI][GL33.GL_RGB10_A2UI]</td><td>[RGBA12][GL11C.GL_RGBA12]</td><td>[RGBA16][GL11C.GL_RGBA16]</td><td>[RGBA16_SNORM][GL31.GL_RGBA16_SNORM]</td><td>[SRGB8][GL21.GL_SRGB8]</td><td>[SRGB8_ALPHA8][GL21.GL_SRGB8_ALPHA8]</td></tr><tr><td>[R16F][GL30.GL_R16F]</td><td>[RG16F][GL30.GL_RG16F]</td><td>[RGB16F][GL30.GL_RGB16F]</td><td>[RGBA16F][GL30.GL_RGBA16F]</td><td>[R32F][GL30.GL_R32F]</td><td>[RG32F][GL30.GL_RG32F]</td></tr><tr><td>[RGB32F][GL30.GL_RGB32F]</td><td>[RGBA32F][GL30.GL_RGBA32F]</td><td>[R11F_G11F_B10F][GL30.GL_R11F_G11F_B10F]</td><td>[RGB9_E5][GL30.GL_RGB9_E5]</td><td>[R8I][GL30.GL_R8I]</td><td>[R8UI][GL30.GL_R8UI]</td></tr><tr><td>[R16I][GL30.GL_R16I]</td><td>[R16UI][GL30.GL_R16UI]</td><td>[R32I][GL30.GL_R32I]</td><td>[R32UI][GL30.GL_R32UI]</td><td>[RG8I][GL30.GL_RG8I]</td><td>[RG8UI][GL30.GL_RG8UI]</td></tr><tr><td>[RG16I][GL30.GL_RG16I]</td><td>[RG16UI][GL30.GL_RG16UI]</td><td>[RG32I][GL30.GL_RG32I]</td><td>[RG32UI][GL30.GL_RG32UI]</td><td>[RGB8I][GL30.GL_RGB8I]</td><td>[RGB8UI][GL30.GL_RGB8UI]</td></tr><tr><td>[RGB16I][GL30.GL_RGB16I]</td><td>[RGB16UI][GL30.GL_RGB16UI]</td><td>[RGB32I][GL30.GL_RGB32I]</td><td>[RGB32UI][GL30.GL_RGB32UI]</td><td>[RGBA8I][GL30.GL_RGBA8I]</td><td>[RGBA8UI][GL30.GL_RGBA8UI]</td></tr><tr><td>[RGBA16I][GL30.GL_RGBA16I]</td><td>[RGBA16UI][GL30.GL_RGBA16UI]</td><td>[RGBA32I][GL30.GL_RGBA32I]</td><td>[RGBA32UI][GL30.GL_RGBA32UI]</td><td>[DEPTH_COMPONENT16][GL14.GL_DEPTH_COMPONENT16]</td><td>[DEPTH_COMPONENT24][GL14.GL_DEPTH_COMPONENT24]</td></tr><tr><td>[DEPTH_COMPONENT32][GL14.GL_DEPTH_COMPONENT32]</td><td>[DEPTH24_STENCIL8][GL30.GL_DEPTH24_STENCIL8]</td><td>[DEPTH_COMPONENT32F][GL30.GL_DEPTH_COMPONENT32F]</td><td>[DEPTH32F_STENCIL8][GL30.GL_DEPTH32F_STENCIL8]</td><td>[COMPRESSED_RED][GL30.GL_COMPRESSED_RED]</td><td>[COMPRESSED_RG][GL30.GL_COMPRESSED_RG]</td></tr><tr><td>[COMPRESSED_RGB][GL13.GL_COMPRESSED_RGB]</td><td>[COMPRESSED_RGBA][GL13.GL_COMPRESSED_RGBA]</td><td>[COMPRESSED_SRGB][GL21.GL_COMPRESSED_SRGB]</td><td>[COMPRESSED_SRGB_ALPHA][GL21.GL_COMPRESSED_SRGB_ALPHA]</td><td>[COMPRESSED_RED_RGTC1][GL30.GL_COMPRESSED_RED_RGTC1]</td><td>[COMPRESSED_SIGNED_RED_RGTC1][GL30.GL_COMPRESSED_SIGNED_RED_RGTC1]</td></tr><tr><td>[COMPRESSED_RG_RGTC2][GL30.GL_COMPRESSED_RG_RGTC2]</td><td>[COMPRESSED_SIGNED_RG_RGTC2][GL30.GL_COMPRESSED_SIGNED_RG_RGTC2]</td><td>[COMPRESSED_RGBA_BPTC_UNORM][GL42.GL_COMPRESSED_RGBA_BPTC_UNORM]</td><td>[COMPRESSED_SRGB_ALPHA_BPTC_UNORM][GL42.GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM]</td><td>[COMPRESSED_RGB_BPTC_SIGNED_FLOAT][GL42.GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT]</td><td>[COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT][GL42.GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT]</td></tr><tr><td>[COMPRESSED_RGB8_ETC2][GL43.GL_COMPRESSED_RGB8_ETC2]</td><td>[COMPRESSED_SRGB8_ETC2][GL43.GL_COMPRESSED_SRGB8_ETC2]</td><td>[COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2][GL43.GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2]</td><td>[COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2][GL43.GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2]</td><td>[COMPRESSED_RGBA8_ETC2_EAC][GL43.GL_COMPRESSED_RGBA8_ETC2_EAC]</td><td>[COMPRESSED_SRGB8_ALPHA8_ETC2_EAC][GL43.GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC]</td></tr><tr><td>[COMPRESSED_R11_EAC][GL43.GL_COMPRESSED_R11_EAC]</td><td>[COMPRESSED_SIGNED_R11_EAC][GL43.GL_COMPRESSED_SIGNED_R11_EAC]</td><td>[COMPRESSED_RG11_EAC][GL43.GL_COMPRESSED_RG11_EAC]</td><td>[COMPRESSED_SIGNED_RG11_EAC][GL43.GL_COMPRESSED_SIGNED_RG11_EAC]</td><td>see [EXTTextureCompressionS3TC]</td><td>see [EXTTextureCompressionLATC]</td></tr><tr><td>see [ATITextureCompression3DC]</td></tr></table>
     * @param width          the texture width
     * @param height         the texture height
     * @param border         the texture border width
     * @param format         the texel data format. One of:<br></br><table><tr><td>[RED][GL11C.GL_RED]</td><td>[GREEN][GL11C.GL_GREEN]</td><td>[BLUE][GL11C.GL_BLUE]</td><td>[ALPHA][GL11C.GL_ALPHA]</td><td>[RG][GL30.GL_RG]</td><td>[RGB][GL11C.GL_RGB]</td><td>[RGBA][GL11C.GL_RGBA]</td><td>[BGR][GL12.GL_BGR]</td></tr><tr><td>[BGRA][GL12.GL_BGRA]</td><td>[RED_INTEGER][GL30.GL_RED_INTEGER]</td><td>[GREEN_INTEGER][GL30.GL_GREEN_INTEGER]</td><td>[BLUE_INTEGER][GL30.GL_BLUE_INTEGER]</td><td>[ALPHA_INTEGER][GL30.GL_ALPHA_INTEGER]</td><td>[RG_INTEGER][GL30.GL_RG_INTEGER]</td><td>[RGB_INTEGER][GL30.GL_RGB_INTEGER]</td><td>[RGBA_INTEGER][GL30.GL_RGBA_INTEGER]</td></tr><tr><td>[BGR_INTEGER][GL30.GL_BGR_INTEGER]</td><td>[BGRA_INTEGER][GL30.GL_BGRA_INTEGER]</td><td>[STENCIL_INDEX][GL11C.GL_STENCIL_INDEX]</td><td>[DEPTH_COMPONENT][GL11C.GL_DEPTH_COMPONENT]</td><td>[DEPTH_STENCIL][GL30.GL_DEPTH_STENCIL]</td></tr></table>
     * @param type           the texel data type. One of:<br></br><table><tr><td>[UNSIGNED_BYTE][GL11C.GL_UNSIGNED_BYTE]</td><td>[BYTE][GL11C.GL_BYTE]</td><td>[UNSIGNED_SHORT][GL11C.GL_UNSIGNED_SHORT]</td><td>[SHORT][GL11C.GL_SHORT]</td></tr><tr><td>[UNSIGNED_INT][GL11C.GL_UNSIGNED_INT]</td><td>[INT][GL11C.GL_INT]</td><td>[HALF_FLOAT][GL30.GL_HALF_FLOAT]</td><td>[FLOAT][GL11C.GL_FLOAT]</td></tr><tr><td>[UNSIGNED_BYTE_3_3_2][GL12.GL_UNSIGNED_BYTE_3_3_2]</td><td>[UNSIGNED_BYTE_2_3_3_REV][GL12.GL_UNSIGNED_BYTE_2_3_3_REV]</td><td>[UNSIGNED_SHORT_5_6_5][GL12.GL_UNSIGNED_SHORT_5_6_5]</td><td>[UNSIGNED_SHORT_5_6_5_REV][GL12.GL_UNSIGNED_SHORT_5_6_5_REV]</td></tr><tr><td>[UNSIGNED_SHORT_4_4_4_4][GL12.GL_UNSIGNED_SHORT_4_4_4_4]</td><td>[UNSIGNED_SHORT_4_4_4_4_REV][GL12.GL_UNSIGNED_SHORT_4_4_4_4_REV]</td><td>[UNSIGNED_SHORT_5_5_5_1][GL12.GL_UNSIGNED_SHORT_5_5_5_1]</td><td>[UNSIGNED_SHORT_1_5_5_5_REV][GL12.GL_UNSIGNED_SHORT_1_5_5_5_REV]</td></tr><tr><td>[UNSIGNED_INT_8_8_8_8][GL12.GL_UNSIGNED_INT_8_8_8_8]</td><td>[UNSIGNED_INT_8_8_8_8_REV][GL12.GL_UNSIGNED_INT_8_8_8_8_REV]</td><td>[UNSIGNED_INT_10_10_10_2][GL12.GL_UNSIGNED_INT_10_10_10_2]</td><td>[UNSIGNED_INT_2_10_10_10_REV][GL12.GL_UNSIGNED_INT_2_10_10_10_REV]</td></tr><tr><td>[UNSIGNED_INT_24_8][GL30.GL_UNSIGNED_INT_24_8]</td><td>[UNSIGNED_INT_10F_11F_11F_REV][GL30.GL_UNSIGNED_INT_10F_11F_11F_REV]</td><td>[UNSIGNED_INT_5_9_9_9_REV][GL30.GL_UNSIGNED_INT_5_9_9_9_REV]</td><td>[FLOAT_32_UNSIGNED_INT_24_8_REV][GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV]</td></tr></table>
     * @param pixels         the texel data
     *
     * @see [Reference Page](http://docs.gl/gl4/glTexImage2D)
     */
    fun glTexImage2D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: ByteBuffer?
    ) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glTexImage2D(
            target,
            level,
            internalformat,
            width,
            height,
            border,
            format,
            type,
            pixels
        )
    }

    fun glGetFloatv(pname: Int, params: FloatArray) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glGetFloatv(pname, params)
    }

    /**
     * Returns error information.
     *
     *
     * Each detectable error is assigned a numeric code. When an error is detected, a flag is set and the code is recorded. Further errors, if they occur, do
     * not affect this recorded code. When `GetError` is called, the code is returned and the flag is cleared, so that a further error will again record
     * its code. If a call to `GetError` returns [NO_ERROR][GL11C.GL_NO_ERROR], then there has been no detectable error since the last call to `GetError` (or since
     * the GL was initialized).
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetError)
     */
    fun glGetError(): Int {
        return com.zakgof.korender.impl.gl.VGL11.gl!!.glGetError()
    }

    /**
     * Sets portions of every pixel in a particular buffer to the same value. The value to which each buffer is cleared depends on the setting of the clear
     * value for that buffer.
     *
     * @param mask Zero or the bitwise OR of one or more values indicating which buffers are to be cleared. One or more of:<br></br><table><tr><td>[COLOR_BUFFER_BIT][GL11C.GL_COLOR_BUFFER_BIT]</td><td>[DEPTH_BUFFER_BIT][GL11C.GL_DEPTH_BUFFER_BIT]</td><td>[STENCIL_BUFFER_BIT][GL11C.GL_STENCIL_BUFFER_BIT]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glClear)
     */
    fun glClear(mask: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glClear(mask)
    }


    /**
     * Specifies the viewport transformation parameters for all viewports.
     *
     *
     * The location of the viewport's bottom-left corner, given by `(x, y)`, are clamped to be within the implementation-dependent viewport bounds range.
     * The viewport bounds range `[min, max]` tuple may be determined by calling [GetFloatv][.glGetFloatv] with the symbolic
     * constant [VIEWPORT_BOUNDS_RANGE][GL41.GL_VIEWPORT_BOUNDS_RANGE]. Viewport width and height are clamped to implementation-dependent maximums when specified. The maximum
     * width and height may be found by calling [GetFloatv][.glGetFloatv] with the symbolic constant [MAX_VIEWPORT_DIMS][GL11C.GL_MAX_VIEWPORT_DIMS]. The
     * maximum viewport dimensions must be greater than or equal to the larger of the visible dimensions of the display being rendered to (if a display
     * exists), and the largest renderbuffer image which can be successfully created and attached to a framebuffer object.
     *
     *
     * In the initial state, `w` and `h` for each viewport are set to the width and height, respectively, of the window into which the GL is to do
     * its rendering. If the default framebuffer is bound but no default framebuffer is associated with the GL context, then `w` and `h` are
     * initially set to zero.
     *
     * @param x the left viewport coordinate
     * @param y the bottom viewport coordinate
     * @param w the viewport width
     * @param h the viewport height
     *
     * @see [Reference Page](http://docs.gl/gl4/glViewport)
     */
    fun glViewport(x: Int, y: Int, w: Int, h: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glViewport(x, y, w, h)
    }

    // --- [ glTexParameteri ] ---
    /**
     * Sets the integer value of a texture parameter, which controls how the texel array is treated when specified or changed, and when applied to a fragment.
     *
     * @param target the texture target. One of:<br></br><table><tr><td>[TEXTURE_1D][GL11C.GL_TEXTURE_1D]</td><td>[TEXTURE_2D][GL11C.GL_TEXTURE_2D]</td><td>[TEXTURE_3D][GL12.GL_TEXTURE_3D]</td><td>[TEXTURE_1D_ARRAY][GL30.GL_TEXTURE_1D_ARRAY]</td></tr><tr><td>[TEXTURE_2D_ARRAY][GL30.GL_TEXTURE_2D_ARRAY]</td><td>[TEXTURE_RECTANGLE][GL31.GL_TEXTURE_RECTANGLE]</td><td>[TEXTURE_CUBE_MAP][GL13.GL_TEXTURE_CUBE_MAP]</td><td>[TEXTURE_CUBE_MAP_ARRAY][GL40.GL_TEXTURE_CUBE_MAP_ARRAY]</td></tr><tr><td>[TEXTURE_2D_MULTISAMPLE][GL32.GL_TEXTURE_2D_MULTISAMPLE]</td><td>[TEXTURE_2D_MULTISAMPLE_ARRAY][GL32.GL_TEXTURE_2D_MULTISAMPLE_ARRAY]</td></tr></table>
     * @param pname  the parameter to set. One of:<br></br><table><tr><td>[TEXTURE_BASE_LEVEL][GL12.GL_TEXTURE_BASE_LEVEL]</td><td>[TEXTURE_BORDER_COLOR][GL11C.GL_TEXTURE_BORDER_COLOR]</td><td>[TEXTURE_COMPARE_MODE][GL14.GL_TEXTURE_COMPARE_MODE]</td><td>[TEXTURE_COMPARE_FUNC][GL14.GL_TEXTURE_COMPARE_FUNC]</td></tr><tr><td>[TEXTURE_LOD_BIAS][GL14.GL_TEXTURE_LOD_BIAS]</td><td>[TEXTURE_MAG_FILTER][GL11C.GL_TEXTURE_MAG_FILTER]</td><td>[TEXTURE_MAX_LEVEL][GL12.GL_TEXTURE_MAX_LEVEL]</td><td>[TEXTURE_MAX_LOD][GL12.GL_TEXTURE_MAX_LOD]</td></tr><tr><td>[TEXTURE_MIN_FILTER][GL11C.GL_TEXTURE_MIN_FILTER]</td><td>[TEXTURE_MIN_LOD][GL12.GL_TEXTURE_MIN_LOD]</td><td>[TEXTURE_SWIZZLE_R][GL33.GL_TEXTURE_SWIZZLE_R]</td><td>[TEXTURE_SWIZZLE_G][GL33.GL_TEXTURE_SWIZZLE_G]</td></tr><tr><td>[TEXTURE_SWIZZLE_B][GL33.GL_TEXTURE_SWIZZLE_B]</td><td>[TEXTURE_SWIZZLE_A][GL33.GL_TEXTURE_SWIZZLE_A]</td><td>[TEXTURE_SWIZZLE_RGBA][GL33.GL_TEXTURE_SWIZZLE_RGBA]</td><td>[TEXTURE_WRAP_S][GL11C.GL_TEXTURE_WRAP_S]</td></tr><tr><td>[TEXTURE_WRAP_T][GL11C.GL_TEXTURE_WRAP_T]</td><td>[TEXTURE_WRAP_R][GL12.GL_TEXTURE_WRAP_R]</td><td>[DEPTH_TEXTURE_MODE][GL14.GL_DEPTH_TEXTURE_MODE]</td><td>[GENERATE_MIPMAP][GL14.GL_GENERATE_MIPMAP]</td></tr></table>
     * @param param  the parameter value
     *
     * @see [Reference Page](http://docs.gl/gl4/glTexParameteri)
     */
    fun glTexParameteri(target: Int, pname: Int, param: Int) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glTexParameteri(target, pname, param)
    }

    fun glClearColor(fl: Float, fl1: Float, fl2: Float, fl3: Float) {
        com.zakgof.korender.impl.gl.VGL11.gl!!.glClearColor(fl, fl1, fl2, fl3)
    }

    fun shaderEnv() = com.zakgof.korender.impl.gl.VGL11.gl!!.shaderEnv


}
