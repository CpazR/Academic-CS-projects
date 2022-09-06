
//*******************************************************************************************************
// Tim Meyers
// CS535
// Programming Assignment 1
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

//#include <X11/Xlib.h>
//#include <GLUT/glut.h>
//#include <OpenGL/gl.h>
#include <GL/glut.h>
//#include <GL/GL.h>
//#include <GL/GLU.h>
#include <iostream>
#include <sstream>
#include <vector>
#include <math.h>

using namespace std;

//Global constants
const GLint WINDOW_WIDTH = 640 - 1;//The -1 is to not be directly against the window's edge.
const GLint WINDOW_HEIGHT = 480 - 1;//The -1 is to not be directly against the window's edge.
const GLfloat PI = 3.14159265;
//These next two constants should be changed depending on the graphical abilities
//	of the user's system. The higher the dampener, the slower the movement/rotation.
const GLfloat speedDamp = 600;//Dampening effect on the translation speed
const GLfloat rotationDamp = 2800;//Dampening effect on the rotation speed

//State variables
GLint B_WIDTH = WINDOW_WIDTH / 8;
GLint B_HEIGHT = WINDOW_HEIGHT / 15;

//A simple struct defined to hold a 2-D point.
struct Point2D
{
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
struct Rect2D
{
	Point2D bottomleft;
	Point2D topright;
	Rect2D() { bottomleft = Point2D();  topright = Point2D(); }
	Rect2D(Point2D bl, Point2D tr)
	{
		bottomleft = bl;
		topright = tr;
	}
	void operator=(const Rect2D& r)
	{
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

//*************************************************************************************
//Name: calcMidpoints
//Description: Recalculates the midpoints of the polygon's edges.
//Function calls: vector::clear(), vector::size(), vector::push_back, Point2D constructor
//Preconditions: vector p is not empty, and defines a polygon
//Postconditions: The vector midpoints contains the set of midpoints for the
//		polygon's edges.
//Parameters: vector<Point2D> p, the polygon to process
//Returns: Nothing
void calcMidpoints(vector<Point2D> p)
{
	//Calculate the midpoints of the edges
	midpoints.clear();
	for (GLint j = 0; j < p.size(); j++)
	{
		GLfloat midX = (p.at(j % p.size()).x + p.at((j + 1) % p.size()).x) / 2;
		GLfloat midY = (p.at(j % p.size()).y + p.at((j + 1) % p.size()).y) / 2;
		midpoints.push_back(Point2D(midX, midY));
	}
}

//*************************************************************************************
//Name: initialize
//Description: Initializes various variables and states
//Function calls: glClearColor, glColor3f, glMatrixMode, glLoadIdentity, gluOrtho2D,
//	vector::push_back, Point2D constructor, calcMidpoints
//Preconditions: None
//Postconditions: The program is initialized
//Parameters: None
//Returns: Nothing
void initialize()
{
	//Performs certain window initalizations
	glClearColor(1.0, 1.0, 1.0, 0.0);   // set background color to be white
	glColor3f(0.0f, 0.0f, 0.0f);       // set the drawing color to be black
	glMatrixMode(GL_PROJECTION);       // set "camera shape"
	glLoadIdentity();                    // clearing the viewing matrix
	gluOrtho2D(0.0, 640.0, 0.0, 480.0);  // setting the world window to be 640 by 480

	//Set the button vertices
	buttons.push_back(Rect2D());//Dummy button
	for (GLint i = 1; i <= 4; i++)
	{
		buttons.push_back(Rect2D(
			Point2D(i * WINDOW_WIDTH / 5 - B_WIDTH / 2, 11 * WINDOW_HEIGHT / 12 - B_HEIGHT / 2),
			Point2D(i * WINDOW_WIDTH / 5 + B_WIDTH / 2, 11 * WINDOW_HEIGHT / 12 + B_HEIGHT / 2)));
	}

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

	//Initialize the midpoints
	calcMidpoints(polygon);
	clipPolygon = vector<Point2D>(polygon);//Copy the polygon
}

//*************************************************************************************
//Name: drawButton
//Description: Draws a button for the menu
//Function calls: glColor3f, glRectf, glColor3d, glRasterPos2f, glutBitmapCharacter
//Preconditions: buttonNum must be between 1 and 4, inclusively
//Postconditions: The button is drawn to the screen
//Parameters: GLint buttonNum, the number of the button
//Returns: Nothing
void drawButton(GLint buttonNum)
{
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
	glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, *os.str().c_str());
}


//*************************************************************************************
//Name: drawButtonBar
//Description: Draws the menu at the top of the window, with 4 buttons
//Function calls: glColor3f, glRectf, drawButton
//Preconditions: None
//Postconditions: The menu is drawn
//Parameters: None
//Returns: Nothing
void drawButtonBar()
{
	glColor3f(.5, .5, .5);//Set to grey
	glRectf(0.0f, 5 * WINDOW_HEIGHT / 6, WINDOW_WIDTH, WINDOW_HEIGHT);//Drawing the bar background

	for (GLint i = 1; i <= 4; i++)
		drawButton(i);

	glColor3f(0.0f, 0.0f, 0.0f); // Reset the drawing color to be black
}

//*************************************************************************************
//Name: displayText
//Description: A general use function to draw given text to the viewing area.
//Function calls: glRaster2f, glColor3f, glutBitmapCharacter
//Preconditions: ostringstream os is not empty
//Postconditions: The text is displayed to the viewing area
//Parameters: ostringstream os, the text to display
//Returns: Nothing
void displayText(ostringstream& os)
{
	string output = os.str();
	glRasterPos2f(WINDOW_WIDTH / 25, 5 * WINDOW_HEIGHT / 6 - 12);//Reset position
	glColor3f(0.0f, 0.0f, 0.0f); //Black text
	for (GLint j = 0; j < output.length(); j++)
	{
		glutBitmapCharacter(GLUT_BITMAP_HELVETICA_12, output[j]);
	}
}

//*************************************************************************************
//Name: bezierCurve
//Description: A mathematical function to calculate a degree-2 Bezier curve's value
//Function calls: Point2D constructor
//Preconditions: A, B, C, and t are all valid; t is in [0,1]
//Postconditions: None
//Parameters: Point2D A, B, C, points that define the curve
//				GLfloat t, the value to plug into the equation
//Returns: Point2D, the result of the eqaution.
Point2D bezierCurve(Point2D A, Point2D B, Point2D C, GLfloat t)
{
	GLfloat fx = pow(1 - t, 2) * A.x + 2 * t * (1 - t) * B.x + pow(t, 2) * C.x;
	GLfloat fy = pow(1 - t, 2) * A.y + 2 * t * (1 - t) * B.y + pow(t, 2) * C.y;
	return Point2D(fx, fy);
}

//*************************************************************************************
//Name: findIntersect
//Description: Finds the intersection point between the line AB and a viewing area
//				border, and returns it. If no intersection exists, (99999, 99999) is
//				returned.
//Function calls: Point2D constructor
//Preconditions: A and B must be valid points
//Postconditions: None
//Parameters: Point2D A, B, points that define a line
//Returns: Point2D that represents the intersection, or no intersection
Point2D findIntersect(Point2D A, Point2D B)
{
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


//*************************************************************************************
//Name: clip
//Description: Clips the polygon defined by rotatedPolygon against the viewing area.
//Function calls: vector::size, vector::at, Point2D constructor, findIntersect,
//					vector::push_back
//Preconditions: rotatedPolygon must be a valid polygon (list of vertices)
//Postconditions: clipPolygon contains a set of vertices that represents the clipped
//					polygon
//Parameters: None
//Returns: bool indicating that the polygon has been clipped
//Notes: This function is based on the Weiler-Atherton algorithm for polygon clipping.
bool clip()
{
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

//*************************************************************************************
//Name: displayPolygon
//Description: Draws the polygon in the viewing area
//Function calls: glColor3f, vector::clear, vector::size, cos, sin, vector::push_back,
//	Point2D constructor, clip, vector::at, glBegin, glEnd, bezierCurve, glVertex2f
//Preconditions: centroid is a valid point, directionVector is valid, rotationSpeed
//	is valid, polygon represents a set of vertices
//Postconditions: The polygon is drawn to the viewing area
//Parameters: None
//Returns: Nothing
void displayPolygon()
{
	glColor3f(0, 0, 0);
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

//*************************************************************************************
//Name: onDisplay
//Description: The display callback
//Function calls: glClear, drawButtonBar, displayText, displayPolygon, glutSwapBuffers
//Preconditions: None
//Postconditions: The contents of the window are shown (button menu, viewing area,
//	polygon, text output)
//Parameters: None
//Returns: Nothing
void onDisplay()
{
	glClear(GL_COLOR_BUFFER_BIT); //clearing the buffer

	drawButtonBar();
	displayText(textOutput);
	if (showPolygon)
		displayPolygon();

	glutSwapBuffers();//display the buffer
}

//*************************************************************************************
//Name: onKeyboard
//Description: The keyboard event callback
//Function calls: exit
//Preconditions: A key was hit
//Postconditions: The key event is handles
//Parameters: unsigned char key, the key hit
//				int x, y, the current position
//Returns: Nothing
void onKeyboard(unsigned char key, int x, int y)
{//Handles keyboard events
	switch (key)
	{
	case 27:  //Typed the Escape key, so exit.
		exit(0);
		break;
	default:
		break;
	}
}

//*************************************************************************************
//Name: onIdle
//Description: The idle event callback (used for animation)
//Function calls: glutPostRedisplay
//Preconditions: None
//Postconditions: The gloabl variables theta and centoid are updated
//Parameters: None
//Returns: Nothing
void onIdle()
{
	if (inputState == 0)
	{
		theta += 1 / (2 * PI);//Add 1 radian
		centroid.x += directionSpeed / speedDamp * (directionVector.x / directionSpeed);
		centroid.y += directionSpeed / speedDamp * (directionVector.y / directionSpeed);
	}

	glutPostRedisplay();
}

//*************************************************************************************
//Name: acceptInputs
//Description: A FSM to accept data
//Function calls: Point2D constrctuor
//Preconditions: x and y are valid points
//Postconditions: Text is ouput, state variables are updated
//Parameters: GLint x, y, the mouise position
//Returns: Nothing
void acceptInputs(GLint x, GLint y)
{
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

//*************************************************************************************
//Name: getButtonPushed
//Description: Finds which button was pushed
//Function calls: vector::at, glutIdleFunc, acceptInputs
//Preconditions: x and y are valid points, vector buttons was initialized
//Postconditions: The appropriate button's behavior is handled
//Parameters: GLint x, y, the mouise position
//Returns: GLint, the button that was pushed
GLint getButtonPushed(GLint x, GLint y)
{
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
		glutIdleFunc(NULL);//Stop animation while accepting input.
	}
	else if ((x >= buttons.at(3).bottomleft.x && x <= buttons.at(3).topright.x)
		&& (y >= (buttons.at(3).bottomleft.y) && y <= (buttons.at(3).topright.y)))
	{
		glutIdleFunc(onIdle);
		textOutput << "Animation Started";
		inputState = 0;
		button = 3;
	}
	else if ((x >= buttons.at(4).bottomleft.x && x <= buttons.at(4).topright.x)
		&& (y >= (buttons.at(4).bottomleft.y) && y <= (buttons.at(4).topright.y)))
	{
		glutIdleFunc(NULL);
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

//*************************************************************************************
//Name: onMouse
//Description: The mouse event callback
//Function calls: getButtonPushed, glutPostResdisplay
//Preconditions: x,y are a valid point
//Postconditions: The mouse event is handled
//Parameters: GLint x, y, the mouise position
//				button, the mouse buttons used
//				state, the properties of the mouse
//Returns: Nothing
void onMouse(int button, int state, int x, int y)
{
	GLint menuButton = 1;
	if (button == GLUT_LEFT_BUTTON && state == GLUT_DOWN)
		menuButton = getButtonPushed(x, y);

	glutPostRedisplay();
}

//*************************************************************************************
//Name: main
//Description: The main function
//Function calls: glutInit, glutInitDisplayMode, glutInitWindowSize, glutInitWindowPosition,
//		glutCreateWindow, initialize, glutKeyboardFunc, glutMouseFunc, glutDisplayFunc,
//		glutIdleFunc, glutMainLoop
//Preconditions: None
//Postconditions: The program runs in an infinite loop, for displaying grapics.
//					An OpenGL graphics window is displayed
//Parameters: int argc, the number of command line arguments
//				char** argv, the command line arguments
//Returns: int, the exit code
int main(int argc, char** argv)
{
	//Initialization functions
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB);
	glutInitWindowSize(640, 480);
	glutInitWindowPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
	glutCreateWindow("Program 1");
	initialize();

	//Call-back functions
	glutDisplayFunc(onDisplay);
	glutKeyboardFunc(onKeyboard);
	glutMouseFunc(onMouse);
	//glutIdleFunc(onIdle);

	//Infinite Loop
	glutMainLoop();
	return 0;
}

//End of Program1_Meyers.cpp
