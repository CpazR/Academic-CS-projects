/*
*course: CS216-008
*Project: Project Three
*Purpose: Bacon Game!
*Author: Nicholas Reel
*/

#include <iostream>
#include <fstream>
#include <vector>
#include <map>
#include <set>
#include <string>
#include "Graph.h"

using namespace std;

int main()
{
	string actorFileName, movieFileName, IDFileName;
	map<int, string> actors, movies;
	set< vector<int> >AandMIDS;
	int verticiesCount = 0;

	// read actor file
	cout << "Please type the name of the file which contains actor IDs and names: (default file name is \"actors.txt\")  ";
	getline(cin, actorFileName);
	if(actorFileName.empty())
		actorFileName="actors.txt";
	ifstream inFileActor;
	inFileActor.open(actorFileName.c_str());
	if (!inFileActor.good()) {
		cout << "Cannot open the file named " << actorFileName << endl;
		return 1;
	}
	while (!inFileActor.eof()) {
		int tmpID = -1;
		string tmpName = "NULL";
		inFileActor >> tmpID;
		string tmpTabFilter = "";
		if (inFileActor.peek() == '\t')
			inFileActor >> ws;
		if (!isdigit(inFileActor.peek()))
			getline(inFileActor, tmpName, '\n');
		if (tmpID != -1) {
			actors.insert(pair<int, string>(tmpID, tmpName));
			verticiesCount++;
		}
	}
	inFileActor.close();

	// read movie file
	cout << "Please type the name of the file which contains movie IDs and names: (default file name is \"movies.txt\")  ";
	getline(cin, movieFileName);
	if(movieFileName.empty())
		movieFileName="movies.txt";
	ifstream inFileMovie;
	inFileMovie.open(movieFileName.c_str());
	if (!inFileMovie.good()) {
		cout << "Cannot open the file named " << movieFileName << endl;
		return 2;
	}
	while (!inFileMovie.eof()) {
		int tmpID = -1;
		string tmpName = "NULL";
		inFileMovie >> tmpID;
		string tmpTabFilter = "";
		if (inFileMovie.peek() == '\t')
			inFileMovie >> ws;
		if (!isdigit(inFileMovie.peek()))
			getline(inFileMovie, tmpName, '\n');
		if (tmpID != -1) {
			movies.insert(pair<int, string>(tmpID, tmpName));
		}
	}
	inFileMovie.close();

	// read ID file
	cout << "Please type the name of the file which contains movie and actor IDs: (default file name is \"movie-actor.txt\")  ";
	getline(cin, IDFileName);
	if(IDFileName.empty())
		IDFileName="movie-actor.txt";
	ifstream inFileID;
	inFileID.open(IDFileName.c_str());
	if (!inFileID.good()) {
		cout << "Cannot open the file named " << IDFileName << endl;
		return 3;
	}
	while (!inFileID.eof()) {
		int tmpIDmov = -1, tmpIDact = -1;
		if (inFileID.peek() != '\t')
			inFileID >> tmpIDmov >> tmpIDact;
		if (tmpIDmov != -1) {
			vector<int> nested;
			nested.push_back(tmpIDmov);
			nested.push_back(tmpIDact);
			AandMIDS.insert(nested);
		}
	}
	inFileID.close();

	vector<int> actorIndex; //assign numerical indexes to each ID
	for (map<int, string>::iterator m = actors.begin(); m != actors.end(); ++m)
		actorIndex.push_back(m->first);

	//create graph dynamically with data from files
	const int VERTICESNUM = verticiesCount;
	Graph baconGraph(VERTICESNUM);

	for (set< vector<int> >::iterator it = AandMIDS.begin(); it != AandMIDS.end(); it++) {
		vector<int> itinner = *it;//vector of current iterator to compare IDs
		for (set< vector<int> >::iterator itNxt = AandMIDS.begin(); itNxt != AandMIDS.end(); itNxt++) {
			vector<int> itNxtinner = *itNxt;//vector of current iterator to compare IDs
			int movieID = itinner[0];
			int firstActorEdge = itinner[1];
			int movieIDnx = itNxtinner[0];
			int secondActorEdge = itNxtinner[1];

			for (int i = 0; i < actorIndex.size(); i++) {// change IDs into index values to use as edges
				if (actorIndex[i] == firstActorEdge)
					firstActorEdge = i;
				if (actorIndex[i] == secondActorEdge)
					secondActorEdge = i;
			}

			if (!baconGraph.hasEdge(firstActorEdge, secondActorEdge) && (movieID == movieIDnx && firstActorEdge != secondActorEdge)) {
				//cout << firstActorEdge << "\t" << secondActorEdge << "\t" << movieID << endl;//debug line
				baconGraph.addEdge(firstActorEdge, secondActorEdge, movieID);
			}
		}
	}
	//baconGraph.display();//debug line

	int source = 0;// source found based off of location of "Kevin Bacon" in actor map
	for (map<int, string>::iterator it = actors.begin(); it != actors.end(); it++) {
		if (it->second == "Kevin Bacon") {
			int tmpID = it->first;
			for (int i = 0; i < actorIndex.size(); i++)// change ID into index value
				if (tmpID == actorIndex[i])
					source = i;
			break;
		}
	}

	cout << "\n*******************************************************************\nThe Bacon number of an actor is the number of degrees of separation he/she has from Bacon.\nThose actors who have worked directly with Kevin Bacon in a movie have a Bacon number of 1.\nThis application helps you find the Bacon number of an actor.\nEnter \"exit\" to quit the program.\nPlease enter an actor's name (case-sensitive): ";
	string userIn = "";// user given actor name
	bool isFound = false;// was the user given actor name found in actor map?
	int retVal = -1;// index of user given actor name
	getline(cin, userIn);
	
	while (userIn != "exit") {
		for (map<int, string>::iterator it = actors.begin(); it != actors.end(); it++) {
			if (userIn == it->second) {
				int tmpID = it->first;
				for (int i = 0; i < actorIndex.size(); i++)// change ID into index value
					if (tmpID == actorIndex[i]) {
						retVal = i;
						isFound = true;
					}
				break;
			}
		}
		if (isFound) {
			vector<int> distance(VERTICESNUM, -1);
			vector<int> go_through(VERTICESNUM, -1);
			baconGraph.BFS(source, distance, go_through);
			if (distance.size() > 0) {
				cout << "You are looking up the Bacon Number for " << userIn << " :" << endl;
			}
			if (distance[retVal] != -1) {// gets adjacent actor and movie
				cout << "\tThe Bacon Number for " << userIn << " is: " << distance[retVal] << endl;
				int tmp = retVal;
				string actorString, movieString;

				for (map<int, string>::iterator it = actors.begin(); it != actors.end(); it++) {
					if (it->first == retVal) {
						actorString=it->second;
						isFound = true;
					}
				}

				while (distance.size() > 0 && tmp != source) {

					// CONVERTING IDs
					for (map<int, string>::iterator it = actors.begin(); it != actors.end(); it++) {
						if (it->first == actorIndex[go_through[tmp]]) {
							actorString=it->second;
							isFound = true;
							break;
						}
					}
					for (map<int, string>::iterator it = movies.begin(); it != movies.end(); it++) {
						if (it->first == baconGraph.getEdge(tmp,go_through[tmp])) {
							movieString=it->second;
							isFound = true;
							break;
						}
					}
					tmp = go_through[tmp];
					//if (tmp != source)
						cout << "\t" << userIn << " appeared in " << movieString << " with " << actorString <<endl;
				}
				cout << endl;
			}
			else
				cout << "There is no path to vertex " << retVal << endl;
		}
		else cout << "Did not find actor " << userIn << "." << endl;

		cout << "\n*******************************************************************\nThe Bacon number of an actor is the number of degrees of separation he/she has from Bacon.\nThose actors who have worked directly with Kevin Bacon in a movie have a Bacon number of 1.\nThis application helps you find the Bacon number of an actor.\nEnter \"exit\" to quit the program.\nPlease enter an actor's name (case-sensitive): ";
		userIn = "";
		isFound = false;
		getline(cin, userIn);
	}
	return 0;
}

