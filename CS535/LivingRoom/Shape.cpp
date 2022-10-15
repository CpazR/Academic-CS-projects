#include <GL/glew.h>
#include <vector>
#include "Shape.h"

Shape::Shape(void) {
}

void Shape::polygonInit() {
	// Setup selected VAO and VBO
	glGenVertexArrays(1, &vao);
	glBindVertexArray(vao);
	glGenBuffers(1, &vbo);

	// Bind polygon vector to buffer
	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glBufferData(GL_ARRAY_BUFFER, sizeof(mVertices), &mVertices, GL_STATIC_DRAW);
}

void Shape::render(void) {
	const int triangleCount = (sizeof(mVertices) / sizeof(mVertices[0])) / NUM_COORDS;
	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glVertexAttribPointer(0, NUM_COORDS, GL_FLOAT, GL_FALSE, NUM_COORDS * sizeof(GLfloat), NULL);
	//glVertexAttribPointer(0, NUM_COORDS, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(0);
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);
	glDrawArrays(GL_TRIANGLES, 0, triangleCount);
}