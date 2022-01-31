package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        var deck = new CardDeck();
        var context = new ApplicationContext(deck);
    }
}


class ApplicationContext extends JFrame implements ActionListener {

    private final JTextArea[][] cards = new JTextArea[3][12];
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
        this.setSize(1280, 720);
        this.setVisible(true);

        var cardPanel = new JPanel();
        cardPanel.setSize(1280, 620);
        cardPanel.setAlignmentY(0f);

        for (int suitCount = 0; suitCount < 3; suitCount++) {
            for (int cardCount = 0; cardCount < 12; cardCount++) {
                cards[suitCount][cardCount] = new JTextArea();
                var cardField = cards[suitCount][cardCount];
                cardField.setSize(100, 150);

                var currentCard = deck.getCardList().get(suitCount * cardCount);
                cardField.setText(currentCard.toString());

                setComponentPercentagePositionRelative(cardField, cardPanel, 1 / (cardCount + 1), 1 / (suitCount + 1));
                cardPanel.add(cardField);
            }
        }

        shuffleButton.setSize(80, 50);
        shuffleButton.setAlignmentX(.4f);
        resetButton.setSize(80, 50);
        resetButton.setAlignmentX(.5f);
        quitButton.setSize(80, 50);
        quitButton.setAlignmentX(.6f);

        var buttonPanel = new JPanel();

        buttonPanel.add(shuffleButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(quitButton);
        buttonPanel.setSize(1280, 100);
        buttonPanel.setAlignmentY(1f);

        this.add(buttonPanel);

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        establishListeners();
        this.revalidate();
    }

    /**
     * Setup listeners for any components that might need them
     */
    private void establishListeners() {
        shuffleButton.addActionListener(e -> {
            deck.shuffleDeck();
            deck.printDeck();
        });
        resetButton.addActionListener(e -> {
            deck.reset();
            deck.printDeck();
        });
        quitButton.addActionListener(e -> System.exit(0));
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

