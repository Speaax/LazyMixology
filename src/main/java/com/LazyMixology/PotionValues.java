package com.LazyMixology;

import javax.inject.Inject;

public class PotionValues {
    String name;
    int blue;
    int green;
    int red;
    int level;

    @Inject
    public PotionValues(String name, int blue, int green, int red, int level) {
        this.name = name;
        this.blue = blue;
        this.green = green;
        this.red = red;
        this.level = level;
    }

    // Optionally, add getters and setters
    public String getName() {
        return name;
    }

    public int getBlue() {
        return blue;
    }

    public int getGreen() {
        return green;
    }

    public int getRed() {
        return red;
    }

    public int getLevel() {
        return level;
    }
}