/* 
 * File:   magicString.cpp
 * Course: CS216-00x
 * Project: Lab 7
 * Purpose: provide the implementation of member functions of magicString class
 */
#include <iostream> 
#include <algorithm>
#include "magicString.h"

using namespace std;

//constructor
magicString::magicString(string ini_string)
{
    this->str = ini_string;
}

// set the current value of str
void magicString::setMagicString(string value)
{
    this->str = value;
}

// return the value of str
string magicString::getMagicString() const
{
    return this->str;
}

//returns a string which transforms each character of str into lower case
//note that str has not been changed
//(algorithm header file is needed for the definition of transform function)
string magicString::toLower() const
{
    string result = this->str;
    transform(this->str.begin(), this->str.end(), result.begin(), ::tolower);
    return result;
}

//returns a string which transforms each character of str into upper case
// use recursive function to implement
//note that str has not been changed
string magicString::toUpper() const
{
    static int fcount = 0; // use a static variable to count the function calling
    fcount++;   // increase count by one while calling the function
    cout << "Calling the recursive function named " << __func__ << " : " << fcount  << " times." << endl;   // note that __func__ is macro for the funciton name, it may be different for different compiler
    // Base case:
    if (this->str.length() == 0)
        return this->str;
    // Recursive case:
    string first = this->str.substr(0,1);
    const char* front = first.c_str();
    char upperFront[LEN];
    string result;
    upperFront[0] = toupper(front[0]);
    upperFront[1] = '\0';
    result = upperFront;
    magicString part(this->str.substr(1, this->str.length()-1));
    return result + part.toUpper();
}

//it remove the extra black spaces(including tab) from str
//It defines as:
//   1. it remains only one space if str contains more than
//      one continuous blank spaces between two non-space charcters
//      For example, if str is "hello,     world",
//      it should change into "hello, world"
//   2. it removes all spaces before the first non-space character
void magicString::removeExtraSpace()
{
    bool hasSpace = true;
    string original = this->str;
    string result;
    for (int i=0; i < original.length(); i++)
    {
        // replace tab with a blank space before parsing
        if (original[i] == '\t')
            original[i] = ' ';

        // for the space(s) before the first non-space character, remove them;
        // for the space(s) in between two non-space characters, replace with only one space
        if (original[i] != ' ')
        {
            result = result + original[i];
            hasSpace = false;
        }
        else if (!hasSpace)
        {
            result = result + original[i];
            hasSpace = true;    
        }
        else
            hasSpace = true;
    }
    this->str = result;
}

// return a string which transforms only the first letter of each word in str into upper case letter
// the word in str is defined as a sequence of characters starting with non-space character and ending right before the next space
// note that str has not been changed
string magicString::wordFormat() const
{
    // before the wordFormat transformation
    // make sure (1)the extra space(s) has been removed from str; 
    //           (2)apply to all lower case letters of str
    return this->str;
}
