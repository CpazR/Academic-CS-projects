#pragma once
#include "shape.h"

/**
* This class defines a cylinder primitive and handles the generation and rendering of it.
* Megan Worley
*/
class Cylinder : public Shape {
	public:
		Cylinder();

		// Creates a cylinder along the z-axis.
		// Params:
		//		base - The radius of the base.
		//		height - The height of the cylinder.
		//		slices - The number of subdivisions around the z-axis.
		//		stacks - The number of subdivions along the z-axis.
		Cylinder(GLdouble base, GLdouble height, GLint slices, GLint stacks);
		void render(void);

	private:
		int mNumIndices;
};
