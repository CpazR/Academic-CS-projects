
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
#include <GL/glew.h>
#include <GL/glut.h>
#include <GLFW/glfw3.h>
#include <iostream>
#include <sstream>
#include <fstream>
#include <vector>
#include <math.h>

#include "glm/mat4x4.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "glm/gtx/string_cast.hpp"

using namespace std;

//Global constants
const double WINDOW_WIDTH = 640 - 1;//The -1 is to not be directly against the window's edge.
const double WINDOW_HEIGHT = 480 - 1;//The -1 is to not be directly against the window's edge.
const int DIMENSIONS = 2; // Total dimensions being used for rendering
const GLfloat PI = 3.14159265;
//These next two constants should be changed depending on the graphical abilities
//	of the user's system. The higher the dampener, the slower the movement/rotation.
const GLfloat speedDamp = 600;//Dampening effect on the translation speed
const GLfloat rotationDamp = 2800;//Dampening effect on the rotation speed
const int VAOS = 1;

//State variables
GLint B_WIDTH = WINDOW_WIDTH / 8;
GLint B_HEIGHT = WINDOW_HEIGHT / 15;

//Window context
GLFWwindow* appWindow;

GLuint renderingProgram;
GLuint vao[VAOS], vbo;

// Organized as (x, y)
vector<GLfloat> vertices;

