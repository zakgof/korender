package com.zakgof.korender.gl

import java.nio.FloatBuffer
import java.nio.IntBuffer

object VGL20 {

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

    var gl: IGL20? = null

    /**
     * Creates a program object.
     *
     * @see [Reference Page](http://docs.gl/gl4/glCreateProgram)
     */
    fun glCreateProgram(): Int {
        return gl!!.glCreateProgram()
    }

    /**
     * Creates a shader object.
     *
     * @param type the type of shader to be created. One of:<br></br><table><tr><td>[VERTEX_SHADER][GL20C.GL_VERTEX_SHADER]</td><td>[FRAGMENT_SHADER][GL20C.GL_FRAGMENT_SHADER]</td><td>[GEOMETRY_SHADER][GL32.GL_GEOMETRY_SHADER]</td><td>[TESS_CONTROL_SHADER][GL40.GL_TESS_CONTROL_SHADER]</td></tr><tr><td>[TESS_EVALUATION_SHADER][GL40.GL_TESS_EVALUATION_SHADER]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glCreateShader)
     */
    fun glCreateShader(type: Int): Int {
        return gl!!.glCreateShader(type)
    }

    /**
     * Attaches a shader object to a program object.
     *
     *
     * In order to create a complete shader program, there must be a way to specify the list of things that will be linked together. Program objects provide
     * this mechanism. Shaders that are to be linked together in a program object must first be attached to that program object. glAttachShader attaches the
     * shader object specified by shader to the program object specified by program. This indicates that shader will be included in link operations that will
     * be performed on program.
     *
     *
     * All operations that can be performed on a shader object are valid whether or not the shader object is attached to a program object. It is permissible to
     * attach a shader object to a program object before source code has been loaded into the shader object or before the shader object has been compiled. It
     * is permissible to attach multiple shader objects of the same type because each may contain a portion of the complete shader. It is also permissible to
     * attach a shader object to more than one program object. If a shader object is deleted while it is attached to a program object, it will be flagged for
     * deletion, and deletion will not occur until glDetachShader is called to detach it from all program objects to which it is attached.
     *
     * @param program the program object to which a shader object will be attached
     * @param shader  the shader object that is to be attached
     *
     * @see [Reference Page](http://docs.gl/gl4/glAttachShader)
     */
    fun glAttachShader(program: Int, shader: Int) {
        gl!!.glAttachShader(program, shader)
    }

    /**
     * Links a program object.
     *
     * @param program the program object to be linked
     *
     * @see [Reference Page](http://docs.gl/gl4/glLinkProgram)
     */
    fun glLinkProgram(program: Int) {
        gl!!.glLinkProgram(program)
    }

    /**
     * Installs a program object as part of current rendering state.
     *
     * @param program the program object whose executables are to be used as part of current rendering state
     *
     * @see [Reference Page](http://docs.gl/gl4/glUseProgram)
     */
    fun glUseProgram(program: Int) {
        gl!!.glUseProgram(program)
    }

    /**
     * Validates a program object.
     *
     * @param program the program object to be validated
     *
     * @see [Reference Page](http://docs.gl/gl4/glValidateProgram)
     */
    fun glValidateProgram(program: Int) {
        gl!!.glValidateProgram(program)
    }

