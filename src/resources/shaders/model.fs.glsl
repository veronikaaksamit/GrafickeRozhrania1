#version 330

in vec3 vNormal;
in vec3 vPosition;

out vec4 fragColor;

uniform vec4 lightPosition1;
uniform vec3 lightAmbientColor1;
uniform vec3 lightDiffuseColor1;
uniform vec3 lightSpecularColor1;



uniform struct Light {
   vec4 position;
   vec3 intensities;
   float attenuation;
   float ambientCoefficient;
   float coneAngle;    // new
   vec3 coneDirection; // new
};
uniform vec4 lightPosition2;
uniform vec3 lightAmbientColor2;
uniform vec3 lightDiffuseColor2;
uniform vec3 lightSpecularColor2;
uniform float coneAngle;
uniform float coneDirection;

uniform vec3 eyePosition;

uniform vec3 materialAmbientColor;
uniform vec3 materialDiffuseColor;
uniform vec3 materialSpecularColor;
uniform float materialShininess;

void main() {
    //normalized light direction
    vec3 lightDirection1;
    if(lightPosition1.w == 0.0) {
        //directional light
        lightDirection1 = normalize(lightPosition1.xyz);
    } else {
        //point light
        lightDirection1 = normalize(lightPosition1.xyz - vPosition);
    }
    //direction from vertex to eye position
    vec3 viewDirection = normalize(eyePosition - vPosition);
    //halfway vector
    vec3 halfVector = normalize(lightDirection1 + viewDirection);
    //lightDiffuseColor changes color of light
    float diffuseFactor = max(dot(vNormal, lightDirection1), 0.0);
    vec3 diffuse = lightDiffuseColor1 * materialDiffuseColor * diffuseFactor;
    //ambient variable ... dark grey = vec3(0.15)
    vec3 ambient = lightAmbientColor1 * materialAmbientColor;
    // specular = lightSpecularColor * pow(max(dot(vNormal, halfVector), 0), materialShininess) * diffuseFactor
    vec3 specular = lightSpecularColor1 * materialSpecularColor * pow(max(dot(vNormal, halfVector), 0.0), materialShininess) * diffuseFactor;
    vec3 lightFinal1 = ambient + diffuse + specular;

    //SECONDLIGHT
    vec3 lightDirection2;
    if(lightPosition2.w == 0.0) {
        //directional light
        lightDirection2 = normalize(lightPosition2.xyz);
    } else {
        //point light
        lightDirection2 = normalize(lightPosition2.xyz - vPosition);
    }
    //halfway vector
    halfVector = normalize(lightDirection2 + viewDirection);
    diffuseFactor = max(dot(vNormal, lightDirection2), 0.0);
    diffuse = lightDiffuseColor2 * materialDiffuseColor * diffuseFactor;
    ambient = lightAmbientColor2 * materialAmbientColor;
    specular = lightSpecularColor2 * materialSpecularColor * pow(max(dot(vNormal, halfVector), 0.0), materialShininess) * diffuseFactor;
    vec3 lightFinal2 = ambient + diffuse + specular ;

    vec3 lightFinal = lightFinal1 + lightFinal2;


    fragColor = vec4(lightFinal, 1.0);
}