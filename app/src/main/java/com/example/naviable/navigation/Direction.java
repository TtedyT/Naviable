package com.example.naviable.navigation;

import com.example.naviable.R;

import java.util.HashMap;
import java.util.Map;

public class Direction {
    private String description;
    private String type;

    /**
     * base on the python file in assets:
     * <p>
     * 'straight',
     * 'right',
     * 'left',
     * 'elevator'
     */


    public Direction(String description, String type) {
        this.description = description;
        this.type = type;
        // todo : add images to each type

    }

    public String getDescription() {
        return description;
    }

//    public static String getPath(String type){
//        return path_map.get(type);
//    }

    public String getType() {
        return this.type;
    }
}
