uniform sampler2D ssaoTexture;

float linearize(float d) {
    return d; // TODO
}

//vec2 vogelDiskSample(int sampleIndex, int numSamples, float phi) {
//    float goldenAngle = 2.39996323;
//    float sampleVal = float(sampleIndex);
//    float angle = sampleVal * goldenAngle + phi;
//    return vec2(cos(angle), sin(angle)) * sqrt(sampleVal + 0.5) / sqrt(float(numSamples));
//}

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
        float weight = exp(-depthDiff * 8.0); // tune depthSigma

        result      += ssao * weight;
        totalWeight += weight;
    }

    return result / max(totalWeight, 1e-4);
}