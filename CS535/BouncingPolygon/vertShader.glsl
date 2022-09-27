#version 430
uniform mat4 u_modelViewProjection;

layout (location=0) in vec2 pos;

void main(void) {
	vec4 position = vec4(pos.x, pos.y, 0.0, 1.0);
	vec4 worldPosition = u_modelViewProjection * position;

	float top = -(480.0)/4;
	float bottom = (480.0)/4;
	float left = -(640.0)/4;
	float right = (640.0)/4;

	// Clipping
	if (worldPosition.x < left) {
		worldPosition = vec4(left, worldPosition.y, 0.0, worldPosition.w);
	}

	if (worldPosition.y < top) {
		worldPosition = vec4(worldPosition.x, top, 0.0, worldPosition.w);
	}

	if (worldPosition.x > right) {
		worldPosition = vec4(right, worldPosition.y, 0.0, worldPosition.w);
	}

	if (worldPosition.y > bottom) {
		worldPosition = vec4(worldPosition.x, bottom, 0.0, worldPosition.w);
	}

	gl_Position = worldPosition;
}
