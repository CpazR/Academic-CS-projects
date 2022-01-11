# CS315 Makefile for Program 4: Word Counter
# Feel free to modify as needed.
# Original author: Raphael Finkel 3/2020
# Modified author: Nicholas Reel 4/2021

CFLAGS = -Wall -g
CPPFLAGS = -Wall -g
JFLAGS = -g
GOAL = wordCount
DATA = data.txt
STUDENTDATA = studentData.txt
WORKING = workingWordCount

JC = javac

# compile and run the student program, placing the result in tmpRun.out
run: $(GOAL) $(DATA)
	java $(GOAL) < $(DATA) > tmpRun.out
	less tmpRun.out

runStudentData: $(GOAL) $(STUDENTDATA)
	java $(GOAL) < $(STUDENTDATA) > tmpStndRun.out
	less tmpStndRun.out

# run a known working program, placing the result in tmpWorking.out
runWorking: $(WORKING) $(DATA)
	./$(WORKING) < $(DATA) > tmpWorking.out
	less tmpWorking.out

# compile student program
$(GOAL):
	 $(JC) $(JFLAGS) $(GOAL).java

# get the working program
$(WORKING):
	wget http://www.cs.uky.edu/~raphael/courses/CS315/prog4/$(WORKING)
	chmod +x $(WORKING)

# get the data
$(DATA):
	wget http://www.cs.uky.edu/~raphael/courses/CS315/prog4/$(DATA)

# Modify the following recipe to zip exactly what you want to include.
zipAll: 
	zip toSubmit.zip $(GOAL).* Makefile README tmpRun.out tmpStndRun.out $(DATA) $(STUDENTDATA)
	
.PHONY: clean
clean:
	rm -f $(GOAL) $(WORKING) $(DATA) tmp* toSubmit.zip
