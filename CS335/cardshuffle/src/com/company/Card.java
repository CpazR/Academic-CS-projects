package com.company;

public class Card {

    enum Suit {
        HEARTS(0),
        DIAMONDS(1),
        CLUBS(2),
        SPADES(3);

        private int value;

        Suit(int suitNumber) {
            this.value = suitNumber;
        }
    }

    /**
     * The numerical value of the card.
     * <br> 1 = A </br>
     * <br> 2 - 10 = Normal values </br>
     * <br> 11 = Jack </br>
     * <br> 12 = Queen </br>
     * <br> 13 = King </br>
     */
    int numericalValue = 0;

    Suit suit = null;

    Card(Suit suit, int number) {
        this.suit = suit;
        this.numericalValue = number;
    }

    @Override
    public String toString() {
        return "Suit: " + suit.name() + "\nValue: " + numericalValue;
    }
}