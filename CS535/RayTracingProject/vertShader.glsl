#version 430

layout (location=0) in vec3 vertPosition;
layout (location=1) in vec2 vertUV;

out vec2 uv;

uniform mat4 mv_matrix;	 
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;

void main(void)
{
	gl_Position = proj_matrix * mv_matrix * vec4(vertPosition, 1.0);
	uv = vertUV;
}
