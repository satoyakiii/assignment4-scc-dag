package graph.scc;

import metrics.Metrics;
import metrics.MetricsImpl;
import model.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Tarjan's SCC algorithm.
 */
class SccTarjanTest {

    /**
     * Test 1: Simple DAG (no cycles, each vertex is its own SCC)
     */
    @Test
    void testSimpleDAG() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);

        Metrics metrics = new MetricsImpl();
        SccTarjan scc = new SccTarjan(g, metrics);
        List<List<Integer>> sccs = scc.findSCCs();

        // Each vertex is its own SCC
        assertEquals(4, sccs.size(), "DAG should have 4 SCCs");

        // Check metrics
        assertEquals(4, metrics.getCount("dfs_visits"), "Should visit 4 vertices");
        assertTrue(metrics.getElapsedNanos() > 0, "Timer should be running");
    }

    /**
     * Test 2: Single cycle (one SCC with 3 vertices)
     */
    @Test
    void testSingleCycle() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1); // cycle: 0→1→2→0

        Metrics metrics = new MetricsImpl();
        SccTarjan scc = new SccTarjan(g, metrics);
        List<List<Integer>> sccs = scc.findSCCs();

        // All vertices in one SCC
        assertEquals(1, sccs.size(), "Should have 1 SCC");
        assertEquals(3, sccs.get(0).size(), "SCC should contain 3 vertices");
        assertTrue(sccs.get(0).contains(0));
        assertTrue(sccs.get(0).contains(1));
        assertTrue(sccs.get(0).contains(2));
    }

    /**
     * Test 3: Two separate SCCs
     */
    @Test
    void testTwoSCCs() {
        Graph g = new Graph(6, true);

        // SCC 1: {0, 1, 2}
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);

        // SCC 2: {3, 4}
        g.addEdge(3, 4, 1);
        g.addEdge(4, 3, 1);

        // Connection between SCCs
        g.addEdge(2, 3, 1);

        // Isolated vertex: {5}

        Metrics metrics = new MetricsImpl();
        SccTarjan scc = new SccTarjan(g, metrics);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(3, sccs.size(), "Should have 3 SCCs");

        // Find sizes
        boolean found3 = false, found2 = false, found1 = false;
        for (List<Integer> component : sccs) {
            if (component.size() == 3) found3 = true;
            if (component.size() == 2) found2 = true;
            if (component.size() == 1) found1 = true;
        }

        assertTrue(found3, "Should have SCC of size 3");
        assertTrue(found2, "Should have SCC of size 2");
        assertTrue(found1, "Should have SCC of size 1");
    }

    /**
     * Test 4: tasks.json example
     */
    @Test
    void testTasksJsonGraph() {
        Graph g = new Graph(8, true);
        g.addEdge(0, 1, 3);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 4);
        g.addEdge(3, 1, 1); // cycle: 1→2→3→1
        g.addEdge(4, 5, 2);
        g.addEdge(5, 6, 5);
        g.addEdge(6, 7, 1);

        Metrics metrics = new MetricsImpl();
        SccTarjan scc = new SccTarjan(g, metrics);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(6, sccs.size(), "Should have 6 SCCs");

        // One SCC should contain {1, 2, 3}
        boolean foundCycle = false;
        for (List<Integer> component : sccs) {
            if (component.size() == 3 &&
                    component.contains(1) &&
                    component.contains(2) &&
                    component.contains(3)) {
                foundCycle = true;
            }
        }

        assertTrue(foundCycle, "Should find cycle SCC {1,2,3}");

        // Check metrics
        assertEquals(8, metrics.getCount("dfs_visits"), "Should visit all 8 vertices");
        assertEquals(6, metrics.getCount("sccs_found"), "Should find 6 SCCs");
    }

    /**
     * Test 5: Component mapping
     */
    @Test
    void testComponentMapping() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 0, 1); // SCC {0,1}
        g.addEdge(2, 3, 1);
        g.addEdge(3, 2, 1); // SCC {2,3}
        // vertex 4 isolated

        Metrics metrics = new MetricsImpl();
        SccTarjan scc = new SccTarjan(g, metrics);
        List<List<Integer>> sccs = scc.findSCCs();

        int[] componentMap = SccTarjan.getComponentMapping(sccs);

        // Vertices 0 and 1 should be in same component
        assertEquals(componentMap[0], componentMap[1], "0 and 1 should be in same SCC");

        // Vertices 2 and 3 should be in same component
        assertEquals(componentMap[2], componentMap[3], "2 and 3 should be in same SCC");

        // Different SCCs should have different indices
        assertNotEquals(componentMap[0], componentMap[2], "Different SCCs should have different indices");
    }

    /**
     * Test 6: Empty graph
     */
    @Test
    void testEmptyGraph() {
        Graph g = new Graph(0, true);

        Metrics metrics = new MetricsImpl();
        SccTarjan scc = new SccTarjan(g, metrics);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(0, sccs.size(), "Empty graph should have 0 SCCs");
    }

    /**
     * Test 7: Single vertex
     */
    @Test
    void testSingleVertex() {
        Graph g = new Graph(1, true);

        Metrics metrics = new MetricsImpl();
        SccTarjan scc = new SccTarjan(g, metrics);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(1, sccs.size(), "Single vertex should be 1 SCC");
        assertEquals(1, sccs.get(0).size(), "SCC should contain 1 vertex");
        assertEquals(0, sccs.get(0).get(0), "Vertex should be 0");
    }
}