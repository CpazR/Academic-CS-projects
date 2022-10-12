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
	mVertices = new GLfloat[slices * (stacks + 1) * NUM_COORDS];
	mNormals = new GLfloat[slices * (stacks + 1) * NUM_COORDS];
	mNumIndices = slices * stacks * 4;
	mIndices = new GLushort[mNumIndices];
	mSize = radius;

	// Generate the vertices and normals for the sphere.
	int index = 0;
	for (int latitude = 0; latitude <= stacks; latitude++) {
		phi = 0;
		for (int longitude = 0; longitude < slices; longitude++) {
			GLfloat x = sin(theta) * sin(phi);
			GLfloat y = cos(theta);
			GLfloat z = sin(theta) * cos(phi);
			Vector3f n(x, y, z);
			n.normalize();
			mVertices[index] = x;
			mNormals[index++] = n.x;
			mVertices[index] = y;
			mNormals[index++] = n.y;
			mVertices[index] = z;
			mNormals[index++] = n.z;
			phi += dPhi;
		}
		theta += dTheta;
	}

	// Generate the face indices for the sphere.
	index = 0;
	for (int latitude = 0; latitude < stacks; latitude++) {
		for (int longitude = 0; longitude < slices; longitude++) {
			int lastIndex = slices * latitude + longitude;
			int currentIndex = slices * (latitude + 1) + longitude;
			mIndices[index++] = lastIndex;
			mIndices[index++] = currentIndex;
			mIndices[index++] = currentIndex + 1;
			mIndices[index++] = lastIndex + 1;
		}
	}
}

void Sphere::render(void) {
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	// Set array pointers
	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_NORMAL_ARRAY);
	glVertexPointer(NUM_COORDS, GL_FLOAT, 0, mVertices);
	glNormalPointer(GL_FLOAT, 0, mNormals);

	glScalef(mSize, mSize, mSize);
	glDrawElements(GL_QUADS, mNumIndices, GL_UNSIGNED_SHORT, mIndices);

	glDisableClientState(GL_VERTEX_ARRAY);
	glDisableClientState(GL_NORMAL_ARRAY);

	glPopMatrix();
}