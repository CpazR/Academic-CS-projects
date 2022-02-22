package com.company;

import java.util.LinkedList;
import java.util.Queue;

public class GameContext {
    private final int width;
    private final int height;
    private final MineButton[][] gameButtons;

    private int moveCount;
    private int spacesRemaining;
    private int totalBombs;

    private GameContextState contextState;

    private int secondCount;

    GameContext(int gridWidth, int gridHeight, int bombCount) {
        this.width = gridWidth;
        this.height = gridHeight;

        gameButtons = new MineButton[gridWidth][gridHeight];
        totalBombs = bombCount;
        spacesRemaining = gridWidth * gridHeight;

        contextState = GameContextState.PLAYING;

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                gameButtons[i][j] = new MineButton(i, j, false);
            }
        }
    }

    /**
     * Expose button and perform algorithm if there are no adjacent bombs
     */
    public void exposeClickedButton(int rowPos, int colPos) {
        var mainButton = gameButtons[rowPos][colPos];

        // Expose first button, and depending on state, update game state
        mainButton.setAdjacentBombCount(countAdjacentBombs(rowPos, colPos));
        var mainButtonState = mainButton.expose();

        // First move? Plant bombs around first picked spot
        if (spacesRemaining == getWidth() * getHeight()) {
            plantBombs(rowPos, colPos);
        }
        spacesRemaining--;

        checkGameState(mainButtonState);

        if (mainButtonState.equals(MineButtonState.EXPOSED_BLANK)) {
            // Run algorithm to show adjacent blank buttons
            exposeAdjacentBlanks(rowPos, colPos);
        }
    }

    private void checkGameState(MineButtonState lastExposedButtonState) {
        if (spacesRemaining == 0) {
            gameEnd(true);
        }

        if (lastExposedButtonState.equals(MineButtonState.EXPOSED_BOMB)) {
            gameEnd(false);
        }
    }

    /**
     * Iterate over adjacent buttons and their adjacent buttons until buttons with adjacent bombs have been found
     */
    private void exposeAdjacentBlanks(int rowPos, int colPos) {
        // Iterate starting from rowPos - 1, colPos - 1 and add blank buttons to stack
        var mineButtonStack = new LinkedList<MineButton>();
        addAdjacentBlanks(rowPos, colPos, mineButtonStack);

        while (!mineButtonStack.isEmpty()) {
            var currentButton = mineButtonStack.pop();
            var adjacentBombs = countAdjacentBombs(currentButton.getRowPosition(), currentButton.getColumnPosition());

            currentButton.setAdjacentBombCount(adjacentBombs);
            var currButtonExposeState = currentButton.expose();
            spacesRemaining--;

            checkGameState(currButtonExposeState);

            // Only search and add adjacent
            if (currentButton.getAdjacentBombCount() == 0) {
                addAdjacentBlanks(currentButton.getRowPosition(), currentButton.getColumnPosition(), mineButtonStack);
            }
        }
    }

    private void addAdjacentBlanks(int startRowPos, int startColPos, Queue<MineButton> mineButtonStackReference) {
        // Bound initial scan row and column
        var startScanRow = Math.max(startRowPos - 1, 0);
        var startScanCol = Math.max(startColPos - 1, 0);
        var endScanRow = Math.min(startRowPos + 2, width);
        var endScanCol = Math.min(startColPos + 2, height);

        // Iterate over 3 x 3 grid around button
        for (int scanRow = startScanRow; scanRow < endScanRow; scanRow++) {
            for (int scanCol = startScanCol; scanCol < endScanCol; scanCol++) {
                var currentButton = gameButtons[scanRow][scanCol];
                if (!mineButtonStackReference.contains(currentButton) && currentButton.getButtonState().equals(MineButtonState.HIDDEN) && !currentButton.areBomb()) {
                    mineButtonStackReference.add(currentButton);
                }
            }
        }
    }

    private int countAdjacentBombs(int startRowPos, int startColPos) {
        // Bound initial scan row and column
        var startScanRow = Math.max(startRowPos - 1, 0);
        var startScanCol = Math.max(startColPos - 1, 0);
        var endScanRow = Math.min(startRowPos + 2, width);
        var endScanCol = Math.min(startColPos + 2, height);

        int adjacentBombCount = 0;

        // Iterate over 3 x 3 grid around button
        for (int scanRow = startScanRow; scanRow < endScanRow; scanRow++) {
            for (int scanCol = startScanCol; scanCol < endScanCol; scanCol++) {
                var currentButton = gameButtons[scanRow][scanCol];
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

    public void plantBombs(int firstMoveRow, int firstMoveCol) {
        var bombIterator = 0;
        while (bombIterator < totalBombs) {
            for (int currRow = 0; currRow < getHeight(); currRow++) {
                for (int currCol = 0; currCol < getWidth(); currCol++) {
                    if (currRow != firstMoveRow && currCol != firstMoveCol) {
                        if (Math.random() <= 0.1) {
                            bombIterator++;
                            gameButtons[currRow][currCol].setBomb();
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

    public void flagButton(int gridRow, int gridCol) {
        var button = gameButtons[gridRow][gridCol];

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
        if (!didWin) {
            // Immediately show all spaces
            for (int currRow = 0; currRow < getHeight(); currRow++) {
                for (int currCol = 0; currCol < getWidth(); currCol++) {
                    var currentButton = gameButtons[currRow][currCol];
                    var adjacentBombs = countAdjacentBombs(currentButton.getRowPosition(), currentButton.getColumnPosition());

                    currentButton.setAdjacentBombCount(adjacentBombs);
                    currentButton.expose();
                }
            }
            contextState = GameContextState.LOST;
            System.out.println("Game Lost!");
        } else {
            contextState = GameContextState.WON;
            System.out.println("Game Won!");
        }
    }
}
