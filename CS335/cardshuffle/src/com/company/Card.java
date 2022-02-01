package com.company;

import javax.swing.*;

public class Card {

    enum Suit {
        HEARTS(0, "hearts", "red"),
        DIAMONDS(1, "diamonds", "red"),
        CLUBS(2, "clubs", "black"),
        SPADES(3, "spades", "black");

        private final String name;

        Suit(int suitNumber, String suitName, String color) {
            name = suitName;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum Value {
        A(1, "ace"),
        TWO(2, "2"),
        THREE(3, "3"),
        FOUR(4, "4"),
        FIVE(5, "5"),
        SIX(6, "6"),
        SEVEN(7, "7"),
        EIGHT(8, "8"),
        NINE(9, "9"),
        TEN(10, "10"),
        JACK(11, "jack"),
        QUEEN(12, "queen"),
        KING(13, "king");

        private final String name;

        Value(int valueIndex, String valueName) {
            name = valueName;
        }

        static Value valueOf(int index) {
            return values()[index - 1];
        }

        @Override
        public String toString() {
            return name;
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
    private final Value value;
    private final Suit suit;
    private final Icon cardImage;

    Card(Suit suit, Value number) {
        this.suit = suit;
        this.value = number;
        this.cardImage = new ImageIcon(".\\assets\\" + this.value + "_of_" + this.suit + "_icon.png");
    }

    public Icon getCardImage() {
        return cardImage;
    }

    @Override
    public String toString() {
        return "Suit: " + suit.name() + " Value: " + value;
    }
}