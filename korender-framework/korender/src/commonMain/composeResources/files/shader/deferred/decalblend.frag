#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D cdiffTexture;
uniform sampler2D normalTexture;
uniform sampler2D materialTexture;

uniform sampler2D decalDiffuse;
uniform sampler2D decalNormal;
uniform sampler2D decalMaterial;

layout(location = 0) out vec3 cdiffChannel;
layout(location = 1) out vec3 normalChannel;
layout(location = 2) out vec4 materialChannel;

void main() {

    vec4 dd = texture(decalDiffuse, vtex);

    cdiffChannel = mix(texture(cdiffTexture, vtex).rgb, dd.rgb, dd.a);
    normalChannel = texture(normalTexture, vtex).rgb;
    materialChannel = mix(texture(materialTexture, vtex), texture(decalMaterial, vtex), dd.a);
}