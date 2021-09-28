package com.example.naviable.navigation;

import java.util.List;


public class EdgeInfo {

	//    private final List<Direction> directions;
	private final List<Direction> directions;
	private final String destId;
	private double distance;

	public EdgeInfo(String destId, List<Direction> directions, double distance) {
		this.destId = destId;
		this.directions = directions;
		this.distance = distance;
	}

	public String getDestId() {
		return destId;
	}

	public List<Direction> getDirections() {
		return directions;
	}

	public double getDistance() {
		return distance;
	}
}
