package com.example.naviable.navigation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<String, MapNode> nodeMap;

    public Graph(Map<String, MapNode> nodeMap){
        this.nodeMap = nodeMap;
    }

    public Graph(InputStream nodesInput){
        this(makeGraphFromJson(nodesInput));
    }

    private static Map<String, MapNode> makeGraphFromJson(InputStream nodesInput) {
        try {
            Gson gson = new Gson();
            Reader nodesReader = new BufferedReader(new InputStreamReader(nodesInput));

            Type nodesMapType = new TypeToken<Map<String, MapNode>>() {}.getType();
            Map<String, MapNode> nameNodesMap = gson.fromJson(nodesReader, nodesMapType);

            // close reader
            nodesReader.close();
            return nameNodesMap;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public MapNode getNode(String stringId){
        return nodeMap.get(stringId);
    }
}