layout(std140) uniform Frame {

    vec3 cameraPos;
    vec3 cameraDir;

    mat4 view;
    float projectionWidth;
    float projectionHeight;
    float projectionNear;
    float projectionFar;

    float screenWidth;
    float screenHeight;
    float time;

    vec3 ambientColor;
    int numDirectionalLights;
    vec3 directionalLightDir[32];
    vec3 directionalLightColor[32];
    int directionalLightShadowTextureIndex[32];
    int directionalLightShadowTextureCount[32];

    int numPointLights;
    vec3 pointLightPos[32];
    vec3 pointLightColor[32];
    vec3 pointLightAttenuation[32];

    int numShadows;
    mat4 bsps[5];
    vec4 cascade[5];
    float yMin[5];
    float yMax[5];
    int shadowMode[5];
    float f1[5];
    float f2[5];
    int i1[5];

};