package graph.topo;

import metrics.Metrics;
import metrics.MetricsImpl;
import model.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Topological Sort algorithms.
 */
class TopologicalSortTest {

    /**
     * Test 1: Simple DAG (linear chain)
     */
    @Test
    void testLinearDAG() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);

        Metrics metrics = new MetricsImpl();
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.kahnSort();

        assertNotNull(order, "Should return valid order");
        assertEquals(4, order.size(), "Order should contain 4 vertices");
        assertEquals(List.of(0, 1, 2, 3), order, "Should be [0,1,2,3]");
    }

    /**
     * Test 2: DAG with multiple valid orders
     */
    @Test
    void testDiamondDAG() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);

        Metrics metrics = new MetricsImpl();
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.kahnSort();

        assertNotNull(order, "Should return valid order");
        assertEquals(4, order.size());

        // Check valid topological order: 0 before 1,2; 1,2 before 3
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    /**
     * Test 3: Cycle detection (should return null)
     */
    @Test
    void testCycleDetection() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1); // cycle

        Metrics metricsKahn = new MetricsImpl();
        TopologicalSort topoKahn = new TopologicalSort(g, metricsKahn);
        List<Integer> orderKahn = topoKahn.kahnSort();

        assertNull(orderKahn, "Kahn should return null for cycle");

        Metrics metricsDFS = new MetricsImpl();
        TopologicalSort topoDFS = new TopologicalSort(g, metricsDFS);
        List<Integer> orderDFS = topoDFS.dfsSort();

        assertNull(orderDFS, "DFS should return null for cycle");
    }

    /**
     * Test 4: DFS sort on DAG
     */
    @Test
    void testDFSSort() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 1);

        Metrics metrics = new MetricsImpl();
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.dfsSort();

        assertNotNull(order, "Should return valid order");
        assertEquals(5, order.size());

        // Check valid order
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(3) < order.indexOf(4));

        // Check metrics
        assertEquals(5, metrics.getCount("dfs_visits"));
        assertTrue(metrics.getCount("stack_pushes") > 0);
    }

    /**
     * Test 5: Empty graph
     */
    @Test
    void testEmptyGraph() {
        Graph g = new Graph(0, true);

        Metrics metrics = new MetricsImpl();
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.kahnSort();

        assertNotNull(order);
        assertEquals(0, order.size(), "Empty graph should have empty order");
    }

    /**
     * Test 6: Single vertex
     */
    @Test
    void testSingleVertex() {
        Graph g = new Graph(1, true);

        Metrics metrics = new MetricsImpl();
        TopologicalSort topo = new TopologicalSort(g, metrics);
        List<Integer> order = topo.kahnSort();

        assertNotNull(order);
        assertEquals(1, order.size());
        assertEquals(0, order.get(0));
    }

    /**
     * Test 7: Compare Kahn and DFS results
     */
    @Test
    void testKahnVsDFS() {
        Graph g = new Graph(6, true);
        g.addEdge(5, 2, 1);
        g.addEdge(5, 0, 1);
        g.addEdge(4, 0, 1);
        g.addEdge(4, 1, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 1, 1);

        Metrics metricsKahn = new MetricsImpl();
        TopologicalSort topoKahn = new TopologicalSort(g, metricsKahn);
        List<Integer> kahnOrder = topoKahn.kahnSort();

        Metrics metricsDFS = new MetricsImpl();
        TopologicalSort topoDFS = new TopologicalSort(g, metricsDFS);
        List<Integer> dfsOrder = topoDFS.dfsSort();

        assertNotNull(kahnOrder);
        assertNotNull(dfsOrder);
        assertEquals(6, kahnOrder.size());
        assertEquals(6, dfsOrder.size());

        // Both should be valid topological orders (may differ)
        // Check that all vertices are present
        assertTrue(kahnOrder.containsAll(List.of(0, 1, 2, 3, 4, 5)));
        assertTrue(dfsOrder.containsAll(List.of(0, 1, 2, 3, 4, 5)));
    }
}