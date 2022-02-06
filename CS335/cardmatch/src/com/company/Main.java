package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        var deck = new CardDeck();
        new ApplicationContext(deck);
    }
}

class ApplicationContext extends JFrame implements ActionListener {

    private final GridLayout cardLayout = new GridLayout(CardDeck.SUIT_COUNT, CardDeck.CARD_SUIT_COUNT);
    private final JPanel cardPanel = new JPanel();
    private final JButton[][] cards = new JButton[CardDeck.SUIT_COUNT][CardDeck.CARD_SUIT_COUNT];

    private final JPanel buttonPanel = new JPanel();
    private final JButton newGameButton = new JButton("New Game");
    private final JButton quitButton = new JButton("Quit");

    private final JPanel textLabelPanel = new JPanel();

    private final String guessesMade = "Guesses made: ";
    private int guessesMadeCount;
    private final JLabel guessesMadeLabel = new JLabel(guessesMade + guessesMadeCount);

    private final String matchesMade = "Matches made: ";
    private int matchesMadeCount;
    private final JLabel matchesMadeLabel = new JLabel(matchesMade + matchesMadeCount);

    private final CardDeck deck;
    private List<Card> matchedCards = new ArrayList<>();
    private Timer matchResetTimer;

    ApplicationContext(CardDeck deck) {
        super("Card matching!!!");
        this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        this.setSize(1080, 620);
        this.setVisible(true);
        this.deck = deck;
        windowSetup();
    }

    private void windowSetup() {
        setupCardPanel();
        setupLabels();
        setupButtonPanel();

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        establishListeners();
        this.revalidate();
        this.newGame();
    }

    private void setupLabels() {
        textLabelPanel.setLayout(new BorderLayout());
        textLabelPanel.setSize(getWidth(), 20);

        textLabelPanel.add(matchesMadeLabel, BorderLayout.WEST);
        textLabelPanel.add(guessesMadeLabel, BorderLayout.EAST);

        this.add(textLabelPanel);
    }

    private void setupCardPanel() {
        cardPanel.setLayout(cardLayout);
        cardPanel.setSize(getWidth(), 420);

        // Initialize card labels
        for (int suitCount = 0; suitCount < CardDeck.SUIT_COUNT; suitCount++) {
            for (int cardCount = 0; cardCount < CardDeck.CARD_SUIT_COUNT; cardCount++) {
                cards[suitCount][cardCount] = new JButton(deck.getCard(suitCount, cardCount).getVisibleCardImage());
                var cardField = cards[suitCount][cardCount];
                cardField.setSize(getWidth() / CardDeck.CARD_SUIT_COUNT, getHeight() / CardDeck.SUIT_COUNT);
                cardPanel.add(cardField);
            }
        }
        // "Reload" to get images based on `cardList` data
        reloadCards();
        this.add(cardPanel, BorderLayout.NORTH);
    }

    private void setupButtonPanel() {
        buttonPanel.setSize(250, 50);

        buttonPanel.add(newGameButton);
        buttonPanel.add(quitButton);

        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Setup listeners for any components that might need them
     */
    private void establishListeners() {
        for (int suitCount = 0; suitCount < CardDeck.SUIT_COUNT; suitCount++) {
            for (int cardCount = 0; cardCount < CardDeck.CARD_SUIT_COUNT; cardCount++) {
                int finalSuitCount = suitCount;
                int finalCardCount = cardCount;
                cards[suitCount][cardCount].addActionListener(e -> matchAttempt(finalSuitCount, finalCardCount));
            }
        }
        newGameButton.addActionListener(e -> {
            newGame();
        });
        quitButton.addActionListener(e -> System.exit(0));
    }

    private void matchAttempt(int suitIndex, int cardIndex) {
        var card = deck.getCard(suitIndex, cardIndex);
        if (!card.isFlipped() && matchedCards.size() < 2) {
            card.flipCard();
            matchedCards.add(card);
            System.out.println(matchedCards);

            if (matchedCards.size() == 2) {
                if (matchedCards.get(0).getValue() == matchedCards.get(1).getValue()) {
                    matchesMadeCount++;
                    matchedCards.clear();
                    if (matchesMadeCount == CardDeck.SUIT_COUNT * CardDeck.CARD_SUIT_COUNT / 2) {
                        newGame();
                    }
                } else {
                    this.matchResetTimer = new Timer(3000, e -> {
                        this.resetMatch();
                    });
                    this.matchResetTimer.start();
                }

                guessesMadeCount++;
                reloadLabels();
            }
            this.reloadCards();
        }
    }

    private void reloadLabels() {
        this.guessesMadeLabel.setText(guessesMade + guessesMadeCount);
        this.matchesMadeLabel.setText(matchesMade + matchesMadeCount);
    }

    private void newGame() {
        deck.reset();
        deck.shuffleDeck();
        deck.printDeck();
        matchesMadeCount = 0;
        guessesMadeCount = 0;
        matchedCards.clear();
        reloadCards();
        reloadLabels();
    }

    private void reloadCards() {
        for (int suitCount = 0; suitCount < CardDeck.SUIT_COUNT; suitCount++) {
            for (int cardCount = 0; cardCount < CardDeck.CARD_SUIT_COUNT; cardCount++) {
                var cardField = cards[suitCount][cardCount];
                var currentCard = deck.getCard(suitCount, cardCount);
                cardField.setIcon(currentCard.getVisibleCardImage());
            }
        }
    }

    private void resetMatch() {
        matchedCards.forEach(Card::resetCard);
        matchedCards.clear();
        this.reloadCards();
        this.matchResetTimer.restart();
        this.matchResetTimer.stop();
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

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

