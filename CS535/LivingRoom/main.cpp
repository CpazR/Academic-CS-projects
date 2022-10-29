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

const int FAN_FIN_COUNT = 4;
double ScaleFactor = 1.7;
double FanSpeed = 2;

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

void keyboardListener(GLFWwindow* window, int key, int scancode, int action, int mods) {

    switch (key) {
    case GLFW_KEY_LEFT:
        if (mods == GLFW_MOD_SHIFT) {
            EyeX = EYE_X_DEFAULT;
        } else {
            EyeX -= EYE_STEP;
            if (EyeX < EYE_X_MIN)
                EyeX = EYE_X_MIN;
        }
        break;
    case GLFW_KEY_RIGHT:
        if (mods == GLFW_MOD_SHIFT) {
            EyeX = EYE_X_DEFAULT;
        } else {
            EyeX += EYE_STEP;
            if (EyeX > EYE_X_MAX)
                EyeX = EYE_X_MAX;
        }
        break;
    case GLFW_KEY_UP:
        if (mods == GLFW_MOD_SHIFT) {
            EyeY = EYE_Y_DEFAULT;
        } else {
            EyeY -= EYE_STEP;
            if (EyeY < EYE_Y_MIN)
                EyeY = EYE_Y_MIN;
        }
        break;
    case GLFW_KEY_DOWN:
        if (mods == GLFW_MOD_SHIFT) {
            EyeY = EYE_Y_DEFAULT;
        } else {
            EyeY += EYE_STEP;

            if (EyeY > EYE_Y_MAX)
                EyeY = EYE_Y_MAX;
        }
        break;
    case GLFW_KEY_ESCAPE:
        exit(0);
        break;
    case GLFW_KEY_MINUS:
        FanSpeed -= 1.0;
        if (FanSpeed < 1.0)
            FanSpeed = 1.0;
        break;
    case GLFW_KEY_EQUAL:
        FanSpeed += 1.0;
        if (FanSpeed > 100.0)
            FanSpeed = 100.0;
        break;
    case GLFW_KEY_D:
        ScaleFactor -= 0.1;
        if (ScaleFactor < 0.5)
            ScaleFactor = 0.5;
        break;
    case GLFW_KEY_S:
        ScaleFactor += 0.1;
        if (ScaleFactor > 2.0)
            ScaleFactor = 2.0;
        break;
    default:
        break;
    }

}

// ------------- Rendering properties ------------ //
GLuint VAO; // Single array object that all shapes will share. Each will have their own buffer object
GLint modelViewLoc, projLoc, normLoc;
GLuint globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
glm::mat4 viewMatrix, projMatrix, normMatrix;
stack<glm::mat4> modelViewStack;
glm::vec3 lightPos;
glm::vec3 lightPosition = glm::vec3(-5.0f, 3.0f, -5.0f);

// white light
float globalAmbient[4] = { 0.7f, 0.7f, 0.7f, 1.0f };
float lightAmbient[4] = { 0.0f, 0.0f, 0.0f, 1.0f };
float lightDiffuse[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
float lightSpecular[4] = { 1.0f, 1.0f, 1.0f, 1.0f };

// ------------- Rendering shapes ------------ //
Cube cube;
Sphere sphere;
Cylinder cylinder;
Cone cone;

void setMeshMaterial(GLuint renderingProgram, float* matAmb, float* matDif, float* matSpe, float matShi) {
    glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb);
    glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif);
    glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe);
    glProgramUniform1f(renderingProgram, mshiLoc, matShi);
}

void setMeshUniforms(glm::mat4 modelViewMat) {
    glUniformMatrix4fv(modelViewLoc, 1, GL_FALSE, glm::value_ptr(modelViewMat));

    glm::mat4 inverseTriNormMat = glm::transpose(glm::inverse(modelViewMat));
    glUniformMatrix4fv(normLoc, 1, GL_FALSE, glm::value_ptr(inverseTriNormMat));
}

void displayLights(GLuint renderingProgram, glm::mat4 viewMatrix) {
    lightPos = glm::vec3(viewMatrix * glm::vec4(lightPosition, 1.0f));

    // get the locations of the light and material fields in the shader
    globalAmbLoc = glGetUniformLocation(renderingProgram, "globalAmbient");
    ambLoc = glGetUniformLocation(renderingProgram, "light.ambient");
    diffLoc = glGetUniformLocation(renderingProgram, "light.diffuse");
    specLoc = glGetUniformLocation(renderingProgram, "light.specular");
    posLoc = glGetUniformLocation(renderingProgram, "light.position");
    mambLoc = glGetUniformLocation(renderingProgram, "material.ambient");
    mdiffLoc = glGetUniformLocation(renderingProgram, "material.diffuse");
    mspecLoc = glGetUniformLocation(renderingProgram, "material.specular");
    mshiLoc = glGetUniformLocation(renderingProgram, "material.shininess");

    //  set the uniform light and material values in the shader
    glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient);
    glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient);
    glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse);
    glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular);
    glProgramUniform3fv(renderingProgram, posLoc, 1, glm::value_ptr(lightPos));
}

