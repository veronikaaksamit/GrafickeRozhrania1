#version 330

in vec3 vNormal;
in vec3 vPosition;

in vec2 vTexCoord;

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

uniform sampler2D myTexture;


struct SpotLight
{
    vec3 position;
    vec3 direction;
    float cutOff;
    float outerCutOff;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform SpotLight spotLight;

vec3 phong(vec3 matAmbientColor, vec3 matDiffuseColor, vec3 matSpecularColor, float matShininess);
vec3 CalcSpotLight( SpotLight light, vec3 normal, vec3 fragPos, vec3 color);

void main() {
    
    vec3 mixColor = texture(myTexture, vTexCoord).rgb;

    vec3 color = phong(mixColor, mixColor, materialSpecularColor, materialShininess);
// (opacity) from 100% to 40% (range is 0.0-1.0)
    
    vec3 color1 = CalcSpotLight( spotLight, vNormal, vPosition, mixColor );

    fragColor = vec4(color + color1, 1);
}

/*
 * Computes lighting using Blinn-Phong model.
 */
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

vec3 CalcSpotLight( SpotLight light, vec3 normal, vec3 fragPos, vec3 color )
{
    vec3 lightDir = normalize( light.position - fragPos );
    vec3 viewDir = normalize(fragPos - light.position);

    // Diffuse shading
    float diff = max( dot( normal, lightDir ), 0.0 );

    // Specular shading
    vec3 reflectDir = reflect( -lightDir, normal );
    float spec = pow( max( dot( viewDir, reflectDir ), 0.0 ), materialShininess);

    // Attenuation
    float distance = length( light.position - fragPos );
    float attenuation = 1;// ( light.constant + light.linear * distance + light.quadratic * ( distance * distance ) );

    // Spotlight intensity
    float theta = dot( lightDir, normalize( -light.direction ) );
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp( ( theta - light.outerCutOff ) / epsilon, 0.0, 1.0 );

    // Combine results
    vec3 ambient = light.ambient *color;
    vec3 diffuse = light.diffuse * diff * color;
    vec3 specular = light.specular * spec * materialSpecularColor;

    ambient *= attenuation * intensity;
    diffuse *= attenuation * intensity;
    specular *= attenuation * intensity;

    return ( ambient + diffuse + specular );
}