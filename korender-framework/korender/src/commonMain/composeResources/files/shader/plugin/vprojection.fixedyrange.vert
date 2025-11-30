#uniform float fixedYMin;
#uniform float fixedYMax;

vec4 pluginVProjection(vec3 viewPos) {
    return vec4(
        viewPos.x * 2. / projectionWidth,
        viewPos.y * 2. / projectionHeight,
        vpos.y * 2.0 / (fixedYMin - fixedYMax) - (fixedYMin + fixedYMax) / (fixedYMin - fixedYMax),
        1.0
    );
}