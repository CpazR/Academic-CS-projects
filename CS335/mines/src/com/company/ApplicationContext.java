package com.company;

import javax.swing.*;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;
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
    private final String moveCountPrefix = "Found left: ";
    private final String activeTimePrefix = "Time: ";
    private final JLabel foundBombsLabel = new JLabel(moveCountPrefix);
    private final JLabel activeTimeLabel = new JLabel(activeTimePrefix);
    private final JLabel gameEndLabel = new JLabel();

    private final JMenuBar mainMenuBar = new JMenuBar();
    private final JMenuItem newBeginnerGameButton = new JMenuItem("Beginner game");
    private final JMenuItem newIntermediateGameButton = new JMenuItem("Intermediate game");
    private final JMenuItem newExpertGameButton = new JMenuItem("Expert game");
    private final JMenuItem newCustomGameButton = new JMenuItem("Custom game");
    private final JMenuItem exitGameButton = new JMenuItem("Exit");
    private final JMenu gameDropdownItem = new JMenu("Game");
    private final JMenuItem helpItem = new JMenuItem("Help");

    ApplicationContext() {
        super("Totally Not Minesweep");

        menuBarSetup();
        beginNewGame(9, 9, 10);
        mainWindowSetup();

        establishListeners();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
        pack();

        var secondCounter = new Timer();
        var counterTask = new TimerTask() {
            @Override
            public void run() {
                if (context.getContextState().equals(GameContextState.PLAYING)) {
                    secondsCount++;
                    activeTimeLabel.setText(activeTimePrefix + secondsCount + " second");
                    revalidate();
                } else {
                    secondCounter.cancel();
                }
            }
        };

        // Run timer repeatedly on 1 second intervals
        secondCounter.scheduleAtFixedRate(counterTask, 1000, 1000);
    }

    /**
     * Given the size of the grid and number of bombs, clear the menus and reset the game
     */
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
        setResizable(false);
    }

    private void menuBarSetup() {
        gameDropdownItem.add(newBeginnerGameButton);
        gameDropdownItem.add(newIntermediateGameButton);
        gameDropdownItem.add(newExpertGameButton);
        gameDropdownItem.addSeparator();
        gameDropdownItem.add(newCustomGameButton);
        gameDropdownItem.addSeparator();
        gameDropdownItem.add(exitGameButton);
        mainMenuBar.add(gameDropdownItem);
        var buttonDimension = new Dimension(40, 20);
        helpItem.setMinimumSize(buttonDimension);
        helpItem.setPreferredSize(buttonDimension);
        helpItem.setMaximumSize(buttonDimension);
        mainMenuBar.add(helpItem);
        setJMenuBar(mainMenuBar);
    }

    /**
     * Reset button layouts and assign listeners
     */
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

                                var gameState = context.getContextState();
                                if (gameState.equals(GameContextState.PLAYING)) {
                                    updateLabels();
                                } else {
                                    updateLabels(true, gameState.equals(GameContextState.WON));
                                }
                            }

                            if (SwingUtilities.isRightMouseButton(e)) {
                                context.flagButton(finalX, finalY);
                                updateLabels();
                            }
                        }
                    }
                });
            }
        }
    }

    private void scorePanelSetup(GameContext context) {
        scorePanel.setLayout(new GridLayout());
        foundBombsLabel.setText(moveCountPrefix + context.getBombsLeft());
        activeTimeLabel.setText(activeTimePrefix + secondsCount + " second");
        scorePanel.add(foundBombsLabel);
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
            foundBombsLabel.setText(moveCountPrefix + context.getBombsLeft());
        } else {
            scorePanel.remove(foundBombsLabel);
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
            var customGamePanel = new CustomGamePanel();
            var result = JOptionPane.showConfirmDialog(this, customGamePanel, "Custom game options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                beginNewGame(customGamePanel.getColValue(), customGamePanel.getRowValue(), customGamePanel.getBombCountValue());
            }
        });
        exitGameButton.addActionListener(e -> System.exit(0));

        helpItem.addActionListener(e -> {
            var helpPanel = new HelpGamePanel();
            JOptionPane.showConfirmDialog(this, helpPanel, "Help", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
        });
    }

    public void centerWindow() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

}
