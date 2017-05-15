#version 330

in vec3 position;
in vec3 normal;

out vec3 vNormal;
out vec3 vPosition;

uniform mat4 MVP; // model-view-projection matrix
uniform mat3 N; // transpose of inversed model matrix
uniform mat4 model; // model matrix

void main() {
    //  transpose of inversed model matrix * vertex normals
    vNormal = normalize(N * normal);

    //vertex world position = *model* matrix    *   vertex position
    vPosition = vec3(model * vec4(position, 1.0));

    gl_Position = MVP * vec4(position, 1.0);
}