#import "!shader/lib/noise.glsl"

#ifdef SKY_CUBE_ENV0
uniform samplerCube envTexture0;
#endif
#ifdef SKY_CUBE_ENV1
uniform samplerCube envTexture1;
#endif
#ifdef SKY_CUBE_ENV2
uniform samplerCube envTexture2;
#endif
#ifdef SKY_CUBE_ENV3
uniform samplerCube envTexture3;
#endif
#ifdef SKY_CUBE_ENV4
uniform samplerCube envTexture4;
#endif
#ifdef SKY_CUBE_ENV5
uniform samplerCube envTexture5;
#endif
#ifdef SKY_CUBE_ENV6
uniform samplerCube envTexture6;
#endif
#ifdef SKY_CUBE_ENV7
uniform samplerCube envTexture7;
#endif
#ifdef SKY_CUBE
uniform samplerCube cubeTexture;
#endif

vec3 sky(vec3 look) {
    #ifdef SKY_CUBE_ENV0
    return texture(envTexture0, look).rgb;
    #endif
    #ifdef SKY_CUBE_ENV1
    return texture(envTexture1, look).rgb;
    #endif
    #ifdef SKY_CUBE_ENV2
    return texture(envTexture2, look).rgb;
    #endif
    #ifdef SKY_CUBE_ENV3
    return texture(envTexture3, look).rgb;
    #endif
    #ifdef SKY_CUBE_ENV4
    return texture(envTexture4, look).rgb;
    #endif
    #ifdef SKY_CUBE_ENV5
    return texture(envTexture5, look).rgb;
    #endif
    #ifdef SKY_CUBE_ENV6
    return texture(envTexture6, look).rgb;
    #endif
    #ifdef SKY_CUBE_ENV7
    return texture(envTexture7, look).rgb;
    #endif
    #ifdef SKY_CUBE
    return texture(cubeTexture, look).rgb;
    #endif
}