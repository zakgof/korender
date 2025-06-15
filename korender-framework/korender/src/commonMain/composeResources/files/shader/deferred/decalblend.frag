#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D diffuseGeometryTexture;
uniform sampler2D normalGeometryTexture;
uniform sampler2D materialGeometryTexture;

uniform sampler2D decalDiffuse;
uniform sampler2D decalNormal;
uniform sampler2D decalMaterial;

layout(location = 0) out vec3 diffuseChannel;
layout(location = 1) out vec3 normalChannel;
layout(location = 2) out vec4 materialChannel;

void main() {

    vec4 dd = texture(decalDiffuse, vtex);
    vec4 dn = texture(decalNormal, vtex);

    diffuseChannel = mix(texture(diffuseGeometryTexture, vtex).rgb, dd.rgb, dd.a);
    normalChannel = mix(texture(normalGeometryTexture, vtex).rgb, dn.rgb, dn.a);
    materialChannel = mix(texture(materialGeometryTexture, vtex), texture(decalMaterial, vtex), dd.a);
}