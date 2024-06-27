uniform vec4 color1;
uniform vec4 color2;

vec4 pluginTexture() {
    return fract(vtex.x * 20.) > 0.5 ? color1 : color2;
}