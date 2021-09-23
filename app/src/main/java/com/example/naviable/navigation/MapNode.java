package com.example.naviable.navigation;

import java.util.Arrays;
import java.util.List;

public class MapNode implements Comparable<MapNode> {
    private String name;
    private double x,y;
    private boolean mappable;
    private double minDistance = Double.POSITIVE_INFINITY;
    private MapNode prev;

    private List<EdgeInfo> adjacency;

    public void setPrev(MapNode prev) {
        this.prev = prev;
    }

    public MapNode getPrev() {
        return prev;
    }

    public MapNode(String name, double x, double y, boolean mappable, List<EdgeInfo> adjacency){
        this.name = name;
        this.x = x;
        this.y = y;
        this.mappable = mappable;
        this.adjacency = adjacency;
    }

    public MapNode(){
        this.minDistance = Double.POSITIVE_INFINITY;
        this.prev = null;
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

    public List<EdgeInfo> getAdjacencies() {
        return adjacency;
    }

    public boolean isMappable() {
        return mappable;
    }

    public void setMinDistance(double newDistance){
        this.minDistance = newDistance;
    }
    public void resetNodeData(){
      this.minDistance = Double.POSITIVE_INFINITY;
      this.prev = null;
	}

    @Override
    public int compareTo(MapNode mapNode) {
        return Double.compare(minDistance, mapNode.minDistance);
    }
}
