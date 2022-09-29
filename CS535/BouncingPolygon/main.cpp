
//*******************************************************************************************************
// CS535
// Programming Assignment 1
// Nicholas Reel
// Based on original program by: Tim Meyers
// This OpenGL program is designed to demonstrate the concepts of 2D clipping, object rotation, object
// translation, and "button" interaction.  The interface for this application is a "menu" area,
// consisting of 4 rudimentary buttons, and a viewing area, where the object is displayed.  For the
// menu buttons:
//	Button 1 (red): Shows or hides the polygon in the viewing area.
//	Button 2 (yellow): Cling on this button initiates a user-inout mode, where the direction for the
//		object's movement, and the speed of rotation, are determined by points selected by the
//		user.
//	Button 3 (green): This button starts the animation of the viewing area.
//	Button 4 (lt blue): Stops the animation process, and resets the screen.
//
// Input is determined as:
//	- Direction of translation (vector defined by first two selection points)
//	- Speed of translation (magnitude of the direction vector)
//	- Speed of rotation (# of horizontal pixels between the second two selection points)
//
// The object is an "H" shape that contains a closed number of Bezier curves of degreee 2, which are
// defined by the midpoints of the object's vertices.
// During animation, the object will then move and rotate simulatenously around the viewing area.
// When the centroid of the object hits one of the viewing area's walls, it "bounces". Any part of
// the object that passes beyond the walls of the viewing area will be clipped, with the associated
// Bezier curves updated accordingly.
//*******************************************************************************************************

#include <Windows.h>
#include <iostream>
#include <sstream>
#include <fstream>
#include <vector>
#include <math.h>

#include <glm/mat4x4.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtx/string_cast.hpp>

#include <ImGui/imgui.h>
#include <ImGui/imgui_impl_glfw.h>
#include <ImGui/imgui_impl_opengl3.h>

#include <GL/glew.h>
#include <GLFW/glfw3.h>

using namespace std;

//Global constants
const double WINDOW_WIDTH = 640 - 1;//The -1 is to not be directly against the window's edge.
const double WINDOW_HEIGHT = 480 - 1;//The -1 is to not be directly against the window's edge.
const int DIMENSIONS = 2; // Total dimensions being used for rendering
const GLfloat PI = 3.14159265;
//These next two constants should be changed depending on the graphical abilities
//	of the user's system. The higher the dampener, the slower the movement/rotation.
const GLfloat speedDamp = 50;//Dampening effect on the translation speed
const GLfloat rotationDamp = 2800;//Dampening effect on the rotation speed
const int VERTEX_OBJS = 2;

//State variables
GLint B_WIDTH = WINDOW_WIDTH / 8;
GLint B_HEIGHT = WINDOW_HEIGHT / 15;

//Window context
GLFWwindow* appWindow;

double POLY_RENDER_WINDOW_HEIGHT = WINDOW_HEIGHT / 6;

float topBoundary = POLY_RENDER_WINDOW_HEIGHT;
float botBoundary = WINDOW_HEIGHT;
float lefBoundary = 0;
float ritBoundary = WINDOW_WIDTH;

GLuint renderingProgram;
GLuint vao[VERTEX_OBJS], vbo[VERTEX_OBJS];

glm::mat4 modelMatrix(1.0f), viewMatrix(1.0f);

// Organized as (x, y)
vector<GLfloat> vertices;
vector<GLfloat> midpointVertices;
vector<GLfloat> verticesBezier;

// Taken from program 2.3 for debugging purposes
void printShaderLog(GLuint shader) {
	int len = 0;
	int chWrittn = 0;
	char* log;
	glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &len);
	if (len > 0) {
		log = (char*)malloc(len);
		glGetShaderInfoLog(shader, len, &chWrittn, log);
		cout << "Shader Info Log: " << log << endl;
		free(log);
	}
}

// Taken from program 2.5
string readFile(const char* filePath) {
	string content;
	ifstream fileStream(filePath, ios::in);
	string line;
	while (!fileStream.eof()) {
		getline(fileStream, line);
		content.append(line + "\n");
	}
	fileStream.close();
	return content;
}

