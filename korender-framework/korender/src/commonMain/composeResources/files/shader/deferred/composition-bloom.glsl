uniform sampler2D bloomTexture;
uniform sampler2D bloomDepth;
#uniform float bloomAmount;

void compositionBloom() {
    vec4 bloomSample = texture(bloomTexture, vtex);
    float bDepth = texture(bloomDepth, vtex).r;
    float depthRatio = 1.0 - smoothstep(depth - 0.1, depth + 0.1, bDepth);
    color += bloomSample.rgb * depthRatio * bloomAmount;
}
