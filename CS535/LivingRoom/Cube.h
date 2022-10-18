#pragma once
#include "shape.h"

/**
* This class defines a cube primitive and handles the generation and rendering of it.
* Megan Worley
*/
class Cube : public Shape {
	public:
		Cube(void);

		// Creates a unit cube, where the size will be used as the scale factor during rendering.
		// Params:
		//		size - The cube's scale factor.
		Cube(GLdouble size);

	private:
		static const int NUM_VERTICES = 24;
		GLdouble mSize;
};