GLuint createShaderProgram() {
	// Shader allows for 2d vectors to be passed in for variable positions
	string vertShaderStr = readFile("vertShader.glsl");
	string fragShaderStr = readFile("fragShader.glsl");
	const char* vshaderSource = vertShaderStr.c_str();
	const char* fshaderSource = fragShaderStr.c_str();

	GLuint vShader = glCreateShader(GL_VERTEX_SHADER);
	GLuint fShader = glCreateShader(GL_FRAGMENT_SHADER);
	GLint vertCompiled;
	GLint fragCompiled;
	GLuint vfprogram = glCreateProgram();

	glShaderSource(vShader, 1, &vshaderSource, NULL);
	glShaderSource(fShader, 1, &fshaderSource, NULL);
	glCompileShader(vShader);
	glGetShaderiv(vShader, GL_COMPILE_STATUS, &vertCompiled);
	if (vertCompiled != 1) {
		cout << "Vertex shader compilation failed" << endl;
		printShaderLog(vShader);
	} else {
		cout << "Vertex shader compilation successful" << endl;
	}

	glCompileShader(fShader);
	glGetShaderiv(fShader, GL_COMPILE_STATUS, &fragCompiled);
	if (fragCompiled != 1) {
		cout << "Fragment shader compilation failed" << endl;
		printShaderLog(fShader);
	} else {
		cout << "Fragment shader compilation successful" << endl;
	}

	glAttachShader(vfprogram, vShader);
	glAttachShader(vfprogram, fShader);
	glLinkProgram(vfprogram);

	return vfprogram;
}

//A simple struct defined to hold a 2-D point.
struct Point2D {
	GLfloat x;
	GLfloat y;
	Point2D() { x = 0; y = 0; }
	Point2D(const Point2D& pointToCopy)	{
		x = pointToCopy.x;
		y = pointToCopy.y;
	}
	Point2D(GLfloat xc, GLfloat yc)	{
		x = xc;
		y = yc;
	}
};

//A struct to hold a rectangular shape, as defined by the bottom left
//  and the upper right corner points.
struct Rect2D {
	Point2D bottomleft;
	Point2D topright;
	Rect2D() { bottomleft = Point2D();  topright = Point2D(); }
	Rect2D(Point2D bl, Point2D tr) {
		bottomleft = bl;
		topright = tr;
	}
	void operator=(const Rect2D& r) {
		bottomleft = r.bottomleft;
		topright = r.topright;
	}
};

string textOutput = "";//Global output, sent to the screen by onDisplay()
Point2D centroid; //The polygon's centroid.
vector <Point2D>polygon;//Vertices that define the polygon.
vector <Point2D>midpoints;//The midpoints between the polygon's vertices.
vector <Point2D>clipPolygon;
vector <Point2D>rotatedPolygon;

bool showPolygon;//Switch for button1
bool animationRunning;//Switch for button3
GLint inputState;//This represents a FSM to track the input given so far.
Point2D directionVector; //The direction vector for the polygon to follow.
Point2D originalDirection; //A "backup" for resetting the animation.
GLfloat directionSpeed;//The speed at which the polygon moves.
GLint rotationSpeed;//The speed factor for the polygon's rotation.
GLfloat theta;//The amount to rotate the polygon; updated by the animation process.


void acceptInputs(GLint x, GLint y) {
	switch (inputState)	{
	case 1:
		directionVector = Point2D(x, y);
		textOutput = "Please select the second point for the direction vector.";
		inputState = 2;
		break;
	case 2:
		directionVector = Point2D(x - directionVector.x, y - directionVector.y);
		originalDirection = directionVector;
		directionSpeed = sqrt(pow(directionVector.x, 2) + pow(directionVector.y, 2));
		textOutput = "Please select the first point for the rotation speed.";
		inputState = 3;
		break;
	case 3:
		rotationSpeed = x;
		textOutput = "Please select the second point for the rotation speed.";
		inputState = 4;
		break;
	case 4:
		rotationSpeed = abs(x - rotationSpeed);
		textOutput = "Data input successfully completed.";
		inputState = 0;
		break;
	default://Invalid state, this case shouldn't arise.
		inputState = 0;
		break;
	}
}

