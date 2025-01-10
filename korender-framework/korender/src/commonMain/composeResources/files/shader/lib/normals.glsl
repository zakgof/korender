vec3 getNormalFromMap(sampler2D normalMap, vec3 vnormal, vec2 vtex, vec3 vpos) {
    vec3 tangentNormal = textureRegOrTriplanar(normalMap, vtex, vpos, vnormal).rgb;
    tangentNormal = tangentNormal * 2.0 - 1.0; // Convert from [0,1] to [-1,1]

    vec3 Q1 = dFdx(vpos);
    vec3 Q2 = dFdy(vpos);
    vec2 st1 = dFdx(vtex);
    vec2 st2 = dFdy(vtex);

    vec3 N = normalize(vnormal);
    vec3 T = normalize(Q1 * st2.t - Q2 * st1.t);
    vec3 B = cross(N, T);

    mat3 TBN = mat3(T, B, N);
    return normalize(TBN * tangentNormal);
}