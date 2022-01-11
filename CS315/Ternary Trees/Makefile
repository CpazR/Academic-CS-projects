# CS315 Makefile for Program 2: ternary
# Feel free to modify as needed.
# Original author: Raphael Finkel 1/2020
# Modified author: Nicholas Reel 2/2021

CFLAGS = -Wall -g
CPPFLAGS = -Wall -g
JFLAGS = -g
GOAL = ternary

JC = javac

NUMPOINTS = 1000
SEED = 49
LIMIT = 10000
FIND = 3

# compile and run the student program, placing the result in tmpRun.out
run: $(GOAL) randGen.pl
	./randGen.pl $(SEED) $(LIMIT) | java $(GOAL) $(NUMPOINTS) > tmpRun.out
	less tmpRun.out

# compile and run the student program, search for a subtree given a value
runFind: $(GOAL) randGen.pl
	./randGen.pl $(SEED) $(LIMIT) | java $(GOAL) $(NUMPOINTS) $(FIND) > tmpRunFind.out
	less tmpRunFind.out

# compile and run a known working program, placing the result in tmpWorking.out
runWorking: workingTernary randGen.pl
	./randGen.pl $(SEED) $(LIMIT) | ./workingTernary $(NUMPOINTS) > tmpWorking.out
	less tmpWorking.out

# compile student program
$(GOAL):
	$(JC) $(JFLAGS) $(GOAL).java

# get the randGen.pl program
randGen.pl:
	wget http://www.cs.uky.edu/~raphael/courses/CS315/utils/randGen.pl
	chmod +x randGen.pl

# get the workingTernary program
workingTernary:
	wget http://www.cs.uky.edu/~raphael/courses/CS315/prog2/workingTernary
	chmod +x workingTernary

# Modify the following recipe to zip exactly what you want to include.
zipAll: 
	zip toSubmit.zip $(GOAL).java Makefile README tmpRun.out
	
.PHONY: clean
clean:
	rm -f randGen.pl $(GOAL) *.class workingTernary tmp* toSubmit.zip
