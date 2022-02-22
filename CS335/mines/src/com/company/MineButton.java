package com.company;

import javax.swing.*;

public class MineButton extends JButton {
    private MineButtonState buttonState;

    private Icon currentIcon = unexposedIcon;
    private static final Icon unexposedIcon = new ImageIcon("./assets/facingDown.png");
    private static final Icon unexposedFlaggedIcon = new ImageIcon("./assets/flagged.png");
    private static final Icon blankIcon = new ImageIcon("./assets/0.png");
    private static final Icon bombIcon = new ImageIcon("./assets/bomb.png");

//    private final String unexposedText = " ";
//    private final String unexposedFlaggedText = "!";
//    private final String blankText = "_";
//    private final String bombText = "*";
//    private String currentText = unexposedText;

    private final int columnPosition;
    private final int rowPosition;

    private boolean isBomb;
    private int adjacentBombCount;

    public MineButton(int rowPosition, int columnPosition, boolean isBomb) {
        this.rowPosition = rowPosition;
        this.columnPosition = columnPosition;
        this.isBomb = isBomb;
        this.buttonState = MineButtonState.HIDDEN;
        setSize(unexposedIcon.getIconWidth(), unexposedIcon.getIconHeight());
        setIcon(unexposedIcon);
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

        updateIcon();
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

    private void updateIcon() {
        var currentIcon = blankIcon;
        switch (buttonState) {
            case EXPOSED_NUMBER:
                currentIcon = new ImageIcon("./assets/" + adjacentBombCount + ".png");
                break;
            case EXPOSED_BOMB:
                currentIcon = bombIcon;
                break;
        }
        setIcon(currentIcon);
    }

    @Override
    public String toString() {
        return "GameButton: [" + rowPosition + ", " + columnPosition + "], is a bomb: " + isBomb;
    }

    public void setFlagged(boolean isFlagged) {
        if (isFlagged) {
            buttonState = MineButtonState.FLAGGED;
            setIcon(unexposedFlaggedIcon);
        } else {
            buttonState = MineButtonState.HIDDEN;
            setIcon(unexposedIcon);
        }
    }
}
