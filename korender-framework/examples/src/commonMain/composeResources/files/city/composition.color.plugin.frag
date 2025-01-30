
vec3 pluginColor(vec3 vpos, vec3 color, float depth) {

    float distance = length(vpos - cameraPos);

    float fogFactor = (depth >= 0.9999) ? 1.0 : exp( -0.2 * distance);

    color = mix(vec3(1.0, 0.0, 0.0), color, fogFactor);

    return color;
}