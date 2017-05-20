#version 330

in vec3 position;
in vec3 normal;
//in vec2 texcoord;

out vec3 vNormal;
out vec3 vPosition;
//out vec2 vTexCoord;
out vec4 vColor;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model[100];
uniform vec4 color[100];

void main() {
    mat3 N =  transpose (inverse (mat3( model[gl_InstanceID] ) ));
    vNormal = normalize(N * normal);
    vPosition = vec3(model[gl_InstanceID] * vec4(position, 1.0));
    //vTexCoord = texcoord;

    vColor = color[gl_InstanceID];


    gl_Position = projection * view * model[gl_InstanceID] * vec4(position, 1.0);
}
