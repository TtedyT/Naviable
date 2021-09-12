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
    @Test
    public void oneEdgeTest() {
        MapNode n1 = new MapNode("0", 0,0,true);
        MapNode n2 = new MapNode("1", 0,1,true);
        Direction d1 = new Direction("do a backflip", "LEFT");
        EdgeInfo e = new EdgeInfo(n1, n2, new ArrayList<>(Arrays.asList(d1)), 1.0);
        List<MapNode> nodes = new ArrayList<>(Arrays.asList(n1,n2));
        List<EdgeInfo> edges = new ArrayList<>(Arrays.asList(e));
        Graph g = new Graph(nodes,edges);
        Navigator navigator = new Navigator(g);
        List<Direction> dirs = navigator.getDirections(n1,n2);
        assertEquals(1, dirs.size());
        assertEquals("do a backflip", dirs.get(0).getDescription());
    }

    private Graph makeGraphFromJson() {
        try {
            Gson gson = new Gson();
            InputStream edgesInput = this.getClass().getClassLoader().getResourceAsStream("triangleEdges.json");
            InputStream nodesInput = this.getClass().getClassLoader().getResourceAsStream("triangleNodes.json");
            Reader edgesReader = new BufferedReader(new InputStreamReader(edgesInput));
            Reader nodesReader = new BufferedReader(new InputStreamReader(nodesInput));

            Type edgeMapType = new TypeToken<Map<String, EdgeInfo>>() {}.getType();
            Type nodesMapType = new TypeToken<Map<String, MapNode>>() {}.getType();
            Map<String, EdgeInfo> nameEdgeMap = gson.fromJson(edgesReader, edgeMapType);
            Map<String, MapNode> nameNodesMap = gson.fromJson(nodesReader, nodesMapType);
            ArrayList<EdgeInfo> edges = new ArrayList<EdgeInfo>(nameEdgeMap.values());
            ArrayList<MapNode> nodes = new ArrayList<MapNode>(nameNodesMap.values());

            Graph g = new Graph(nodes, edges);

            // close reader
            edgesReader.close();
            nodesReader.close();
            return g;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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