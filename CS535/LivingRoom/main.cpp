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

double ScaleFactor = 1.7;
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
float aspectRatio;

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
GLuint globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
glm::mat4 viewMatrix, projMatrix;
stack<glm::mat4> modelViewStack;

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
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(2.0, 0.03, 2.0));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    floorCube.render();
    modelViewStack.pop();
}

// --------------  Render the walls --------------------
GLfloat walls_ambient[] = { 0.3f, 0.3f, 0.2f, 1.0f };
GLfloat walls_diffuse[] = { 0.3f, 0.3f, 0.4f, 1.0f };
GLfloat walls_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat walls_shininess[] = { 20.0f };
void drawWalls(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, walls_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, walls_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, walls_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, walls_shininess);

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0, 0.5, -1.0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(2.0, 1.0, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    wallCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-1.0, 0.5, 0));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(2.0, 1.0, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    wallCube.render();
    modelViewStack.pop();
}

// ---------------- Render the sofa --------- //
GLfloat sofa_ambient[] = { 0.2f, 0.075f, 0.0f, 1.0f };
GLfloat sofa_diffuse[] = { 0.5f, 0.5f, 0.5f, 1.0f };
GLfloat sofa_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat sofa_shininess[] = { 20.0f };
void drawSofa(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, sofa_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, sofa_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, sofa_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, sofa_shininess);

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.2, 0.1, -.75));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.5, 0.2, 1.2));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    sofaCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.2, 0.2, -.95));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(1.2, 0.1, .45));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    sofaCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.75, 0.25, -.75));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(.35, 0.1, .10));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));

    sofaCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.35, 0.25, -.75));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(.35, 0.1, .10));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));

    sofaCube.render();
    modelViewStack.pop();
}

GLfloat table_ambient[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_diffuse[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_specular[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_shininess[] = { 10.0f };
void drawCoffeeTable(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, table_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, table_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, table_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, table_shininess);

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.2, 0.25, 0));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.4, 0.03, 1.));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    coffeeTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.25, 0.125, 0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    coffeeTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.65, 0.125, 0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    coffeeTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.25, 0.125, -0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    coffeeTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.65, 0.125, -0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    coffeeTableCube.render();
    modelViewStack.pop();


}

void drawEndTable(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, table_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, table_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, table_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, table_shininess);

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.65, 0.20, -0.7));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.4, 0.03, 0.4));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    endTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.48, 0.10, -0.55));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    endTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.48, 0.10, -0.85));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    endTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.82, 0.10, -0.85));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    endTableCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.82, 0.10, -0.55));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    endTableCube.render();
    modelViewStack.pop();
}

void drawChinaCabinet(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, table_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, table_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, table_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, table_shininess);

    //draw bottom half
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.85, .25, 0.5));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.25, .30, 0.5));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    chinaCabinetCube.render();
    modelViewStack.pop();

    //daw top half
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.87, .55, 0.5));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.23, .30, 0.45));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    chinaCabinetCube.render();
    modelViewStack.pop();

    //Draw top knobs
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.75, .55, 0.5));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.02));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    doorKnobSphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.75, .55, .45));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.02));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    doorKnobSphere.render();
    modelViewStack.pop();

    //Draw bottom knobs
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .33, 0.67));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    doorKnobSphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .33, .34));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    doorKnobSphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .21, 0.67));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    doorKnobSphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .21, .34));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    doorKnobSphere.render();
    modelViewStack.pop();

    //Draw legs
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.75, 0.045, 0.30));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    chinaCabinetCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.95, 0.045, 0.30));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    chinaCabinetCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.75, 0.045, 0.70));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    chinaCabinetCube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.95, 0.045, 0.70));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    glUniformMatrix4fv(mvLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    chinaCabinetCube.render();
    modelViewStack.pop();

}

