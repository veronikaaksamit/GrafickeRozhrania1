#version 330

in vec3 vNormal;
in vec3 vPosition;
in vec2 vTexcoord;

out vec4 fragColor;


uniform int flag;
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

float mod289(float x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 mod289(vec4 x){return x - floor(x * (1.0 / 289.0)) * 289.0;}
vec4 perm(vec4 x){return mod289(((x * 34.0) + 1.0) * x);}

float noise(vec3 p){
    vec3 a = floor(p);
    vec3 d = p - a;
    d = d * d * (3.0 - 2.0 * d);

    vec4 b = a.xxyy + vec4(0.0, 1.0, 0.0, 1.0);
    vec4 k1 = perm(b.xyxy);
    vec4 k2 = perm(k1.xyxy + b.zzww);

    vec4 c = k2 + a.zzzz;
    vec4 k3 = perm(c);
    vec4 k4 = perm(c + 1.0);

    vec4 o1 = fract(k3 * (1.0 / 41.0));
    vec4 o2 = fract(k4 * (1.0 / 41.0));

    vec4 o3 = o2 * d.z + o1 * (1.0 - d.z);
    vec2 o4 = o3.yw * d.x + o3.xz * (1.0 - d.x);

    return o4.y * d.y + o4.x * (1.0 - d.y);
}


vec3 phong(vec3 matAmbientColor, vec3 matDiffuseColor, vec3 matSpecularColor, float matShininess,vec4 lightPosition,vec3 lightAmbientColor, vec3 lightDiffuseColor, vec3 lightSpecularColor);

vec3 marble_color (float x)
{
  vec3 col;
  x = 0.5*(x+1.);          // transform -1<x<1 to 0<x<1
  x = sqrt(x);             // make x fall of rapidly...
  x = sqrt(x);
  x = sqrt(x);
  col = vec3(.2 + .75*x);  // scale x from 0<x<1 to 0.2<x<0.95
  col.b*=0.95;             // slightly reduce blue component (make color "warmer"):
  return col;
}



float turbulence (vec3 P, int numFreq)
{
   float val = 0.0;
   float freq = 1.0;
   for (int i=0; i < numFreq; i++) {
      val += abs (noise(P*freq) / freq);
      freq *= 2.07;
   }
   return val;
}

void main ()
{
    float amplitude = 8.0;
    const int roughness = 4;     // noisiness of veins (#octaves in turbulence)

    float t = 6.28 * vPosition.x /1.1 ;
    vec3 color1;
    vec3 color2;
    if(flag == 1){
        t += amplitude * turbulence(vPosition.xyz, roughness);
        // replicate over rows of tiles (wont be identical, because noise is depending on all coordinates of the input vector):
        t = sin(t);
        vec3 marbleColor = marble_color(t) *0.4;
        color1 = phong(marbleColor, marbleColor, materialSpecularColor, materialShininess, 
        lightPosition1,lightAmbientColor1, lightDiffuseColor1,  lightSpecularColor1);

        color2 = phong(marbleColor, marbleColor, materialSpecularColor, materialShininess, 
        lightPosition2,lightAmbientColor2, lightDiffuseColor2,  lightSpecularColor2);
    }else{
        color1 = phong(materialAmbientColor, materialDiffuseColor, materialSpecularColor, materialShininess, 
        lightPosition1,lightAmbientColor1, lightDiffuseColor1,  lightSpecularColor1);

        color2 = phong(materialAmbientColor, materialDiffuseColor, materialSpecularColor, materialShininess, 
        lightPosition2,lightAmbientColor2, lightDiffuseColor2,  lightSpecularColor2);
    }
    



    fragColor = vec4(color1 + color2, 1.0);
}

/*
 * Computes lighting using Blinn-Phong model.
 */
vec3 phong(vec3 matAmbientColor, vec3 matDiffuseColor, vec3 matSpecularColor, float matShininess,
           vec4 lightPosition,vec3 lightAmbientColor, vec3 lightDiffuseColor, vec3 lightSpecularColor) {
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
