#version 430

#define FLT_MAX 3.402823466e+38
#define FLT_MIN 1.175494351e-38

// Shape constants. Since GLSL does not have enums.
#define SHAPE_SPHERE 0
#define SHAPE_BOX 1
#define SHAPE_PLANE 2

#define j -1

layout (local_size_x = 1) in;
layout (binding = 0, rgba8) uniform image2D renderTextureOutput;


struct Ray {
	vec3 start;
	vec3 dir;
};

struct Collision {
	float rayIntersectionPoint;
	vec3 position;
	vec3 normal;
	bool inside;
	int objectIndex;
	int face_index;
};

struct StackElement {
	int type;
	int depth;
	int phase;
	vec3 colorPhong;
	vec3 colorReflect;
	vec3 colorRefract;
	vec3 colorCalcualted;
	Ray ray;
	Collision collision;
};

struct PointLight {
	vec3 position;
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
};

// Based on compute shader object struct from example programs
struct Object {
	int type;
	float radius;
	vec3 mins; // if object is a box, inner bounds of volume
	vec3 maxs; // if object is a box, outer bounds of volume
	vec3 rotation;
	vec3 position;
	bool isReflective;
	bool isTransparent;
	bool useColor;
	vec3 color;
	float reflectionIntensity;
	float refractionIntensity;
	float indexOfRefraction; // index of refraction (transparent material representing)
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

// ---------------------------------------------------------------------------------------
// World objects in scene


vec3 worldOrigin = vec3(0, -3, -1);

const float mirrorFrameSize = 5.0;
const float tableWindowSize = 2.5;
const float glassTableSize = 2.0;
const float glassTableLegSize = .2;
const float fanFinSize = .1;
const float fanFinLength = 2.33;

// Rough recreation of room from project 2 with new furnature.
Object[] objects = {
	/// ---
	/// Main room
	/// ---
	// Room walls
	{
		SHAPE_BOX, 0, vec3(-10, -10, -10), vec3( 10, 10, 10), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(0),
		true, false, true, vec3(0.25, 0.25, 0.25), 0, 0, 0,
		vec4(0.2, 0.2, 0.2, 1.0), vec4(0.9, 0.9, 0.9, 1.0), vec4(1,1,1,1), 20.0
	},
	// floor
	{
		SHAPE_PLANE, 0, vec3(20, 0, 20), vec3(0), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(0.0, 0.0, 0.0),
		true, false, true, vec3(.50, .25, .0), 0.0, 0.0, 0.0,
		vec4(0.3, 0.3, 0.2, 1.0), vec4(0.3, 0.3, 0.4, 1.0), vec4(0.0, 0.0, 0.0, 1.0), 50.0
	},
	// Rug under table
	{
		SHAPE_PLANE, 0, vec3(5.5, 0, 5.5), vec3(0), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(0.0, 0.01, 0.0),
		true, false, true, vec3(.25, .75, .50), 0.0, 0.0, 0.0,
		vec4(0.3, 0.3, 0.2, 1.0), vec4(0.3, 0.3, 0.4, 1.0), vec4(0.0, 0.0, 0.0, 1.0), 50.0
	},

	/// ---
	/// Table
	/// ---
	// Glass body
	{
		SHAPE_BOX, 0, vec3(-glassTableSize, 0.0, -glassTableSize), vec3(glassTableSize, 0.5, glassTableSize), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(0.0, 1.0, 0.0),
		true, true, true, vec3(.75, .25, .0), 0.8, 0.8, 1.5,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	// Leg 1/4
	{
		SHAPE_BOX, 0, vec3(-glassTableLegSize, 0.0, -glassTableLegSize), vec3(glassTableLegSize, 1.0, glassTableLegSize), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(0.0, 0.0, -2.0),
		true, false, true, vec3(.75, .25, .0), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},
	// Leg 2/4
	{
		SHAPE_BOX, 0, vec3(-glassTableLegSize, 0.0, -glassTableLegSize), vec3(glassTableLegSize, 1.0, glassTableLegSize), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(0.0, 0.0, 2.0),
		true, false, true, vec3(.75, .25, .0), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},
	// Leg 3/4
	{
		SHAPE_BOX, 0, vec3(-glassTableLegSize, 0.0, -glassTableLegSize), vec3(glassTableLegSize, 1.0, glassTableLegSize), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(-2.0, 0.0, 0.0),
		true, false, true, vec3(.75, .25, .0), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},
	// Leg 4/4
	{
		SHAPE_BOX, 0, vec3(-glassTableLegSize, 0.0, -glassTableLegSize), vec3(glassTableLegSize, 1.0, glassTableLegSize), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(2.0, 0.0, 0.0),
		true, false, true, vec3(.75, .25, .0), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},

	/// ---
	/// Ceiling lights
	/// ---
	// Fixture #1
	{
		SHAPE_BOX, 0, vec3(-.7, 0.0, -.7), vec3(.7, 0.4, .7), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(-1, 7.0, 0.0),
		true, false, true, vec3(0.4, 0.4, 0.4), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},
	// Fixture pole #1
	{
		SHAPE_BOX, 0, vec3(-.1, 0.0, -.1), vec3(.1, 5.0, .1), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(-1, 7.0, 0.0),
		true, false, true, vec3(0.4, 0.4, 0.4), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},
	// Builb #1
	{
		SHAPE_SPHERE, 0.3, vec3(0), vec3(0), vec3(0.0, 0.0, 0.0), worldOrigin + vec3(-1, 7.0, 0.0),
		true, true, false, vec3(0.0), 0.05, 10, 1.5,
		vec4(1, 1, 1, 1), vec4(1,1,1,1), vec4(1,1,1,1), 100.0
	},
	// Fixture #2
	{
		SHAPE_BOX, 0, vec3(-.7, 0.0, -.7), vec3(.7, 0.4, .7), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(1, 7.0, 0.0),
		true, false, true, vec3(0.4, 0.4, 0.4), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},
	// Fixture pole #2
	{
		SHAPE_BOX, 0, vec3(-.1, 0.0, -.1), vec3(.1, 5.0, .1), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(1, 7.0, 0.0),
		true, false, true, vec3(0.4, 0.4, 0.4), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 50.0
	},
	// Builb #2
	{
		SHAPE_SPHERE, 0.3, vec3(0), vec3(0), vec3(0.0, 0.0, 0.0), worldOrigin + vec3(1, 7.0, 0.0),
		true, true, false, vec3(0.0), 0.05, 10, 1.5,
		vec4(1, 1, 1, 1), vec4(1,1,1,1), vec4(1,1,1,1), 100.0
	},

	/// ---
	/// Corner table
	/// ---
	{
		SHAPE_BOX, 0, vec3(-1.0, -0.2, -1.0), vec3(1.0, 0, 1.0), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(2.0, 2.0, -8.0),
		true, false, true, vec3(.75, .40, .0), 0.0, 0.0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	{
		SHAPE_BOX, 0, vec3(-0.2, 0.0, -0.2), vec3(0.2, 2, 0.2), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(2.0, 0.0, -8.0),
		true, false, true, vec3(.60, .30, .0), 0.0, 0.0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},

	/// ---
	/// Table fan
	/// ---
	{
		SHAPE_BOX, 0, vec3(-0.15, 0.0, -0.15), vec3(0.15, 3.3, 0.15), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(5.3, 0, -5.2),
		true, false, true, vec3(0.7, 0.5, 0), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	{
		SHAPE_SPHERE, 0.4, vec3(0), vec3(0), vec3(0.0, 0.0, 0.0), worldOrigin + vec3(5.3, 3.5, -5.2),
		true, false, true, vec3(0.65, 0.5, 0), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	// Base
	{
		SHAPE_BOX, 0, vec3(-0.3, 0.0, -0.3), vec3(0.3, .3, 0.3), vec3(0.0, 45.0, 0.0), worldOrigin + vec3(5.3, 0, -5.2),
		true, false, true, vec3(0.7, 0.7, 0.7), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	// Fin hinge
	{
		SHAPE_BOX, 0, vec3(-fanFinSize, 0, -fanFinSize), vec3(fanFinSize, .5, fanFinSize), vec3(0.0, 45.0, 90.0), worldOrigin + vec3(5.3, 3.5, -5.2),
		true, false, true, vec3(0.6, 0.5, 0), 0, 0, 0,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	// Fan fin 1/4
	{
		SHAPE_BOX, 0.0, vec3(-fanFinSize, 0.0, -fanFinSize), vec3(fanFinSize, fanFinLength, fanFinSize), vec3(45.0, 40.0, 2.0), worldOrigin + vec3(5.1, 3.5, -5),
		true, false, true, vec3(0.5, 0.5, 0.5), 1, 0, 1.440,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	// Fan fin 2/4
	{
		SHAPE_BOX, 0.0, vec3(-fanFinSize, 0.0, -fanFinSize), vec3(fanFinSize, fanFinLength, fanFinSize), vec3(90 + 45.0, 40.0, 2.0), worldOrigin + vec3(5.1, 3.5, -5),
		true, false, true, vec3(0.5, 0.5, 0.5), 1, 0, 1.440,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	// Fan fin 3/4
	{
		SHAPE_BOX, 0.0, vec3(-fanFinSize, 0.0, -fanFinSize), vec3(fanFinSize, fanFinLength, fanFinSize), vec3(180 + 45.0, 40.0, 2.0), worldOrigin + vec3(5.1, 3.5, -5),
		true, false, true, vec3(0.5, 0.5, 0.5), 1, 0, 1.440,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
	// Fan fin 3/4
	{
		SHAPE_BOX, 0.0, vec3(-fanFinSize, 0.0, -fanFinSize), vec3(fanFinSize, fanFinLength, fanFinSize), vec3(270 + 45.0, 40.0, 2.0), worldOrigin + vec3(5.1, 3.5, -5),
		true, false, true, vec3(0.5, 0.5, 0.5), 1, 0, 1.440,
		vec4(0.5, 0.5, 0.5, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},

	/// ---
	/// Mirror
	/// ---
	{
		SHAPE_BOX, 0.0,  vec3(-mirrorFrameSize, 0.0, -mirrorFrameSize), vec3(mirrorFrameSize, 0.25, mirrorFrameSize), vec3(90.0, 45.0, .0), worldOrigin + vec3(-5.0, 4.0, -6.0),
		true, false, true, vec3(.5, .5, .5), 1, 0.0, 0,
		vec4(1, 1, 1, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},

	/// ---
	/// Window displaying table
	/// ---
	{
		SHAPE_BOX, 0.0,  vec3(-tableWindowSize, 0.0, -tableWindowSize), vec3(tableWindowSize, 0.25, tableWindowSize), vec3(90.0, -45.0, .0), worldOrigin + vec3(3.0, 3.0, -2.0),
		false, true, true, vec3(.5, .5, .5), 0, 1.0, 1.5,
		vec4(1, 1, 1, 1.0), vec4(1,1,1,1), vec4(1,1,1,1), 80.0
	},
};
int numObjects = 26;
float cameraDistance = 7.0;
const int maxDepth = 4;
const int stackSize = 100;

// ---------------------------------------------------------------------------------------

const float PI = 3.14159265358;
const float DEG_TO_RAD = PI / 180.0;

vec4 worldAmbientIntensity = vec4(0.25, 0.25, 0.25, 1.0);
// Point lights in scene
const int LIGHTS_COUNT = 2;

PointLight lights[LIGHTS_COUNT] = {
	{
		worldOrigin + vec3(-1, 6.4, 0.0),
		vec4(0.0, 0.0, 0.0, 1.0),
		vec4(.2, .2, .2, 1.0),
		vec4(.5, .5, .5, 1.0),
	},
	{
		worldOrigin + vec3(1, 6.4, 0.0),
		vec4(0.0, 0.0, 0.0, 1.0),
		vec4(.2, .2, .2, 1.0),
		vec4(.2, .2, .2, 1.0),
	}
};

const int RAY_TYPE_REFLECTION = 1;
const int RAY_TYPE_REFRACTION = 2;

Ray emptyRay = { vec3(0.0), vec3(0.0) };
Collision emptyCollision = { -1.0, vec3(0.0), vec3(0.0), false, -1, -1 };
StackElement emptyStackElement = { 0, -1, -1, vec3(0), vec3(0), vec3(0), vec3(0), emptyRay, emptyCollision };

StackElement stack[stackSize];

int stackPointer = -1;
StackElement poppedStackElement;

mat4 buildTranslate(float x, float y, float z) {
	return mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, x, y, z, 1.0);
}
mat4 buildRotateX(float rad) {
	return mat4(1.0,0.0,0.0,0.0,0.0,cos(rad),sin(rad),0.0,0.0,-sin(rad),cos(rad),0.0,0.0,0.0,0.0,1.0);
}
mat4 buildRotateY(float rad) {
	return mat4(cos(rad),0.0,-sin(rad),0.0,0.0,1.0,0.0,0.0,sin(rad),0.0,cos(rad),0.0,0.0,0.0,0.0,1.0);
}
mat4 buildRotateZ(float rad) {
	return mat4(cos(rad),sin(rad),0.0,0.0,-sin(rad),cos(rad),0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
}


// ---------------------------------------------------------------------------------------
// Object intersect calculation functions

Collision intersectPlaneObject(Ray intersectRay, Object intersectingObject) {
	// Calculate the planes's local-space to world-space transform matrices, and their inverse
	mat4 worldTransformation = buildTranslate((intersectingObject.position).x, (intersectingObject.position).y, (intersectingObject.position).z);
	mat4 worldRotation = buildRotateY(DEG_TO_RAD * intersectingObject.rotation.y)
		* buildRotateX(DEG_TO_RAD * intersectingObject.rotation.x)
		* buildRotateZ(DEG_TO_RAD * intersectingObject.rotation.z);

	mat4 worldRotatedTransformation = worldTransformation * worldRotation;
	mat4 worldRotatedTransformationInverse = inverse(worldRotatedTransformation);
	mat4 worldRotationInverse = inverse(worldRotation);

	// Convert the world-space ray to the planes's local space
	vec3 rayStartingPosition = (worldRotatedTransformationInverse * vec4(intersectRay.start, 1.0)).xyz;
	vec3 rayDirection = (worldRotationInverse * vec4(intersectRay.dir, 1.0)).xyz;
	
	Collision intersectionCollision;
	intersectionCollision.inside = false;
	
	// Calculate intersection point of ray with plane
	intersectionCollision.rayIntersectionPoint = dot((vec3(0, 0, 0) - rayStartingPosition), vec3(0, 1, 0)) / dot(rayDirection, vec3(0, 1, 0));
	
	// Calculate the world-position of the collision
	intersectionCollision.position = intersectRay.start + intersectionCollision.rayIntersectionPoint * intersectRay.dir;
	
	// Calculate the position of the intersection in plane space
	vec3 intersectPoint = rayStartingPosition + intersectionCollision.rayIntersectionPoint * rayDirection;
	
	// If the ray didn't intersect the plane object, return a negative rayIntersectionPoint value
	if ( (abs(intersectPoint.x) > ((intersectingObject.mins).x)/2.0)
	  || (abs(intersectPoint.z) > ((intersectingObject.mins).z)/2.0) ) {
		intersectionCollision.rayIntersectionPoint = -1.0;
		return intersectionCollision;
	}

	// Create the collision normal. Invert if ray direction is upward.
	intersectionCollision.normal = vec3(0.0, 1.0, 0.0);
	if (rayDirection.y > 0.0) {
		intersectionCollision.normal *= -1.0;
	}
	
	// Convert the normal back into world space
	intersectionCollision.normal = transpose(inverse(mat3(worldRotation))) * intersectionCollision.normal;
	return intersectionCollision;
}

Collision intersectBoxObject(Ray intersectRay, Object intersectingObject) {
	// Calculate the box's local-space to world-space transform matrices, and their inverse
	mat4 worldTransformation = buildTranslate((intersectingObject.position).x, (intersectingObject.position).y, (intersectingObject.position).z);
	mat4 worldRotation = buildRotateY(DEG_TO_RAD*intersectingObject.rotation.y)
		* buildRotateX(DEG_TO_RAD*intersectingObject.rotation.x)
		* buildRotateZ(DEG_TO_RAD*intersectingObject.rotation.z);

	mat4 worldRotatedTransformation = worldTransformation * worldRotation;
	mat4 worldRotatedTransformationInverse = inverse(worldRotatedTransformation);
	mat4 worldRotationInverse = inverse(worldRotation);

	// Convert the world-space ray to the box's local space:
	vec3 rayStartingPosition = (worldRotatedTransformationInverse * vec4(intersectRay.start,1.0)).xyz;
	vec3 rayDirection = (worldRotationInverse * vec4(intersectRay.dir,1.0)).xyz;
	
	// Calculate the box's world mins and maxs:
	vec3 intersectMin = (intersectingObject.mins - rayStartingPosition) / rayDirection;
	vec3 intersectMax = (intersectingObject.maxs - rayStartingPosition) / rayDirection;
	vec3 intersectMinDistance = min(intersectMin, intersectMax);
	vec3 intersectMaxDistance = max(intersectMin, intersectMax);
	float intersectNear = max(max(intersectMinDistance.x, intersectMinDistance.y), intersectMinDistance.z);
	float intersectFar = min(min(intersectMaxDistance.x, intersectMaxDistance.y), intersectMaxDistance.z);

	Collision intersectionCollision;
	intersectionCollision.rayIntersectionPoint = intersectNear;
	intersectionCollision.inside = false;

	float intersectDistance = intersectNear;
	vec3 planeIntersectDistances = intersectMinDistance;
	
	// If the ray didn'rayIntersectionPoint intersect the box, return a negative rayIntersectionPoint value
	if (intersectNear >= intersectFar || intersectFar <= 0.0)	{
		intersectionCollision.rayIntersectionPoint = -1.0;
		return intersectionCollision;
	}

	// if intersectNear < 0, then the ray started inside the box and left the box
	if ( intersectNear < 0.0) {
		intersectionCollision.rayIntersectionPoint = intersectFar;
		intersectDistance = intersectFar;
		planeIntersectDistances = intersectMaxDistance;
		intersectionCollision.inside = true;
	}

	// Checking which boundary the intersection lies on
	int face_index = 0;
	if (intersectDistance == planeIntersectDistances.y) face_index = 1;
	else if (intersectDistance == planeIntersectDistances.z) face_index = 2;
	
	// Create the collision normal. Invert if ray direction is upward.
	intersectionCollision.normal = vec3(0.0);
	intersectionCollision.normal[face_index] = 1.0;
	if (rayDirection[face_index] > 0.0) {
		intersectionCollision.normal *= -1.0;
	}
	
	// now convert the normal back into world space
	intersectionCollision.normal = transpose(inverse(mat3(worldRotation))) * intersectionCollision.normal;

	// Calculate the world-position of the intersection:
	intersectionCollision.position = intersectRay.start + intersectionCollision.rayIntersectionPoint * intersectRay.dir;
		
	return intersectionCollision;
}

Collision intersectSphereObject(Ray intersectRay, Object intersectingObject) {
	// Calculate quadratic coefficients
	float qa = dot(intersectRay.dir, intersectRay.dir);
	float qb = dot(2 * intersectRay.dir, intersectRay.start - intersectingObject.position);
	float qc = dot(intersectRay.start - intersectingObject.position, intersectRay.start - intersectingObject.position) - intersectingObject.radius * intersectingObject.radius;

	// Solving for qa * rayIntersectionPoint^2 + qb * rayIntersectionPoint + qc = 0
	float qd = qb * qb - 4 * qa * qc;

	Collision intersectionCollision;
	intersectionCollision.inside = false;

	// No collision occurred
	if (qd < 0.0) {
		intersectionCollision.rayIntersectionPoint = -1.0;
		return intersectionCollision;
	}

	float t1 = (-qb + sqrt(qd)) / (2.0 * qa);
	float t2 = (-qb - sqrt(qd)) / (2.0 * qa);

	float intersectNear = min(t1, t2);
	float intersectFar = max(t1, t2);

	intersectionCollision.rayIntersectionPoint = intersectNear;

	// Sphere is behind the ray, no intersection
	if (intersectFar < 0.0)	{
		intersectionCollision.rayIntersectionPoint = -1.0;
		return intersectionCollision;
	}

	// the ray started inside the sphere
	if (intersectNear < 0.0) {
		intersectionCollision.rayIntersectionPoint = intersectFar;
		intersectionCollision.inside = true;
	}

	intersectionCollision.position = intersectRay.start + intersectionCollision.rayIntersectionPoint * intersectRay.dir;
	intersectionCollision.normal = normalize(intersectionCollision.position - intersectingObject.position);

	// if collision is leaving the sphere, flip the normal
	if (intersectionCollision.inside) {
		intersectionCollision.normal *= -1.0;
	}
	
	return intersectionCollision;
}

// ---------------------------------------------------------------------------------------

// Returns the nearest collision found using a given ray. If rayIntersectionPoint is negative, than no collision occurred
Collision getNearestCollision(Ray intersectRay) {
	// Initialize to a very large number
	float closest = FLT_MAX;
	Collision nearestCollision;
	// Initialize to no collision
	nearestCollision.objectIndex = -1;
	
	for(int i=0; i<numObjects; i++) {
		Collision intersectionCollision;
		
		if (objects[i].type == SHAPE_SPHERE) {
			intersectionCollision = intersectSphereObject(intersectRay, objects[i]);
		} else if (objects[i].type == SHAPE_BOX) {
			intersectionCollision = intersectBoxObject(intersectRay, objects[i]);
		} else if (objects[i].type == SHAPE_PLANE) {
			intersectionCollision = intersectPlaneObject(intersectRay, objects[i]);
		} else continue;

		// No collision occurred, continue to next object
		if (intersectionCollision.rayIntersectionPoint <= 0) {
			continue;
		}

		// Collision occurred. Determine nearest.
		if (intersectionCollision.rayIntersectionPoint < closest) {
			closest = intersectionCollision.rayIntersectionPoint;
			nearestCollision = intersectionCollision;
			nearestCollision.objectIndex = i;
		}
	}
	return nearestCollision;	
}

// Computes the ADS lighting using the phone method for an intersectRay at the surface of the object by returning the color pixel of the intersection point
vec3 adsPhoneLighting(Ray intersectRay, Collision intersectionCollision, PointLight light) {
	// Add the contribution from the ambient and positional lights
	vec4 ambient = worldAmbientIntensity + light.ambient * objects[intersectionCollision.objectIndex].ambient;
	
	// Initialize diffuse and specular contributions
	vec4 diffuse = vec4(0.0);
	vec4 specular = vec4(0.0);
	
	// Check to see if any object is casting a shadow on this surface
	Ray phongLightRay;
	phongLightRay.start = intersectionCollision.position + intersectionCollision.normal * 0.01;
	phongLightRay.dir = normalize(light.position - intersectionCollision.position);
	bool in_shadow = false;

	// Cast the ray against the scene
	Collision shadowCollision = getNearestCollision(phongLightRay);

	// If the ray hit an object and if the hit occurred between the surface and the light
	if ((shadowCollision.objectIndex != -1)
		&& shadowCollision.rayIntersectionPoint < length(light.position  - intersectionCollision.position))	{
		in_shadow = true;
	}

	// If this surface is in shadow, don'rayIntersectionPoint add diffuse and specular components
	if (in_shadow == false)	{
		// Calculate the light's reflection on the surface
		vec3 light_dir = normalize(light.position  - intersectionCollision.position);
		vec3 light_ref = normalize(reflect(-light_dir, intersectionCollision.normal));
		float cos_theta = dot(light_dir, intersectionCollision.normal);
		float cos_phi = dot(normalize(-intersectRay.dir), light_ref);

		diffuse = light.diffuse * objects[intersectionCollision.objectIndex].diffuse * max(cos_theta, 0.0);
		specular = light.specular
			* objects[intersectionCollision.objectIndex].specular
			* pow(max(cos_phi, 0.0), objects[intersectionCollision.objectIndex].shininess);
	}
	vec4 colorPhong = ambient + diffuse + specular;
	return colorPhong.rgb;
}

// ---------------------------------------------------------------------------------------
// Recursive stack management functions

// Adds a new raytrace to the top of the stack
void push(Ray intersectRay, int depth, int type) {
	if (stackPointer >= stackSize - 1) {
		return;
	}

	StackElement element;
	element = emptyStackElement;
	element.type = type;
	element.depth = depth;
	element.phase = 1;
	element.ray = intersectRay;

	stackPointer++;
	stack[stackPointer] = element;
}

// Removes and returns the topmost stack element
StackElement pop() {
	StackElement topElement = stack[stackPointer];
	
	// Erase the element from the stack
	stack[stackPointer] = emptyStackElement;
	stackPointer--;
	return topElement;
}

// Given an index in the stack, process the raytrace element
void processStackElement(int index) {
	// A stack element was previously proccessed. Add reflection or refraction color, depending on ray type.
	if (poppedStackElement != emptyStackElement) {
		if (poppedStackElement.type == RAY_TYPE_REFLECTION) {
			stack[index].colorReflect = poppedStackElement.colorCalcualted;
		} else if (poppedStackElement.type == RAY_TYPE_REFRACTION) {
			stack[index].colorRefract = poppedStackElement.colorCalcualted;
		}
		poppedStackElement = emptyStackElement;
	}

	Ray intersectRay = stack[index].ray;
	Collision intersectionCollision = stack[index].collision;

	// Stages of ray tracing
	switch (stack[index].phase) {
		case 1: // Initial raytrace collision detection
			intersectionCollision = getNearestCollision(intersectRay);
			if (intersectionCollision.objectIndex != -1) {
				stack[index].collision = intersectionCollision;
			}
			break;
		case 2: // Calculate addative lighting for each light source using ADS phong method
			stack[index].colorPhong = vec3(0);
			for (int li = 0; li < LIGHTS_COUNT; li++) {
				PointLight currentLight = lights[li];
				stack[index].colorPhong += adsPhoneLighting(intersectRay, intersectionCollision, currentLight);
			}
			break;
		case 3: // Calculate reflections
			// Only make recursive raytrace passes if we're not at max depth
			if (stack[index].depth < maxDepth) {
				if (objects[intersectionCollision.objectIndex].isReflective) {
					Ray reflected_ray;
					reflected_ray.start = intersectionCollision.position + intersectionCollision.normal * 0.001;
					reflected_ray.dir = reflect(intersectRay.dir, intersectionCollision.normal);
				
					// Add a raytrace for that ray to the stack
					push(reflected_ray, stack[index].depth+1, RAY_TYPE_REFLECTION);
				}
			}
			break;
		case 4: // Calculate refractions
			// Only make recursive raytrace passes if we're not at max depth
			if (stack[index].depth < maxDepth) {
				if (objects[intersectionCollision.objectIndex].isTransparent) {
					Ray refracted_ray;
					refracted_ray.start = intersectionCollision.position - intersectionCollision.normal * 0.001;
					float refraction_ratio = 1.0 / objects[intersectionCollision.objectIndex].indexOfRefraction;
					if (intersectionCollision.inside) {
						refraction_ratio = 1.0 / refraction_ratio;
					}
					refracted_ray.dir = refract(intersectRay.dir, intersectionCollision.normal, refraction_ratio);
			
					// Add a raytrace for that ray to the stack
					push(refracted_ray, stack[index].depth+1, RAY_TYPE_REFRACTION);
				}
			}
			break;
		case 5: // Mix to produce the final pixel color
			if (intersectionCollision.objectIndex > 0) {
				// next, get object color if applicable
				vec3 objColor = vec3(0.0);
				if (objects[intersectionCollision.objectIndex].useColor)
					objColor = objects[intersectionCollision.objectIndex].color;
				
				// then get reflected and refractive colors, if applicable
				vec3 colorReflect = stack[index].colorReflect;
				vec3 colorRefract = stack[index].colorRefract;

				if (stack[index].depth < maxDepth) {
					if (objects[intersectionCollision.objectIndex].isReflective) {
						objColor = mix(objColor, colorReflect, objects[intersectionCollision.objectIndex].reflectionIntensity);
					}

					if (objects[intersectionCollision.objectIndex].isTransparent) {
						objColor = mix(objColor, colorRefract, objects[intersectionCollision.objectIndex].refractionIntensity);
					}
				}

				stack[index].colorCalcualted = 1.5 * objColor * stack[index].colorPhong;
			}

			if (intersectionCollision.objectIndex == 0) {
				vec3 lightFactor = vec3(1.0);
				if (objects[intersectionCollision.objectIndex].isReflective)
					lightFactor = stack[index].colorPhong;
				if (objects[intersectionCollision.objectIndex].useColor)
					stack[index].colorCalcualted = lightFactor * objects[intersectionCollision.objectIndex].color;
			}
			break;
		// If all five phases completed, pop stack and terminate recursion
		case 6: 
			poppedStackElement = pop();
			return;
	}

	stack[index].phase++;
	return;
}

vec3 raytrace(Ray intersectRay) {
	push(intersectRay, 0, RAY_TYPE_REFLECTION);

	while (stackPointer >= 0) {
		processStackElement(stackPointer);
	}

	return poppedStackElement.colorCalcualted;
}

// ---------------------------------------------------------------------------------------

void main() {
	ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);

	// Convert the pixel position to world-space. Using the work groups to get the screen dimensions.
	float xPixel = 2.0 * pixel.x / int(gl_NumWorkGroups.x) - 1.0;
	float yPixel = 2.0 * pixel.y / int(gl_NumWorkGroups.y) - 1.0;
	
	// Get the pixel's world-space ray
	Ray worldRay;
	worldRay.start = vec3(0.0, 0.0, cameraDistance);
	vec4 worldRayEnd = vec4(xPixel, yPixel, cameraDistance - 1.0, 1.0);
	worldRay.dir = normalize(worldRayEnd.xyz - worldRay.start);

	// Run ray cast to get color at the pixel location
	vec3 pixelColor = raytrace(worldRay);
	imageStore(renderTextureOutput, pixel, vec4(pixelColor, 1.0));
}