GLfloat fan_ambient[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_diffuse[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_specular[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_shininess[] = { 50.0f };
float fanAngle = 0.0;
const float ANGLE_STEP = 2 * 3.14 / 10;
void drawFan(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, fan_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, fan_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, fan_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, fan_shininess);

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.8, 0.03, 0.8));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.08, 0.02, 0.08));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    glUniformMatrix4fv(modelViewLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    fanCylinder.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.8, 0.03, 0.8));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, -0.5, 0.02));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    glUniformMatrix4fv(modelViewLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    fanCylinder.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.8, 0.5, 0.8));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(25.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.04, 0.04, 0.04));
    glUniformMatrix4fv(modelViewLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
    fanSphere.render();

    modelViewStack.push(modelViewStack.top());
    float currentAngle = fanAngle;
    for (int i = 0; i < 3; i++) {
        modelViewStack.push(modelViewStack.top());
        modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(currentAngle), glm::vec3(0, 0, 1));
        modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(3.0, 0, -.8));
        modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(3.5, 0.5, 0.05));
        glUniformMatrix4fv(modelViewLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));
        fanSphere.render();
        modelViewStack.pop();
        currentAngle += 360.0 / 3.0;
    }
    modelViewStack.pop();
    modelViewStack.pop();

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
void drawLamp(GLint mvLoc) {
    glMaterialfv(GL_FRONT, GL_AMBIENT, lamp_base_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, lamp_base_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, lamp_base_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, lamp_base_shininess);
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.65, 0.25, -0.7));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.08, 0.02, 0.08));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    lampCylinder.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.65, 0.50, -0.7));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.25, 0.02));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    lampCylinder.render();
    modelViewStack.pop();

    glMaterialfv(GL_FRONT, GL_AMBIENT, lamp_shade_ambient);
    glMaterialfv(GL_FRONT, GL_DIFFUSE, lamp_shade_diffuse);
    glMaterialfv(GL_FRONT, GL_SPECULAR, lamp_shade_specular);
    glMaterialfv(GL_FRONT, GL_SHININESS, lamp_shade_shininess);
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.65, 0.50, -0.7));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.18, 0.20, 0.18));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(-90.0f), glm::vec3(1, 0, 0));
    lampCone.render();
    modelViewStack.pop();
}

// This method is the main rendering function for the scene.  It is called every frame
// to allow animation of the fan.
void displayScene(GLuint renderingProgram) {
    // Set refresh buffers
    glClearColor(0.05f, 0.05f, 0.2f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glUseProgram(renderingProgram);

    modelViewLoc = glGetUniformLocation(renderingProgram, "mv_matrix");
	projLoc = glGetUniformLocation(renderingProgram, "proj_matrix");

    // Build matrices for shader computation
    viewMatrix = glm::lookAt(glm::vec3(4, 2, 4), glm::vec3(0.0, 0.3, 0.0), glm::vec3(0.0, 1.0, 0.0));
    modelViewStack.push(viewMatrix);

    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projMatrix));

	//modelViewStack.push(modelViewStack.top());
 //   modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), (float)glfwGetTime(), glm::vec3(0.0, 1.0, 0.0));
    /*glUniformMatrix4fv(modelViewLoc, 1, GL_FALSE, glm::value_ptr(modelViewStack.top()));*/
 //   doorKnobSphere.render();
 //   modelViewStack.pop();

    drawFloor(modelViewLoc);
    drawWalls(modelViewLoc);
    drawSofa(modelViewLoc);
    drawCoffeeTable(modelViewLoc);
    drawFan(modelViewLoc);
    drawChinaCabinet(modelViewLoc);
    drawEndTable(modelViewLoc);
    //drawLamp(modelViewLoc);

    modelViewStack.pop(); // For view matrix
}

GLuint init(GLFWwindow* appWindow) {
    GLuint renderingProgram = Utils::createShaderProgram("vertShader.glsl", "fragShader.glsl");
    int width = WINDOW_WIDTH;
    int height = WINDOW_HEIGHT;
    glfwGetFramebufferSize(appWindow, &width, &height);

    aspectRatio = width / height;
    double winHt = 1.0;  //half-height of the window
    projMatrix = glm::ortho(-winHt * aspectRatio * ScaleFactor, winHt * aspectRatio * ScaleFactor, -winHt * ScaleFactor, winHt * ScaleFactor, 0.1, 100.0);

    glGenVertexArrays(1, &VAO);
    glBindVertexArray(VAO);

    // TODO: Optimize by reducing to single instances of each shape if possible
    floorCube = Cube(1.0);
    wallCube = Cube(1.0);
    sofaCube = Cube(1.0);
    coffeeTableCube = Cube(1.0);
    endTableCube = Cube(1.0);
    chinaCabinetCube = Cube(1.0);
    doorKnobSphere = Sphere(1.0, 20, 20);
    //fanCylinder = Cylinder(1.0, 1.0, 20, 10);
    fanSphere = Sphere(1.0, 20, 20);
    //lampCylinder = Cylinder(1.0, 1.0, 20, 10);
    //lampCone = Cone(1.0, 1.0, 5, 10);

    return renderingProgram;
}

void windowSizeCallback(GLFWwindow* win, int newWidth, int newHeight) {
    aspectRatio = newWidth / newHeight;
    double winHt = 1.0;  //half-height of the window
    projMatrix = glm::ortho(-winHt * aspectRatio * ScaleFactor, winHt * aspectRatio * ScaleFactor, -winHt * ScaleFactor, winHt * ScaleFactor, 0.1, 100.0);
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

    glDisable(GL_CULL_FACE);


    glfwSetWindowSizeCallback(appWindow, windowSizeCallback);

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
        displayScene(shaderProgram);
        glfwSwapBuffers(appWindow);
        glfwPollEvents();
    }
    
    glfwDestroyWindow(appWindow);
    glfwTerminate();
    exit(EXIT_SUCCESS);
}