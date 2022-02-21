package com.company;

import java.util.LinkedList;
import java.util.Queue;

public class GameContext {
    private final int width;
    private final int height;
    private final MineButton[][] gameButtons;

    private int moveCount;
    private int bombCount;

    private int secondCount;

    GameContext(int gridWidth, int gridHeight) {
        this.width = gridWidth;
        this.height = gridHeight;

        gameButtons = new MineButton[gridWidth][gridHeight];
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                gameButtons[i][j] = new MineButton(i, j, false);
//                if (Math.random() <= 0.1) {
                if (bombCount < 5) {
                    bombCount++;
                    gameButtons[i][j].setBomb();
                }
            }
        }
    }

    public void exposeButton(int rowPos, int colPos) {
        var mainButton = gameButtons[rowPos][colPos];

        // Expose first button, and depending on state, update game state
        mainButton.setAdjacentBombCount(countAdjacentBombs(rowPos, colPos));
        var mainButtonState = mainButton.expose();
        switch (mainButtonState) {
            case EXPOSED_BLANK:
                // Run algorithm to show adjacent blank buttons
                exposeAdjacentBlanks(rowPos, colPos);
                break;
            case EXPOSED_BOMB:
                // End game
                break;
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
            currentButton.expose();

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
        var endScanRow = Math.min(startRowPos + 1, width);
        var endScanCol = Math.min(startColPos + 1, height);

        // Iterate over 3 x 3 grid around button
        for (int scanRow = startScanRow; scanRow <= endScanRow; scanRow++) {
            for (int scanCol = startScanCol; scanCol <= endScanCol; scanCol++) {
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
        var endScanRow = Math.min(startRowPos + 1, width);
        var endScanCol = Math.min(startColPos + 1, height);

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

    public void plantBombs() {

    }

    public MineButton[][] getGameButtons() {
        return gameButtons;
    }
}
