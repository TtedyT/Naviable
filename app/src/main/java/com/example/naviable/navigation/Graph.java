package com.example.naviable.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<String, MapNode> nodeMap;

    public Graph(Map<String, MapNode> nodeMap){
        this.nodeMap = nodeMap;

    }
    public MapNode getNode(String stringId){
        return nodeMap.get(stringId);
    }
}
