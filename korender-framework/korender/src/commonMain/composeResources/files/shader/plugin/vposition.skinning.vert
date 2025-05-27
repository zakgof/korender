layout(location = 3) in uvec4 joints;
layout(location = 4) in vec4 weights;

const int MAX_JOINTS = 32;
uniform mat4 jntMatrices[MAX_JOINTS];

mat4 skinningMatrix;

vec4 pluginVPosition() {
    skinningMatrix =
    weights.x * jntMatrices[joints.x] +
    weights.y * jntMatrices[joints.y] +
    weights.z * jntMatrices[joints.z] +
    weights.w * jntMatrices[joints.w];
    return (skinningMatrix * vec4(pos, 1.0));
}