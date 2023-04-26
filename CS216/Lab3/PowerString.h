/* 
 * File:   PowerString.h
 * Purpose: provide the declaration of the PowerString class
 *
 * Author: Pike
 *
 */

#ifndef POWERSTRING_H
#define	POWERSTRING_H

#include <string>

using namespace std;

class PowerString
{
    public:
        // constructor: initialize str with ini_str passing as a parameter
        PowerString(string ini_str);

        // return the current value of the private data member: str
        string getString() const;

        // set the value of str to be the passed in parameter input_str
        void setString(string input_str);

        // return a reversed string. Note that str has not been changed
        string reverse() const;

        // return a palindrome which contains str then followed by
        //                     the reverse of str without the last character of str
        // for example, if str is "abc", it returns "abcba";
        //              if str is "ab", it returns "aba", and so on.
        // return value is guaranteed to be a palindrome with odd number of characters
        // Note that str has not been changed
        string oddPalindrome() const;

        // return true if str is a palindrome
        // otherwise return false
        // A palindrome is defined as a sequence of characters which reads the same backward as forward
        bool isPalindrome() const;

        // displays str, followed by a new line marker, 
        //               to the standard output
        void print() const;

    private:
        string str;
};

#endif	/* POWERSTRING_H */

