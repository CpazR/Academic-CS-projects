# CS315 Makefile for Program 3: k-d trees
# Feel free to modify as needed.
# Original author: Raphael Finkel 1/2020
# Modified author: Nicholas Reel 3/2021

CFLAGS = -Wall -g
CPPFLAGS = -Wall -g
JFLAGS = -g
GOAL = kd

JC = javac

DIMENSION = 3
NUMPOINTS = 64
SEED = 43
MODULUS = 1000
NUMPROBES = 10

# compile and run the student program, placing the result in tmpRun.out
run: $(GOAL) randGen.pl
	./randGen.pl $(SEED) $(MODULUS) | java $(GOAL) $(DIMENSION) $(NUMPOINTS) $(NUMPROBES) > tmpRun.out
	less tmpRun.out

# compile and run the student program, placing the result in tmpRun.out
runTest: $(GOAL) randGen.pl
	./randGen.pl 42 10000 | java $(GOAL) 3 64 2 > tmpRunTest.out
	less tmpRunTest.out

# compile and run a known working program, placing the result in tmpWorking.out
runWorking: workingKD randGen.pl
	./randGen.pl $(SEED) $(MODULUS) | \
		./workingKD $(DIMENSION) $(NUMPOINTS) $(NUMPROBES) > tmpWorking.out
	less tmpWorking.out

# compile student program
$(GOAL):
	$(JC) $(JFLAGS) $(GOAL).java

# get the randGen.pl program
randGen.pl:
	wget http://www.cs.uky.edu/~raphael/courses/CS315/utils/randGen.pl
	chmod +x randGen.pl

# get the working program
workingKD:
	wget http://www.cs.uky.edu/~raphael/courses/CS315/prog3/workingKD
	chmod +x workingKD

# Modify the following recipe to zip exactly what you want to include.
zipAll: 
	zip toSubmit.zip $(GOAL).java Makefile README tmpRun.out
	
.PHONY: clean
clean:
	rm -f randGen.pl $(GOAL) workingKD tmp* toSubmit.zip
