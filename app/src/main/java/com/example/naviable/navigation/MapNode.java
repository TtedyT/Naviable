package com.example.naviable.navigation;

public class MapNode {
    private String name;
    private double x,y;
    private boolean mappable;

    public MapNode(String name, double x, double y, boolean mappable){
        this.name = name;
        this.x = x;
        this.y = y;
        this.mappable = mappable;
    }

    public String getName() {
        return  name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isMappable() {
        return mappable;
    }
}
