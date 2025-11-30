layout(location = 3) in uvec4 joints;
layout(location = 4) in vec4 weights;

mat4 skinningMatrix;

#ifdef INSTANCING
    uniform sampler2D jntTexture;
#else
    #uniform mat4 jntMatrices[32];
#endif


#ifdef INSTANCING
    mat4 fetchJointMatrix(uint jnt) {
        mat4 jointMatrix;
        jointMatrix[0] = texelFetch(jntTexture, ivec2(int(jnt) * 4 + 0, gl_InstanceID), 0);
        jointMatrix[1] = texelFetch(jntTexture, ivec2(int(jnt) * 4 + 1, gl_InstanceID), 0);
        jointMatrix[2] = texelFetch(jntTexture, ivec2(int(jnt) * 4 + 2, gl_InstanceID), 0);
        jointMatrix[3] = texelFetch(jntTexture, ivec2(int(jnt) * 4 + 3, gl_InstanceID), 0);
        return jointMatrix;
    }
#endif

vec4 pluginVPosition() {
    #ifdef INSTANCING
        skinningMatrix =
            weights.x * fetchJointMatrix(joints.x) +
            weights.y * fetchJointMatrix(joints.y) +
            weights.z * fetchJointMatrix(joints.z) +
            weights.w * fetchJointMatrix(joints.w);
    #else
        skinningMatrix =
            weights.x * jntMatrices[joints.x] +
            weights.y * jntMatrices[joints.y] +
            weights.z * jntMatrices[joints.z] +
            weights.w * jntMatrices[joints.w];
    #endif
    return totalModel * (skinningMatrix * vec4(pos, 1.0));
}