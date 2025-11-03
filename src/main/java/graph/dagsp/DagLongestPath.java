package graph.dagsp;

import graph.topo.TopologicalSort;
import metrics.Metrics;
import model.Edge;
import model.Graph;

import java.util.*;

/**
 * Longest path in a DAG (Critical Path).
 *
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 *
 * Algorithm:
 * 1. Compute topological order
 * 2. Initialize distances: dist[source] = 0, others = -infinity
 * 3. Process vertices in topological order
 * 4. Use max-relaxation (instead of min) for longest path
 *
 * Applications: Project scheduling (CPM), critical path analysis
 */
public class DagLongestPath {

    private final Graph graph;
    private final Metrics metrics;

    /**
     * Constructor.
     * @param graph input DAG
     * @param metrics metrics tracker
     */
    public DagLongestPath(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Compute longest paths from source to all reachable vertices.
     * @param source source vertex
     * @return result containing distances and predecessors
     */
    public LongestPathResult compute(int source) {
        int n = graph.getN();

        // Handle empty graph or invalid source
        if (n == 0 || source < 0 || source >= n) {
            return new LongestPathResult(new int[0], new int[0], source);
        }



        // Get topological order
        TopologicalSort topoSort = new TopologicalSort(graph, new metrics.MetricsImpl());
        List<Integer> topoOrder = topoSort.dfsSort();

        if (topoOrder == null) {
            throw new IllegalArgumentException("Graph contains a cycle, not a DAG");
        }

        // Initialize distances and predecessors
        // Use Integer.MIN_VALUE to represent -infinity
        int[] dist = new int[n];
        int[] pred = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(pred, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order
        for (int u : topoOrder) {
            metrics.increment("vertices_processed");

            // Skip unreachable vertices
            if (dist[u] == Integer.MIN_VALUE) {
                continue;
            }

            // Max-relaxation for longest path
            for (Edge e : graph.getEdges(u)) {
                int v = e.getV();
                int weight = e.getW();

                metrics.increment("edge_relaxations");

                // Max-relaxation: take maximum distance
                if (dist[u] != Integer.MIN_VALUE && dist[u] + weight > dist[v]) {
                    dist[v] = dist[u] + weight;
                    pred[v] = u;
                    metrics.increment("successful_relaxations");
                }
            }
        }

        metrics.stopTimer();

        return new LongestPathResult(dist, pred, source);
    }

    /**
     * Find the critical path (longest path ending at any vertex).
     * @param source source vertex
     * @return critical path result with maximum distance vertex
     */
    public CriticalPathResult findCriticalPath(int source) {
        LongestPathResult result = compute(source);

        // Find vertex with maximum distance
        int maxDist = Integer.MIN_VALUE;
        int criticalVertex = -1;

        for (int v = 0; v < graph.getN(); v++) {
            if (result.isReachable(v) && result.getDistance(v) > maxDist) {
                maxDist = result.getDistance(v);
                criticalVertex = v;
            }
        }

        List<Integer> criticalPath = Collections.emptyList();
        if (criticalVertex != -1) {
            criticalPath = result.reconstructPath(criticalVertex);
        }

        return new CriticalPathResult(criticalPath, maxDist, criticalVertex);
    }

    /**
     * Result of longest path computation.
     */
    public static class LongestPathResult {
        private final int[] dist;
        private final int[] pred;
        private final int source;

        public LongestPathResult(int[] dist, int[] pred, int source) {
            this.dist = dist;
            this.pred = pred;
            this.source = source;
        }

        /**
         * Get distance to vertex.
         * @param v target vertex
         * @return distance, or Integer.MIN_VALUE if unreachable
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
            return dist[v] != Integer.MIN_VALUE;
        }

        /**
         * Reconstruct longest path from source to target.
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
            sb.append("Longest Paths from source ").append(source).append(":\n");

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
    }

    /**
     * Critical path result.
     */
    public static class CriticalPathResult {
        private final List<Integer> path;
        private final int length;
        private final int endVertex;

        public CriticalPathResult(List<Integer> path, int length, int endVertex) {
            this.path = path;
            this.length = length;
            this.endVertex = endVertex;
        }

        public List<Integer> getPath() {
            return path;
        }

        public int getLength() {
            return length;
        }

        public int getEndVertex() {
            return endVertex;
        }

        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("Critical Path (Longest):\n");
            sb.append("  Path: ").append(path).append("\n");
            sb.append("  Length: ").append(length).append("\n");
            sb.append("  End vertex: ").append(endVertex).append("\n");
            return sb.toString();
        }
    }
}