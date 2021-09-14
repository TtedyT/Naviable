package com.example.naviable.navigation;

import java.util.ArrayList;
import java.util.List;


public class EdgeInfo {

    private final List<Direction> directions;
    private double distance;
    private final String destId;

    public EdgeInfo(String destId, List<Direction> directions, double distance)
    {
        this.directions = directions;
        this.destId = destId;
        this.distance = distance;
    }

    public String getDestId() {
        return destId;
    }

    public List<Direction> getDirections() {
        return directions;
    }

    public double getDistance()
    {
        return distance;
    }
}
