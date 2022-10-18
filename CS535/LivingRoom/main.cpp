/**
*
* This program displays a simple scene of a living room using custom primitive types.
* Isaac Hands
*
**/
using namespace std;

#include <iostream>
#include <stack>

#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>

#include "Utils.h"
#include "Cone.h"
#include "Cube.h"
#include "Cylinder.h"
#include "Sphere.h"

double ScaleFactor = 1.4;
double FanSpeed = 10;

const double EYE_STEP = 0.5;
const double EYE_RANGE = 20;

const double EYE_X_DEFAULT = 4;
const double EYE_X_MIN = EYE_X_DEFAULT - EYE_RANGE;
const double EYE_X_MAX = EYE_X_DEFAULT + EYE_RANGE;

const double EYE_Z_DEFAULT = 4;
const double EYE_Z_MIN = EYE_Z_DEFAULT - EYE_RANGE;
const double EYE_Z_MAX = EYE_Z_DEFAULT + EYE_RANGE;

const double EYE_Y_DEFAULT = 2;
const double EYE_Y_MIN = EYE_Y_DEFAULT - EYE_RANGE;
const double EYE_Y_MAX = EYE_Y_DEFAULT + EYE_RANGE;

double EyeX = EYE_X_DEFAULT;
double EyeZ = EYE_Z_DEFAULT;
double EyeY = EYE_Y_DEFAULT;

const float WINDOW_WIDTH = 800;
const float WINDOW_HEIGHT = 600;
const float WINDOW_ASPECT_RATIO = WINDOW_WIDTH / WINDOW_HEIGHT;

stack<glm::mat4> modelViewStack;

void specialKey(int key, int x, int y)
{
    int mod = glutGetModifiers();

    switch (key) {
    case GLUT_KEY_LEFT:
        if (mod == GLUT_ACTIVE_SHIFT) {
            EyeX = EYE_X_DEFAULT;
        }
        else {
            EyeX -= EYE_STEP;
            if (EyeX < EYE_X_MIN)
                EyeX = EYE_X_MIN;
        }
        break;
    case GLUT_KEY_RIGHT:
        if (mod == GLUT_ACTIVE_SHIFT) {
            EyeX = EYE_X_DEFAULT;
        }
        else {
            EyeX += EYE_STEP;
            if (EyeX > EYE_X_MAX)
                EyeX = EYE_X_MAX;
        }
        break;
    case GLUT_KEY_UP:
        if (mod == GLUT_ACTIVE_SHIFT) {
            EyeY = EYE_Y_DEFAULT;
        }
        else {
            EyeY -= EYE_STEP;
            if (EyeY < EYE_Y_MIN)
                EyeY = EYE_Y_MIN;
        }
        break;
    case GLUT_KEY_DOWN:
        if (mod == GLUT_ACTIVE_SHIFT) {
            EyeY = EYE_Y_DEFAULT;
        }
        else {
            EyeY += EYE_STEP;

            if (EyeY > EYE_Y_MAX)
                EyeY = EYE_Y_MAX;
        }
        break;
    default:
        break;
    }

}

void onKeyboard(unsigned char key, int x, int y) {//Handles keyboard events

    switch (key) {
    case 27:  //Typed the Escape key, so exit.
        exit(0);
        break;
    case 83:
        ScaleFactor += 0.1;
        if (ScaleFactor > 2.0)
            ScaleFactor = 2.0;
        break;
    case 115:
        ScaleFactor -= 0.1;
        if (ScaleFactor < 0.5)
            ScaleFactor = 0.5;
        break;
    case '+':
    case '=':
    {
        FanSpeed += 1.0;
        if (FanSpeed > 100.0)
            FanSpeed = 100.0;

        break;
    }

    case '-':
    case '_':
    {
        FanSpeed -= 1.0;
        if (FanSpeed < 1.0)
            FanSpeed = 1.0;

        break;
    }
    default:
        break;
    }
}

// ------------- Rendering properties ------------ //
GLuint VAO; // Single array object that all shapes will share. Each will have their own buffer object
GLint modelViewLoc, projLoc;

// ------------- Rendering shapes ------------ //
Cube floorCube;
Cube wallCube;
Cube sofaCube;
Cube coffeeTableCube;
Cube endTableCube;
Cube chinaCabinetCube;
Sphere doorKnobSphere;
Cylinder fanCylinder;
Sphere fanSphere;
Cylinder lampCylinder;
Cone lampCone;

