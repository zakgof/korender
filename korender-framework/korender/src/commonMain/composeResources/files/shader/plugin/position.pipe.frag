vec3 pipeNormal;
in vec3 vleft;
in vec2 vscale;

vec3 pluginPosition() {
    float cos = (vtex.x - 0.5) * 2.0;
    float width = mix(vscale.x, vscale.y, vtex.y);
    vec3 flatPos = vpos + vleft * cos + vnormal * vtex.y;

    vec3 toEye = normalize(cameraPos - vpos);
    vec3 back = normalize(cross(vnormal, vleft));
    pipeNormal =  normalize((vleft * cos + back * sqrt(1.0 - cos * cos)) * length(vnormal) +
        normalize(vnormal) * (vscale.y - vscale.x));

    return vpos - back * width * (1.0 - dot(pipeNormal, toEye));
}