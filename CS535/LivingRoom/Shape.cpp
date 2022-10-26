#include <GL/glew.h>
#include <vector>
#include "Shape.h"

Shape::Shape(void) {
}

void Shape::polygonInit() {
	// Setup selected VBO
	glGenBuffers(1, &vbo);

	// Bind polygon vector to buffer
	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glBufferData(GL_ARRAY_BUFFER, NUM_COORDS * mVertices.size() * sizeof(float), mVertices.data(), GL_STATIC_DRAW);
}

void Shape::render() {
	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glVertexAttribPointer(0, NUM_COORDS, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(0);
	glFrontFace(GL_CCW);
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);
	glCullFace(GL_CULL_FACE);
	int vertNum = mVertices.size();
	glDrawArrays(GL_TRIANGLES, 0, vertNum);
}

std::vector <glm::vec3> Shape::getMVertices() {
	return mVertices;
}

GLuint Shape::getVbo() {
	return vbo;
}
