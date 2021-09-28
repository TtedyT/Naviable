package com.example.naviable.navigation;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Navigator {
	private Graph graph;

	public Navigator(Graph graph) {
		this.graph = graph;
	}

	private void resetAllNodeData() {

		for (String id : graph.getNodeMap().keySet()) {
			graph.getNode(id).resetNodeData();
		}
	}

	private void findPath(String srcId) {
		MapNode src = graph.getNode(srcId);
		resetAllNodeData();
		src.setMinDistance(0);
		PriorityQueue<MapNode> queue = new PriorityQueue<>();
		queue.add(src);

		while (!queue.isEmpty()) {
			MapNode u = queue.poll();

			assert u != null;
			for (EdgeInfo e : u.getAdjacencies()) {
				MapNode v = graph.getNode(e.getDestId());
				double weight = e.getDistance();
				double distanceThroughU = u.getMinDistance() + weight;
				if (distanceThroughU < v.getMinDistance()) {
					queue.remove(v);
					v.setMinDistance(distanceThroughU);
					v.setPrev(u);
					queue.add(v);
				}
			}
		}
	}

	private List<MapNode> getShortestPathTo(String destId) {
		List<MapNode> path = new ArrayList<>();
		MapNode dest = graph.getNode(destId);
		for (MapNode node = dest; node != null; node = node.getPrev()) {
			path.add(node);
		}
		Collections.reverse(path);
		return path;
	}

	public List<Direction> getDirections(String srcId, String destId) {
		findPath(srcId);
		List<MapNode> path = getShortestPathTo(destId);
		List<Direction> directions = new ArrayList<>();
		for (int i = 0; i < path.size() - 1; i++) {
			List<EdgeInfo> adjacencies = path.get(i).getAdjacencies();
			for (EdgeInfo currEdge : adjacencies) {
				if (currEdge.getDestId().equals(path.get(i + 1).getName())) {
					directions.addAll(currEdge.getDirections());
				}
			}
		}
		return directions;
	}

	public ArrayList<LatLng> getPathLatLng(String srcId, String destId) {
		findPath(srcId);
		List<MapNode> shortestPath = getShortestPathTo(destId);
		ArrayList<LatLng> pathLatLng = new ArrayList<>();
		for (MapNode mapNode : shortestPath) {
			pathLatLng.add(new LatLng(mapNode.getX(), mapNode.getY()));
		}
		return pathLatLng;
	}

	public ArrayList<String> getLocations() {
		return graph.getMappableLocations();
	}

	public LatLng getCoordinate(String locName) {
		MapNode n = graph.getNode(locName);
		return new LatLng(n.getX(), n.getY());
	}
}