// ------------- Render the floor ------------ //
GLfloat floor_ambient[] = { 0.4f, 0.2f, 0.0f, 1.0f };
GLfloat floor_diffuse[] = { 0.4f, 0.3f, 0.2f, 1.0f };
GLfloat floor_specular[] = { 0.0f, 0.0f, 0.0f, 1.0f };
void drawFloor() {

    // Apply scaling to view and apply
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(2.0, 0.03, 2.0));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();
}

// --------------  Render the walls --------------------
GLfloat walls_ambient[] = { 0.3f, 0.3f, 0.2f, 1.0f };
GLfloat walls_diffuse[] = { 0.3f, 0.3f, 0.4f, 1.0f };
GLfloat walls_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat walls_shininess = 20.0f;
void drawWalls() {
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0, 0.5, -1.0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(2.0, 1.0, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-1.0, 0.5, 0));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(2.0, 1.0, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();
}

// ---------------- Render the sofa --------- //
GLfloat sofa_ambient[] = { 0.2f, 0.075f, 0.0f, 1.0f };
GLfloat sofa_diffuse[] = { 0.5f, 0.5f, 0.5f, 1.0f };
GLfloat sofa_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat sofa_shininess = 20.0f;
void drawSofa() {
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.2, 0.1, -.75));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.5, 0.2, 1.2));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.2, 0.2, -.95));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(1.2, 0.1, .45));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.75, 0.25, -.75));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(.35, 0.1, .10));
    setMeshUniforms(modelViewStack.top());

    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.35, 0.25, -.75));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(.35, 0.1, .10));
    setMeshUniforms(modelViewStack.top());

    cube.render();
    modelViewStack.pop();
}

GLfloat table_ambient[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_diffuse[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_specular[] = { 1.0f, 0.5f, 0.0f, 0.0f };
GLfloat table_shininess = 10.0f;
void drawCoffeeTable() {
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.2, 0.25, 0));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.4, 0.03, 1.));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.25, 0.125, 0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.65, 0.125, 0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.25, 0.125, -0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(0.65, 0.125, -0.15));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.06, 0.25, 0.06));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();


}

void drawEndTable() {
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.65, 0.20, -0.7));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.4, 0.03, 0.4));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.48, 0.10, -0.55));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.48, 0.10, -0.85));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.82, 0.10, -0.85));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.82, 0.10, -0.55));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.22, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();
}

void drawChinaCabinet() {

    //draw bottom half
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.85, .25, 0.5));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.25, .30, 0.5));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    //daw top half
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.87, .55, 0.5));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.23, .30, 0.45));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    //Draw top knobs
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.75, .55, 0.5));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.02));
    setMeshUniforms(modelViewStack.top());
    sphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.75, .55, .45));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.02));
    setMeshUniforms(modelViewStack.top());
    sphere.render();
    modelViewStack.pop();

    //Draw bottom knobs
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .33, 0.67));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    setMeshUniforms(modelViewStack.top());
    sphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .33, .34));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    setMeshUniforms(modelViewStack.top());
    sphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .21, 0.67));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    setMeshUniforms(modelViewStack.top());
    sphere.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-.70, .21, .34));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.02, 0.04));
    setMeshUniforms(modelViewStack.top());
    sphere.render();
    modelViewStack.pop();

    //Draw legs
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.75, 0.045, 0.30));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.95, 0.045, 0.30));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.75, 0.045, 0.70));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.95, 0.045, 0.70));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.03, 0.12, 0.03));
    setMeshUniforms(modelViewStack.top());
    cube.render();
    modelViewStack.pop();

}

