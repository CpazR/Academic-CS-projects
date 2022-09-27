#version 430
uniform mat4 u_modelViewProjection;

layout (location=0) in vec2 pos;

void main(void) {
	vec4 position = vec4(pos.x, pos.y, 0.0, 1.0);
	vec4 worldPosition = u_modelViewProjection * position;
	gl_Position = worldPosition;
}
