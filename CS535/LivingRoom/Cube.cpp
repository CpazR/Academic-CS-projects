#include "Cube.h"
#include <glm/gtc/matrix_transform.hpp>

Cube::Cube(void) {
}

Cube::Cube(GLdouble size) {
	GLfloat vertices[] = {
		// Back face
		-0.5f,  0.5f, -0.5f,
		-0.5f, -0.5f, -0.5f,
		0.5f, -0.5f, -0.5f,
		0.5f, -0.5f, -0.5f,
		0.5f,  0.5f, -0.5f,
		-0.5f,  0.5f, -0.5f,

		// Right face
		0.5f, -0.5f, -0.5f,
		0.5f, -0.5f,  0.5f,
		0.5f,  0.5f, -0.5f,
		0.5f, -0.5f,  0.5f,
		0.5f,  0.5f,  0.5f,
		0.5f,  0.5f, -0.5f,

		// Front face
		0.5f, -0.5f,  0.5f,
		-0.5f, -0.5f,  0.5f,
		0.5f,  0.5f,  0.5f,
		-0.5f, -0.5f,  0.5f,
		-0.5f,  0.5f,  0.5f,
		0.5f,  0.5f,  0.5f,

		// Left face
		-0.5f, -0.5f,  0.5f,
		-0.5f, -0.5f, -0.5f,
		-0.5f,  0.5f,  0.5f,
		-0.5f, -0.5f, -0.5f,
		-0.5f,  0.5f, -0.5f,
		-0.5f,  0.5f,  0.5f,

		// Bottom face
		-0.5f, -0.5f,  0.5f,
		0.5f, -0.5f,  0.5f,
		0.5f, -0.5f, -0.5f,
		0.5f, -0.5f, -0.5f,
		-0.5f, -0.5f, -0.5f,
		-0.5f, -0.5f,  0.5f,

		// Top face
		-0.5f,  0.5f, -0.5f,
		0.5f,  0.5f, -0.5f,
		0.5f,  0.5f,  0.5f,
		0.5f,  0.5f,  0.5f,
		-0.5f,  0.5f,  0.5f,
		-0.5f,  0.5f, -0.5f
	};

	GLfloat normals[] = {
		// Back tris
		0, 0, -1,
		0, 0, -1,
		0, 0, -1,
		0, 0, -1,
		0, 0, -1,
		0, 0, -1,

		// Right tris
		1, 0, 0,
		1, 0, 0,
		1, 0, 0,
		1, 0, 0,
		1, 0, 0,
		1, 0, 0,

		// Front tris
		0, 0, 1,
		0, 0, 1,
		0, 0, 1,
		0, 0, 1,
		0, 0, 1,
		0, 0, 1,

		// Left tris
		-1, 0, 0,
		-1, 0, 0,
		-1, 0, 0,
		-1, 0, 0,
		-1, 0, 0,
		-1, 0, 0,

		// Bottom tris
		0, -1, 0,
		0, -1, 0,
		0, -1, 0,
		0, -1, 0,
		0, -1, 0,
		0, -1, 0,

		// Top tris
		0, 1, 0,
		0, 1, 0,
		0, 1, 0,
		0, 1, 0,
		0, 1, 0,
		0, 1, 0,
	};

	GLushort indices[] = {
		0, 1, 2, 3, // Front
		4, 5, 6, 7, // Right
		8, 9, 10, 11, // Back
		12, 13, 14, 15, // Left
		16, 17, 18, 19, // Top
		20, 21, 22, 23, // Bottom
	};

	// Copy the vertices, normals, and indices to the tables.
	mIndices = std::vector<GLushort>(NUM_VERTICES);
	for (int i = 0; i < NUM_VERTICES * NUM_COORDS; i += 3) {
		mVertices.push_back(glm::vec3(vertices[i], vertices[i + 1], vertices[i + 2]));
		mNormals.push_back(glm::vec3(normals[i], normals[i + 1], normals[i + 2]));
		mIndices[i / 3] = indices[i / 3];
	}

	mSize = size;

	polygonInit();
}

void Cube::render() {
	Shape::render();
}
