package com.company;

import java.util.Stack;

public class GameContext {
    private int width;
    private int height;
    private MineButton[][] gameButtons;

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
                if (Math.random() <= 0.25) {
                    gameButtons[i][j].setBomb();
                }
            }
        }
    }

    private void exposeAdjacentBlanks(int rowPos, int colPos) {
        // Iterate starting from rowPos - 1, colPos - 1 and add blank buttons to stack
        var mineButtonStack = new Stack<MineButton>();

        addAdjacentBlanks(rowPos, colPos, mineButtonStack);

        while (!mineButtonStack.isEmpty()) {
            var currentButton = mineButtonStack.pop();
            currentButton.expose();

            addAdjacentBlanks(currentButton.getRowPosition(), currentButton.getColumnPosition(), mineButtonStack);
        }
    }

    private void addAdjacentBlanks(int startRowPos, int startColPos, Stack<MineButton> mineButtonStackReference) {
        // Bound initial scan row and column
        var startScanRow = Math.max(startRowPos - 1, 0);
        var startScanCol = Math.max(startColPos - 1, 0);
        var endScanRow = Math.min(startScanRow + 3, width);
        var endScanCol = Math.min(startScanCol + 3, height);

        // Iterate over 3 x 3 grid around button
        for (int i = startScanRow; i < endScanRow; i++) {
            for (int j = startScanCol; j < endScanCol; j++) {
                var currentButton = gameButtons[i][j];
                // TODO: add bomb count check
                if (!mineButtonStackReference.contains(currentButton) &&
                        currentButton.getButtonState().equals(MineButtonState.HIDDEN) && !currentButton.isBomb()) {
                    mineButtonStackReference.add(currentButton);
                }
            }
        }
    }

    private int countAdjacentBombs(int startRowPos, int startColPos) {
        // Bound initial scan row and column
        var startScanRow = Math.min(Math.max(startRowPos - 1, 0), width);
        var startScanCol = Math.min(Math.max(startColPos - 1, 0), height);

        int adjacentBombCount = 0;

        // Iterate over 3 x 3 grid around button
        for (int i = startScanCol; i < startScanRow + 3; i++) {
            for (int j = startScanRow; j < startScanRow + 3; j++) {
                var currentButton = gameButtons[i][j];
                if (currentButton.getButtonState().equals(MineButtonState.HIDDEN) && !currentButton.isBomb()) {
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

    public void exposeButton(int rowPos, int colPos) {
        var mainButton = gameButtons[rowPos][colPos];

        // Expose first button, and depending on state, update game state
        var mainButtonState = mainButton.expose();
        switch (mainButtonState) {
            case EXPOSED_BLANK:
                // Run algorithm to show adjacent blank buttons
                exposeAdjacentBlanks(rowPos, colPos);
                break;
            case EXPOSED_NUMBER:
                // Only expose the main button, update number shown
                break;
            case EXPOSED_BOMB:
                // End game
                break;
        }
    }

    public void plantBombs() {

    }

    public MineButton[][] getGameButtons() {
        return gameButtons;
    }
}
