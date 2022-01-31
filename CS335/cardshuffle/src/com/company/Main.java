package com.company;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Card[][] deckOfCards = new Card[3][13];

        for (int suitCount = 0; suitCount < 3; suitCount++) {
            for (int cardCount = 0; cardCount < 13; cardCount++) {
                deckOfCards[suitCount][cardCount] = new Card(Card.Suit.values()[suitCount], cardCount);
            }
        }
    }
}

class Card {

    static enum Suit {
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
     * <br> 0 = A </br>
     * <br> 1- 10 = Normal values </br>
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
}

class CardShuffleApplication {

}