// Mouse click???
GLint getButtonPushed(int buttonAction) {
	GLint button = -1;
	//y = WINDOW_HEIGHT - y;
	textOutput = "";
	switch (buttonAction) {
	case 0:
		showPolygon = !showPolygon; //Toggle showing the polygon.
		button = 1;
		break;
	case 1:
		textOutput = "Please select the first point for the direction vector.";
		button = 2;
		inputState = 1;//Ready to accept input.
		directionVector = Point2D();
		theta = 0;
		centroid = Point2D(WINDOW_WIDTH / 2, 5 * WINDOW_HEIGHT / 10);
		directionVector = originalDirection;//In case input is not completed before restarting animation.
		break;
	case 2:
		if (directionSpeed == 0 && directionVector.x == originalDirection.x && directionVector.y && originalDirection.y) {
			textOutput = "Animation Started";
			inputState = 0;
			button = 3;
			animationRunning = true;
		} else {
			textOutput = "Please input motion vectors before playing animation";
		}
		break;
	case 3:
		if (animationRunning) {
			textOutput = "Animation Stopped";
			theta = 0;
			centroid = Point2D(WINDOW_WIDTH / 2, 5 * WINDOW_HEIGHT / 10);
			directionVector = originalDirection;
			button = 4;
			animationRunning = false;
		} else {
			textOutput = "No animation playing";
		}
		break;
	}

	return button;
}

void onMouse(GLFWwindow* window, int button, int action, int mods) {
	GLint menuButton = 1;
	if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
		if (showPolygon) {
			GLdouble mouseXpos, mouseYpos;
			glfwGetCursorPos(window, &mouseXpos, &mouseYpos);
			acceptInputs(mouseXpos, mouseYpos);
		}
	}
	// Passes GLFW callback parameters to ImGui for compatability
	ImGui_ImplGlfw_MouseButtonCallback(window, button, action, mods);
}

void onKeyboard(GLFWwindow* window, int key, int scancode, int action, int mods) {
	switch (key) {
	case GLFW_KEY_ESCAPE:  //Typed the Escape key, so exit.
		exit(0);
		break;
	default:
		break;
	}
}

void insertPointsIntoVectorBuffer(vector<GLfloat> *vectorBuffer, vector<Point2D> pointVectorInput, bool emptyFirst) {
	// Insert point values into VBO
	if (emptyFirst) {
		vectorBuffer->clear();
	}

	for (auto point2D : pointVectorInput) {
		vectorBuffer->push_back(point2D.x);
		vectorBuffer->push_back(point2D.y);
	}
}

void calcMidpoints(vector<Point2D> p) {
	//Calculate the midpoints of the edges
	midpoints.clear();
	for (GLint j = 0; j < p.size(); j++)
	{
		GLfloat midX = (p.at(j % p.size()).x + p.at((j + 1) % p.size()).x) / 2;
		GLfloat midY = (p.at(j % p.size()).y + p.at((j + 1) % p.size()).y) / 2;
		midpoints.push_back(Point2D(midX, midY));
	}
}

void initPolygon() {
	showPolygon = false;
	inputState = 0;
	directionVector = Point2D();
	directionSpeed = 0;
	rotationSpeed = 0;
	theta = 0;

	//Initialize the polygon vertices
	centroid = Point2D(WINDOW_WIDTH / 2, 5 * WINDOW_HEIGHT / 10);
	GLfloat unit = WINDOW_HEIGHT / 12;
	polygon.push_back(Point2D(-3 * unit, -3 * unit));
	polygon.push_back(Point2D(-3 * unit, 3 * unit));
	polygon.push_back(Point2D(3 * unit, 3 * unit));
	polygon.push_back(Point2D(3 * unit, unit));
	polygon.push_back(Point2D(1.5 * unit, unit));
	polygon.push_back(Point2D(1.5 * unit, 2 * unit));
	polygon.push_back(Point2D(-1.5 * unit, 2 * unit));
	polygon.push_back(Point2D(-1.5 * unit, -2 * unit));
	polygon.push_back(Point2D(1.5 * unit, -2 * unit));
	polygon.push_back(Point2D(1.5 * unit, -unit));
	polygon.push_back(Point2D(3 * unit, -unit));
	polygon.push_back(Point2D(3 * unit, -3 * unit));

	//Initialize the midpoints
	calcMidpoints(polygon);

	clipPolygon = vector<Point2D>(polygon);

	// Move polygons into simplified vectors for easy use with VAO/VBOs
	insertPointsIntoVectorBuffer(&vertices, clipPolygon, true);
	insertPointsIntoVectorBuffer(&midpointVertices, midpoints, true);
}