// Taken from program 2.3 for debugging purposes
void printShaderLog(GLuint shader) {
	int len = 0;
	int chWrittn = 0;
	char* log;
	glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &len);
	if (len > 0)
	{
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
	Point2D(GLfloat xc, GLfloat yc)
	{
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

vector <Rect2D>buttons; //Specifies the buttons.
ostringstream textOutput;//Global output, sent to the screen by onDisplay()
Point2D centroid; //The polygon's centroid.
vector <Point2D>polygon;//Vertices that define the polygon.
vector <Point2D>midpoints;//The midpoints between the polygon's vertices.
vector <Point2D>clipPolygon;
vector <Point2D>rotatedPolygon;

bool showPolygon;//Switch for button1
GLint inputState;//This represents a FSM to track the input given so far.
Point2D directionVector; //The direction vector for the polygon to follow.
Point2D originalDirection; //A "backup" for resetting the animation.
GLfloat directionSpeed;//The speed at which the polygon moves.
GLint rotationSpeed;//The speed factor for the polygon's rotation.
GLfloat theta;//The amount to rotate the polygon; updated by the animation process.

void bitmapCharacter(void* font, int character) {
	// TODO: figure out replacement for drawing text. Or figure out GLUT for 64 bit systems?
	//glutBitmapCharacter(font, character);
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
	directionSpeed = 1;
	rotationSpeed = 0;
	theta = 0;

	//Initialize the polygon vertices
	centroid = Point2D(WINDOW_WIDTH / 2, 5 * WINDOW_HEIGHT / 12);
	GLfloat unit = WINDOW_HEIGHT / 12;
	polygon.push_back(Point2D(-3 * unit, -3 * unit));
	polygon.push_back(Point2D(-3 * unit, 3 * unit));
	polygon.push_back(Point2D(-unit, 3 * unit));
	polygon.push_back(Point2D(-unit, unit));
	polygon.push_back(Point2D(unit, unit));
	polygon.push_back(Point2D(unit, 3 * unit));
	polygon.push_back(Point2D(3 * unit, 3 * unit));
	polygon.push_back(Point2D(3 * unit, -3 * unit));
	polygon.push_back(Point2D(unit, -3 * unit));
	polygon.push_back(Point2D(unit, -unit));
	polygon.push_back(Point2D(-unit, -unit));
	polygon.push_back(Point2D(-unit, -3 * unit));

	// Insert point values into VBO
	for (auto point2D : polygon) {
		vertices.push_back(point2D.x);
		vertices.push_back(point2D.y);
	}

	//Initialize the midpoints
	//calcMidpoints(polygon);
	//clipPolygon = vector<Point2D>(polygon);//Copy the polygon
}

void drawButton(GLint buttonNum) {
	if (buttonNum == 1)
		glColor3f(1, 0, 0);//Red
	else if (buttonNum == 2)
		glColor3f(1, 1, 0);//Yellow
	else if (buttonNum == 3)
		glColor3f(0, 1, 0);//Green
	else
		glColor3f(0, .5, 1);//Light blue

	//Drawing the button
	glRectf(buttons.at(buttonNum).bottomleft.x, buttons.at(buttonNum).bottomleft.y,
		buttons.at(buttonNum).topright.x, buttons.at(buttonNum).topright.y);

	//Outputting the button number to the screen
	glColor3d(0, 0, 0); //Black text
	glRasterPos2f(buttonNum * WINDOW_WIDTH / 5 - (B_WIDTH / 15), 11 * WINDOW_HEIGHT / 12 - (B_HEIGHT / 5));
	ostringstream os;
	os << buttonNum;
	bitmapCharacter(GLUT_BITMAP_HELVETICA_18, *os.str().c_str());
}

void drawButtonBar() {
	glColor3f(.5, .5, .5);//Set to grey
	glRectf(0.0f, 5 * WINDOW_HEIGHT / 6, WINDOW_WIDTH, WINDOW_HEIGHT);//Drawing the bar background

	for (GLint i = 1; i <= 4; i++)
		drawButton(i);

	glColor3f(0.0f, 0.0f, 0.0f); // Reset the drawing color to be black
}

void displayText(ostringstream& os) {
	string output = os.str();
	glRasterPos2f(WINDOW_WIDTH / 25, 5 * WINDOW_HEIGHT / 6 - 12);//Reset position
	glColor3f(0.0f, 0.0f, 0.0f); //Black text
	for (GLint j = 0; j < output.length(); j++)
	{
		bitmapCharacter(GLUT_BITMAP_HELVETICA_12, output[j]);
	}
}

Point2D bezierCurve(Point2D A, Point2D B, Point2D C, GLfloat t) {
	GLfloat fx = pow(1 - t, 2) * A.x + 2 * t * (1 - t) * B.x + pow(t, 2) * C.x;
	GLfloat fy = pow(1 - t, 2) * A.y + 2 * t * (1 - t) * B.y + pow(t, 2) * C.y;
	return Point2D(fx, fy);
}

Point2D findIntersect(Point2D A, Point2D B) {
	Point2D C, D;
	GLfloat t = -1;
	if ((B.x < 0 && A.x >= 0) || (A.x < 0 && B.x >= 0))//Left clipping
	{
		C = Point2D(0, 0);
		D = Point2D(0, 5 * WINDOW_HEIGHT / 6);
	}
	else if ((B.x > WINDOW_WIDTH && A.x <= WINDOW_WIDTH)
		|| (A.x > WINDOW_WIDTH && B.x <= WINDOW_WIDTH))//Right clipping
	{
		C = Point2D(WINDOW_WIDTH, 5 * WINDOW_HEIGHT / 6);
		D = Point2D(WINDOW_WIDTH, 0);
	}
	else if ((B.y < 0 && A.y >= 0) || (A.y < 0 && B.y >= 0))//Bottom clipping
	{
		C = Point2D(WINDOW_WIDTH, 0);
		D = Point2D(0, 0);
	}
	else if ((B.y > (5 * WINDOW_HEIGHT / 6) && A.y <= (5 * WINDOW_HEIGHT / 6))
		|| (A.y > (5 * WINDOW_HEIGHT / 6) && B.y <= (5 * WINDOW_HEIGHT / 6)))//Top clipping
	{
		C = Point2D(0, 5 * WINDOW_HEIGHT / 6);
		D = Point2D(WINDOW_WIDTH, 5 * WINDOW_HEIGHT / 6);
	}
	else
		return Point2D(99999, 99999);//No intersection

	GLfloat denom = (D.y - C.y) * (B.x - A.x) - (D.x - C.x) * (B.y - A.y);
	if (denom != 0)
		t = ((D.x - C.x) * (A.y - C.y) - (D.y - C.y) * (A.x - C.x)) / denom;

	GLfloat interX = 99999, interY = 99999;
	if (t >= 0 && t <= 1)
	{
		interX = (1 - t) * A.x + t * B.x;
		interY = (1 - t) * A.y + t * B.y;
	}
	return Point2D(interX, interY);
}

bool clip() {
	bool updatedP = false;
	Point2D intersection;
	bool lookingForEnter = false;
	bool cornerAdded = false;
	vector<Point2D> newPolygon;
	clipPolygon = rotatedPolygon;

	for (GLint i = 0; i < clipPolygon.size(); i++)
	{
		Point2D A = clipPolygon.at(i % clipPolygon.size());
		Point2D B = clipPolygon.at((i + 1) % clipPolygon.size());

		Point2D I1 = Point2D(99999, 99999);

		if ((A.x < 0 && B.x < 0) || (A.x > WINDOW_WIDTH && B.x > WINDOW_WIDTH)
			|| (A.y < 0 && B.y < 0) || (A.y > (5 * WINDOW_HEIGHT / 6) && B.y > (5 * WINDOW_HEIGHT / 6)))
		{
			lookingForEnter = true;
		}
		else
		{
			I1 = findIntersect(A, B);
		}

		if ((A.x<0 || A.x>WINDOW_WIDTH || A.y<0 || A.y>(5 * WINDOW_HEIGHT / 6))
			&& !lookingForEnter)
			lookingForEnter = true;

		if (lookingForEnter && !cornerAdded)
		{
			if (A.y > (5 * WINDOW_HEIGHT / 6) && A.x < 0)
			{
				newPolygon.push_back(Point2D(0, 5 * WINDOW_HEIGHT / 6));
				cornerAdded = true;
			}
			else if (A.x < 0 && A.y < 0)
			{
				newPolygon.push_back(Point2D(0, 0));
				cornerAdded = true;
			}
			else if (A.y<0 && A.x>WINDOW_WIDTH)
			{
				newPolygon.push_back(Point2D(WINDOW_WIDTH, 0));
				cornerAdded = true;
			}
			else if (A.x > WINDOW_WIDTH && A.y > (5 * WINDOW_HEIGHT / 6))
			{
				newPolygon.push_back(Point2D(WINDOW_WIDTH, 5 * WINDOW_HEIGHT / 6));
				cornerAdded = true;
			}
		}

		if (!lookingForEnter)
			newPolygon.push_back(clipPolygon.at(i % clipPolygon.size()));

		if (I1.x != 99999 && I1.y != 99999)//If the point is valid.
		{
			if (!lookingForEnter)
			{
				lookingForEnter = true;
				newPolygon.push_back(I1);
				updatedP = true;
				intersection = I1;
			}
			else
			{
				lookingForEnter = false;
				newPolygon.push_back(I1);
				updatedP = true;
			}
		}
	}
	clipPolygon = newPolygon;

	return updatedP;
}

void onKeyboard(GLFWwindow* window, int key, int scancode, int action, int mods) {//Handles keyboard events
	switch (key)
	{
	case 27:  //Typed the Escape key, so exit.
		exit(0);
		break;
	default:
		break;
	}
}

void onIdle() {
	if (inputState == 0)
	{
		theta += 1 / (2 * PI);//Add 1 radian
		centroid.x += directionSpeed / speedDamp * (directionVector.x / directionSpeed);
		centroid.y += directionSpeed / speedDamp * (directionVector.y / directionSpeed);
	}
}

void acceptInputs(GLint x, GLint y) {
	switch (inputState)
	{
	case 1:
		directionVector = Point2D(x, y);
		textOutput << "Please select the second point for the direction vector.";
		inputState = 2;
		break;
	case 2:
		directionVector = Point2D(x - directionVector.x, y - directionVector.y);
		originalDirection = directionVector;
		directionSpeed = sqrt(pow(directionVector.x, 2) + pow(directionVector.y, 2));
		textOutput << "Please select the first point for the rotation speed.";
		inputState = 3;
		break;
	case 3:
		rotationSpeed = x;
		textOutput << "Please select the second point for the rotation speed.";
		inputState = 4;
		break;
	case 4:
		rotationSpeed = abs(x - rotationSpeed);
		textOutput << "Data input successfully completed.";
		inputState = 0;
		break;
	default://Invalid state, this case shouldn't arise.
		inputState = 0;
		break;
	}
}

GLint getButtonPushed(GLint x, GLint y) {
	GLint button = -1;
	y = WINDOW_HEIGHT - y;
	textOutput.str("");
	if ((x >= buttons.at(1).bottomleft.x && x <= buttons.at(1).topright.x)
		&& (y >= (buttons.at(1).bottomleft.y) && y <= (buttons.at(1).topright.y)))
	{
		showPolygon = !showPolygon; //Toggle showing the polygon.
		button = 1;
	}
	else if ((x >= buttons.at(2).bottomleft.x && x <= buttons.at(2).topright.x)
		&& (y >= (buttons.at(2).bottomleft.y) && y <= (buttons.at(2).topright.y)))
	{
		textOutput << "Please select the first point for the direction vector.";
		button = 2;
		inputState = 1;//Ready to accept input.
		directionVector = Point2D();
		theta = 0;
		centroid = Point2D(WINDOW_WIDTH / 2, 5 * WINDOW_HEIGHT / 12);
		directionVector = originalDirection;//In case input is not completed before restarting animation.
		//glutIdleFunc(NULL);//Stop animation while accepting input.
	}
	else if ((x >= buttons.at(3).bottomleft.x && x <= buttons.at(3).topright.x)
		&& (y >= (buttons.at(3).bottomleft.y) && y <= (buttons.at(3).topright.y)))
	{
		//glutIdleFunc(onIdle);
		textOutput << "Animation Started";
		inputState = 0;
		button = 3;
	}
	else if ((x >= buttons.at(4).bottomleft.x && x <= buttons.at(4).topright.x)
		&& (y >= (buttons.at(4).bottomleft.y) && y <= (buttons.at(4).topright.y)))
	{
		//glutIdleFunc(NULL);
		textOutput << "Animation Stopped";
		theta = 0;
		centroid = Point2D(WINDOW_WIDTH / 2, 5 * WINDOW_HEIGHT / 12);
		directionVector = originalDirection;
		button = 4;
	}
	else if (inputState > 0)
		acceptInputs(x, y);

	return button;
}

void onMouse(GLFWwindow* window, int button, int action, int mods) {
	GLint menuButton = 1;
	int state = glfwGetMouseButton(window, button);

	if (button == GLFW_MOUSE_BUTTON_LEFT && state == GLFW_PRESS)
	{
		double xPos, yPos;
		glfwGetCursorPos(window, &xPos, &yPos);
		menuButton = getButtonPushed(xPos, yPos);
	}
}

void calculatePolygon() {
	GLfloat newTheta = theta;

	if (inputState == 0)
	{
		if (centroid.x <= 0 || centroid.x >= WINDOW_WIDTH)
			directionVector.x *= -1;
		if (centroid.y <= 0 || centroid.y >= 5 * WINDOW_HEIGHT / 6)
			directionVector.y *= -1;
	}
	newTheta *= rotationSpeed / (rotationDamp * PI);

	rotatedPolygon.clear();
	for (GLint n = 0; n < polygon.size(); n++)
	{
		GLfloat rotatedX = polygon.at(n % polygon.size()).x * cos(newTheta)
			+ polygon.at(n % polygon.size()).y * sin(newTheta);
		GLfloat rotatedY = polygon.at(n % polygon.size()).x * -sin(newTheta)
			+ polygon.at(n % polygon.size()).y * cos(newTheta);
		GLfloat newX = rotatedX + centroid.x;
		GLfloat newY = rotatedY + centroid.y;
		rotatedPolygon.push_back(Point2D(newX, newY));
	}
	clipPolygon = rotatedPolygon;

	bool updatedP = clip();

	//Draw the polygon
	glBegin(GL_LINE_STRIP);
	for (GLint i = 0; i <= clipPolygon.size(); i++)
	{
		GLfloat newX = clipPolygon.at(i % clipPolygon.size()).x;
		GLfloat newY = clipPolygon.at(i % clipPolygon.size()).y;
		glVertex2f(newX, newY);
	}
	glEnd();

	calcMidpoints(clipPolygon);

	//Draw the Bezier curves
	glBegin(GL_LINE_STRIP);
	for (GLint k = 0; k < midpoints.size(); k++)
	{
		Point2D old = Point2D(midpoints.at(k % midpoints.size()).x,
			midpoints.at(k % midpoints.size()).y);
		Point2D A = old;
		GLfloat deltaT = .1;

		for (GLfloat t = 0; t <= 1; t += deltaT)
		{
			Point2D B = Point2D(clipPolygon.at((k + 1) % clipPolygon.size()).x,
				clipPolygon.at((k + 1) % clipPolygon.size()).y);
			Point2D C = Point2D(midpoints.at((k + 1) % midpoints.size()).x,
				midpoints.at((k + 1) % midpoints.size()).y);
			Point2D current = bezierCurve(A, B, C, t);
			glVertex2f(current.x, current.y);
			old = current;
		}
	}
	glEnd();
}

void displayShader() {
	GLuint modelViewProjectionLoc = glGetUniformLocation(renderingProgram, "u_modelViewProjection");
	glm::mat4 modelMatrix(1.0f), viewMatrix(1.0f);
	glm::mat4 projMatrix = glm::ortho(0.0, WINDOW_WIDTH, WINDOW_HEIGHT, 0.0);
	modelMatrix = glm::translate(modelMatrix, glm::vec3(centroid.x, centroid.y, 0.0f));
	modelMatrix = glm::rotate(modelMatrix, theta, glm::vec3(0, 0, 1));

	glm::mat4 mvpMatrix = projMatrix * modelMatrix;
	glUniformMatrix4fv(modelViewProjectionLoc, 1, GL_FALSE, value_ptr(mvpMatrix));
}

void displayPolygon() {
	glBindVertexArray(vao[0]);
	glDrawArrays(GL_LINE_LOOP, 0, vertices.size() / DIMENSIONS);
	calculatePolygon();
}

void onDisplay(GLFWwindow* window) {
	// Set refresh buffers
	glClearColor(1.0, 1.0, 1.0, 1.0);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	displayShader();
	displayPolygon();
}

void shaderInit() {
	glUseProgram(renderingProgram);
	displayShader();
}

void init(GLFWwindow* window) {
	renderingProgram = createShaderProgram();
	// Establish vao and vbo buffers
	glGenVertexArrays(VAOS, vao);
	glGenBuffers(1, &vbo);
	glBindVertexArray(vao[0]);

	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), vertices.data(), GL_STATIC_DRAW);

	// Pipe buffer for shader input
	glVertexAttribPointer(0, DIMENSIONS, GL_FLOAT, GL_FALSE, DIMENSIONS * sizeof(float), NULL);
	glEnableVertexAttribArray(0);
}

int main(int argc, char** argv) {
	if (!glfwInit()) {return -1;}
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	appWindow = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Program 1 - Nicholas Reel", NULL, NULL);
	glfwMakeContextCurrent(appWindow);
	if (glewInit() != GLEW_OK) { exit(EXIT_FAILURE); }
	glfwSwapInterval(1);

	// Initialize vertices before they are piped into buffers
	initPolygon();

	init(appWindow);
	shaderInit();

	//Infinite Loop
	while (!glfwWindowShouldClose(appWindow)) {
		// TODO: Should be callback???
		//onIdle();

		onDisplay(appWindow);

		// TODO: Maybe keep behind `displayPolygon()`?
		//display the buffer
		glfwSwapBuffers(appWindow);

		glfwPollEvents();
	}

	glfwDestroyWindow(appWindow);
	glfwTerminate();
	return EXIT_SUCCESS;
}
