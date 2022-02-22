package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class ApplicationContext extends JFrame {

    /**
     * Stores the game state
     */
    private GameContext context;

    private int secondsCount;

    /**
     * Pieces of main page
     */
    private final JPanel mainPanel = new JPanel();

    private final JPanel gamePanel = new JPanel();

    private final JPanel scorePanel = new JPanel();
    private final String moveCountPrefix = "Moves taken: ";
    private final String activeTimePrefix = "Playing for ";
    private final JLabel moveCountLabel = new JLabel(moveCountPrefix);
    private final JLabel activeTimeLabel = new JLabel(activeTimePrefix);
    private final JLabel gameEndLabel = new JLabel();

    private final JMenuBar mainMenuBar = new JMenuBar();
    private final JMenuItem newBeginnerGameButton = new JMenuItem("Beginner");
    private final JMenuItem newIntermediateGameButton = new JMenuItem("Intermediate");
    private final JMenuItem newExpertGameButton = new JMenuItem("Expert");
    private final JMenuItem newCustomGameButton = new JMenuItem("Custom");
    private final JMenuItem exitGameButton = new JMenuItem("Exit");
    private final JMenu gameDropdownItem = new JMenu("Game");

    /**
     * New game pieces
     */


    ApplicationContext() {
        super("Totally Not Minesweep");

        menuBarSetup();
        beginNewGame(9, 9, 10);
        mainWindowSetup();

        establishListeners();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
        pack();

        // Initialize timer
        var secondCounter = new Timer();
        var counterTask = new TimerTask() {
            @Override
            public void run() {
                if (context.getContextState().equals(GameContextState.PLAYING)) {
                    secondsCount++;
                    updateLabels();
                } else {
                    secondCounter.cancel();
                }
            }
        };

        secondCounter.scheduleAtFixedRate(counterTask, 1000, 1000);
    }

    private void beginNewGame(int gridWidth, int gridHeight, int bombCount) {
        this.context = new GameContext(gridWidth, gridHeight, bombCount);
        gamePanel.removeAll();
        gamePanelSetup(context);
        gamePanel.revalidate();

        scorePanel.removeAll();
        scorePanelSetup(context);
        scorePanel.revalidate();
        pack();
        centerWindow();
    }

    private void menuBarSetup() {
        gameDropdownItem.add(newBeginnerGameButton);
        gameDropdownItem.add(newIntermediateGameButton);
        gameDropdownItem.add(newExpertGameButton);
        gameDropdownItem.add(newCustomGameButton);
        gameDropdownItem.add(exitGameButton);
        mainMenuBar.add(gameDropdownItem);

        setJMenuBar(mainMenuBar);
    }

    private void gamePanelSetup(GameContext context) {
        // The grid layout is rows first and this is dumb. That is all.
        gamePanel.setLayout(new GridLayout(context.getHeight(), context.getWidth()));
        var gameButtons = context.getGameButtons();
        for (int currY = 0; currY < context.getHeight(); currY++) {
            // Since components are added row first, iterate in a less efficient manner.
            for (int currX = 0; currX < context.getWidth(); currX++) {
                var button = gameButtons[currX][currY];
                System.out.println(button);
                gamePanel.add(button);

                var finalX = currX;
                var finalY = currY;
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        // Only accept mouse presses when game is active
                        if (context.getContextState().equals(GameContextState.PLAYING)) {
                            if (SwingUtilities.isLeftMouseButton(e)) {
                                context.exposeClickedButton(finalX, finalY);
                                context.addMove();
                                System.out.println("Exposed: " + context.getGameButtons()[finalX][finalY]);

                                var gameState = context.getContextState();
                                if (gameState.equals(GameContextState.PLAYING)) {
                                    updateLabels();
                                } else {
                                    updateLabels(true, gameState.equals(GameContextState.WON));
                                }
                            }

                            if (SwingUtilities.isRightMouseButton(e)) {
                                context.flagButton(finalX, finalY);
                                System.out.println("Flagged: " + context.getGameButtons()[finalX][finalY]);
                            }
                        }
                    }
                });
            }
        }
    }

    private void scorePanelSetup(GameContext context) {
        scorePanel.setLayout(new GridLayout());
        moveCountLabel.setText(moveCountPrefix + context.getMoveCount());
        activeTimeLabel.setText(activeTimePrefix + context.getSpacesRemaining());
        scorePanel.add(moveCountLabel);
        scorePanel.add(activeTimeLabel);
    }

    private void mainWindowSetup() {
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(scorePanel);
        mainPanel.add(gamePanel);
        add(mainPanel);
    }

    private void updateLabels() {
        updateLabels(false, false);
    }

    private void updateLabels(boolean gameEnd, boolean gameWon) {
        if (!gameEnd) {
            moveCountLabel.setText(moveCountPrefix + context.getMoveCount());
            activeTimeLabel.setText(activeTimePrefix + secondsCount + " second");
        } else {
            scorePanel.remove(moveCountLabel);
            scorePanel.remove(activeTimeLabel);
            String gameWonMessage = "You win!";
            String gameLostMessage = "You lost!";
            gameEndLabel.setText(gameWon ? gameWonMessage : gameLostMessage);
            System.out.println(gameEndLabel.getText());
            scorePanel.add(gameEndLabel);
            revalidate();
        }
    }

    private void establishListeners() {
        // Button listeners
        newBeginnerGameButton.addActionListener(e -> beginNewGame(9, 9, 10));
        newIntermediateGameButton.addActionListener(e -> beginNewGame(16, 16, 40));
        newExpertGameButton.addActionListener(e -> beginNewGame(24, 20, 99));
        newCustomGameButton.addActionListener(e -> {
            System.out.println("Need to add a window for this");
        });

        exitGameButton.addActionListener(e -> System.exit(0));
    }

    public void centerWindow() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

}
