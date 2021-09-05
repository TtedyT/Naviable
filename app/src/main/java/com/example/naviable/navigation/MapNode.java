package com.example.naviable.navigation;

public class MapNode {
    private int x,y;
    private boolean mappable;

    public MapNode(int x, int y, boolean mappable){
        this.x = x;
        this.y = y;
        this.mappable = mappable;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isMappable() {
        return mappable;
    }
}
