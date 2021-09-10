package com.example.naviable.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Navigator {
    private Graph graph;

    public Navigator(Graph graph){
        this.graph = graph;
    }

    private void findPath(MapNode src){
        src.setMinDistance(0);
        PriorityQueue<MapNode> queue = new PriorityQueue<>();
        queue.add(src);

        while(!queue.isEmpty()){
            MapNode u = queue.poll();

            for (Edge e : graph.getAdjacent(u)){
                MapNode v = e.getSecond();
                double weight = e.getDistance();
                assert u != null;
                double distanceThroughU = u.getMinDistance() + weight;
                if(distanceThroughU < v.getMinDistance()){
                    queue.remove(v);
                    v.setMinDistance(distanceThroughU);
                    v.setPrev(u);
                    queue.add(v);
                }
            }
        }
    }

    private static List<MapNode> getShortestPathTo(MapNode dest){
        List<MapNode> path = new ArrayList<>();
        for(MapNode node  = dest; dest != null; dest = dest.getPrev()){
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }

    public List<Direction> getDirections(MapNode src, MapNode dest){
        findPath(src);
        List<MapNode> path = getShortestPathTo(dest);
        List<Direction> directions = new ArrayList<>();

        for(int i = 0; i < path.size() - 1 ; i++){
            for (Edge e: graph.getEdges()) {
                if(e.getFirst() == path.get(i) && e.getSecond() == path.get(i+1)){
                    directions.addAll(e.getDirections());
                }
            }
        }
        return directions;

    }
}
