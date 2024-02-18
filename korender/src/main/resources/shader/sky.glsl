vec2 skydisk(vec2 vtex, vec3 cameraPos, mat4 projview) {
    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 direction = inverse(projview) * vec4(csp, 0.0, 1.0);
    vec3 look = normalize(direction.xyz / direction.w - cameraPos);
    float phi = atan(look.z, look.x);
    float theta = atan(sqrt(look.x * look.x + look.z * look.z), look.y * 1.5);
    return 0.5 + theta / 3.1415926 * vec2(cos(phi), sin(phi));
}