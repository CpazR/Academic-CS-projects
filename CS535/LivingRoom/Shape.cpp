#include <GL/glew.h>
#include <vector>
#include "Shape.h"

Shape::Shape(void) {
}

void Shape::polygonInit() {
	// Setup selected VBO
	glGenBuffers(2, vbo);

	// Bind polygon vector to array buffer
	glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
	glBufferData(GL_ARRAY_BUFFER, NUM_COORDS * mVertices.size() * sizeof(float), mVertices.data(), GL_STATIC_DRAW);

	// Bind polygon normals to array buffer
	glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
	glBufferData(GL_ARRAY_BUFFER, NUM_COORDS * mNormals.size() * sizeof(float), mNormals.data(), GL_STATIC_DRAW);
}

void Shape::render() {
	glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
	glVertexAttribPointer(0, NUM_COORDS, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(0);

	glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
	glVertexAttribPointer(1, NUM_COORDS, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(1);

	glFrontFace(GL_CCW);
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);
	glCullFace(GL_CULL_FACE);
	//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

	glDrawArrays(GL_TRIANGLES, 0, mVertices.size());

	//glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[2]);
	//glDrawElements(GL_TRIANGLES, mIndices.size(), GL_UNSIGNED_SHORT, 0);
}

std::vector <glm::vec3> Shape::getMVertices() {
	return mVertices;
}

GLuint* Shape::getVbo() {
	return vbo;
}
