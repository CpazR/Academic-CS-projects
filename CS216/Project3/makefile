HEADERS = Graph.h Matrix.h
OBJECTS = Project3.o Graph.o Matrix.o
EXEC = CS216PA3

#build executable program
$(EXEC): $(OBJECTS)
	g++ $^ -o $@
#build testing target for demonstration
$(DEMO_EXEC): $(DEMO_OBJECTS)
	g++ $^ -o $@
#build objective files from source files
%.o: %.cpp $(HEADERS)
	g++ -g -c $< -o $@
#clean up the mess
clean:
	rm -i `find ./ -type f -executable`
	rm -i *.o

