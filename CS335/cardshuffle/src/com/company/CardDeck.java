package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {
    public static final int SUIT_COUNT = 4;
    public static final int CARD_SUIT_COUNT = 13;


    private final Card[][] unShuffledCard = new Card[SUIT_COUNT][CARD_SUIT_COUNT];

    private final List<Card> cardList = new ArrayList<>();

    CardDeck() {
        for (int suitCount = 0; suitCount < SUIT_COUNT; suitCount++) {
            for (int cardCount = 0; cardCount < CARD_SUIT_COUNT; cardCount++) {
                unShuffledCard[suitCount][cardCount] = new Card(Card.Suit.values()[suitCount], Card.Value.valueOf(cardCount + 1));
            }
            cardList.addAll(List.of(unShuffledCard[suitCount]));
        }
    }

    public List<Card> getCardList() {
        return cardList;
    }

    public void shuffleDeck() {
        Collections.shuffle(cardList);
        System.out.println("Shuffled!");
    }

    public void reset() {
        cardList.clear();
        for (int suitCount = 0; suitCount < 3; suitCount++) {
            cardList.addAll(List.of(unShuffledCard[suitCount]));
        }
    }

    public void printDeck() {
        cardList.forEach(System.out::println);
        System.out.println("--------------------");
    }
}