package com.example.naviable.navigation;

import java.util.ArrayList;


public class Edge {

    private MapNode first;
    private MapNode second;
    private ArrayList<Direction> directions;

    public ArrayList<Direction> getDirections() {
        return directions;
    }

    public Edge(MapNode first, MapNode second, ArrayList<Direction> directions)
    {
        this.first = first;
        this.second = second;
        this.directions = directions;
    }

    public double getDistance()
    {
        return Math.sqrt(Math.pow((first.getX() - second.getX()),2)-
                        Math.pow((first.getY() - second.getY()),2));
    }

    public MapNode getFirst() {
        return first;
    }

    public MapNode getSecond() {
        return second;
    }
}
