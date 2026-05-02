uniform sampler2D ssaoTexture;

float linearize(float d) {
    return d; // TODO
}

float sampleSsao() {

    float refDepth = linearize(texture(depthGeometryTexture, vtex).r);

    float radius = 1.3 / vec2(textureSize(ssaoTexture, 0)).x; // TODO

    float result = 0.0, totalWeight = 0.0;
    for (int i = 0; i < 5; i++) {

        vec2 offset = vogelDiskSample(i, 5, 0.) * radius;
        vec2 sampleUV = vtex + offset;
        vec4 ssaoSmpl = texture(ssaoTexture, sampleUV);
        float sampleDepth = linearize(ssaoSmpl.g);
        float ssao = ssaoSmpl.r;

        float depthDiff = abs(refDepth - sampleDepth);
        float weight = exp(-depthDiff * 32.); // tune depthSigma

        result      += ssao * weight;
        totalWeight += weight;
    }

    return result / max(totalWeight, 1e-4);
}