#ifdef GLES
    #version 300 es
    precision mediump float;
//vec4 _texture2D(sampler2D sampler, vec2 tex) {
//    return texture(sampler, tex);
//}
#else
    #version 140
//vec4 _texture2D(sampler2D sampler, vec2 tex) {
//    return texture2D(sampler, tex);
//}
#endif