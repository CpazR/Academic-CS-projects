package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ApplicationContext extends JFrame {

    /**
     * Stores the game state
     */
    private GameContext context;

    /**
     * Pieces of main page
     */
    private final JPanel mainPanel = new JPanel();

    private final JPanel gamePanel = new JPanel();

    private final JPanel scorePanel = new JPanel();
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
    }

    private void beginNewGame(int gridWidth, int gridHeight, int bombCount) {
        this.context = new GameContext(gridWidth, gridHeight, bombCount);
        gamePanel.removeAll();
        gamePanelSetup(context);
        gamePanel.revalidate();
        pack();
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
        gamePanel.setLayout(new GridLayout(context.getWidth(), context.getHeight()));
        var gameButtons = context.getGameButtons();
        for (int i = 0; i < context.getWidth(); i++) {
            for (int j = 0; j < context.getHeight(); j++) {
                var button = gameButtons[i][j];
                System.out.println(button);
                gamePanel.add(button);

                var finalI = i;
                var finalJ = j;
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            context.exposeClickedButton(finalI, finalJ);
                            context.addMove();
                        }

                        if (SwingUtilities.isRightMouseButton(e)) {
                            context.flagButton(finalI, finalJ);
                        }
                    }
                });
            }
        }
    }

    private void mainWindowSetup() {
//        mainPanel.add(scorePanel);
        mainPanel.add(gamePanel);
        add(mainPanel);
    }

    private void establishListeners() {
        newBeginnerGameButton.addActionListener(e -> beginNewGame(9, 9, 10));
        newIntermediateGameButton.addActionListener(e -> beginNewGame(16, 16, 40));
        newExpertGameButton.addActionListener(e -> beginNewGame(24, 20, 99));
        newCustomGameButton.addActionListener(e -> {
            System.out.println("Need to add a window for this");
        });

        exitGameButton.addActionListener(e -> System.exit(0));
    }

}
