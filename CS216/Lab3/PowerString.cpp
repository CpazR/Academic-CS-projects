/* 
 * File:   PowerString.cpp
 * Purpose: provide the definition of the PowerString class
 *
 * Solution 0: using stack's Last-In-First-Out feature
 *             to reverse a sequence 
 * Author: Pike
 *
 */
#include <iostream>
#include "PowerString.h"


// initialize str with ini_str passing as a parameter
PowerString::PowerString(string ini_str){
    str=ini_str;
}
// return the current value of the private data member: str
string PowerString::getString() const{
    return str;
}

// set the value of str to be the passed in parameter input_str
void PowerString::setString(string input_str){
    str=input_str;
}

// return a reversed string. Note that str has not been changed
string PowerString::reverse() const {
    if (str.length()>1){
        string shorter=str.substr(1,str.length()-1);
        PowerString rev(shorter);
        return rev.reverse()+str[0];
    }
    else
        return str;
}


// return a palindrome which contains str then followed by
//                     the reverse of str without the last character of str
// for example, if str is "abc", it returns "abcba";
//              if str is "ab", it returns "aba", and so on.
// return value is guaranteed to be a palindrome with odd number of characters
// Note that str has not been changed
string PowerString::oddPalindrome() const{
    if (str.length()>1){
        string shorter=str.substr(0,str.length()-1);
        PowerString rev(shorter);
        return str+rev.reverse();
    }
    else
        return str;
}

// return true if str is a palindrome
// otherwise return false
// A palindrome is defined as a sequence of characters which reads the same backward as forward
bool PowerString::isPalindrome() const{
    if (str.length()<=1)
       return true;
    if (str[0]==str[str.length()-1]){
        string shorter=str.substr(1,str.length()-2);
        PowerString smaller(shorter);
        return smaller.isPalindrome();
    }
    else
        return false;
}

// displays str followed by a new line marker
//              to the standard output
void PowerString::print() const
{
    cout << str << endl;
}

