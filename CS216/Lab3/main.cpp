/*
 * Course: CS216-00x
 * Project: Lab Assignment 3
 * Purpose: To reverse each text line, say original,
 *          either from the specified file passing as the command line argument
 *          or from the user input
 *          and then decide if each of original, 
 *          and two manipulations applied to it,
 *          is a palindrome or not
 * Author: (your name)
 *
 */

#include <iostream>
#include <string>
#include <fstream>
#include "PowerString.h"   //declare the PowerString class

using namespace std;

int main(int argc, char** argv)
{

    // check if the number of command line arguments is correct
    if (argc < 1 || argc > 2)
    {
	    cout << "Usage: " << argv[0] << "  or " << argv[0] << " <filename>" << endl;
	    return 1;
    }
    // if there is no command line argument, read the standard input from the keyboard
    if (argc == 1)
    {
   	    string original;
   	    cout << "Please input a string to watch its magic: " << endl;
   	    getline(cin, original);
        // Stop the loop when the user clicks Ctrl-d
   	    while (!cin.eof())
   	    {	
	   	    PowerString magic(original);
   		    cout << "The original string is: ";
            magic.print();
   		    cout << "The reversed string is: " << magic.reverse() << endl;
            cout << "The palindrome with odd number of characters: " << magic.oddPalindrome() << endl;

            // when the boolalpha format flag is set, bool values are represented as true or false, instead of integral values
            // for standard streams, the boolalpha flas is not set on initialization
            cout << boolalpha;
            cout << "Is \" " << magic.getString() << " \" a palindrome? " << magic.isPalindrome() << endl;
            string revstr, oddP;
            revstr = magic.reverse();
            oddP = magic.oddPalindrome();
            magic.setString(revstr);
            cout << "Is \" " << magic.getString() << " \" a palindrome? " << magic.isPalindrome() << endl;
            // Note that the following check should always return true
            // since the string is created as a palindrom with odd number of characters
            magic.setString(oddP);
            cout << "Is \" " << magic.getString() << " \" a palindrome? " << magic.isPalindrome() << endl;

            // asking for another input string...till the user clicks Ctrl-d
   		    cout << "Please input a string to watch its magic: " << endl;
            getline(cin, original);
   	    }
   	    return 0;
    }
    // if there is one command line argument, read each line from the input file 
    else 
    {
	    string original;
	    ifstream inFile;
	    inFile.open(argv[1]);
	    if (!inFile.good())
	    {
		    cout <<"File open failure!" << endl;
		    return 1;
	    }
        //display each text lines from the file in original order, then followed by reversed order
        cout << "Display each text line from the file in original order, then followed by the reversed order: " << endl;
	    while (getline(inFile, original))     // it also contains eof check, avoid an extra line
        {
            PowerString magic(original);
            magic.print();
            cout << "--> " << magic.reverse() << endl;
	    }
	    inFile.close();
	    return 0;
    }
}

