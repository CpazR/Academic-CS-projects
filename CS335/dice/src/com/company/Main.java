package com.company;

import java.util.*;

public class Main {

    private static final Random randomNumberGenerator = new Random();

    /**
     * Get two random integers from 1 - 6 and return them as an array of ints
     */
    static Integer rollDice() {
        return randomNumberGenerator.nextInt(6) + 1;
    }

    /**
     * Run simulation once and return number of snake eyes
     * A `snake eye` is a single 1 for documentation's sake
     */
    static boolean runSimulation() {
        boolean rolledSnakeEyes = false;
        List<Integer> allDiceRolls = new ArrayList<>();

        // repeat three time for three pairs
        for (int i = 0; i < 6; i++) {
            // Add dice rolls to list
            allDiceRolls.add(rollDice());
        }

        // Sum up the dice
        int diceRollSum = allDiceRolls.stream().reduce(Integer::sum).orElseThrow();

        // Check if "snake eyes" was rolled by checking sum
        if (diceRollSum == 6) {
            rolledSnakeEyes = true;
        }

        return rolledSnakeEyes;
    }

    /**
     * Helper method to make things more readable
     */
    static void repeatedTest(int nTimes) {
        int totalSnakeEyes = 0;

        // Run simulation nTimes and log result
        for (int i = 0; i < nTimes; i++) {
            if (runSimulation()) {
                totalSnakeEyes++;
            }
            System.out.println("Ran " +i + " times");
        }

        System.out.println(nTimes + ": " + totalSnakeEyes + " snake eyes");
    }

    public static void main(String[] args) {
        // Run 100 times
        repeatedTest(100);

        // Run 1000 times
        repeatedTest(1000);

        // Run 10000 times
        repeatedTest(10000);

        // Run 100000 times
        repeatedTest(100000);
    }
}