    /**
     * Returns a parameter from a program object.
     *
     * @param program the program object to be queried
     * @param pname   the object parameter. One of:<br></br><table><tr><td>[DELETE_STATUS][GL20C.GL_DELETE_STATUS]</td><td>[LINK_STATUS][GL20C.GL_LINK_STATUS]</td><td>[VALIDATE_STATUS][GL20C.GL_VALIDATE_STATUS]</td></tr><tr><td>[INFO_LOG_LENGTH][GL20C.GL_INFO_LOG_LENGTH]</td><td>[ATTACHED_SHADERS][GL20C.GL_ATTACHED_SHADERS]</td><td>[ACTIVE_ATTRIBUTES][GL20C.GL_ACTIVE_ATTRIBUTES]</td></tr><tr><td>[ACTIVE_ATTRIBUTE_MAX_LENGTH][GL20C.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH]</td><td>[ACTIVE_UNIFORMS][GL20C.GL_ACTIVE_UNIFORMS]</td><td>[ACTIVE_UNIFORM_MAX_LENGTH][GL20C.GL_ACTIVE_UNIFORM_MAX_LENGTH]</td></tr><tr><td>[TRANSFORM_FEEDBACK_BUFFER_MODE][GL30.GL_TRANSFORM_FEEDBACK_BUFFER_MODE]</td><td>[TRANSFORM_FEEDBACK_VARYINGS][GL30.GL_TRANSFORM_FEEDBACK_VARYINGS]</td><td>[TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH][GL30.GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH]</td></tr><tr><td>[ACTIVE_UNIFORM_BLOCKS][GL31.GL_ACTIVE_UNIFORM_BLOCKS]</td><td>[ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH][GL31.GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH]</td><td>[GEOMETRY_VERTICES_OUT][GL32.GL_GEOMETRY_VERTICES_OUT]</td></tr><tr><td>[GEOMETRY_INPUT_TYPE][GL32.GL_GEOMETRY_INPUT_TYPE]</td><td>[GEOMETRY_OUTPUT_TYPE][GL32.GL_GEOMETRY_OUTPUT_TYPE]</td><td>[PROGRAM_BINARY_LENGTH][GL41.GL_PROGRAM_BINARY_LENGTH]</td></tr><tr><td>[ACTIVE_ATOMIC_COUNTER_BUFFERS][GL42.GL_ACTIVE_ATOMIC_COUNTER_BUFFERS]</td><td>[COMPUTE_WORK_GROUP_SIZE][GL43.GL_COMPUTE_WORK_GROUP_SIZE]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetProgram)
     */
    fun glGetProgrami(program: Int, pname: Int): Int {
        return gl!!.glGetProgrami(program, pname)
    }

    /**
     * Returns the information log for a shader object.
     *
     * @param shader the shader object whose information log is to be queried
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetShaderInfoLog)
     */
    fun glGetShaderInfoLog(shader: Int): String {
        return gl!!.glGetShaderInfoLog(shader)
    }

    /**
     * Returns the information log for a program object.
     *
     * @param program the program object whose information log is to be queried
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetProgramInfoLog)
     */
    fun glGetProgramInfoLog(program: Int): String {
        return gl!!.glGetProgramInfoLog(program)
    }

