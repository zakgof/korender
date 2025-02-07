vec3 screentolook(vec2 vtex, mat4 projview, vec3 cameraPos) {
    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 direction = inverse(projview) * vec4(csp, 0.0, 1.0);
    return normalize(direction.xyz / direction.w - cameraPos);
}

vec3 lookfromskydisk(vec2 vtex, float distortion) {
    vec2 uv = vtex - vec2(0.5);
    float angle = 3.1415926 * length(uv);
    float dsin = distortion * sin(angle);
    return normalize(vec3(uv.x * dsin, length(uv) * cos(angle), uv.y * dsin));
}

vec2 skydiskfromlook(vec3 look, float distortion) {
    float l = length(look.xz);
    float theta = atan(l, look.y * distortion);
    return vec2(0.5) + vec2(look.xz) * (theta / (3.1415926 * l));
}

vec2 skydisk(vec2 vtex, vec3 cameraPos, mat4 projview, float distortion) {
    vec3 look = screentolook(vtex, projview, cameraPos);
    return skydiskfromlook(look, distortion);
}