void drawButtonBar() {
	ImGui::SetNextWindowPos(ImVec2(0.0, 0.0));
	ImGui::SetNextWindowSize(ImVec2(WINDOW_WIDTH, POLY_RENDER_WINDOW_HEIGHT));
	ImGui::Begin("Bouncing Polygon!");
	
	ImGui::SameLine();
	if (ImGui::Button("Toggle polygon")) {
		getButtonPushed(0);
	}

	ImGui::SameLine();
	if (ImGui::Button("Set motion vectors")) {
		getButtonPushed(1);
	}

	ImGui::SameLine();
	if (ImGui::Button("Start animation")) {
		getButtonPushed(2);
	}

	ImGui::SameLine();
	if (ImGui::Button("Stop animation")) {
		getButtonPushed(3);
	}
	
	ImGui::Separator();
	ImGui::Text("%s", textOutput.c_str());

	ImGui::End();
}

// Convert point from "model" to "world"
Point2D convertPointToProjectionPoint(Point2D pointToCovnert) {
	glm::vec4 unrotatedPoint = glm::vec4(modelMatrix * glm::vec4(pointToCovnert.x, pointToCovnert.y, 0.0, 1.0));
	return Point2D(unrotatedPoint.x, unrotatedPoint.y);
}

// Convert point from "world" to "model"
Point2D convertPointToModelPoint(Point2D pointToCovnert) {
	glm::vec4 unrotatedPoint = glm::vec4(modelMatrix / glm::vec4(pointToCovnert.x, pointToCovnert.y, 0.0, 1.0));
	return Point2D(unrotatedPoint.x, unrotatedPoint.y);
}

Point2D findIntersect(Point2D A, Point2D B) {
	// Pair of points representing intersecting border to be checked against
	Point2D C, D;
	GLfloat t = -1;
	if ((B.x <= lefBoundary && A.x >= lefBoundary) || (A.x <= lefBoundary && B.x >= lefBoundary)) {//Left clipping
		C = Point2D(lefBoundary, topBoundary);
		D = Point2D(lefBoundary, botBoundary);
	} else if ((B.x >= ritBoundary && A.x <= ritBoundary) || (A.x >= ritBoundary && B.x <= ritBoundary)) {//Right clipping
		C = Point2D(ritBoundary, botBoundary);
		D = Point2D(ritBoundary, topBoundary);
	} else if ((B.y <= topBoundary && A.y >= topBoundary) || (A.y <= topBoundary && B.y >= topBoundary)) {//Bottom clipping
		C = Point2D(ritBoundary, topBoundary);
		D = Point2D(lefBoundary, topBoundary);
	} else if ((B.y >= botBoundary && A.y <= botBoundary) || (A.y >= botBoundary && B.y <= botBoundary)) {//Top clipping
		C = Point2D(lefBoundary, botBoundary);
		D = Point2D(ritBoundary, botBoundary);
	} else
		return Point2D(99999, 99999);//No intersection

	GLfloat denom = (D.y - C.y) * (B.x - A.x) - (D.x - C.x) * (B.y - A.y);
	if (denom != 0)
		t = ((D.x - C.x) * (A.y - C.y) - (D.y - C.y) * (A.x - C.x)) / denom;

	GLfloat interX = 99999, interY = 99999;
	if (t >= 0 && t <= 1) {
		// Clamp intersection values to the boundaries
		interX = max(lefBoundary, min((1 - t) * A.x + t * B.x, ritBoundary));
		interY = max(topBoundary, min((1 - t) * A.y + t * B.y, botBoundary));
	}
	return Point2D(interX, interY);
}

