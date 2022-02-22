package com.company;

import javax.swing.*;
import java.awt.*;

public class MineButton extends JButton {
    private MineButtonState buttonState;

    private Icon currentIcon = unexposedIcon;
    private static final Icon unexposedIcon = new ImageIcon("./assets/facingDown.png");
    private static final Icon unexposedFlaggedIcon = new ImageIcon("./assets/flagged.png");
    private static final Icon blankIcon = new ImageIcon("./assets/0.png");
    private static final Icon bombIcon = new ImageIcon("./assets/bomb.png");

    private final int xPosition;
    private final int yPosition;

    private boolean isBomb;
    private int adjacentBombCount;

    public MineButton(int xPosition, int yPosition, boolean isBomb) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.isBomb = isBomb;
        this.buttonState = MineButtonState.HIDDEN;
        setIcon(currentIcon);
    }

    /**
     * Force size of button to uniform icon size
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(currentIcon.getIconWidth(), currentIcon.getIconHeight());
    }

    /**
     * If a bomb, effectively end the game. Otherwise, simply change to image
     */
    public MineButtonState expose() {
        if (!buttonState.equals(MineButtonState.FLAGGED)) {
            if (!isBomb) {
                buttonState = (adjacentBombCount == 0) ? MineButtonState.EXPOSED_BLANK : MineButtonState.EXPOSED_NUMBER;
            } else {
                buttonState = MineButtonState.EXPOSED_BOMB;
            }
            updateIcon();
        }

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

    public int getXPosition() {
        return xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public int getAdjacentBombCount() {
        return adjacentBombCount;
    }

    public void setAdjacentBombCount(int newBombCount) {
        this.adjacentBombCount = newBombCount;
    }

    private void updateIcon() {
        currentIcon = blankIcon;
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
        return "GameButton: [" + xPosition + ", " + yPosition + "], is a bomb: " + isBomb;
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
