package com.company;

import javax.swing.*;

public class ApplicationContext extends JFrame {

    /**
     * Stores the game state
     */
    private final GameContext context;

    /**
     * Pieces of main page
     */
    private final JPanel mainPanel = new JPanel();

    private final JPanel gamePanel = new JPanel();
    private JButton[][] gameButtons;

    private final JPanel scorePanel = new JPanel();
    private final JMenuBar mainMenuBar = new JMenuBar();
    private final JMenuItem newGameButton = new JMenuItem("New");
    private final JMenuItem setupButton = new JMenuItem("Setup");
    private final JMenu gameDropdownItem = new JMenu("Game");

    /**
     * New game pieces
     */


    ApplicationContext(GameContext context) {
        super("Totally Not Minesweep");
        this.context = context;

        menuBarSetup();
        gamePanelSetup(context);
//        mainWindowSetup();

        setSize(400, 350);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void menuBarSetup() {
        gameDropdownItem.add(newGameButton);
        gameDropdownItem.add(setupButton);
        mainMenuBar.add(gameDropdownItem);

        setJMenuBar(mainMenuBar);
    }

    private void gamePanelSetup(GameContext context) {
        gameButtons = new JButton[context.getWidth()][context.getHeight()];
        for (int i = 0; i < context.getWidth(); i++) {
            for (int j = 0; j < context.getHeight(); j++) {
                gameButtons[i][j] = new MineButton();
                gamePanel.add(gameButtons[i][j]);
            }
        }
    }

    private void mainWindowSetup() {
        mainPanel.add(scorePanel);
        mainPanel.add(gamePanel);
        add(mainPanel);
    }

}
