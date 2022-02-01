package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        var deck = new CardDeck();
        new ApplicationContext(deck);
    }
}

class ApplicationContext extends JFrame implements ActionListener {

    private final GridLayout cardLayout = new GridLayout(CardDeck.SUIT_COUNT, CardDeck.CARD_SUIT_COUNT);
    private final JLabel[][] cards = new JLabel[CardDeck.SUIT_COUNT][CardDeck.CARD_SUIT_COUNT];

    private final FlowLayout buttonLayout = new FlowLayout();
    private final JButton shuffleButton = new JButton("Shuffle");
    private final JButton resetButton = new JButton("Reset");
    private final JButton quitButton = new JButton("Quit");

    private final CardDeck deck;

    ApplicationContext(CardDeck deck) {
        this.deck = deck;
        windowSetup();
    }

    private void windowSetup() {
        this.setTitle("Card Shuffling");
        this.setLayout(null);
        this.setSize(1080, 600);
        this.setVisible(true);

        setupCardPanel();
        setupButtonPanel();

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        establishListeners();
        this.revalidate();
    }

    private void setupCardPanel() {
        var cardPanel = new JPanel();
        cardPanel.setLayout(cardLayout);
        cardPanel.setSize(getWidth(), 420);

        for (int suitCount = 0; suitCount < CardDeck.SUIT_COUNT; suitCount++) {
            for (int cardCount = 0; cardCount < CardDeck.CARD_SUIT_COUNT; cardCount++) {
                cards[suitCount][cardCount] = new JLabel();
                var cardField = cards[suitCount][cardCount];
                cardField.setSize(getWidth() / CardDeck.CARD_SUIT_COUNT, getHeight() / CardDeck.SUIT_COUNT);

                var currentCard = deck.getCardList().get(CardDeck.CARD_SUIT_COUNT * suitCount + cardCount);
                cardField.setIcon(currentCard.getCardImage());

                cardPanel.add(cardField, suitCount, cardCount);
            }
        }
        this.add(cardPanel);
    }

    private void setupButtonPanel() {
        var buttonPanel = new JPanel();
        buttonPanel.setLayout(buttonLayout);

        buttonPanel.add(shuffleButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(quitButton);
        buttonPanel.setSize(getWidth(), 100);

        this.add(buttonPanel);
    }

    /**
     * Setup listeners for any components that might need them
     */
    private void establishListeners() {
        shuffleButton.addActionListener(e -> {
            deck.shuffleDeck();
            deck.printDeck();
            reloadCards();
        });
        resetButton.addActionListener(e -> {
            deck.reset();
            deck.printDeck();
            reloadCards();
        });
        quitButton.addActionListener(e -> System.exit(0));
    }

    private void reloadCards() {
        for (int suitCount = 0; suitCount < CardDeck.SUIT_COUNT; suitCount++) {
            for (int cardCount = 0; cardCount < CardDeck.CARD_SUIT_COUNT; cardCount++) {
                var cardField = cards[suitCount][cardCount];
                cardField.setSize(getWidth() / CardDeck.CARD_SUIT_COUNT, getHeight() / CardDeck.SUIT_COUNT);

                var currentCard = deck.getCardList().get(CardDeck.CARD_SUIT_COUNT * suitCount + cardCount);
                cardField.setIcon(currentCard.getCardImage());
            }
        }
    }

    /**
     * Provided a component and a percentage (value from 0 - 100), return a point the component is centered around
     */
    private void setComponentPercentagePosition(JComponent component, int percentage) {
        setComponentPercentagePositionAbsolute(component, percentage, percentage);
    }

    private void setComponentPercentagePositionAbsolute(JComponent component, int percentageWidth, int percentageHeight) {
        double calculatedPercentageWidth = (double) percentageWidth / 100;
        double calculatedPercentageHeight = (double) percentageHeight / 100;
        component.setAlignmentX(Double.valueOf(getWidth() * calculatedPercentageWidth - (component.getWidth() * calculatedPercentageWidth)).floatValue() / 100);
        component.setAlignmentY(Double.valueOf(getHeight() * calculatedPercentageHeight - (component.getHeight() * calculatedPercentageHeight)).floatValue() / 100);
    }

    private void setComponentPercentagePositionRelative(JComponent component, JPanel relativePanel, int percentageWidth, int percentageHeight) {
        var calculatedPercentageWidth = percentageWidth / 100;
        var calculatedPercentageHeight = percentageHeight / 100;
        component.setAlignmentX(Double.valueOf(relativePanel.getWidth() * calculatedPercentageWidth - (component.getWidth() * calculatedPercentageWidth)).floatValue() / 100);
        component.setAlignmentY(Double.valueOf(relativePanel.getHeight() * calculatedPercentageHeight - (component.getHeight() * calculatedPercentageHeight)).floatValue() / 100);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

