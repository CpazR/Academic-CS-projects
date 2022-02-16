package com.company;

public class GameContext {
    private int width;
    private int height;

    GameContext(int gridWidth, int gridHeight) {
         this.width = gridWidth;
         this.height = gridHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
