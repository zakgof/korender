uniform sampler2D hbaoTexture;

float linearize(float d) {
    return d; // TODO
}

float sampleHbao() {
    float refDepth = linearize(texture(depthGeometryTexture, vtex).r);

    float radius = 1.3 / vec2(textureSize(hbaoTexture, 0)).x; // TODO

    float result = 0.0, totalWeight = 0.0;
    for (int i = 0; i < 5; i++) {

        vec2 offset = vogelDiskSample(i, 5, 0.) * radius;
        vec2 sampleUV = vtex + offset;
        vec4 hbaoSmpl = texture(hbaoTexture, sampleUV);
        float sampleDepth = linearize(hbaoSmpl.g);
        float hbao = hbaoSmpl.r;

        float depthDiff = abs(refDepth - sampleDepth);
        float weight = exp(-depthDiff * 32.); // tune depthSigma

        result      += hbao * weight;
        totalWeight += weight;
    }

    return result / max(totalWeight, 1e-4);
}
