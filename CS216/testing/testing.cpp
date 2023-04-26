#include <iostream>
#include <set>
#include <map>
#include <vector>

using namespace std;

//templetes for passing the item at the given index
template<class T>
void sortingList<T>::insert(T newitem) {
	items.push_back(newitem);
}

T& SortingList<T>::operator[](int index) {
	assert(insrt >= 0 && index < this->size());
	return items[index];
}


int main(){
    int n=1;
    ///data structures
    //sets
    set<string> students; //NO DOUPLICATES
    
    //maps
    map<string, double> scores; //string key, double value
    scores["James"]=81.6;
    scores.at("Jaiden")=83.6;
    scores["Ari"]=100;

    scores.erase("Ari");
    for(auto i=scores.begin();i!=scores.end();i++)
        cout << *i << endl;

    //maps of maps are common

    //priority queues
   
    //vectors
    vector<int> nums; //could specify initial size of vector IE: scores(100);
    nums[0]=75;
    nums[1]=100;

    cout << "Scores (unnamed): " << endl;
    for(auto i=nums.begin();i!=nums.end();i++)
        cout << ">> " << i << endl;

    if (scores.size()>0)
        nums.pop_back(); //remove last item in vector
    else
        nums.push_back(int); //add item to the end of a vector

    ///graphs
    //direted (diagraph)
    //undirected
    return 0;
}