// ------------- Render the floor ------------ //
GLfloat floor_ambient[] = { 0.4f, 0.2f, 0.0f, 1.0f };
GLfloat floor_diffuse[] = { 0.4f, 0.3f, 0.2f, 1.0f };
GLfloat floor_specular[] = { 0.0f, 0.0f, 0.0f, 1.0f };
void drawFloor(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, floor_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, floor_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, floor_specular);

    // Apply scaling to view and apply
    glm::mat4 modelMatrix = glm::scale(glm::mat4(1.0f), glm::vec3(20.0, 20.0, 20.0));
    glm::mat4 mvMatrix = modelMatrix * modelViewStack.top();
    //modelViewStack.push(modelViewStack.top());
    //modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(2.0, 0.03, 2.0));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(mvMatrix));
    floorCube.render();
    //modelViewStack.pop();
}

// --------------  Render the walls --------------------
GLfloat walls_ambient[] = { 0.3f, 0.3f, 0.2f, 1.0f };
GLfloat walls_diffuse[] = { 0.3f, 0.3f, 0.4f, 1.0f };
GLfloat walls_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat walls_shininess[] = { 20.0f };
void drawWalls() {

    glMaterialfv(GL_FRONT, GL_AMBIENT, walls_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, walls_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, walls_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, walls_shininess);

    glPushMatrix();
    glTranslated(0.0, 0.5, -1.0);
    glScaled(2.0, 1.0, 0.03);
    wallCube.render();
    glPopMatrix();
    glPushMatrix();
    glTranslated(-1, 0.5, 0);
    glRotated(90, 0, 1, 0);
    glScaled(2.0, 1.0, 0.03);
    wallCube.render();
    glPopMatrix();
}

// ---------------- Render the sofa --------- //
GLfloat sofa_ambient[] = { 0.2f, 0.075f, 0.0f, 1.0f };
GLfloat sofa_diffuse[] = { 0.5f, 0.5f, 0.5f, 1.0f };
GLfloat sofa_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat sofa_shininess[] = { 20.0f };
void drawSofa() {
    glMaterialfv(GL_FRONT, GL_AMBIENT, sofa_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, sofa_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, sofa_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, sofa_shininess);

    glPushMatrix();
    glTranslated(0.2, 0.1, -.75);
    glRotated(90, 0, 1, 0);
    glScaled(0.5, 0.2, 1.2);
    sofaCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(0.2, 0.2, -.95);
    glRotated(90, 1, 0, 0);
    glScaled(1.2, 0.1, .45);
    sofaCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(0.75, 0.25, -.75);
    glRotated(90, 0, 1, 0);
    glScaled(.35, 0.1, .10);
    sofaCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-.35, 0.25, -.75);
    glRotated(90, 0, 1, 0);
    glScaled(.35, 0.1, .10);
    sofaCube.render();
    glPopMatrix();
}

GLfloat table_ambient[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_diffuse[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_specular[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_shininess[] = { 10.0f };
void drawCoffeeTable() {
    glMaterialfv(GL_FRONT, GL_AMBIENT, table_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, table_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, table_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, table_shininess);

    glPushMatrix();
    glTranslated(0.2, 0.25, 0);
    glRotated(90, 0, 1, 0);
    glScaled(0.4, 0.03, 1.);
    coffeeTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.25, 0.125, 0.15);
    glScaled(0.06, 0.25, 0.06);
    coffeeTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(0.65, 0.125, 0.15);
    glScaled(0.06, 0.25, 0.06);
    coffeeTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.25, 0.125, -0.15);
    glScaled(0.06, 0.25, 0.06);
    coffeeTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(0.65, 0.125, -0.15);
    glScaled(0.06, 0.25, 0.06);
    coffeeTableCube.render();
    glPopMatrix();


}

void drawEndTable() {
    glMaterialfv(GL_FRONT, GL_AMBIENT, table_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, table_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, table_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, table_shininess);

    glPushMatrix();
    glTranslated(-0.65, 0.20, -0.7);
    glRotated(90, 0, 1, 0);
    glScaled(0.4, 0.03, 0.4);
    endTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.48, 0.10, -0.55);
    glScaled(0.03, 0.22, 0.03);
    endTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.48, 0.10, -0.85);
    glScaled(0.03, 0.22, 0.03);
    endTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.82, 0.10, -0.85);
    glScaled(0.03, 0.22, 0.03);
    endTableCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.82, 0.10, -0.55);
    glScaled(0.03, 0.22, 0.03);
    endTableCube.render();
    glPopMatrix();
}

