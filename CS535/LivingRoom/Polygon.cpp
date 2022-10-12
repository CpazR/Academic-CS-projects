#include <cmath>
#include <vector>
#include <iostream>

#include <GL/glew.h>
#include <glm/glm.hpp>

#include "Polygon.h"
using namespace std;

void Polygon::polygonInit() {
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

void Polygon::render() {
	glBindVertexArray(VAO);
	glDrawArrays(GL_LINE_LOOP, 0, vertices.size() / DIMENSIONS);
}

float Polygon::toRadians(float degrees) {
	return (degrees * 2.0f * 3.14159f) / 360.0f;
}

int Polygon::getNumVertices() {
	return numVertices;
}

int Polygon::getNumIndices() {
	return numIndices;
}

std::vector<int> Polygon::getIndices() {
	return indices;
}

std::vector<glm::vec3> Polygon::getVertices() {
	return vertices;
}

std::vector<glm::vec2> Polygon::getTexCoords() {
	return texCoords;
}

std::vector<glm::vec3> Polygon::getNormals() {
	return normals;
}

std::vector<glm::vec3> Polygon::getTangents() {
	return tangents;
}