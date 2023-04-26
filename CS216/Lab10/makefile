OBJECTS = term.o autocomplete.o SortingList.o Lab10.o
HEADERS = term.h autocomplete.h SortingList.h
Lab9:
	g++ term.cpp autocomplete.cpp SortingList.cpp Lab10.cpp -o Lab10
Lab9UPDFATE: $(OBJECT)
		g++ $^ -o $@
%.o: %.cpp $(HEADERS)
		g++ -c $< -o $@
clean:
		rm -i *.o Lab10


