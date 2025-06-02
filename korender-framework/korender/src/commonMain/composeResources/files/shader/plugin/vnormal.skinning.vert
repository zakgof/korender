vec3 pluginVNormal() {
    return mat3(transpose(inverse(model * skinningMatrix))) * normal;
}