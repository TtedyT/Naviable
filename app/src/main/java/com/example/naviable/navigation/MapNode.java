package com.example.naviable.navigation;

public class MapNode implements Comparable<MapNode> {
    private String name;
    private double x,y;
    private boolean mappable;
    private double minDistance = Double.POSITIVE_INFINITY;
    private MapNode prev;

    public void setPrev(MapNode prev) {
        this.prev = prev;
    }

    public MapNode getPrev() {
        return prev;
    }

    public MapNode(String name, double x, double y, boolean mappable){
        this.name = name;
        this.x = x;
        this.y = y;
        this.mappable = mappable;
    }

    public double getMinDistance() {
        return minDistance;
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

    public void setMinDistance(double newDistance){
        this.minDistance = newDistance;
    }

    @Override
    public int compareTo(MapNode mapNode) {
        return Double.compare(minDistance, mapNode.minDistance);
    }
}
