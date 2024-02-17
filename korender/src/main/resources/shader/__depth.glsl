// TODO : uniforms
float far = 30000.0;
float near = 3.0;

float worlddepth(float fragdepth) {
  return far * near / (far - fragdepth * (far - near)); 
}

float fragdepth(float worlddepth) {
  return far * (worlddepth - near) / (worlddepth * (far - near)); 
}