vec4 pluginVProjection(vec3 viewPos) {
    return vec4(
        viewPos.x * 2. * projectionNear / projectionWidth,
        viewPos.y * 2. * projectionNear / projectionHeight,
        -viewPos.z * (projectionFar + projectionNear) / (projectionFar - projectionNear) - 2. * projectionFar * projectionNear / (projectionFar - projectionNear),
        -viewPos.z
    );
}

vec4 screenToViewSpace(vec2 vtex, float depth) {
    vec3 ndc = vec3(vtex * 2.0 - 1.0, depth * 2.0 - 1.0);
    float zview = - 2.0 * projectionNear * projectionFar / ((projectionFar + projectionNear) - ndc.z * (projectionFar - projectionNear));
    return vec4(
        - ndc.x * zview * projectionWidth * 0.5 / projectionNear,
        - ndc.y * zview *  projectionHeight * 0.5 / projectionNear,
        zview,
        1.0
    );
}