bool clip() {
	bool updatedP = false;
	Point2D intersection;
	bool lookingForEnter = false;
	vector<Point2D> newPolygon;

	for (GLint i = 0; i < clipPolygon.size(); i++) {
		Point2D A = Point2D(clipPolygon.at(i % clipPolygon.size()));
		Point2D B = Point2D(clipPolygon.at((i + 1) % clipPolygon.size()));

		float oldAx = A.x;
		float oldAy = A.y;
		float oldBx = B.x;
		float oldBy = B.y;

		// Apply model matrix to rotate points and put in relation to the "world"
		Point2D rotatedA = convertPointToProjectionPoint(A);
		Point2D rotatedB = convertPointToProjectionPoint(B);

		A.x = rotatedA.x;
		A.y = rotatedA.y;
		B.x = rotatedB.x;
		B.y = rotatedB.y;

		Point2D I1 = Point2D(99999, 99999);

		if ((A.x < lefBoundary && B.x < lefBoundary)
			|| (A.x > ritBoundary && B.x > ritBoundary)
			|| (A.y < topBoundary && B.y < topBoundary)
			|| (A.y > botBoundary && B.y > botBoundary)) {
			lookingForEnter = true;
		} else {
			I1 = findIntersect(A, B);
		}

		if ((A.x <= lefBoundary || A.x >= ritBoundary || A.y <= topBoundary || A.y >= botBoundary)
			&& !lookingForEnter)
			lookingForEnter = true;

		// Check corners
		if (lookingForEnter) {
			if (A.y >= botBoundary && A.x <= lefBoundary) { // Bottom left
				newPolygon.push_back(convertPointToModelPoint(Point2D(lefBoundary, botBoundary)));
			} else if (A.x <= lefBoundary&& A.y <= topBoundary) { // Top left
				newPolygon.push_back(convertPointToModelPoint(Point2D(lefBoundary, topBoundary)));
			} else if (A.y <= topBoundary && A.x >= ritBoundary) { // Top right
				newPolygon.push_back(convertPointToModelPoint(Point2D(ritBoundary, topBoundary)));
			} else if (A.x >= ritBoundary && A.y >= botBoundary) { // Bottom right
				newPolygon.push_back(convertPointToModelPoint(Point2D(ritBoundary, botBoundary)));
			}
		}

		if (!lookingForEnter)
			newPolygon.push_back(Point2D(clipPolygon.at(i % clipPolygon.size())));

		if (I1.x != 99999 && I1.y != 99999) {//If the point is valid.
			if (!lookingForEnter) {
				lookingForEnter = true;
				newPolygon.push_back(convertPointToModelPoint(Point2D(I1.x, I1.y)));
				updatedP = true;
				intersection = I1;
			} else {
				lookingForEnter = false;
				newPolygon.push_back(convertPointToModelPoint(Point2D(I1.x, I1.y)));
				updatedP = true;
			}
		}

		// Reverse rotation
		A.x = oldAx;
		A.y = oldAy;
		B.x = oldBx;
		B.y = oldBy;
	}
	clipPolygon.clear();
	clipPolygon.insert(clipPolygon.end(), newPolygon.begin(), newPolygon.end());

	return updatedP;
}

Point2D bezierCurve(Point2D A, Point2D B, Point2D C, GLfloat t) {
	GLfloat fx = pow(1 - t, 2) * A.x + 2 * t * (1 - t) * B.x + pow(t, 2) * C.x;
	GLfloat fy = pow(1 - t, 2) * A.y + 2 * t * (1 - t) * B.y + pow(t, 2) * C.y;
	return Point2D(fx, fy);
}

void calculatePolygon() {

	if (animationRunning) {
		theta += (1 / speedDamp) / (2 * PI); //Add 1 radian
		float xDelta = directionVector.x / directionSpeed;
		float yDelta = directionVector.y / directionSpeed;
		centroid.x += (directionSpeed / speedDamp) * xDelta;
		centroid.y += (directionSpeed / speedDamp) * yDelta;
	}

	GLfloat newTheta = theta;
	if (inputState == 0) {
		if (centroid.x <= lefBoundary || centroid.x >= ritBoundary)
			directionVector.x *= -1;
		if (centroid.y <= topBoundary || centroid.y >= botBoundary)
			directionVector.y *= -1;
	}
	
	newTheta *= rotationSpeed / (rotationDamp * PI);

	clipPolygon = vector<Point2D>(polygon);

	// Calcualte polygon clipping and insert them into buffer for VBO
	bool didClipPolygon = clip();
	insertPointsIntoVectorBuffer(&vertices, clipPolygon, true);

	// Recalculate midpoints of polygon for updated bezier polygon
	calcMidpoints(clipPolygon);
	if (clipPolygon.size() > 0) {
		verticesBezier.clear();
		for (GLint k = 0; k < midpoints.size(); k++) {
			Point2D old = Point2D(midpoints.at(k % midpoints.size()));
			Point2D A = old;
			GLfloat deltaT = .1;

			int polygonSize = clipPolygon.size();

			for (GLfloat t = 0; t <= 1; t += deltaT) {
				Point2D nextPoint = clipPolygon.at((k + 1) % clipPolygon.size());
				Point2D nextMidpoint = midpoints.at((k + 1) % midpoints.size());
				Point2D B = Point2D(nextPoint.x, nextPoint.y);
				Point2D C = Point2D(nextMidpoint.x, nextMidpoint.y);
				Point2D current = bezierCurve(A, B, C, t);
				verticesBezier.push_back(current.x);
				verticesBezier.push_back(current.y);
				old = current;
			}
		}
	} else {
		// Unexpeted error when calcualting clip resulting in empty polygon
		throw "ERROR: Miscalculation in clipped polygon";
	}

	// Update vertex buffer to use calculated vertices
	glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
	glBufferData(GL_ARRAY_BUFFER, verticesBezier.size() * sizeof(float), verticesBezier.data(), GL_DYNAMIC_DRAW);
	glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
	glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), vertices.data(), GL_DYNAMIC_DRAW);
}

