#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D cdiffTexture;
uniform sampler2D normalTexture;
uniform sampler2D materialTexture;

uniform sampler2D decalAlbedo;
uniform sampler2D decalNormal;

layout(location = 0) out vec3 cdiffChannel;
layout(location = 1) out vec3 normalChannel;
layout(location = 2) out vec4 materialChannel;

void main() {
    cdiffChannel = texture(cdiffTexture, vtex).xyz + texture(decalAlbedo, vtex).xyz;
    normalChannel = texture(normalTexture, vtex).xyz;
    materialChannel = texture(materialTexture, vtex);
}