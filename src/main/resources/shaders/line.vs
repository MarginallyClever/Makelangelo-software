#version 150

uniform vec2 uViewPort; //Width and Height of the viewport
varying vec2 vLineCenter;

void main(void)
{
  vec4 pp = gl_ModelViewProjectionMatrix * gl_Vertex;
  gl_Position = pp;
  vec2 vp = uViewPort;
  vLineCenter = 0.5*(pp.xy + vec2(1, 1))*vp;
};