void displayShader() {
	GLuint modelViewProjectionLoc = glGetUniformLocation(renderingProgram, "u_modelViewProjection");
	// Build matrices for shader computation
	glm::mat4 projMatrix = glm::ortho(0.0, WINDOW_WIDTH, WINDOW_HEIGHT, 0.0);
	modelMatrix = glm::mat4(1.0f);
	modelMatrix = glm::translate(modelMatrix, glm::vec3(centroid.x, centroid.y, 0.0f));
	modelMatrix = glm::rotate(modelMatrix, theta, glm::vec3(0, 0, 1));

	glm::mat4 mvpMatrix = projMatrix * modelMatrix;
	glUniformMatrix4fv(modelViewProjectionLoc, 1, GL_FALSE, value_ptr(mvpMatrix));
}

void polygonInit(int vaoNum, vector<float>* polygonVertices) {
	// Setup selected VAO and VBO
	glGenVertexArrays(1, &vao[vaoNum]);
	glBindVertexArray(vao[vaoNum]);
	glGenBuffers(1, &vbo[vaoNum]);

	// Bind polygon vector to buffer
	glBindBuffer(GL_ARRAY_BUFFER, vbo[vaoNum]);
	glBufferData(GL_ARRAY_BUFFER, polygonVertices->size() * sizeof(float), polygonVertices->data(), GL_DYNAMIC_DRAW);

	// Bind position vector in shader
	glEnableVertexAttribArray(0);
	glVertexAttribPointer(0, DIMENSIONS, GL_FLOAT, GL_FALSE, DIMENSIONS * sizeof(float), NULL);
}

void displayPolygon(int vaoNum, vector<float>* polygonVertices) {
	glBindVertexArray(vao[vaoNum]);
	glDrawArrays(GL_LINE_LOOP, 0, polygonVertices->size() / DIMENSIONS);
}

void init(GLFWwindow* window) {
	renderingProgram = createShaderProgram();
	
	polygonInit(0, &verticesBezier);
	polygonInit(1, &vertices);

	glBindVertexArray(0);
}

int main(int, char**) {
	// GLFW initialization
	if (!glfwInit()) {return -1;}
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	appWindow = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Program 1 - Nicholas Reel", NULL, NULL);
	glfwMakeContextCurrent(appWindow);
	if (glewInit() != GLEW_OK) { exit(EXIT_FAILURE); }
	glfwSwapInterval(1); // vsync enabled

	// IMGUI initialization
	IMGUI_CHECKVERSION();
	ImGui::CreateContext();
	ImGuiIO& io = ImGui::GetIO();
	ImGui::StyleColorsLight();

	ImGui_ImplGlfw_InitForOpenGL(appWindow, true);
	ImGui_ImplOpenGL3_Init("#version 430");

	// Initialize vertices before they are piped into buffers
	initPolygon();
	init(appWindow);
	glfwSetMouseButtonCallback(appWindow, onMouse);
	glfwSetKeyCallback(appWindow, onKeyboard);

	// Init shaders
	glUseProgram(renderingProgram);
	displayShader();

	//Infinite Loop
	while (!glfwWindowShouldClose(appWindow)) {
		// Set refresh buffers
		glClearColor(1.0, 1.0, 1.0, 1.0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		ImGui_ImplOpenGL3_NewFrame();
		ImGui_ImplGlfw_NewFrame();
		ImGui::NewFrame();

		drawButtonBar();

		ImGui::Render();


		// Only render if polygon is triggered to show by GUI
		if (showPolygon) {
			displayShader();

			displayPolygon(0, &verticesBezier);
			displayPolygon(1, &vertices);

			calculatePolygon();
		}

		ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

		//display the buffer
		glfwSwapBuffers(appWindow);

		glfwPollEvents();
	}

	ImGui_ImplOpenGL3_Shutdown();
	ImGui_ImplGlfw_Shutdown();
	ImGui::DestroyContext();

	glfwDestroyWindow(appWindow);
	glfwTerminate();
	return EXIT_SUCCESS;
}
