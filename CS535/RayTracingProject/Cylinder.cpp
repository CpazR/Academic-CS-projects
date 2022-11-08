#include "Cylinder.h"


Cylinder::Cylinder(void)
{
}

Cylinder::Cylinder(GLdouble base, GLdouble height, GLint slices, GLint stacks) {
	float PI = 3.14159;
	float angleStep = 2.0 * PI / (float)slices;
	int iPivot = 0;
	float slopeStep = height / (float)stacks;
	mNumIndices = (slices * stacks) * 6 + slices * 6;
	int numVertices = slices * (stacks + 1) + slices * 2 + 2; // Center section + 2 bases + 2 centers of each base
	// Calculate the vertices of the sides of the cylinder.
	std::vector<glm::vec3> mTempVertices = std::vector<glm::vec3>(numVertices);
	std::vector<glm::vec3> mTempNormals = std::vector<glm::vec3>(numVertices);
	mIndices = std::vector <GLushort>(mNumIndices);
	int index = 0;
	float epsilon = 0.2f;
	GLfloat z = 0;
	for (int rows = 0; rows < stacks + 1; rows++) {
		for (float angle = 0.0; angle < (2.0 * PI - epsilon); angle += angleStep) {
			// Calculate x and y position of the next vertex
			GLfloat x = cos(angle);
			GLfloat y = sin(angle);
			Vector3f n(x, y, 0);
			n.normalize();
			mTempVertices[index] = glm::vec3(x, y, z);
			mTempNormals[index++] = glm::vec3(n.x, n.y, n.z);
		}
		z += slopeStep;
	}

	// Add the vertices and normals for the caps.
	for (int cap = 0; cap < 2; cap++) {
		int capIndex = 0;
		if (cap == 1) {
			capIndex = slices * stacks;
		}
		for (int i = 0; i < slices; i++) {
			glm::vec3 vertex = mTempVertices[capIndex + i];
			GLfloat x = vertex.x;
			GLfloat y = vertex.y;
			z = vertex.z;
			mTempVertices[index] = glm::vec3(x, y, z);
			if (cap == 0) {
				mTempNormals[index++] = glm::vec3(0.0, 0.0, -1.0f);
			} else {
				mTempNormals[index++] = glm::vec3(0.0, 0.0, 1.0f);
			}
		}
		// Center of cap
		mTempVertices[index] = glm::vec3(0, 0, z);
		if (cap == 0) {
			mTempNormals[index++] = glm::vec3(0.0, 0.0, -1.0f);
		} else {
			mTempNormals[index++] = glm::vec3(0.0, 0.0, 1.0f);
		}
	}

	// Calculate the indices for the triangles that make up the sides.
	index = 0;
	for (int latitude = 0; latitude < stacks; latitude++) {
		for (int longitude = 0; longitude < slices; longitude++) {

			int botLeft = (latitude)     * slices + longitude;
			int topLeft = (latitude + 1) * slices + longitude;
			int botRight = botLeft + 1;
			int topRight = topLeft + 1;
			if (longitude == slices - 1) {
				botRight = latitude * slices;
				topRight = (latitude + 1) * slices;
			}
			// Triangle 1
			mIndices[index++] = botRight;
			mIndices[index++] = botLeft;
			mIndices[index++] = topLeft;
			// Triangle 2
			mIndices[index++] = botRight;
			mIndices[index++] = topRight;
			mIndices[index++] = topLeft;
		}
	}

	// Add the face indices for the caps.
	for (int cap = 0; cap < 2; cap++) {
		int capIndex = slices * (stacks + 1);
		int centerIndex = slices * (stacks + 2);
		if (cap == 1) {
			capIndex = slices * (stacks + 2) + 1;
			centerIndex = slices * (stacks + 3) + 1;
		}
		for (int i = 0; i < slices; i++) {
			int botLeft = capIndex + i;
			int botRight = botLeft + 1;
			if (i == slices - 1) {
				botRight = capIndex;
			}
			mIndices[index++] = centerIndex;
			mIndices[index++] = botLeft;
			mIndices[index++] = botRight;
		}
	}

	for (int i = 0; i < mNumIndices; i++) {
		mVertices.push_back(mTempVertices[mIndices[i]]);
		mNormals.push_back(mTempVertices[mIndices[i]]);
	}

	polygonInit();
}

void Cylinder::render() {
	Shape::render();
}