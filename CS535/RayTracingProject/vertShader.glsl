#version 430

layout (location=0) in vec3 vertPosition;
layout (location=1) in vec2 vertUV;

out vec2 uv;

void main(void)
{
	gl_Position = vec4(vertPosition, 1.0);
	uv = vertUV;
}