    /**
     * Returns a parameter from a program object.
     *
     * @param program the program object to be queried
     * @param pname   the object parameter. One of:<br></br><table><tr><td>[DELETE_STATUS][GL20C.GL_DELETE_STATUS]</td><td>[LINK_STATUS][GL20C.GL_LINK_STATUS]</td><td>[VALIDATE_STATUS][GL20C.GL_VALIDATE_STATUS]</td></tr><tr><td>[INFO_LOG_LENGTH][GL20C.GL_INFO_LOG_LENGTH]</td><td>[ATTACHED_SHADERS][GL20C.GL_ATTACHED_SHADERS]</td><td>[ACTIVE_ATTRIBUTES][GL20C.GL_ACTIVE_ATTRIBUTES]</td></tr><tr><td>[ACTIVE_ATTRIBUTE_MAX_LENGTH][GL20C.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH]</td><td>[ACTIVE_UNIFORMS][GL20C.GL_ACTIVE_UNIFORMS]</td><td>[ACTIVE_UNIFORM_MAX_LENGTH][GL20C.GL_ACTIVE_UNIFORM_MAX_LENGTH]</td></tr><tr><td>[TRANSFORM_FEEDBACK_BUFFER_MODE][GL30.GL_TRANSFORM_FEEDBACK_BUFFER_MODE]</td><td>[TRANSFORM_FEEDBACK_VARYINGS][GL30.GL_TRANSFORM_FEEDBACK_VARYINGS]</td><td>[TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH][GL30.GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH]</td></tr><tr><td>[ACTIVE_UNIFORM_BLOCKS][GL31.GL_ACTIVE_UNIFORM_BLOCKS]</td><td>[ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH][GL31.GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH]</td><td>[GEOMETRY_VERTICES_OUT][GL32.GL_GEOMETRY_VERTICES_OUT]</td></tr><tr><td>[GEOMETRY_INPUT_TYPE][GL32.GL_GEOMETRY_INPUT_TYPE]</td><td>[GEOMETRY_OUTPUT_TYPE][GL32.GL_GEOMETRY_OUTPUT_TYPE]</td><td>[PROGRAM_BINARY_LENGTH][GL41.GL_PROGRAM_BINARY_LENGTH]</td></tr><tr><td>[ACTIVE_ATOMIC_COUNTER_BUFFERS][GL42.GL_ACTIVE_ATOMIC_COUNTER_BUFFERS]</td><td>[COMPUTE_WORK_GROUP_SIZE][GL43.GL_COMPUTE_WORK_GROUP_SIZE]</td></tr></table>
     * @param params  the requested object parameter
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetProgram)
     */
    fun glGetProgramiv(program: Int, pname: Int, params: IntBuffer) {
        gl!!.glGetProgramiv(program, pname, params)
    }

    /**
     * Returns information about an active uniform variable for the specified program object.
     *
     * @param program the program object to be queried
     * @param index   the index of the uniform variable to be queried
     * @param size    the size of the uniform variable
     * @param type    the data type of the uniform variable
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetActiveUniform)
     */
    fun glGetActiveUniform(program: Int, index: Int, size: IntBuffer, type: IntBuffer): String {
        return gl!!.glGetActiveUniform(program, index, size, type)
    }

    /**
     * Returns information about an active attribute variable for the specified program object.
     *
     * @param program the program object to be queried
     * @param index   the index of the attribute variable to be queried
     * @param size    the size of the attribute variable
     * @param type    the data type of the attribute variable
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetActiveAttrib)
     */
    fun glGetActiveAttrib(program: Int, index: Int, size: IntBuffer, type: IntBuffer): String {
        return gl!!.glGetActiveAttrib(program, index, size, type)
    }

    /**
     * Sets the source code in `shader` to the source code in the array of strings specified by `strings`. Any source code previously stored in the
     * shader object is completely replaced. The number of strings in the array is specified by `count`. If `length` is `NULL`, each string is
     * assumed to be null terminated. If `length` is a value other than `NULL`, it points to an array containing a string length for each of the
     * corresponding elements of `strings`. Each element in the length array may contain the length of the corresponding string (the null character is not
     * counted as part of the string length) or a value less than 0 to indicate that the string is null terminated. The source code strings are not scanned or
     * parsed at this time; they are simply copied into the specified shader object.
     *
     * @param shader the shader object whose source code is to be replaced
     *
     * @see [Reference Page](http://docs.gl/gl4/glShaderSource)
     */
    fun glShaderSource(shader: Int, source: String) {
        gl!!.glShaderSource(shader, source)
    }

    /**
     * Compiles a shader object.
     *
     * @param shader the shader object to be compiled
     *
     * @see [Reference Page](http://docs.gl/gl4/glCompileShader)
     */
    fun glCompileShader(shader: Int) {
        gl!!.glCompileShader(shader)
    }

