#version 330

out vec4 fragColor;

in vec3 vertColor;
in vec3 vertNormal;

void main() {
    vec3 n = normalize(vertNormal);
    vec3 light = vec3(0.0, 0.0, 1.0);

    // diffuse term of directional Phong lighting
    float d = 0.8 * dot(n, light);

    // multiply vertColor by d to obtain very basic direct lighting
    fragColor = vec4(vertColor, 1.0) * d;
}