void drawChinaCabinet() {
    glMaterialfv(GL_FRONT, GL_AMBIENT, table_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, table_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, table_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, table_shininess);

    //draw bottom half
    glPushMatrix();
    glTranslated(-.85, .25, 0.5);
    glScaled(0.25, .30, 0.5);
    chinaCabinetCube.render();
    glPopMatrix();

    //daw top half
    glPushMatrix();
    glTranslated(-.87, .55, 0.5);
    glScaled(0.23, .30, 0.45);
    chinaCabinetCube.render();
    glPopMatrix();

    //Draw top knobs
    glPushMatrix();
    glTranslated(-.75, .55, 0.5);
    glScaled(0.02, 0.02, 0.02);
    doorKnobSphere.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-.75, .55, .45);
    glScaled(0.02, 0.02, 0.02);
    doorKnobSphere.render();
    glPopMatrix();

    //Draw bottom knobs
    glPushMatrix();
    glTranslated(-.70, .33, 0.67);
    glScaled(0.02, 0.02, 0.04);
    doorKnobSphere.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-.70, .33, .34);
    glScaled(0.02, 0.02, 0.04);
    doorKnobSphere.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-.70, .21, 0.67);
    glScaled(0.02, 0.02, 0.04);
    doorKnobSphere.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-.70, .21, .34);
    glScaled(0.02, 0.02, 0.04);
    doorKnobSphere.render();
    glPopMatrix();

    //Draw legs
    glPushMatrix();
    glTranslated(-0.75, 0.045, 0.30);
    glScaled(0.03, 0.12, 0.03);
    chinaCabinetCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.95, 0.045, 0.30);
    glScaled(0.03, 0.12, 0.03);
    chinaCabinetCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.75, 0.045, 0.70);
    glScaled(0.03, 0.12, 0.03);
    chinaCabinetCube.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.95, 0.045, 0.70);
    glScaled(0.03, 0.12, 0.03);
    chinaCabinetCube.render();
    glPopMatrix();

}

