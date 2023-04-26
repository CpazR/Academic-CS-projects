/* course: CS216-008
* Project: Project 3
* Purpose: the declaration for the Matrix class.
*   *it uses two dimensional vector to store data items
*   * it is a template class
*** DO NOT CHANGE THE DECLARATION OF Matrix CLASS ***
*/

#ifndef MATRIX_CPP
#define MATRIX_CPP

#include <iostream>
#include "assert.h"
#include "Matrix.h"

//constructor
template<class T>
Matrix<T>::Matrix(int sizeX, int sizeY, T initValue){
	assert(sizeX > 0 && sizeY > 0);
	dx = sizeX;
	dy = sizeY;
	for (int i = 0; i<dx; i++) {
		vector<T> temp;
		for (int j = 0; j<dy; j++)
			temp.push_back(initValue);
		data.push_back(temp);
	}
}

//overloading operations:
template<class T>
T &Matrix<T>::operator()(int x, int y) {
	assert(x >= 0 && x < dx && y >= 0 && y < dy);
	return data[x][y];
}

template<class Type>
ostream &operator<<(ostream &out, const Matrix<Type> &m) {
	out << endl;
	for (int x = 0; x<m.dx; x++) {
		for (int y = 0; y<m.dy; y++)
			out << m.data[x][y] << "\t";
		out << endl;
	}
	return out;
}

template<class MType>
Matrix<MType> operator+(const Matrix<MType> &m1, const Matrix<MType> &m2) {
	assert(m1.dx == m2.dx && m1.dy == m2.dy);
	Matrix<MType> temp(m1.dx,m1.dy);
	for (int i = 0; i<m1.dx; i++) {
		for (int j = 0; j<m1.dy; j++) {
			temp.data[i][j] = m1.data[i][j] + m2.data[i][j];
		}
	}
	return temp;
}

#endif
