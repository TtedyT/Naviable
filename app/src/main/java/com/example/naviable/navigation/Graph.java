package com.example.naviable.navigation;

import java.util.ArrayList;

public class Graph {
    private ArrayList<MapNode> nodes;
    private ArrayList<Edge> edges;

    public Graph(ArrayList<MapNode> nodes, ArrayList<Edge> edges){
        this.nodes = nodes;
        this.edges = edges;

    }
}