GLfloat fan_ambient[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_diffuse[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_specular[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_shininess[] = { 50.0f };
float fanAngle = 0.0;
const float ANGLE_STEP = 2 * 3.14 / 10;
void drawFan() {
    glMaterialfv(GL_FRONT, GL_AMBIENT, fan_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, fan_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, fan_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, fan_shininess);

    glPushMatrix();
    glTranslated(0.8, 0.03, 0.8);
    glScaled(0.08, 0.02, 0.08);
    glRotatef(90, 1, 0, 0);
    fanCylinder.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(0.8, 0.03, 0.8);
    glScaled(0.02, -0.5, 0.02);
    glRotatef(90, 1, 0, 0);
    fanCylinder.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(0.8, 0.5, 0.8);
    glRotatef(25, 0, 1, 0);
    glScaled(0.04, 0.04, 0.04);
    fanSphere.render();

    glPushMatrix();
    float currentAngle = fanAngle;
    for (int i = 0; i < 3; i++) {
        glPushMatrix();
        glRotated(currentAngle, 0, 0, 1);
        glTranslated(3.0, 0, -.8);
        glScaled(3.5, 0.5, 0.05);
        fanSphere.render();
        glPopMatrix();
        currentAngle += 360.0 / 3.0;
    }
    glPopMatrix();
    glPopMatrix();

    fanAngle += ANGLE_STEP * FanSpeed;
}

GLfloat lamp_base_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat lamp_base_diffuse[] = { 0.1f, 0.1f, 0.1f, 1.0f };
GLfloat lamp_base_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat lamp_base_shininess[] = { 100.0f };
GLfloat lamp_shade_ambient[] = { 0.2f, 0.2f, 0.0f, 1.0f };
GLfloat lamp_shade_diffuse[] = { 1.0f, 1.0f, 0.6f, 1.0f };
GLfloat lamp_shade_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat lamp_shade_shininess[] = { 50.0f };
void drawLamp() {
    glMaterialfv(GL_FRONT, GL_AMBIENT, lamp_base_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, lamp_base_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, lamp_base_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, lamp_base_shininess);
    glPushMatrix();
    glTranslated(-0.65, 0.25, -0.7);
    glScaled(0.08, 0.02, 0.08);
    glRotatef(90, 1, 0, 0);
    lampCylinder.render();
    glPopMatrix();

    glPushMatrix();
    glTranslated(-0.65, 0.50, -0.7);
    glScaled(0.02, 0.25, 0.02);
    glRotatef(90, 1, 0, 0);
    lampCylinder.render();
    glPopMatrix();

    glMaterialfv(GL_FRONT, GL_AMBIENT, lamp_shade_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, lamp_shade_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, lamp_shade_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, lamp_shade_shininess);
    glPushMatrix();
    glTranslated(-0.65, 0.50, -0.7);
    glScaled(0.18, 0.20, 0.18);
    glRotatef(-90, 1, 0, 0);
    lampCone.render();
    glPopMatrix();
}

// This method is the main rendering function for the scene.  It is called every frame
// to allow animation of the fan.
void displayScene(GLuint renderingProgram) {
    // TODO: Move lighting to shader
    // set the light source properties
    //GLfloat lightIntensity[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    //GLfloat light_position[] = { 0.0f, 0.8f, 1.0f, 1.0f };
    //glLightfv(GL_LIGHT0, GL_POSITION, light_position);
    //glLightfv(GL_LIGHT0, GL_DIFFUSE, lightIntensity);
    //glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, 0.5);

    //GLfloat ceilingIntensity[] = { 0.8f, 0.8f, 0.8f, 1.0f };
    //GLfloat ceiling_light_position[] = { 0.0f, 6.0f, 0.0f, 0.0f };
    //glLightfv(GL_LIGHT1, GL_POSITION, ceiling_light_position);
    //glLightfv(GL_LIGHT1, GL_DIFFUSE, ceilingIntensity);

    //glMatrixMode(GL_MODELVIEW);      // position and aim the camera

    glEnable(GL_DEPTH_TEST);
    glCullFace(GL_BACK);

    //glShadeModel(GL_SMOOTH);

    glUseProgram(renderingProgram);
    modelViewLoc = glGetUniformLocation(renderingProgram, "mv_matrix");
	projLoc = glGetUniformLocation(renderingProgram, "proj_matrix");

    // Build matrices for shader computation
    double winHt = 1.0;  //half-height of the window
    glm::mat4 projMatrix = glm::ortho(-winHt * WINDOW_ASPECT_RATIO * ScaleFactor, winHt * WINDOW_ASPECT_RATIO * ScaleFactor, -winHt * ScaleFactor, winHt * ScaleFactor, 0.1, 100.0);
    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projMatrix));

    gluLookAt(EyeX, EyeY, EyeZ, 0.0, 0.3, 0.0, 0.0, 1.0, 0.0);
    glm::mat4 viewMatrix = glm::lookAt(glm::vec3(EyeX, EyeY, EyeZ), glm::vec3(0.0, 0.5, 0.0), glm::vec3(0.0, 1.0, 0.0));
    modelViewStack.push(viewMatrix);

    drawFloor(modelViewLoc);
    //drawWalls();
    //drawSofa();
    //drawCoffeeTable();
    //drawFan();
    //drawChinaCabinet();
    //drawEndTable();
    //drawLamp();

    modelViewStack.pop(); // For view matrix
}

GLuint init(GLFWwindow* appWindow) {
    GLuint renderingProgram = Utils::createShaderProgram("vertShader.glsl", "fragShader.glsl");
    int width = WINDOW_WIDTH;
    int height = WINDOW_HEIGHT;
    glfwGetFramebufferSize(appWindow, &width, &height);

    floorCube = Cube(1.0);
    //wallCube = Cube(1.0);
    //sofaCube = Cube(1.0);
    //coffeeTableCube = Cube(1.0);
    //endTableCube = Cube(1.0);
    //chinaCabinetCube = Cube(1.0);
    //doorKnobSphere= Sphere(1.0, 20, 20);
    //fanCylinder = Cylinder(1.0, 1.0, 20, 10);
    //fanSphere = Sphere(1.0, 20, 20);
    //lampCylinder = Cylinder(1.0, 1.0, 20, 10);
    //lampCone = Cone(1.0, 1.0, 5, 10);

    return renderingProgram;
}

int main(int, char**) {
    // GLFW initialization
    if (!glfwInit()) { return -1; }
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    GLFWwindow* appWindow = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Program 2 - Nicholas Reel", NULL, NULL);
    glfwMakeContextCurrent(appWindow);
    if (glewInit() != GLEW_OK) { exit(EXIT_FAILURE); }
    glfwSwapInterval(1); // vsync enabled

    GLuint shaderProgram = init(appWindow);

    // Lighting information
    //glEnable(GL_LIGHTING);
    //glEnable(GL_LIGHT0);
    //glEnable(GL_LIGHT1);
    //glEnable(GL_NORMALIZE);
    glClearColor(0.05f, 0.05f, 0.2f, 0.0f);  // background is light gray
    glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
   
    //glutKeyboardFunc(onKeyboard);
    //glutSpecialFunc(specialKey);

    glUseProgram(shaderProgram);

    while (!glfwWindowShouldClose(appWindow)) {
        // Set refresh buffers
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearColor(0.05f, 0.05f, 0.2f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        displayScene(shaderProgram);

        //display the buffer
        glfwSwapBuffers(appWindow);

        glfwPollEvents();
    }
    
    glfwDestroyWindow(appWindow);
    glfwTerminate();
    exit(EXIT_SUCCESS);
}