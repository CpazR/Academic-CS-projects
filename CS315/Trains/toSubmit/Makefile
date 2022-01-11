# CS315 Makefile for Program 1: Trains
# Feel free to modify as needed.
# Original author: Raphael Finkel 1/2021
# Modified author: Nicholas Reel 2/2021

CFLAGS = -Wall -g
CPPFLAGS = -Wall -g
JFLAGS = -g

JC = javac

MAIN_CLASS = trains

NUMTRAINS = 5
NUMCARS = 6
NUMSTEPS = 1000

SEED = 49

# compile and run the student program, placing the result in output.txt
run: trains randGen.pl
	randGen.pl $(SEED) | java trains $(NUMTRAINS) $(NUMCARS) $(NUMSTEPS) > output.txt
	less output.txt

#compile trains from main class file
trains:
	$(JC) $(JFLAGS) $(MAIN_CLASS).java

# compile and run a known working program, placing the result in goodOutput.txt
runWorking: workingTrains randGen.pl
	randGen.pl $(SEED) | workingTrains $(NUMTRAINS) $(NUMCARS) $(NUMSTEPS) \
		> goodOutput.txt
	less goodOutput.txt

# get the randGen.pl program
randGen.pl:
	wget http://www.cs.uky.edu/~raphael/courses/CS315/utils/randGen.pl
	chmod +x randGen.pl

# get the workingTrains program
workingTrains:
	wget http://www.cs.uky.edu/~raphael/courses/CS315/prog1/workingTrains
	chmod +x workingTrains

# Modify the following recipe to zip exactly what you want to include.
zipAll: clean
	zip toSubmit.zip trains.java Makefile README

.PHONY: clean
clean:
	rm -f *.class randGen.pl trains workingTrains tmp* toSubmit.zip