GLfloat fan_ambient[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_diffuse[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_specular[] = { 1.0f, 0.2f, 0.0f, 0.0f };
GLfloat fan_shininess = 50.0f;

float fanAngle = 0.0;
const float ANGLE_STEP = 2 * 3.14 / 10;
void drawFan(GLuint renderingProgram) {
    // Initial position
    glm::vec3 fanPosition = glm::vec3(-0.85, 0.03, -0.1);

    setMeshMaterial(renderingProgram, fan_ambient, fan_diffuse, fan_specular, fan_shininess);

    // Base
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), fanPosition);
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.08, 0.02, 0.08));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    setMeshUniforms(modelViewStack.top());
    cylinder.render();
    modelViewStack.pop();

    // Connecting rod
    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), fanPosition);
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, -0.5, 0.02));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    setMeshUniforms(modelViewStack.top());
    cylinder.render();
    modelViewStack.pop();

    // Sphere connecting fins
    modelViewStack.push(modelViewStack.top());
    fanPosition.y += 0.45f;
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), fanPosition);
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(96.0f), glm::vec3(0, 1, 0));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.04, 0.04, 0.04));
    setMeshUniforms(modelViewStack.top());
    sphere.render();

    // Fins
    setMeshMaterial(renderingProgram, Utils::silverAmbient(), Utils::silverDiffuse(), Utils::silverSpecular(), 100.0f);
    modelViewStack.push(modelViewStack.top());
    float currentAngle = fanAngle;
    for (int i = 0; i < FAN_FIN_COUNT; i++) {
        modelViewStack.push(modelViewStack.top());
        modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(currentAngle), glm::vec3(0, 0, 1));
        modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(3.0, 0, +.8));
        modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(3.5, 0.5, 0.05));
        setMeshUniforms(modelViewStack.top());
        sphere.render();
        modelViewStack.pop();
        currentAngle += 360.0 / (float)FAN_FIN_COUNT;
    }
    modelViewStack.pop();
    modelViewStack.pop();

    // Change sign to change direction
    fanAngle += ANGLE_STEP * FanSpeed;
}

GLfloat lamp_base_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat lamp_base_diffuse[] = { 0.1f, 0.1f, 0.1f, 1.0f };
GLfloat lamp_base_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat lamp_base_shininess = 100.0f;
GLfloat lamp_shade_ambient[] = { 0.2f, 0.2f, 0.0f, 1.0f };
GLfloat lamp_shade_diffuse[] = { 1.0f, 1.0f, 0.6f, 1.0f };
GLfloat lamp_shade_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat lamp_shade_shininess = 50.0f;
void drawLamp(GLuint renderingProgram) {
    setMeshMaterial(renderingProgram, lamp_base_ambient, lamp_base_diffuse, lamp_base_specular, lamp_base_shininess);

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.65, 0.25, -0.7));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.08, 0.02, 0.08));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    setMeshUniforms(modelViewStack.top());
    cylinder.render();
    modelViewStack.pop();

    modelViewStack.push(modelViewStack.top());
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), glm::vec3(-0.65, 0.50, -0.7));
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.02, 0.25, 0.02));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(90.0f), glm::vec3(1, 0, 0));
    setMeshUniforms(modelViewStack.top());
    cylinder.render();
    modelViewStack.pop();

    setMeshMaterial(renderingProgram, lamp_shade_ambient, lamp_shade_diffuse, lamp_shade_specular, lamp_shade_shininess);
    modelViewStack.push(modelViewStack.top());
    lightPosition = glm::vec3(-0.65, 0.50, -0.7);
    modelViewStack.top() *= glm::translate(glm::mat4(1.0f), lightPosition);
    modelViewStack.top() *= glm::scale(glm::mat4(1.0f), glm::vec3(0.18, 0.20, 0.18));
    modelViewStack.top() *= glm::rotate(glm::mat4(1.0f), glm::radians(-90.0f), glm::vec3(1, 0, 0));
    setMeshUniforms(modelViewStack.top());
    cone.render();
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
    normLoc = glGetUniformLocation(renderingProgram, "norm_matrix");

    // Build matrices for shader computation
    viewMatrix = glm::lookAt(glm::vec3(EyeX, EyeY, EyeZ), glm::vec3(0.0, 0.3, 0.0), glm::vec3(0.0, 1.0, 0.0));
    modelViewStack.push(viewMatrix);

    displayLights(renderingProgram, viewMatrix);

    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projMatrix));

    setMeshMaterial(renderingProgram, floor_ambient, floor_diffuse, floor_specular, 0.0f);
    drawFloor();

	setMeshMaterial(renderingProgram, walls_ambient, walls_diffuse, walls_specular, walls_shininess);
    drawWalls();

	setMeshMaterial(renderingProgram, sofa_ambient, sofa_diffuse, sofa_specular, sofa_shininess);
    drawSofa();

	setMeshMaterial(renderingProgram, table_ambient, table_diffuse, table_specular, table_shininess);
    drawCoffeeTable();

    // Uses multiple materials
    drawFan(renderingProgram);

    setMeshMaterial(renderingProgram, table_ambient, table_diffuse, table_specular, table_shininess);
    drawChinaCabinet();

    setMeshMaterial(renderingProgram, table_ambient, table_diffuse, table_specular, table_shininess);
    drawEndTable();

    // Uses multiple materials
    drawLamp(renderingProgram);

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

    cube = Cube(1.0);
    sphere = Sphere(1.0, 20, 20);
    cylinder = Cylinder(1.0, 1.0, 20, 10);
    cone = Cone(1.0, 1.0, 10, 10);

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

    glClearColor(0.05f, 0.05f, 0.2f, 0.0f);  // background is light gray
    glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

    glfwSetKeyCallback(appWindow, keyboardListener);

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