package com.example.naviable.navigation;

import java.util.HashMap;
import java.util.Map;

public class Direction {

    private String description;
    private String type;
    private Map<String, String> path_map = new HashMap<>();

    public Direction(String description, String type){
        this.description = description;
        this.type = type;
        // todo : add images to each type

    }

    public String getPath(String type){
        return path_map.get(type);
    }

}
