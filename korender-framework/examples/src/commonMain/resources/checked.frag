uniform vec3 color1;
uniform vec3 color2;

vec4 pluginTexture() {
    return vec4(fract(vtex.x * 20.) > 0.5 ? color1 : color2, 1.);
}