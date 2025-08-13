#ifdef VERTEX_SHADER
    out float vdepth;
#else
    in float vdepth;
#endif

vec4 pluginVProjection(vec3 viewPos) {

    #ifdef VERTEX_SHADER
        vdepth = 1.0 - viewPos.z;
    #endif

    return vec4(
        viewPos.x * 2. * projectionNear / projectionWidth,
        viewPos.y * 2. * projectionNear / projectionHeight,
        - viewPos.z * (2.0 * log2(max(1e-6, 1.0 - viewPos.z)) / log2(projectionFar + 1.0) - 1.0),
        - viewPos.z
    );
}

vec4 screenToViewSpace(vec2 vtex, float depth) {
    vec3 ndc = vec3(vtex * 2.0 - 1.0, depth * 2.0 - 1.0);
    float zview = 1.0 - pow(projectionFar + 1.0, depth);
    return vec4(
        - ndc.x * zview * projectionWidth * 0.5 / projectionNear,
        - ndc.y * zview *  projectionHeight * 0.5 / projectionNear,
        zview,
        1.0
    );
}