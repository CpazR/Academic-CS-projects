OBJECTS = term.o TermSortingList.o Lab9.o
HEADERS = term.h TermSortingList.h
Lab9:
	g++ Lab9.cpp term.cpp TermSortingList.cpp -o Lab9
Lab9UPDFATE: $(OBJECT)
		g++ $^ -o $@
%.o: %.cpp $(HEADERS)
		g++ -c $< -o $@
clean:
		rm -i *.o Lab9


