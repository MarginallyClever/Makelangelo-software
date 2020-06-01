#version 150

uniform float uLineWidth;
uniform vec4 uColor;
uniform float uBlendFactor; //1.5..2.5
varying vec2 vLineCenter;

void main(void) {
      vec4 col = uColor;        
      double d = length(vLineCenter-gl_FragCoord.xy);
      double w = uLineWidth;
      if (d>w)
        col.w = 0;
      else
        col.w *= pow(float((w-d)/w), uBlendFactor);
      gl_FragColor = col;
};