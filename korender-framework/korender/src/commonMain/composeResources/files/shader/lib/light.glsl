mat3 cotangentFrame(vec3 N, vec3 p, vec2 uv) {
    vec3 dp1 = dFdx(p);
    vec3 dp2 = dFdy(p);
    vec2 duv1 = dFdx(uv);
    vec2 duv2 = dFdy(uv);
    vec3 dp2perp = cross(dp2, N);
    vec3 dp1perp = cross(N, dp1);
    vec3 T = dp2perp * duv1.x + dp1perp * duv2.x;
    vec3 B = dp2perp * duv1.y + dp1perp * duv2.y;
    float invmax = inversesqrt(max(dot(T, T), dot(B, B)));
    return mat3(T * invmax, B * invmax, N);
}

vec3 perturbNormal(vec3 normal, sampler2D normalMap, vec2 uv, vec3 vPos) {
    vec3 map = texture(normalMap, uv).xyz * 255. / 127. - 128. / 127.;
    mat3 TBN = cotangentFrame(normal, -vPos, uv);
    return normalize(TBN * map);
}

float lite(vec3 light, vec3 normal, vec3 look, float ambient, float diffuse, float specular, float specularPower) {
    float diffuseRatio = diffuse * clamp(dot(-light, normal), 0.0, 1.0);
    float specRatio = specular * pow(clamp(dot(reflect(-light, normal), look), 0.0, 1.0), specularPower);
    return ambient + diffuseRatio + specRatio;
}

float[25] kernel5 = float[](
    1.,  4.,  7.,  4.,  1.,
    4., 20., 33., 20., 4.,
    7., 33., 55., 33., 7.,
    4., 20., 33., 20., 4.,
    1.,  4.,  7.,  4.,  1.
);

float shadow(sampler2D shadowTexture, vec3 vshadow) {
    float beavis = 0.01;
    #ifdef PCSS
    float centerSample = texture(shadowTexture, vshadow.xy).r;
        float penumbraWidth = centerSample > 0.001 ?  0.03 * (vshadow.z - centerSample) / centerSample : 0.0;
        float cumulative = 0.;
        float weight = 0.;
        for (int x = -2; x <= 2; ++x) {
            for (int y = -2; y <= 2; ++y) {
                float w = kernel5[(x+2)*5 + (y+2)];
                vec2 uv = vshadow.xy + vec2(x, y) * penumbraWidth;
                float shadowSample = texture(shadowTexture, uv).r;
                float val = (shadowSample > 0.001 && shadowSample > vshadow.z + beavis && uv.x > 0. && uv.x < 1. && uv.y > 0. && uv.y < 1.) ? 1. : 0.;
                cumulative += w * val;
                weight += w;
            }
        }
        return cumulative / weight;
    #else
        float shadowSample = texture(shadowTexture, vshadow.xy).r;
        return (shadowSample > 0.001 && shadowSample > vshadow.z + beavis) ? 1.0 : 0.0;
    #endif
}