/*
 ctors_in_movie2* Course: CS216-008
 * Project: Project 1
 * Purpose: it reads rom input file and presents it in a number of forms, depending on user input
 * Author: Nicholas Reel
 */

#include <iostream>
#include <fstream>
#include <string>
#include <cctype>
#include <set>
#include <map>
#include <sstream>
#include "imdb.h"

using namespace std;

void sortMovies(const IMDB& imdb);
void coActors(const IMDB& imdb);

int main(int argc, char* argv[]){
    //check if there is exactly one command line argument 
    if (argc != 2){
        cout << "Warning: need exactly one command line argument." << endl;
        cout << "Usage: " << argv[0] << " inputfile_name" << endl;
        return 1;
    }

    //open file and extract data into database
    ifstream fileIn;
    fileIn.open(argv[1]);

    //check if file can be open successfully or not
    if (!fileIn.good()){
        cout << "Warning: cannot open file named " << argv[1] << "!" << endl;
        return 2;
    }

    IMDB imdbDB;

    while (!fileIn.eof()){
        string line;
        getline(fileIn, line);
        string actorName, movieTitle;
        istringstream iss(line);
        getline(iss, actorName,',');
        iss>>ws;

        //insert an actor with a set of movies into the database
        set<string> movies;
        while (getline(iss, movieTitle, ',')){
            movies.insert(movieTitle);
            iss>>ws;
        }
        imdbDB.insert_an_actor(actorName, movies);
    }
    fileIn.close(); //finished!

    //get user input on what to do with database
    int option;
    while (true){
        cout << "This application stores information about Actors and their Movies, please choose your option (Enter Q or q to quit):" << endl;
        cout << "1. Actors in Movies" << endl;
        cout << "2. Actors and co-actors" << endl;
        cin >> option;
        cin.ignore(256, '\n');
        
        //if "option" isn't an integer
        if (cin.fail()){
            cin.clear();
            string input_to_check;
            cin >> input_to_check;
            
            if (input_to_check == "Q" || input_to_check == "q")
                break;
            else{
                cout << "Invalid option!" << endl;
                continue;
            }
        }
        //activcate function depending on user input
        switch (option){
            case 1: sortMovies(imdbDB);
                    break;
            case 2: coActors(imdbDB);
                    break;
            default:
                    cout << "Invalid option!" << endl;
        }
    }
    cout << "Thank you for using my program." << endl <<"Bye!" << endl;
    return 0;
}

//given the database, get two movies from user, and sort them depending on user input
void sortMovies(const IMDB& imdb){
    string movie1, movie2;
    char option;

    //get movies to sort actors from
    cout << "Please input the first movie title: ";
    getline(cin,movie1);
    cout << "Please input the second movie title: ";
    getline(cin,movie2);
    
    //have user choose how to sort actors
    string matchedMovie1, matchedMovie2;
    matchedMovie1 = imdb.matchExistingMovie(movie1);
    matchedMovie2 = imdb.matchExistingMovie(movie2);
    if ((matchedMovie1.length() > 0) && (matchedMovie2.length() > 0)){ //if movies arent empty strings
        cout << "Your input matches the following two movies: "<<endl;
        cout << matchedMovie1 << endl;
        cout << matchedMovie2 << endl;
        cout << "Both movies are in the database, searching actors..."<<endl;
        
        cout << "Please input your sorting choice (enter Q/q to quit)"<<endl
            <<  "A --to print all the actors in either of the two movies."<<endl
            <<  "C --to print all the common actors in both of the movies."<<endl
            <<  "O --to print all the actors who are in one movies, but not in both."<<endl
            <<  ">>: ";

        cin >> option;
        cin.ignore(256,'\n');
        
        while(option!='q' && option!='Q'){
            //set up sets that will be used for the whole switch statement
            set<string> movie1Actors=imdb.find_actors_in_a_movie(matchedMovie1);
            set<string> movie2Actors=imdb.find_actors_in_a_movie(matchedMovie2);
            switch(option){
                case 'A': case 'a':{
                    set<string> all;   //stores all actors from both movies
                    all = imdb.find_actors_in_a_movie(matchedMovie1);
                    for (auto i = movie2Actors.begin(); i != movie2Actors.end(); i++)
                        all.insert(*i);
                    //prints actors
                    cout << "All the actors in either of the two movies:" << endl;
                    for (auto i = all.begin(); i != all.end(); i++)
                        cout << *i << endl;
                    cout << "***********************************" << endl;
                    break;
                }
                case 'C': case 'c':{
                    set<string> intersect;   //stores the common actors in both movies
                    for (auto i = movie1Actors.begin(); i != movie1Actors.end(); i++){
                        for (auto j = movie2Actors.begin(); j != movie2Actors.end(); j++){
                            if (*j==*i) //if both actors are the same
                                intersect.insert(*j);
                        }
                    }
                    //prints actors
                    cout << "Actors in both of the two movies:" << endl;
                    for (auto i = intersect.begin(); i != intersect.end(); i++)
                        cout << *i << endl;
                    cout << "***********************************" << endl;
                    break;
                }
                case 'O': case 'o':{
                    set<string> difference;   //stores actors in only one movie

                    //uses the algorithmic approach to symmetric difference
                    auto i=movie1Actors.begin(), j=movie2Actors.begin();
                    while(i!=movie1Actors.end() && j!=movie2Actors.end()){
                        if (*i<*j){
                            difference.insert(*i);
                            i++;
                        }
                        else if (*j<*i){
                            difference.insert(*j);
                            j++;
                        }
                        else{
                            i++;
                            j++;
                        }
                    }
                    //prints actors
                    cout << "Actors only in one (but not the other) movie:" << endl;
                    for (auto i = difference.begin(); i != difference.end(); i++)
                        cout << *i << endl;
                    cout << "***********************************" << endl;
                    break;
                }
                default:    cout << "Invalid option." << endl;     break;   //if input is anything else
            }
            cout << "Please input your sorting choice (enter Q/q to quit)"<<endl
                <<  "A --to print all the actors in either of the two movies."<<endl
                <<  "C --to print all the common actors in both of the movies."<<endl
                <<  "O --to print all the actors who are in one movies, but not in both."<<endl
                <<  ">>:";
            cin >> option;
            cin.ignore(256,'\n');
        }

    }
    else{
        cout << "Invalid movie title." << endl;
        return;
    }
}

//given a database, extract co-actors of a single actor from movies the single actor has been in
void coActors(const IMDB& imdb){
    //get user input
    string actor_name;
    cout << "Finding the co-actors of the actor by typing his/her name: ";
    getline(cin, actor_name);
    if (!imdb.isExistingActor(actor_name)){
        cout << "The actor name you entered is not in the database." << endl;
        return;
    }   

    //set up a temporary set to hold the movies the actor has been in
    set<string> movies_of_actor;
    movies_of_actor = imdb.find_movies_for_an_actor(actor_name);
    //loop through and print all the actors in the movies, excluding the original actor
    for (auto i = movies_of_actor.begin(); i != movies_of_actor.end(); i++)
    {
        cout << "The co-actors of " << actor_name << " in the movie \"" << *i << "\" are: " << endl;
        //print co-actors for a single movie
        set<string> coactors = imdb.find_actors_in_a_movie(*i);
        for (auto j = coactors.begin(); j != coactors.end(); j++)
        {
            if (*j!=actor_name)    
                cout << *j << endl;
        }    
        cout << "***********************************" << endl;
    }
}
