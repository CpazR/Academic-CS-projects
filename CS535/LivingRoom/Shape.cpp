#include <GL/glew.h>
#include <vector>
#include "Shape.h"

#include "Cube.h"

Shape::Shape(void) {
}

void Shape::polygonInit() {
	// Setup selected VBO
	glGenBuffers(1, &vbo);

	// Bind polygon vector to buffer
	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glBufferData(GL_ARRAY_BUFFER, mVertices.size() * sizeof(GLfloat), mVertices.data(), GL_STATIC_DRAW);
}

void Shape::render() {
	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glVertexAttribPointer(0, NUM_COORDS, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(0);
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);
	//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
	glDrawArrays(GL_TRIANGLES, 0, 36);
}

std::vector <GLfloat> Shape::getMVertices() {
	return mVertices;
}

GLuint Shape::getVbo() {
	return vbo;
}