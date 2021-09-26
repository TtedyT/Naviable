package com.example.naviable;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.naviable.navigation.Direction;
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
import java.util.List;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NavigatorUnitTest {
    private Graph makeGraphFromJson(String filename) {
        InputStream nodesInput = this.getClass().getClassLoader().getResourceAsStream(filename);
        Graph g = new Graph(nodesInput);

        return g;
    }


    @Test
    public void oneEdgeTest() {
        // Prepare test
        String filename = "oneEdge.json";
        Graph g = makeGraphFromJson(filename);
        Navigator navigator = new Navigator(g);
        String n1 = "a";
        String n2 = "b";
        String expectedDir = "Go Straight";

        // Run test
        List<Direction> dirs = navigator.getDirections(n1, n2);

        // Check results
        assertEquals(1, dirs.size());
        assertEquals(expectedDir, dirs.get(0).getDescription());
    }

    @Test
    public void triangleTest() {
        // Prepare test
        String filename = "triangleTest.json";
        Graph g = makeGraphFromJson(filename);
        Navigator navigator = new Navigator(g);
        String n1 = "src";
        String n2 = "dest";
        String expectedDir = "Go the correct path!";

        // Run test
        List<Direction> dirs = navigator.getDirections(n1, n2);

        // Check results
        assertEquals(1, dirs.size());
        assertEquals(expectedDir, dirs.get(0).getDescription());
    }

    @Test
    public void sinkTest1() {
        // Prepare test
        String filename = "sinkTest1.json";
        Graph g = makeGraphFromJson(filename);
        Navigator navigator = new Navigator(g);
        String n1 = "src";
        String n2 = "dest";

        // Run test
        List<Direction> dirs = navigator.getDirections(n1, n2);

        // Check results
        assertEquals(4, dirs.size());
        assertEquals("walk", dirs.get(0).getDescription());
        assertEquals("getting closer", dirs.get(1).getDescription());
        assertEquals("even closer", dirs.get(2).getDescription());
        assertEquals("this is it", dirs.get(3).getDescription());
    }

    @Test
    public void sinkTest2() {
        // Prepare test
        String filename = "sinkTest2.json";
        Graph g = makeGraphFromJson(filename);
        Navigator navigator = new Navigator(g);
        String n1 = "s";
        String n2 = "e";

        // Run test
        List<Direction> dirs = navigator.getDirections(n1, n2);

        // Check results
        assertEquals(3, dirs.size());
        assertEquals("go from s to a", dirs.get(0).getDescription());
        assertTrue(dirs.get(1).getDescription().equals("go from a to d")
                || dirs.get(1).getDescription().equals("go from a to c"));
        assertTrue(dirs.get(2).getDescription().equals("go from c to e")
                || dirs.get(2).getDescription().equals("go from d to e"));
    }
}