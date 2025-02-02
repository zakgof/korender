uniform sampler2D moonTexture;

vec3 pluginSecsky(vec3 look, vec3 color) {

    vec2 uve = skydiskfromlook(look, 1.5);

    vec2 tex = (uve - vec2(-0.4, -0.4)) * 12.0;

    if (tex.x > 0.0 && tex.x < 1.0 && tex.y > 0.0 && tex.y < 1.0) {
        vec4 moon = texture(moonTexture, tex);
        color = moon.rgb * moon.a + color * (1.0 - moon.a);
    }
    return color;
}