    /**
     * Returns a parameter from a shader object.
     *
     * @param shader the shader object to be queried
     * @param pname  the object parameter. One of:<br></br><table><tr><td>[SHADER_TYPE][GL20C.GL_SHADER_TYPE]</td><td>[DELETE_STATUS][GL20C.GL_DELETE_STATUS]</td><td>[COMPILE_STATUS][GL20C.GL_COMPILE_STATUS]</td><td>[INFO_LOG_LENGTH][GL20C.GL_INFO_LOG_LENGTH]</td><td>[SHADER_SOURCE_LENGTH][GL20C.GL_SHADER_SOURCE_LENGTH]</td></tr></table>
     * @param params the requested object parameter
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetShader)
     */
    fun glGetShaderiv(shader: Int, pname: Int, params: IntBuffer) {
        gl!!.glGetShaderiv(shader, pname, params)
    }

    /**
     * Enables a generic vertex attribute array.
     *
     * @param index the index of the generic vertex attribute to be enabled
     *
     * @see [Reference Page](http://docs.gl/gl4/glEnableVertexAttribArray)
     */
    fun glEnableVertexAttribArray(index: Int) {
        gl!!.glEnableVertexAttribArray(index)
    }

    /**
     * Returns the location of a uniform variable.
     *
     * @param program the program object to be queried
     * @param name    a null terminated string containing the name of the uniform variable whose location is to be queried
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetUniformLocation)
     */
    fun glGetUniformLocation(program: Int, name: String): Int {
        return gl!!.glGetUniformLocation(program, name)
    }

    /**
     * Returns the location of an attribute variable.
     *
     * @param program the program object to be queried
     * @param name    a null terminated string containing the name of the attribute variable whose location is to be queried
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetAttribLocation)
     */
    fun glGetAttribLocation(program: Int, name: String): Int {
        return gl!!.glGetAttribLocation(program, name)
    }

    /**
     * Specifies the value of an int uniform variable for the current program object.
     *
     * @param location the location of the uniform variable to be modified
     * @param v0       the uniform value
     *
     * @see [Reference Page](http://docs.gl/gl4/glUniform)
     */
    fun glUniform1i(location: Int, v0: Int) {
        gl!!.glUniform1i(location, v0)
    }

    /**
     * Specifies the value of a float uniform variable for the current program object.
     *
     * @param location the location of the uniform variable to be modified
     * @param v0       the uniform value
     *
     * @see [Reference Page](http://docs.gl/gl4/glUniform)
     */
    fun glUniform1f(location: Int, v0: Float) {
        gl!!.glUniform1f(location, v0)
    }

    /**
     * Specifies the value of a vec3 uniform variable for the current program object.
     *
     * @param location the location of the uniform variable to be modified
     * @param v0       the uniform x value
     * @param v1       the uniform y value
     * @param v2       the uniform z value
     *
     * @see [Reference Page](http://docs.gl/gl4/glUniform)
     */
    fun glUniform3f(location: Int, v0: Float, v1: Float, v2: Float) {
        gl!!.glUniform3f(location, v0, v1, v2)
    }

