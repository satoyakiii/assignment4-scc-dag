package graph.topo;

import metrics.Metrics;
import model.Edge;
import model.Graph;

import java.util.*;

/**
 * Topological sorting algorithms for DAGs.
 *
 * Provides two implementations:
 * 1. Kahn's algorithm (BFS-based, uses queue)
 * 2. DFS-based (recursive with stack)
 *
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 */
public class TopologicalSort {

    private final Graph graph;
    private final Metrics metrics;

    /**
     * Constructor.
     * @param graph input DAG
     * @param metrics metrics tracker
     */
    public TopologicalSort(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Kahn's algorithm: BFS-based topological sort.
     * Repeatedly removes vertices with in-degree 0.
     *
     * @return topological order (list of vertices), or null if graph has cycle
     */
    public List<Integer> kahnSort() {
        int n = graph.getN();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (Edge e : graph.getEdges(u)) {
                inDegree[e.getV()]++;
            }
        }

        // Initialize queue with vertices of in-degree 0
        Queue<Integer> queue = new LinkedList<>();
        for (int u = 0; u < n; u++) {
            if (inDegree[u] == 0) {
                queue.offer(u);
                metrics.increment("queue_pushes");
            }
        }

        List<Integer> order = new ArrayList<>();
        metrics.startTimer();

        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.increment("queue_pops");
            order.add(u);

            // Reduce in-degree of neighbors
            for (Edge e : graph.getEdges(u)) {
                int v = e.getV();
                inDegree[v]--;
                metrics.increment("edge_relaxations");

                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.increment("queue_pushes");
                }
            }
        }

        metrics.stopTimer();

        // Check if all vertices are included (no cycle)
        if (order.size() != n) {
            return null; // graph has cycle
        }

        return order;
    }

    /**
     * DFS-based topological sort.
     * Post-order DFS traversal, then reverse.
     *
     * @return topological order, or null if graph has cycle
     */
    public List<Integer> dfsSort() {
        int n = graph.getN();
        int[] color = new int[n]; // 0=white, 1=gray, 2=black
        Stack<Integer> stack = new Stack<>();

        metrics.startTimer();

        for (int u = 0; u < n; u++) {
            if (color[u] == 0) {
                if (!dfsSortVisit(u, color, stack)) {
                    metrics.stopTimer();
                    return null; // cycle detected
                }
            }
        }

        metrics.stopTimer();

        // Pop all from stack to get topological order
        List<Integer> order = new ArrayList<>();
        while (!stack.isEmpty()) {
            order.add(stack.pop());
            metrics.increment("stack_pops");
        }

        return order;
    }

    /**
     * DFS visit for topological sort.
     * @param u current vertex
     * @param color vertex colors
     * @param stack result stack
     * @return false if cycle detected
     */
    private boolean dfsSortVisit(int u, int[] color, Stack<Integer> stack) {
        color[u] = 1; // gray (in progress)
        metrics.increment("dfs_visits");

        for (Edge e : graph.getEdges(u)) {
            int v = e.getV();
            metrics.increment("edge_checks");

            if (color[v] == 1) {
                // Back edge = cycle
                return false;
            }

            if (color[v] == 0) {
                if (!dfsSortVisit(v, color, stack)) {
                    return false;
                }
            }
        }

        color[u] = 2; // black (finished)
        stack.push(u);
        metrics.increment("stack_pushes");
        return true;
    }

    /**
     * Expand topological order of components to original vertices.
     * @param componentOrder topological order of component indices
     * @param sccs list of SCCs
     * @return expanded order of original vertices
     */
    public static List<Integer> expandToVertices(List<Integer> componentOrder, List<List<Integer>> sccs) {
        List<Integer> vertexOrder = new ArrayList<>();
        for (int compIndex : componentOrder) {
            // Add all vertices from this component (already sorted within)
            vertexOrder.addAll(sccs.get(compIndex));
        }
        return vertexOrder;
    }

    /**
     * Format topological order for output.
     * @param order list of vertices/components
     * @param label description (e.g., "Components", "Vertices")
     * @return formatted string
     */
    public static String formatOrder(List<Integer> order, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append("Topological Order (").append(label).append("):\n");
        sb.append("  ").append(order).append("\n");
        return sb.toString();
    }
}