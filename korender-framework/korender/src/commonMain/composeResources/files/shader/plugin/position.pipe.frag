vec3 pipeNormal;
in vec3 vleft;
in vec2 vscale;

vec3 pluginPosition() {
    float cos = (vtex.x - 0.5) * 2.0;
    float width = mix(vscale.x, vscale.y, vtex.y);
    vec3 toEye = normalize(cameraPos - vpos);
    pipeNormal =  normalize((vleft * cos + toEye * sqrt(1.0 - cos * cos)) * length(vnormal) + normalize(vnormal) * (vscale.y - vscale.x));
    return vpos + width * toEye * dot(pipeNormal, toEye);
}