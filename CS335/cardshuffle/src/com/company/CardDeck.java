package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {
    private final Card[][] sortedCards = new Card[3][12];

    private final List<Card> cardList = new ArrayList<>();

    CardDeck() {
        for (int suitCount = 0; suitCount < 3; suitCount++) {
            for (int cardCount = 0; cardCount < 12; cardCount++) {
                sortedCards[suitCount][cardCount] = new Card(Card.Suit.values()[suitCount], cardCount + 1);
            }
            cardList.addAll(List.of(sortedCards[suitCount]));
        }
    }

    public List<Card> getCardList() {
        return cardList;
    }

    public void shuffleDeck() {
        Collections.shuffle(cardList);
    }

    public void reset() {
        cardList.clear();
        for (int suitCount = 0; suitCount < 3; suitCount++) {
            cardList.addAll(List.of(sortedCards[suitCount]));
        }
    }

    public void printDeck() {
        cardList.forEach(System.out::println);
        System.out.println("--------------------");
    }
}