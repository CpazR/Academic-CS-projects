package com.company;

import javax.swing.*;

public class Card {

    enum Suit {
        SPADES(0, "spades", "black"),
        HEARTS(1, "hearts", "red"),
        DIAMONDS(2, "diamonds", "red"),
        CLUBS(3, "clubs", "black");

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

        private int valueIndex;
        private final String name;

        Value(int valueIndex, String valueName) {
            this.valueIndex = valueIndex;
            this.name = valueName;
        }

        /**
         * Get enum from an index from 1 - 13
         */
        static Value valueOf(int index) throws Exception {
            if (index < 1 || index > 13) {
                throw new Exception("Card number is supposed to be between 1 and 13 but was: " + index);
            }
            return values()[index - 1];
        }

        public int getNumericalValue() {
            return valueIndex;
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
    private final Icon actualCardImage;
    private Icon visibleCardImage;

    private final Icon notFlippedImage = new ImageIcon("./assets/red_joker_icon.png");
    private boolean flippedOver = false;

    public void flipCard() {
        this.flippedOver = true;
        visibleCardImage = actualCardImage;
    }

    public boolean isFlipped() {
        return flippedOver;
    }

    public void resetCard() {
        this.flippedOver = false;
        visibleCardImage = notFlippedImage;
    }

    Card(Suit suit, Value number) {
        this.suit = suit;
        this.value = number;
        this.actualCardImage = new ImageIcon("./assets/" + this.value + "_of_" + this.suit + "_icon.png");
        this.visibleCardImage = notFlippedImage;
    }

    public Icon getVisibleCardImage() {
        return visibleCardImage;
    }

    public int getValue() {
        return value.getNumericalValue();
    }

    @Override
    public String toString() {
        return "Suit: " + suit.name() + " Value: " + value;
    }
}