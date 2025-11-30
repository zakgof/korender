vec4 pluginVProjection(vec3 viewPos) {
    return vec4(
        viewPos.x * 2. / projectionWidth,
        viewPos.y * 2. / projectionHeight,
        - 2.0 * viewPos.z / (projectionFar - projectionNear) - (projectionFar + projectionNear) / (projectionFar - projectionNear),
        1.0
    );
}

vec4 screenToViewSpace(vec2 vtex, float depth) {
    vec3 ndc = vec3(vtex * 2.0 - 1.0, depth * 2.0 - 1.0);
    float zview = - 2.0 * projectionNear * projectionFar / ((projectionFar + projectionNear) - ndc.z * (projectionFar - projectionNear));
    return vec4(
        ndc.x * projectionWidth * 0.5,
        ndc.y *  projectionHeight * 0.5,
        - 0.5 * ndc.z  * (projectionFar - projectionNear) - (projectionFar + projectionNear),
        1.0
    );
}