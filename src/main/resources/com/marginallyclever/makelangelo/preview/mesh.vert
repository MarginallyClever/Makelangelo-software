#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTexture;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

uniform mat4 lightSpaceMatrix;
uniform mat4 lightViewMatrix;
uniform mat4 lightProjectionMatrix;

out VS_OUT {
    vec4 fragmentColor;
    vec3 normalVector;
    vec3 fragmentPosition;
    vec2 textureCoord;
    vec4 fragPosLightSpace;
} vs_out;

void main() {
    vec3 worldPose = vec3(modelMatrix * vec4(aPosition, 1.0));
    gl_Position = projectionMatrix * viewMatrix * vec4(worldPose,1);

    vs_out.fragmentColor = aColor;
    vs_out.normalVector = transpose(inverse(mat3(modelMatrix))) * aNormal;
    vs_out.fragmentPosition = worldPose;
    vs_out.textureCoord = aTexture;
    //vs_out.fragPosLightSpace = lightSpaceMatrix * vec4(worldPose,1);
    vs_out.fragPosLightSpace = lightProjectionMatrix * lightViewMatrix * vec4(worldPose,1);
}
