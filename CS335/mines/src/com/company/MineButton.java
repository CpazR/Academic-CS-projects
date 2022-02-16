package com.company;

import javax.swing.*;

public class MineButton extends JButton {
    private MineButtonState buttonState;

    private Icon currentIcon = unexposedIcon;
    private static final Icon unexposedIcon = new ImageIcon("./assets/unexposed_icon.png");
    private static final Icon unexposedFlaggedIcon = new ImageIcon("./assets/unexposed_flagged_icon.png");
    private static final Icon blankIcon = new ImageIcon("./assets/blank_icon.png");
    private static final Icon numberedIcon = new ImageIcon("./assets/numbered_icon.png");
    private static final Icon bombIcon = new ImageIcon("./assets/bomb_icon.png");

    private final int columnPosition;
    private final int rowPosition;

    private final boolean isBomb;
    private int adjacentBombCount;

    public MineButton(int columnPosition, int rowPosition, boolean isBomb) {
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
        this.isBomb = isBomb;
        this.buttonState = MineButtonState.HIDDEN;
    }

    /**
     * If a bomb, effectively end the game. Otherwise, expose adjacent empty spaces via an algorithm
     */
    private void expose() {
        if (!isBomb) {
            buttonState = (adjacentBombCount == 0) ? MineButtonState.EXPOSED_BLANK : MineButtonState.EXPOSED_NUMBER;
        } else {
            buttonState = MineButtonState.EXPOSED_BOMB;
        }

        updateImage();
    }

    private void updateImage() {
        switch (buttonState) {
            case EXPOSED_BLANK:
                currentIcon = blankIcon;
                break;
            case EXPOSED_NUMBER:
                currentIcon = numberedIcon;
                break;
            case EXPOSED_BOMB:
                currentIcon = bombIcon;
                break;
        }
        setIcon(currentIcon);
    }
}