    /**
     * Specifies the value of a single mat2 uniform variable or a mat2 uniform variable array for the current program object.
     *
     * @param location  the location of the uniform variable to be modified
     * @param transpose whether to transpose the matrix as the values are loaded into the uniform variable
     * @param value     a pointer to an array of `count` values that will be used to update the specified uniform variable
     *
     * @see [Reference Page](http://docs.gl/gl4/glUniform)
     */
    fun glUniformMatrix2fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        gl!!.glUniformMatrix2fv(location, transpose, value)
    }

    /**
     * Specifies the value of a single mat3 uniform variable or a mat3 uniform variable array for the current program object.
     *
     * @param location  the location of the uniform variable to be modified
     * @param transpose whether to transpose the matrix as the values are loaded into the uniform variable
     * @param value     a pointer to an array of `count` values that will be used to update the specified uniform variable
     *
     * @see [Reference Page](http://docs.gl/gl4/glUniform)
     */
    fun glUniformMatrix3fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        gl!!.glUniformMatrix3fv(location, transpose, value)
    }

    /**
     * Specifies the value of a single mat4 uniform variable or a mat4 uniform variable array for the current program object.
     *
     * @param location  the location of the uniform variable to be modified
     * @param transpose whether to transpose the matrix as the values are loaded into the uniform variable
     * @param value     a pointer to an array of `count` values that will be used to update the specified uniform variable
     *
     * @see [Reference Page](http://docs.gl/gl4/glUniform)
     */
    fun glUniformMatrix4fv(location: Int, transpose: Boolean, value: FloatBuffer) {
        gl!!.glUniformMatrix4fv(location, transpose, value)
    }

    /**
     * Specifies the location and organization of a vertex attribute array.
     *
     * @param index      the index of the generic vertex attribute to be modified
     * @param size       the number of values per vertex that are stored in the array. The initial value is 4. One of:<br></br><table><tr><td>1</td><td>2</td><td>3</td><td>4</td><td>[BGRA][GL12.GL_BGRA]</td></tr></table>
     * @param type       the data type of each component in the array. The initial value is GL_FLOAT. One of:<br></br><table><tr><td>[BYTE][GL11.GL_BYTE]</td><td>[UNSIGNED_BYTE][GL11.GL_UNSIGNED_BYTE]</td><td>[SHORT][GL11.GL_SHORT]</td><td>[UNSIGNED_SHORT][GL11.GL_UNSIGNED_SHORT]</td><td>[INT][GL11.GL_INT]</td><td>[UNSIGNED_INT][GL11.GL_UNSIGNED_INT]</td><td>[HALF_FLOAT][GL30.GL_HALF_FLOAT]</td><td>[FLOAT][GL11.GL_FLOAT]</td></tr><tr><td>[DOUBLE][GL11.GL_DOUBLE]</td><td>[UNSIGNED_INT_2_10_10_10_REV][GL12.GL_UNSIGNED_INT_2_10_10_10_REV]</td><td>[INT_2_10_10_10_REV][GL33.GL_INT_2_10_10_10_REV]</td><td>[FIXED][GL41.GL_FIXED]</td></tr></table>
     * @param normalized whether fixed-point data values should be normalized or converted directly as fixed-point values when they are accessed
     * @param stride     the byte offset between consecutive generic vertex attributes. If stride is 0, the generic vertex attributes are understood to be tightly packed in
     * the array. The initial value is 0.
     * @param pointer    the vertex attribute data or the offset of the first component of the first generic vertex attribute in the array in the data store of the buffer
     * currently bound to the [ARRAY_BUFFER][GL15.GL_ARRAY_BUFFER] target. The initial value is 0.
     *
     * @see [Reference Page](http://docs.gl/gl4/glVertexAttribPointer)
     */
    fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        pointer: Int
    ) {
        gl!!.glVertexAttribPointer(index, size, type, normalized, stride, pointer)
    }

    /**
     * Returns a parameter from a shader object.
     *
     * @param shader the shader object to be queried
     * @param pname  the object parameter. One of:<br></br><table><tr><td>[SHADER_TYPE][GL20C.GL_SHADER_TYPE]</td><td>[DELETE_STATUS][GL20C.GL_DELETE_STATUS]</td><td>[COMPILE_STATUS][GL20C.GL_COMPILE_STATUS]</td><td>[INFO_LOG_LENGTH][GL20C.GL_INFO_LOG_LENGTH]</td><td>[SHADER_SOURCE_LENGTH][GL20C.GL_SHADER_SOURCE_LENGTH]</td></tr></table>
     *
     * @see [Reference Page](http://docs.gl/gl4/glGetShader)
     */
    fun glGetShaderi(shader: Int, pname: Int): Int {
        return gl!!.glGetShaderi(shader, pname)
    }

    fun glDeleteShader(shader: Int) = gl!!.glDeleteShader(shader)

    fun glDeleteProgram(program: Int) = gl!!.glDeleteProgram(program)

}
