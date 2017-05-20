#version 330

in vec3 vNormal;
in vec3 vPosition;
//in vec2 vTexCoord;
in vec4 vColor;

out vec4 fragColor;

uniform vec4 lightPosition;
uniform vec3 lightAmbientColor;
uniform vec3 lightDiffuseColor;
uniform vec3 lightSpecularColor;

uniform vec3 eyePosition;


vec3 phong(vec3 matAmbientColor, vec3 matDiffuseColor, vec3 matSpecularColor, float matShininess);

void main() {
    vec3 color = vColor.rgb;

    vec3 lighting = phong(color, color, vec3(1), 32);

    fragColor = vec4(lighting, 1.0);
}

vec3 phong(vec3 matAmbientColor, vec3 matDiffuseColor, vec3 matSpecularColor, float matShininess) {
    vec3 N = normalize(vNormal);

    vec3 lightDirection;
    if (lightPosition.w == 0.0) {
        lightDirection = normalize(lightPosition.xyz);
    } else {
        lightDirection = normalize(lightPosition.xyz - vPosition);
    }

    vec3 viewDirection = normalize(eyePosition - vPosition);
    vec3 halfVector = normalize(lightDirection + viewDirection);

    vec3 ambient = lightAmbientColor * matAmbientColor;

    float diffuseFactor = max(dot(N, lightDirection), 0.0);
    vec3 diffuse = lightDiffuseColor * matDiffuseColor * diffuseFactor;

    float specularFactor = pow(max(dot(N, halfVector), 0.0), matShininess) * diffuseFactor;
    vec3 specular = lightSpecularColor * matSpecularColor * specularFactor;

    return ambient + diffuse + specular;
}
