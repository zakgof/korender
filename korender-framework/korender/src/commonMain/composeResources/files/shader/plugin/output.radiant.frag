#uniform float radiantMax;

vec4 pluginOutput() {
    float radiant = length(cameraPos - vpos) / radiantMax;
    return vec4(radiant, radiant, radiant, 1.0);
}