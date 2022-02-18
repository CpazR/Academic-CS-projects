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

    public MineButton(int columnPosition, int rowPosition, boolean isBomb) {
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
        this.isBomb = isBomb;
        this.buttonState = MineButtonState.HIDDEN;
        setText(currentText);
    }

    /**
     * If a bomb, effectively end the game. Otherwise, simply change to image
     */
    public MineButtonState expose() {
        if (!isBomb) {
            buttonState = (adjacentBombCount == 0) ? MineButtonState.EXPOSED_BLANK : MineButtonState.EXPOSED_NUMBER;
        } else {
            buttonState = MineButtonState.EXPOSED_BOMB;
        }

        updateImage();
        return buttonState;
    }

    public MineButtonState getButtonState() {
        return buttonState;
    }

    public boolean isBomb() {
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

    private void updateImage() {
        switch (buttonState) {
            case EXPOSED_BLANK:
                currentText = blankText;
                break;
            case EXPOSED_NUMBER:
                currentText = String.valueOf(adjacentBombCount);
                break;
            case EXPOSED_BOMB:
                currentText = bombText;
                break;
        }
//        setIcon(currentIcon);
        setText(currentText);
    }

    @Override
    public String toString() {
        return "GameButton: [" + rowPosition + ", " + columnPosition + "], is a bomb: " + isBomb;
    }
}
