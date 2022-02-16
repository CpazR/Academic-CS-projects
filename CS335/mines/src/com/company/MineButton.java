package com.company;

import javax.swing.*;

public class MineButton extends JButton {
    private MineButtonState buttonState;

    public MineButton() {
        this.buttonState = MineButtonState.HIDDEN;
    }
}
