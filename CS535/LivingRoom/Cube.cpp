#include <cmath>
#include <vector>
#include <iostream>

#include <GL/glew.h>
#include <glm/glm.hpp>

#include "Cube.h"
using namespace std;

Cube::Cube(float verticesSize) {
	init();
}

void Cube::init() {
	// Setup selected VAO and VBO
	glGenVertexArrays(1, &VAO);
	glBindVertexArray(VAO);
	glGenBuffers(1, &VBO);

	// Bind polygon vector to buffer
	glBindBuffer(GL_ARRAY_BUFFER, VBO);
	glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), vertices.data(), GL_DYNAMIC_DRAW);

	// Bind position vector in shader
	glEnableVertexAttribArray(0);
	glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), NULL);
}

void Cube::render() {
	glBindVertexArray(VAO);
	glDrawArrays(GL_LINE_LOOP, 0, vertices.size() / 3);
}