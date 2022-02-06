package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {
    public static final int SUIT_COUNT = 4;
    public static final int CARD_SUIT_COUNT = 13;


    private final Card[][] unshuffledCard = new Card[SUIT_COUNT][CARD_SUIT_COUNT];

    private final List<Card> cardList = new ArrayList<>();
    private final List<Card> matchedCardBucket = new ArrayList<>();

    CardDeck() throws Exception {
        for (int suitCount = 0; suitCount < SUIT_COUNT; suitCount++) {
            for (int cardCount = 0; cardCount < CARD_SUIT_COUNT; cardCount++) {
                unshuffledCard[suitCount][cardCount] = new Card(Card.Suit.values()[suitCount], Card.Value.valueOf(cardCount + 1));
            }
            cardList.addAll(List.of(unshuffledCard[suitCount]));
        }
    }

    public List<Card> getCardList() {
        return cardList;
    }

    public Card getCard(int suitIndex, int cardIndex) {
        return cardList.get(CardDeck.CARD_SUIT_COUNT * suitIndex + cardIndex);
    }

    /**
     * Shuffle the card list using collections
     */
    public void shuffleDeck() {
        Collections.shuffle(cardList);
        System.out.println("Shuffled!");
    }

    /**
     * Reset card list to original unshuffled order
     */
    public void reset() {
        // Empty card list
        cardList.clear();
        matchedCardBucket.clear();
        // Reinsert all cards from unshuffled array one suit at a time
        for (int suitCount = 0; suitCount < SUIT_COUNT; suitCount++) {
            cardList.addAll(List.of(unshuffledCard[suitCount]));
        }
        cardList.forEach(Card::resetCard);
    }

    /**
     * Debug method to help visualize the card list
     */
    public void printDeck() {
        cardList.forEach(System.out::println);
        System.out.println("--------------------");
    }
}