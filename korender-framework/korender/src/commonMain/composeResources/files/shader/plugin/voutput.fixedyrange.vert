#uniform float fixedYMin;
#uniform float fixedYMax;

vec4 pluginVOutput(vec4 worldPos) {
    vec4 tpos = projection * (view * worldPos);
    tpos.z = (worldPos.y * 2.0 / (fixedYMin - fixedYMax) - (fixedYMin + fixedYMax) / (fixedYMin - fixedYMax)) * tpos.w;
    return tpos;
}