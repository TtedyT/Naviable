package com.example.naviable;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.naviable.navigation.Direction;
import com.example.naviable.navigation.EdgeInfo;
import com.example.naviable.navigation.Graph;
import com.example.naviable.navigation.MapNode;
import com.example.naviable.navigation.Navigator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NavigatorUnitTest {
    private Graph makeGraphFromJson(String filename) {
        try {
            Gson gson = new Gson();
            InputStream nodesInput = this.getClass().getClassLoader().getResourceAsStream(filename);
            Reader nodesReader = new BufferedReader(new InputStreamReader(nodesInput));

            Type nodesMapType = new TypeToken<Map<String, MapNode>>() {}.getType();
            Map<String, MapNode> nameNodesMap = gson.fromJson(nodesReader, nodesMapType);

            Graph g = new Graph(nameNodesMap);

            // close reader
            nodesReader.close();
            return g;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    @Test
    public void oneEdgeTest() {
        // Prepare test
        String filename = "oneEdge.json";
        Graph g = makeGraphFromJson(filename);
        Navigator navigator = new Navigator(g);
        String n1 = "a";
        String n2 = "b";

        // Run test
        List<Direction> dirs = navigator.getDirections(n1,n2);

        // Check results
        assertEquals(1, dirs.size());
        assertEquals("do a backflip", dirs.get(0).getDescription());

//        MapNode n1 = new MapNode("0", 0,0,true);
//        MapNode n2 = new MapNode("1", 0,1,true);
//        Direction d1 = new Direction("do a backflip", "LEFT");
//        EdgeInfo e = new EdgeInfo(n1, n2, new ArrayList<>(Arrays.asList(d1)), 1.0);
//        List<MapNode> nodes = new ArrayList<>(Arrays.asList(n1,n2));
//        List<EdgeInfo> edges = new ArrayList<>(Arrays.asList(e));
//        Graph g = new Graph(nodes,edges);
//        List<Direction> dirs = navigator.getDirections(n1,n2);
//        assertEquals(1, dirs.size());
//        assertEquals("do a backflip", dirs.get(0).getDescription());
    }

    @Test
    public void triangleTest(){
        Graph g = makeGraphFromJson();
        assertNotNull(g);
        Navigator navigator = new Navigator(g);
        List<Direction> dirs = navigator.getDirections(g.getNodes().get(0), g.getNodes().get(1));
        assertEquals(dirs.size(), 1);
        assertEquals(dirs.get(0).getDescription() , "go the right way");
        assertEquals(dirs.get(0).getType() , "end");

    }
}