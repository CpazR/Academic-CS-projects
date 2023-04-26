OBJECTS = building.o BuildingList.o Lab8.o
HEADERS = building.h BuildingList.h
Lab8: $(OBJECT)
	g++ $^ -o $@
%.o: %.cpp $(HEADERS)
	g++ -c $< -o $@
clean:
	rm -i *.o Lab8
