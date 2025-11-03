package graph.dagsp;

import metrics.Metrics;
import metrics.MetricsImpl;
import model.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DAG Shortest Path algorithm.
 */
class DagShortestPathTest {

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
        DagShortestPath sp = new DagShortestPath(g, metrics);
        DagShortestPath.ShortestPathResult result = sp.compute(0);

        assertEquals(0, result.getDistance(0), "Distance to source is 0");
        assertEquals(2, result.getDistance(1), "Distance to 1 is 2");
        assertEquals(5, result.getDistance(2), "Distance to 2 is 5");
        assertEquals(6, result.getDistance(3), "Distance to 3 is 6");

        List<Integer> path = result.reconstructPath(3);
        assertEquals(List.of(0, 1, 2, 3), path, "Path should be [0,1,2,3]");
    }

    /**
     * Test 2: Diamond DAG with multiple paths
     */
    @Test
    void testDiamondDAG() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 4);
        g.addEdge(1, 2, 2);  // path 0→1→2 = 3 (shorter)
        g.addEdge(1, 3, 5);
        g.addEdge(2, 3, 1);

        Metrics metrics = new MetricsImpl();
        DagShortestPath sp = new DagShortestPath(g, metrics);
        DagShortestPath.ShortestPathResult result = sp.compute(0);

        assertEquals(0, result.getDistance(0));
        assertEquals(1, result.getDistance(1));
        assertEquals(3, result.getDistance(2), "Should take path 0→1→2");
        assertEquals(4, result.getDistance(3), "Should take path 0→1→2→3");

        List<Integer> path = result.reconstructPath(3);
        assertEquals(List.of(0, 1, 2, 3), path);
    }

    /**
     * Test 3: Unreachable vertices
     */
    @Test
    void testUnreachableVertices() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        // Vertices 3 and 4 are unreachable from 0
        g.addEdge(3, 4, 1);

        Metrics metrics = new MetricsImpl();
        DagShortestPath sp = new DagShortestPath(g, metrics);
        DagShortestPath.ShortestPathResult result = sp.compute(0);

        assertTrue(result.isReachable(0));
        assertTrue(result.isReachable(1));
        assertTrue(result.isReachable(2));
        assertFalse(result.isReachable(3), "Vertex 3 should be unreachable");
        assertFalse(result.isReachable(4), "Vertex 4 should be unreachable");

        assertEquals(Integer.MAX_VALUE, result.getDistance(3));
        assertEquals(Integer.MAX_VALUE, result.getDistance(4));

        assertTrue(result.reconstructPath(3).isEmpty(), "Path to unreachable vertex is empty");
    }

    /**
     * Test 4: Single vertex
     */
    @Test
    void testSingleVertex() {
        Graph g = new Graph(1, true);

        Metrics metrics = new MetricsImpl();
        DagShortestPath sp = new DagShortestPath(g, metrics);
        DagShortestPath.ShortestPathResult result = sp.compute(0);

        assertEquals(0, result.getDistance(0));
        assertEquals(List.of(0), result.reconstructPath(0));
    }

    /**
     * Test 5: tasks.json condensation example
     */
    @Test
    void testTasksJsonCondensation() {
        // Condensation DAG from tasks.json
        // Components: 6, Edges: 4
        // 5→4→3→2 is the path
        Graph g = new Graph(6, true);
        g.addEdge(1, 0, 3);  // component 1 → 0
        g.addEdge(5, 4, 2);  // component 5 → 4
        g.addEdge(4, 3, 5);  // component 4 → 3
        g.addEdge(3, 2, 1);  // component 3 → 2

        Metrics metrics = new MetricsImpl();
        DagShortestPath sp = new DagShortestPath(g, metrics);
        DagShortestPath.ShortestPathResult result = sp.compute(5);

        assertEquals(0, result.getDistance(5));
        assertEquals(2, result.getDistance(4));
        assertEquals(7, result.getDistance(3));
        assertEquals(8, result.getDistance(2));

        List<Integer> path = result.reconstructPath(2);
        assertEquals(List.of(5, 4, 3, 2), path);

        // Check metrics
        assertTrue(metrics.getCount("edge_relaxations") > 0);
        assertTrue(metrics.getCount("successful_relaxations") > 0);
    }

    /**
     * Test 6: Graph with cycle (should throw exception)
     */
    @Test
    void testCycleThrowsException() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1); // cycle

        Metrics metrics = new MetricsImpl();
        DagShortestPath sp = new DagShortestPath(g, metrics);

        assertThrows(IllegalArgumentException.class, () -> {
            sp.compute(0);
        }, "Should throw exception for cyclic graph");
    }

    /**
     * Test 7: Empty graph
     */
    @Test
    void testEmptyGraph() {
        Graph g = new Graph(0, true);

        Metrics metrics = new MetricsImpl();
        DagShortestPath sp = new DagShortestPath(g, metrics);

        // Should not throw, just handle empty case
        assertDoesNotThrow(() -> {
            DagShortestPath.ShortestPathResult result = sp.compute(0);
        });
    }

    /**
     * Test 8: Path reconstruction with weights
     */
    @Test
    void testPathReconstruction() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 10);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 1, 3);  // 0→2→1 = 8 (shorter than 0→1 = 10)
        g.addEdge(2, 3, 9);
        g.addEdge(3, 4, 2);

        Metrics metrics = new MetricsImpl();
        DagShortestPath sp = new DagShortestPath(g, metrics);
        DagShortestPath.ShortestPathResult result = sp.compute(0);

        // Shortest path to 1: 0→2→1 = 8
        assertEquals(8, result.getDistance(1));
        assertEquals(List.of(0, 2, 1), result.reconstructPath(1));

        // Shortest path to 3: 0→2→1→3 = 9
        assertEquals(9, result.getDistance(3));
        assertEquals(List.of(0, 2, 1, 3), result.reconstructPath(3));

        // Shortest path to 4: 0→2→1→3→4 = 11
        assertEquals(11, result.getDistance(4));
        assertEquals(List.of(0, 2, 1, 3, 4), result.reconstructPath(4));
    }
}