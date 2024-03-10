vec3 screentolook(vec2 vtex, mat4 projview, vec3 cameraPos) {
    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 direction = inverse(projview) * vec4(csp, 0.0, 1.0);
    return normalize(direction.xyz / direction.w - cameraPos);
}

vec2 skydiskfromlook(vec3 look, float distortion) {
    float phi = atan(look.z, look.x);
    float theta = atan(sqrt(look.x * look.x + look.z * look.z), look.y * distortion);
    return 0.5 + theta / 3.1415926 * vec2(cos(phi), sin(phi));
}

vec2 skydisk(vec2 vtex, vec3 cameraPos, mat4 projview, float distortion) {
    vec3 look = screentolook(vtex, projview, cameraPos);
    return skydiskfromlook(look, distortion);
}