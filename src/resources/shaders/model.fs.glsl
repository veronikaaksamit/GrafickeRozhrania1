#version 330

in vec3 vNormal;
in vec3 vPosition;

out vec4 fragColor;

uniform vec4 lightPosition;
uniform vec3 lightAmbientColor;
uniform vec3 lightDiffuseColor;
uniform vec3 lightSpecularColor;

uniform vec3 eyePosition;

uniform vec3 materialAmbientColor;
uniform vec3 materialDiffuseColor;
uniform vec3 materialSpecularColor;
uniform float materialShininess;

void main() {
    //normalized light direction
    vec3 lightDirection;
    if(lightPosition.w == 0.0) {
        //directional light
        lightDirection = normalize(lightPosition.xyz);
    } else {
        //point light
        lightDirection = normalize(lightPosition.xyz - vPosition);
    }

    //direction from vertex to eye position
    vec3 viewDirection = normalize(eyePosition - vPosition);
    //halfway vector
    vec3 halfVector = normalize(lightDirection + viewDirection);

    //lightDiffuseColor changes color of light
    float diffuseFactor = max(dot(vNormal, lightDirection), 0.0);
    vec3 diffuse = lightDiffuseColor * materialDiffuseColor * diffuseFactor;

    //ambient variable ... dark grey = vec3(0.15)
    vec3 ambient = lightAmbientColor * materialAmbientColor;

    // specular = lightSpecularColor * pow(max(dot(vNormal, halfVector), 0), materialShininess) * diffuseFactor
    vec3 specular = lightSpecularColor * materialSpecularColor * pow(max(dot(vNormal, halfVector), 0.0), materialShininess) * diffuseFactor;

    vec3 lightFinal = ambient + diffuse + specular;
    fragColor = vec4(lightFinal, 1.0);
}