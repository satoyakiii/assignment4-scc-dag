package graph.dagsp;

import graph.topo.TopologicalSort;
import metrics.Metrics;
import model.Edge;
import model.Graph;

import java.util.*;

/**
 * Shortest paths in a DAG using topological sort + relaxation.
 *
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 *
 * Algorithm:
 * 1. Compute topological order of vertices
 * 2. Initialize distances: dist[source] = 0, others = infinity
 * 3. Process vertices in topological order
 * 4. Relax all outgoing edges from each vertex
 */
public class DagShortestPath {

    private final Graph graph;
    private final Metrics metrics;

    /**
     * Constructor.
     * @param graph input DAG
     * @param metrics metrics tracker
     */
    public DagShortestPath(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Compute shortest paths from source to all reachable vertices.
     * @param source source vertex
     * @return result containing distances and predecessors
     */
    public ShortestPathResult compute(int source) {
        int n = graph.getN();

        // Handle empty graph or invalid source
        if (n == 0 || source < 0 || source >= n) {
            return new ShortestPathResult(new int[0], new int[0], source);
        }

        // Get topological order
        TopologicalSort topoSort = new TopologicalSort(graph, new metrics.MetricsImpl());
        List<Integer> topoOrder = topoSort.dfsSort();

        if (topoOrder == null) {
            throw new IllegalArgumentException("Graph contains a cycle, not a DAG");
        }

        // Initialize distances and predecessors
        int[] dist = new int[n];
        int[] pred = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(pred, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order
        for (int u : topoOrder) {
            metrics.increment("vertices_processed");

            // Skip unreachable vertices
            if (dist[u] == Integer.MAX_VALUE) {
                continue;
            }

            // Relax all outgoing edges
            for (Edge e : graph.getEdges(u)) {
                int v = e.getV();
                int weight = e.getW();

                metrics.increment("edge_relaxations");

                // Relaxation step
                if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    pred[v] = u;
                    metrics.increment("successful_relaxations");
                }
            }
        }

        metrics.stopTimer();

        return new ShortestPathResult(dist, pred, source);
    }

    /**
     * Result of shortest path computation.
     */
    public static class ShortestPathResult {
        private final int[] dist;
        private final int[] pred;
        private final int source;

        public ShortestPathResult(int[] dist, int[] pred, int source) {
            this.dist = dist;
            this.pred = pred;
            this.source = source;
        }

        /**
         * Get distance to vertex.
         * @param v target vertex
         * @return distance, or Integer.MAX_VALUE if unreachable
         */
        public int getDistance(int v) {
            return dist[v];
        }

        /**
         * Check if vertex is reachable from source.
         * @param v target vertex
         * @return true if reachable
         */
        public boolean isReachable(int v) {
            return dist[v] != Integer.MAX_VALUE;
        }

        /**
         * Reconstruct shortest path from source to target.
         * @param target target vertex
         * @return path as list of vertices, or empty if unreachable
         */
        public List<Integer> reconstructPath(int target) {
            if (!isReachable(target)) {
                return Collections.emptyList();
            }

            List<Integer> path = new ArrayList<>();
            int current = target;

            while (current != -1) {
                path.add(current);
                current = pred[current];
            }

            Collections.reverse(path);
            return path;
        }

        /**
         * Format result as string.
         * @return formatted output
         */
        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("Shortest Paths from source ").append(source).append(":\n");

            for (int v = 0; v < dist.length; v++) {
                if (isReachable(v)) {
                    sb.append("  To ").append(v).append(": distance=").append(dist[v]);
                    sb.append(", path=").append(reconstructPath(v)).append("\n");
                }
            }

            return sb.toString();
        }

        public int getSource() {
            return source;
        }

        public int[] getDistances() {
            return dist;
        }

        public int[] getPredecessors() {
            return pred;
        }
    }
}