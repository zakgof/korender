uniform sampler2D hbaoTexture;

float sampleHbao() {
    return texture(hbaoTexture, vtex).r;
}
