#version 330 core

in vec4 fragmentColor;
in vec3 normalVector;
in vec3 fragmentPosition;
in vec2 textureCoord;

out vec4 finalColor;

uniform vec4 specularColor = vec4(0.5, 0.5, 0.5,1);
uniform vec4 ambientColor = vec4(0.2, 0.2, 0.2,1);
uniform vec4 diffuseColor = vec4(1,1,1,1);
uniform vec4 emissionColor = vec4(0,0,0,1);
uniform vec4 lightColor = vec4(1,1,1,1);
uniform int shininess = 32;

uniform vec3 lightPos; // Light position in world space
uniform vec3 cameraPos;  // Camera position in world space

uniform sampler2D diffuseTexture;

uniform bool useTexture;
uniform bool useLighting;
uniform bool useVertexColor;  // per-vertex color

void main() {
    vec4 myColor = diffuseColor;
    if(useVertexColor) myColor *= fragmentColor;
    if(useTexture) myColor *= texture(diffuseTexture, textureCoord);

    vec4 result = myColor;

    if(useLighting) {
        vec3 norm = normalize(normalVector);
        vec3 lightDir = normalize(lightPos - fragmentPosition);

        // Diffuse
        float diff = max(dot(norm, lightDir), 0.0);
        vec4 diffuseLight = diff * lightColor;

        // Specular
        vec3 viewDir = normalize(cameraPos - fragmentPosition);
        vec3 reflectDir = reflect(-lightDir, norm);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
        vec4 specularLight = spec * specularColor * lightColor;

        // put it all together.
        result *= ambientColor + diffuseLight + specularLight;
        result += emissionColor;
    }

    //finalColor = vec4(textureCoord.x,textureCoord.y,0,1);  // for testing texture coordinates
    finalColor = result;
    finalColor.a = myColor.a;
}
