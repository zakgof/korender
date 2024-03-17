package com.zakgof.korender.gl

import java.nio.ByteBuffer

object VGL15 {

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

    var gl: IGL15? = null

    /**
     * Binds a named buffer object.
     *
     * @param target the target to which the buffer object is bound. One of:<br></br><table><tr><td>[ARRAY_BUFFER][GL15C.GL_ARRAY_BUFFER]</td><td>[ELEMENT_ARRAY_BUFFER][GL15C.GL_ELEMENT_ARRAY_BUFFER]</td><td>[PIXEL_PACK_BUFFER][GL21.GL_PIXEL_PACK_BUFFER]</td><td>[PIXEL_UNPACK_BUFFER][GL21.GL_PIXEL_UNPACK_BUFFER]</td></tr><tr><td>[TRANSFORM_FEEDBACK_BUFFER][GL30.GL_TRANSFORM_FEEDBACK_BUFFER]</td><td>[UNIFORM_BUFFER][GL31.GL_UNIFORM_BUFFER]</td><td>[TEXTURE_BUFFER][GL31.GL_TEXTURE_BUFFER]</td><td>[COPY_READ_BUFFER][GL31.GL_COPY_READ_BUFFER]</td></tr><tr><td>[COPY_WRITE_BUFFER][GL31.GL_COPY_WRITE_BUFFER]</td><td>[DRAW_INDIRECT_BUFFER][GL40.GL_DRAW_INDIRECT_BUFFER]</td><td>[ATOMIC_COUNTER_BUFFER][GL42.GL_ATOMIC_COUNTER_BUFFER]</td><td>[DISPATCH_INDIRECT_BUFFER][GL43.GL_DISPATCH_INDIRECT_BUFFER]</td></tr><tr><td>[SHADER_STORAGE_BUFFER][GL43.GL_SHADER_STORAGE_BUFFER]</td><td>[PARAMETER_BUFFER_ARB][ARBIndirectParameters.GL_PARAMETER_BUFFER_ARB]</td></tr></table>
     * @param buffer the name of a buffer object
     *
     * @see [Reference Page](http://docs.gl/gl4/glBindBuffer)
     */
    fun glBindBuffer(target: Int, buffer: Int) {
        gl!!.glBindBuffer(target, buffer)
    }

    /**
     * Creates and initializes a buffer object's data store.
     *
     *
     * `usage` is a hint to the GL implementation as to how a buffer object's data store will be accessed. This enables the GL implementation to make
     * more intelligent decisions that may significantly impact buffer object performance. It does not, however, constrain the actual usage of the data store.
     * `usage` can be broken down into two parts: first, the frequency of access (modification and usage), and second, the nature of that access. The
     * frequency of access may be one of these:
     *
     *
     *  * *STREAM* - The data store contents will be modified once and used at most a few times.
     *  * *STATIC* - The data store contents will be modified once and used many times.
     *  * *DYNAMIC* - The data store contents will be modified repeatedly and used many times.
     *
     *
     *
     * The nature of access may be one of these:
     *
     *
     *  * *DRAW* - The data store contents are modified by the application, and used as the source for GL drawing and image specification commands.
     *  * *READ* - The data store contents are modified by reading data from the GL, and used to return that data when queried by the application.
     *  * *COPY* - The data store contents are modified by reading data from the GL, and used as the source for GL drawing and image specification commands.
     *
     *
     * @param target the target buffer object. One of:<br></br><table><tr><td>[ARRAY_BUFFER][GL15C.GL_ARRAY_BUFFER]</td><td>[ELEMENT_ARRAY_BUFFER][GL15C.GL_ELEMENT_ARRAY_BUFFER]</td><td>[PIXEL_PACK_BUFFER][GL21.GL_PIXEL_PACK_BUFFER]</td><td>[PIXEL_UNPACK_BUFFER][GL21.GL_PIXEL_UNPACK_BUFFER]</td></tr><tr><td>[TRANSFORM_FEEDBACK_BUFFER][GL30.GL_TRANSFORM_FEEDBACK_BUFFER]</td><td>[UNIFORM_BUFFER][GL31.GL_UNIFORM_BUFFER]</td><td>[TEXTURE_BUFFER][GL31.GL_TEXTURE_BUFFER]</td><td>[COPY_READ_BUFFER][GL31.GL_COPY_READ_BUFFER]</td></tr><tr><td>[COPY_WRITE_BUFFER][GL31.GL_COPY_WRITE_BUFFER]</td><td>[DRAW_INDIRECT_BUFFER][GL40.GL_DRAW_INDIRECT_BUFFER]</td><td>[ATOMIC_COUNTER_BUFFER][GL42.GL_ATOMIC_COUNTER_BUFFER]</td><td>[DISPATCH_INDIRECT_BUFFER][GL43.GL_DISPATCH_INDIRECT_BUFFER]</td></tr><tr><td>[SHADER_STORAGE_BUFFER][GL43.GL_SHADER_STORAGE_BUFFER]</td><td>[PARAMETER_BUFFER_ARB][ARBIndirectParameters.GL_PARAMETER_BUFFER_ARB]</td></tr></table>
     * @param data   a pointer to data that will be copied into the data store for initialization, or `NULL` if no data is to be copied
     * @param usage  the expected usage pattern of the data store. One of:<br></br><table><tr><td>[STREAM_DRAW][GL15C.GL_STREAM_DRAW]</td><td>[STREAM_READ][GL15C.GL_STREAM_READ]</td><td>[STREAM_COPY][GL15C.GL_STREAM_COPY]</td><td>[STATIC_DRAW][GL15C.GL_STATIC_DRAW]</td><td>[STATIC_READ][GL15C.GL_STATIC_READ]</td><td>[STATIC_COPY][GL15C.GL_STATIC_COPY]</td><td>[DYNAMIC_DRAW][GL15C.GL_DYNAMIC_DRAW]</td></tr><tr><td>[DYNAMIC_READ][GL15C.GL_DYNAMIC_READ]</td><td>[DYNAMIC_COPY][GL15C.GL_DYNAMIC_COPY]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glBufferData)
     */
    fun glBufferData(target: Int, data: ByteBuffer, usage: Int) {
        gl!!.glBufferData(target, data, usage)
    }

    /**
     * Generates buffer object names.
     *
     * @see [Reference Page](http://docs.gl/gl4/glGenBuffers)
     */
    fun glGenBuffers(): Int = gl!!.glGenBuffers()


    fun glDeleteBuffers(buffer: Int) = gl!!.glDeleteBuffers(buffer)

}
