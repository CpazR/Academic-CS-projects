package com.company;

import javax.swing.*;

public class MineButton extends JButton {
    private MineButtonState buttonState;

//    private Icon currentIcon = unexposedIcon;
//    private static final Icon unexposedIcon = new ImageIcon("./assets/unexposed_icon.png");
//    private static final Icon unexposedFlaggedIcon = new ImageIcon("./assets/unexposed_flagged_icon.png");
//    private static final Icon blankIcon = new ImageIcon("./assets/blank_icon.png");
//    private static final Icon numberedIcon = new ImageIcon("./assets/numbered_icon.png");
//    private static final Icon bombIcon = new ImageIcon("./assets/bomb_icon.png");

    private final String unexposedText = " ";
    private final String unexposedFlaggedText = "!";
    private final String blankText = "_";
    private final String bombText = "*";
    private String currentText = unexposedText;

    private final int columnPosition;
    private final int rowPosition;

    private boolean isBomb;
    private int adjacentBombCount;

    public MineButton(int rowPosition, int columnPosition, boolean isBomb) {
        this.rowPosition = rowPosition;
        this.columnPosition = columnPosition;
        this.isBomb = isBomb;
        this.buttonState = MineButtonState.HIDDEN;
        setText(currentText);
    }

    /**
     * If a bomb, effectively end the game. Otherwise, simply change to image
     */
    public MineButtonState expose(int adjacentBombCount) {
        if (!isBomb) {
            buttonState = (adjacentBombCount == 0) ? MineButtonState.EXPOSED_BLANK : MineButtonState.EXPOSED_NUMBER;
        } else {
            buttonState = MineButtonState.EXPOSED_BOMB;
        }
        this.adjacentBombCount = adjacentBombCount;

        updateText();
        return buttonState;
    }

    public MineButtonState getButtonState() {
        return buttonState;
    }

    public boolean areBomb() {
        return isBomb;
    }

    public void setBomb() {
        isBomb = true;
    }

    public int getColumnPosition() {
        return columnPosition;
    }

    public int getRowPosition() {
        return rowPosition;
    }

    public int getAdjacentBombCount() {
        return adjacentBombCount;
    }

    public void setAdjacentBombCount(int newBombCount) {
        this.adjacentBombCount = newBombCount;
    }

    private void updateText() {
        switch (buttonState) {
            case EXPOSED_BLANK -> currentText = blankText;
            case EXPOSED_NUMBER -> currentText = String.valueOf(adjacentBombCount);
            case EXPOSED_BOMB -> currentText = bombText;
        }
        setText(currentText);
    }

    @Override
    public String toString() {
        return "GameButton: [" + rowPosition + ", " + columnPosition + "], is a bomb: " + isBomb;
    }
}
