package graph.dagsp;

import metrics.Metrics;
import metrics.MetricsImpl;
import model.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DAG Longest Path (Critical Path) algorithm.
 */
class DagLongestPathTest {

    /**
     * Test 1: Simple linear DAG
     */
    @Test
    void testLinearPath() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 2);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, 1);

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);
        DagLongestPath.LongestPathResult result = lp.compute(0);

        assertEquals(0, result.getDistance(0));
        assertEquals(2, result.getDistance(1));
        assertEquals(5, result.getDistance(2));
        assertEquals(6, result.getDistance(3));

        List<Integer> path = result.reconstructPath(3);
        assertEquals(List.of(0, 1, 2, 3), path);
    }

    /**
     * Test 2: Diamond DAG with multiple paths
     */
    @Test
    void testDiamondDAG() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 4);
        g.addEdge(1, 2, 2);  // path 0→1→2 = 3
        g.addEdge(1, 3, 5);
        g.addEdge(2, 3, 1);

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);
        DagLongestPath.LongestPathResult result = lp.compute(0);

        assertEquals(0, result.getDistance(0));
        assertEquals(1, result.getDistance(1));
        assertEquals(4, result.getDistance(2), "Should take longer path 0→2");
        assertEquals(6, result.getDistance(3), "Should take path 0→1→3");

        List<Integer> path = result.reconstructPath(3);
        assertEquals(List.of(0, 1, 3), path);
    }

    /**
     * Test 3: Critical path finding
     */
    @Test
    void testCriticalPath() {
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 5);
        g.addEdge(0, 2, 3);
        g.addEdge(1, 3, 6);
        g.addEdge(2, 3, 4);
        g.addEdge(2, 4, 2);
        g.addEdge(3, 5, 2);
        g.addEdge(4, 5, 6);

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);
        DagLongestPath.CriticalPathResult cp = lp.findCriticalPath(0);

        // Critical path should be 0→1→3→5 = 13 or 0→2→4→5 = 11
        // Actually longest is 0→1→3→5 = 5+6+2 = 13
        assertEquals(13, cp.getLength(), "Critical path length should be 13");
        assertEquals(List.of(0, 1, 3, 5), cp.getPath());
    }

    /**
     * Test 4: tasks.json condensation example
     */
    @Test
    void testTasksJsonCondensation() {
        // Same as shortest path test, but longest
        Graph g = new Graph(6, true);
        g.addEdge(1, 0, 3);
        g.addEdge(5, 4, 2);
        g.addEdge(4, 3, 5);
        g.addEdge(3, 2, 1);

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);
        DagLongestPath.CriticalPathResult cp = lp.findCriticalPath(5);

        assertEquals(8, cp.getLength(), "Critical path is 5→4→3→2 = 8");
        assertEquals(List.of(5, 4, 3, 2), cp.getPath());
        assertEquals(2, cp.getEndVertex());
    }

    /**
     * Test 5: Unreachable vertices
     */
    @Test
    void testUnreachableVertices() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 3);
        // Vertices 3 and 4 are unreachable
        g.addEdge(3, 4, 2);

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);
        DagLongestPath.LongestPathResult result = lp.compute(0);

        assertTrue(result.isReachable(0));
        assertTrue(result.isReachable(1));
        assertTrue(result.isReachable(2));
        assertFalse(result.isReachable(3));
        assertFalse(result.isReachable(4));

        assertEquals(Integer.MIN_VALUE, result.getDistance(3));
        assertEquals(Integer.MIN_VALUE, result.getDistance(4));
    }

    /**
     * Test 6: Single vertex
     */
    @Test
    void testSingleVertex() {
        Graph g = new Graph(1, true);

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);
        DagLongestPath.LongestPathResult result = lp.compute(0);

        assertEquals(0, result.getDistance(0));
        assertEquals(List.of(0), result.reconstructPath(0));

        DagLongestPath.CriticalPathResult cp = lp.findCriticalPath(0);
        assertEquals(0, cp.getLength());
        assertEquals(List.of(0), cp.getPath());
    }

    /**
     * Test 7: Complex DAG with multiple paths
     */
    @Test
    void testComplexDAG() {
        Graph g = new Graph(7, true);
        g.addEdge(0, 1, 3);
        g.addEdge(0, 2, 2);
        g.addEdge(1, 3, 4);
        g.addEdge(2, 3, 1);
        g.addEdge(2, 4, 5);
        g.addEdge(3, 5, 2);
        g.addEdge(4, 5, 3);
        g.addEdge(5, 6, 1);

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);
        DagLongestPath.CriticalPathResult cp = lp.findCriticalPath(0);

        // Longest path: 0→2→4→5→6 = 2+5+3+1 = 11
        // or 0→1→3→5→6 = 3+4+2+1 = 10
        assertTrue(cp.getLength() >= 10, "Critical path should be at least 10");
        assertEquals(6, cp.getEndVertex(), "Should end at vertex 6");

        // Check metrics
        assertTrue(metrics.getCount("edge_relaxations") > 0);
        assertTrue(metrics.getElapsedNanos() > 0);
    }

    /**
     * Test 8: Graph with cycle (should throw exception)
     */
    @Test
    void testCycleThrowsException() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1); // cycle

        Metrics metrics = new MetricsImpl();
        DagLongestPath lp = new DagLongestPath(g, metrics);

        assertThrows(IllegalArgumentException.class, () -> {
            lp.compute(0);
        }, "Should throw exception for cyclic graph");
    }
}