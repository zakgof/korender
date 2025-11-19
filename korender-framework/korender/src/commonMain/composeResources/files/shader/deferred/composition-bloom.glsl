uniform sampler2D bloomTexture;
uniform sampler2D bloomDepth;
#uniform float bloomAmount;

vec3 compositionBloom(vec3 originalColor, float originalDepth, vec2 vtex) {
    vec4 bloomSample = texture(bloomTexture, vtex);
    float bDepth = texture(bloomDepth, vtex).r;
    float depthRatio = smoothstep(originalDepth + 0.1, originalDepth - 0.1, bDepth);
    return originalColor + bloomSample.rgb * depthRatio * bloomAmount;
}
