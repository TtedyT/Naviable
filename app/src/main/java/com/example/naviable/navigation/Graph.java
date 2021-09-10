package com.example.naviable.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class Graph {
    private List<MapNode> nodes;
    private List<Edge> edges;

    public Graph(List<MapNode> nodes, List<Edge> edges){
        this.nodes = nodes;
        this.edges = edges;
    }

    public Set<Edge> getAdjacent(MapNode node){
        Set<Edge> nodes = new HashSet<>();
        for (Edge edge : edges) {
            if(edge.getFirst() == node){
                nodes.add(edge);
            }
        }
        return nodes;
    }

    public List<MapNode> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
