/* File: Graph.cpp
 * Course: CS216-00x
 * Project: Project 3
 * Purpose: the implementation of member functions for the Graph class.
 *          it represents a undirected graph;
 *          it uses adjacency matrix representation (store 0 if no edge exists; store non-zero value(weight) if an edge exists)
 *
 */
#include <cassert>
#include "Graph.h"
#include <queue>

// constructor
Graph::Graph(int numVertices):adj(Matrix<int>(numVertices, numVertices, 0))
{
}

// return the private data member 
// as the adjacency representation of the Graph
Matrix<int> Graph::getAdjacency() const
{
    return adj;
}

// to check if an edge exists between v and w
// return true if an edge exists
// otherwise return false
bool Graph::hasEdge(int v, int w)
{
    assert(v>=0 && v < adj.GetSizeX() && w >=0 && w < adj.GetSizeX());
    if (adj(v, w)==0)
        return false;
    return true;
}

// add an edge between v and w (marked with value of edge)
void Graph::addEdge(int v, int w, int edge)
{
    assert(v>=0 && v < adj.GetSizeX() && w >=0 && w < adj.GetSizeX());
    adj(v,w) = edge;
    adj(w,v) = edge;
}

// return marked value from the edge v to w
int Graph::getEdge(int v, int w)
{
    assert(v>=0 && v < adj.GetSizeX() && w >=0 && w < adj.GetSizeX());
    return adj(v,w);
}

// Apply BFS traversal to find the shortest path from the given source s
// store the shortest path distance from the given source s in distance vector
void Graph::BFS(int s, vector<int>& distance)
{
	bool visited[distance.size()];
	for (int i = 0; i < distance.size(); i++)
		visited[i] = false;
	visited[s] = true;
	distance[s]=0;
	queue<int> Q;
	Q.push(s);

	while (!Q.empty()) {
		int u=Q.front();
		Q.pop();
		for (int w=0; w<distance.size(); ++w) {
			if (adj(w,u)!=0&&!visited[w]) {
				visited[w] = true;
				distance[w]=distance[u]+1;
				Q.push(w);
			}
		}
	}
}

