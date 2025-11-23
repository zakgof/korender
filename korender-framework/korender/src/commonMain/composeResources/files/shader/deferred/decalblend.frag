#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D albedoGeometryTexture;
uniform sampler2D normalGeometryTexture;

uniform sampler2D decalAlbedo;
uniform sampler2D decalNormal;

layout(location = 0) out vec4 albedoChannel;
layout(location = 1) out vec4 normalChannel;

void main() {

    vec4 da = texture(decalAlbedo, vtex);
    vec4 dn = texture(decalNormal, vtex);

    vec4 origAlbedoSample = texture(albedoGeometryTexture, vtex);
    vec4 origNormalSample = texture(normalGeometryTexture, vtex);

    albedoChannel.rgb = mix(origAlbedoSample.rgb, da.rgb, da.a);
    albedoChannel.a = origAlbedoSample.a;

    normalChannel.rgb = mix(origNormalSample.rgb, dn.rgb, dn.a);
    normalChannel.a = origNormalSample.a;
}