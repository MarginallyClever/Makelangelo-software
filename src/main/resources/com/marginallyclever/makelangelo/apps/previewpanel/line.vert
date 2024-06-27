#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexture;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform float viewportWidth;
uniform float viewportHeight;

out vec4 fragmentColor;
out vec2 lineStart;
out vec2 lineEnd;

void main() {
    gl_Position = projectionMatrix * viewMatrix * (modelMatrix * vec4(aPosition, 1.0));

    fragmentColor = aColor;

    vec4 lineStart4 = projectionMatrix * viewMatrix * (modelMatrix * vec4(aNormal, 1.0));
    vec4 lineEnd4 = projectionMatrix * viewMatrix * (modelMatrix * vec4(aTexture.xy, 0.0, 1.0));

    vec2 lineStart2 =  lineStart4.xy / lineStart4.w;
    vec2 lineEnd2 =  lineEnd4.xy / lineStart4.w;

    vec2 viewportSize = vec2(viewportWidth, viewportHeight);
    lineStart = (lineStart2 * 0.5 + 0.5) * viewportSize;
    lineEnd = (lineEnd2 * 0.5 + 0.5) * viewportSize;
}
