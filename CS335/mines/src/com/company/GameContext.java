package com.company;

import java.util.LinkedList;
import java.util.Queue;

public class GameContext {
    private final int width;
    private final int height;
    private final MineButton[][] gameButtons;

    private int moveCount;
    private int spacesRemaining;
    private final int totalBombs;

    private GameContextState contextState;


    GameContext(int gridWidth, int gridHeight, int bombCount) {
        this.width = gridWidth;
        this.height = gridHeight;

        gameButtons = new MineButton[gridWidth][gridHeight];
        totalBombs = bombCount;
        spacesRemaining = gridWidth * gridHeight;

        contextState = GameContextState.PLAYING;

        for (int xPos = 0; xPos < getWidth(); xPos++) {
            for (int yPos = 0; yPos < getHeight(); yPos++) {
                gameButtons[xPos][yPos] = new MineButton(xPos, yPos, false);
            }
        }
    }

    /**
     * Expose button and perform algorithm if there are no adjacent bombs
     */
    public void exposeClickedButton(int xPos, int yPos) {
        var mainButton = gameButtons[xPos][yPos];

        // Expose first button, and depending on state, update game state
        mainButton.setAdjacentBombCount(countAdjacentBombs(xPos, yPos));
        var mainButtonState = mainButton.expose();

        // First move? Plant bombs around first picked spot
        if (spacesRemaining == getWidth() * getHeight()) {
            plantBombs(xPos, yPos);
        }
        spacesRemaining--;

        checkGameState(mainButtonState);

        if (mainButtonState.equals(MineButtonState.EXPOSED_BLANK)) {
            // Run algorithm to show adjacent blank buttons
            exposeAdjacentBlanks(xPos, yPos);
        }
    }

    private void checkGameState(MineButtonState lastExposedButtonState) {
        if (spacesRemaining == totalBombs) {
            gameEnd(true);
        }

        if (lastExposedButtonState.equals(MineButtonState.EXPOSED_BOMB)) {
            gameEnd(false);
        }
    }

    /**
     * Iterate over adjacent buttons and their adjacent buttons until buttons with adjacent bombs have been found
     */
    private void exposeAdjacentBlanks(int xPos, int yPos) {
        // Iterate starting from yPos - 1, xPos - 1 and add blank buttons to stack
        var mineButtonsQueue = new LinkedList<MineButton>();
        addAdjacentBlanks(xPos, yPos, mineButtonsQueue);

        while (!mineButtonsQueue.isEmpty()) {
            var currentButton = mineButtonsQueue.pop();
            var adjacentBombs = countAdjacentBombs(currentButton.getXPosition(), currentButton.getYPosition());

            currentButton.setAdjacentBombCount(adjacentBombs);
            var currButtonExposeState = currentButton.expose();
            spacesRemaining--;

            checkGameState(currButtonExposeState);

            // Only search and add adjacent
            if (currentButton.getAdjacentBombCount() == 0) {
                addAdjacentBlanks(currentButton.getXPosition(), currentButton.getYPosition(), mineButtonsQueue);
            }
        }
    }

    private void addAdjacentBlanks(int startXPos, int startYPos, Queue<MineButton> mineButtonStackReference) {
        // Bound scan positions
        var startScanX = Math.max(startXPos - 1, 0);
        var startScanY = Math.max(startYPos - 1, 0);
        var endScanX = Math.min(startXPos + 2, width);
        var endScanY = Math.min(startYPos + 2, height);

        // Iterate over 3 x 3 grid around button
        for (int scanX = startScanX; scanX < endScanX; scanX++) {
            for (int scanY = startScanY; scanY < endScanY; scanY++) {
                var currentButton = gameButtons[scanX][scanY];
                if (!mineButtonStackReference.contains(currentButton) && currentButton.getButtonState().equals(MineButtonState.HIDDEN) && !currentButton.areBomb()) {
                    mineButtonStackReference.add(currentButton);
                }
            }
        }
    }

    private int countAdjacentBombs(int startXPos, int startYPos) {
        // Bound scan positions
        var startScanX = Math.max(startXPos - 1, 0);
        var startScanY = Math.max(startYPos - 1, 0);
        var endScanX = Math.min(startXPos + 2, width);
        var endScanY = Math.min(startYPos + 2, height);

        int adjacentBombCount = 0;

        // Iterate over 3 x 3 grid around button
        for (int scanX = startScanX; scanX < endScanX; scanX++) {
            for (int scanY = startScanY; scanY < endScanY; scanY++) {
                var currentButton = gameButtons[scanX][scanY];
                if (currentButton.areBomb()) {
                    adjacentBombCount++;
                }
            }
        }
        return adjacentBombCount;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void addMove() {
        moveCount++;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getSpacesRemaining() {
        return spacesRemaining;
    }

    public GameContextState getContextState() {
        return contextState;
    }

    public void plantBombs(int firstMoveX, int firstMoveY) {
        var bombIterator = 0;
        while (bombIterator < totalBombs) {
            for (int currX = 0; currX < getWidth(); currX++) {
                for (int currY = 0; currY < getHeight(); currY++) {
                    if (currY != firstMoveY && currX != firstMoveX) {
                        if (Math.random() <= 0.1) {
                            bombIterator++;
                            gameButtons[currX][currY].setBomb();
                        }
                    }
                    if (bombIterator == totalBombs) {
                        break;
                    }
                }
                if (bombIterator == totalBombs) {
                    break;
                }
            }
        }
    }

    public MineButton[][] getGameButtons() {
        return gameButtons;
    }

    public void flagButton(int gridX, int gridY) {
        var button = gameButtons[gridX][gridY];

        switch (button.getButtonState()) {
            case HIDDEN:
                button.setFlagged(true);
                break;
            case FLAGGED:
                button.setFlagged(false);
                break;
        }
    }

    private void gameEnd(boolean didWin) {
        // Immediately show all spaces
        for (int currX = 0; currX < getWidth(); currX++) {
            for (int currY = 0; currY < getHeight(); currY++) {
                var currentButton = gameButtons[currX][currY];
                var adjacentBombs = countAdjacentBombs(currentButton.getXPosition(), currentButton.getYPosition());

                currentButton.setFlagged(false);
                currentButton.setAdjacentBombCount(adjacentBombs);
                currentButton.expose();
            }
        }
        if (!didWin) {
            contextState = GameContextState.LOST;
            System.out.println("Game Lost!");
        } else {
            contextState = GameContextState.WON;
            System.out.println("Game Won!");
        }
    }
}
