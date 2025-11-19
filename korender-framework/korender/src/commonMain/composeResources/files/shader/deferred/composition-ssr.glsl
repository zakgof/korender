uniform sampler2D ssrTexture;
uniform sampler2D ssrDepth;
#uniform float ssrDepthTolerance;

vec3 compositionSsr(vec3 originalColor, float originalDepth, vec2 vtex) {

    vec2 offset = 1.0 / vec2(textureSize(ssrDepth, 0));

    vec3 ssrc0 = texture(ssrTexture, vtex).rgb;
    vec3 ssrc1 = texture(ssrTexture, vtex + offset * vec2( 1.0,  1.0)).rgb;
    vec3 ssrc2 = texture(ssrTexture, vtex + offset * vec2(-1.0,  1.0)).rgb;
    vec3 ssrc3 = texture(ssrTexture, vtex + offset * vec2( 1.0, -1.0)).rgb;
    vec3 ssrc4 = texture(ssrTexture, vtex + offset * vec2(-1.0, -1.0)).rgb;

    float ssrd0 = texture(ssrDepth, vtex).r;
    float ssrd1 = texture(ssrDepth, vtex + offset * vec2( 1.0,  1.0)).r;
    float ssrd2 = texture(ssrDepth, vtex + offset * vec2(-1.0,  1.0)).r;
    float ssrd3 = texture(ssrDepth, vtex + offset * vec2( 1.0, -1.0)).r;
    float ssrd4 = texture(ssrDepth, vtex + offset * vec2(-1.0, -1.0)).r;

    float ssrR0  = 1.0 - smoothstep(originalDepth - ssrDepthTolerance, originalDepth + ssrDepthTolerance, ssrd0);
    float ssrR1  = 1.0 - smoothstep(originalDepth - ssrDepthTolerance, originalDepth + ssrDepthTolerance, ssrd1);
    float ssrR2  = 1.0 - smoothstep(originalDepth - ssrDepthTolerance, originalDepth + ssrDepthTolerance, ssrd2);
    float ssrR3  = 1.0 - smoothstep(originalDepth - ssrDepthTolerance, originalDepth + ssrDepthTolerance, ssrd3);
    float ssrR4  = 1.0 - smoothstep(originalDepth - ssrDepthTolerance, originalDepth + ssrDepthTolerance, ssrd4);

    vec3 ssrColor = (4.0 * ssrc0 * ssrR0 + ssrc1 * ssrR1 + ssrc2 * ssrR2 + ssrc3 * ssrR3 + ssrc4 * ssrR4) / 8.0;

    return originalColor + ssrColor;
}
