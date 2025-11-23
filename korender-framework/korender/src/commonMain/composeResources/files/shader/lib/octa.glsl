vec2 encodeOcta(vec3 n) {
    n /= (abs(n.x) + abs(n.y) + abs(n.z));
    vec2 e = n.xy;
    if (n.z < 0.0)
    e = (1.0 - abs(e.yx)) * vec2(
    e.x >= 0.0 ? 1.0 : -1.0,
    e.y >= 0.0 ? 1.0 : -1.0);
    return e * 0.5 + 0.5;  // map [-1,1] → [0,1]
}

vec3 decodeOcta(vec2 e)
{
    e = e * 2.0 - 1.0; // [0,1] -> [-1,1]
    vec3 n = vec3(e.x, e.y, 1.0 - abs(e.x) - abs(e.y));
    float t = clamp(-n.z, 0.0, 1.0);
    n.x += (n.x >= 0.0 ? -t : t);
    n.y += (n.y >= 0.0 ? -t : t);
    return normalize(n);
}

