package com.example.njokidraw;

import java.nio.file.Path;

public class Draw {
    public int Color;
    public int strokeWidth;
    public Path path;

    public Draw(int color, int strokeWidth, Path path) {
        Color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}
