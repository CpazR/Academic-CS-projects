#include "Sphere.h"


Sphere::Sphere(void)
{
}

Sphere::Sphere(GLdouble radius, GLint slices, GLint stacks) {
	float PI = 3.14;
	GLfloat theta = 0;
	GLfloat phi = 0;
	GLfloat dTheta = PI / (float)stacks;
	GLfloat dPhi = 2 * PI / (float)slices;
	mNumIndices = slices * stacks * 6;
	std::vector<glm::vec3> mTempVertices = std::vector<glm::vec3>(slices * (stacks + 1) * NUM_COORDS);
	std::vector<glm::vec3> mTempNormals= std::vector<glm::vec3>(slices * (stacks + 1) * NUM_COORDS);
	mIndices = std::vector <GLushort>(mNumIndices);
	mSize = radius;

	// Generate the vertices and normals for the sphere.
	int index = 0;
	for (int latitude = 0; latitude < stacks; latitude++) {
		phi = 0;
		for (int longitude = 0; longitude < slices; longitude++) {
			GLfloat x = sin(theta) * sin(phi);
			GLfloat y = cos(theta);
			GLfloat z = sin(theta) * cos(phi);
			Vector3f n(x, y, z);
			n.normalize();
			mTempVertices[index] = glm::vec3(x, y, z);
			mTempNormals[index++] = glm::vec3(n.x, n.y, n.z);
			phi += dPhi;
		}
		theta += dTheta;
	}

	// Generate the face indices for the sphere.
	index = 0;
	for (int latitude = 0; latitude < stacks; latitude++) {
		for (int longitude = 0; longitude < slices; longitude++) {

			int lastIndex =    (latitude)     * slices + longitude;
			int currentIndex = (latitude + 1) * slices + longitude;

			// First triangle
			mIndices[index++] = lastIndex;
			mIndices[index++] = lastIndex + 1;
			mIndices[index++] = currentIndex;
			// Second triangle
			mIndices[index++] = lastIndex + 1;
			mIndices[index++] = currentIndex + 1;
			mIndices[index++] = currentIndex;
		}
	}

	for (int i = 0; i < mNumIndices; i++) {
		mVertices.push_back(mTempVertices[mIndices[i]]);
		mNormals.push_back(mTempVertices[mIndices[i]]);
	}

	polygonInit();
}

void Sphere::render(void) {
	Shape::render();
}