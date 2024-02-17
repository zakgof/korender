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
    vec3 map = texture2D(normalMap, uv).xyz * 255. / 127. - 128. / 127.;
    mat3 TBN = cotangentFrame(normal, -vPos, uv);
    return normalize(TBN * map);
}

float lite(vec3 light, vec3 normal, vec3 look, float ambient, float diffuse, float specular, float specularPower) {
    float diffuseRatio = diffuse * clamp(dot(-light, normal), 0.0, 1.0);
    float specRatio = specular * pow(clamp(dot(reflect(-light, normal), look), 0.0, 1.0), specularPower);
    return ambient + diffuseRatio + specRatio;
}