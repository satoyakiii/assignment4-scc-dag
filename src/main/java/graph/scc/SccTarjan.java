package graph.scc;

import metrics.Metrics;
import model.Edge;
import model.Graph;

import java.util.*;

/**
 * Tarjan's algorithm for finding Strongly Connected Components.
 *
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 *
 * Algorithm:
 * 1. DFS traversal with discovery time and low-link values
 * 2. Maintain a stack of vertices in current path
 * 3. When low[u] == disc[u], found root of SCC
 * 4. Pop stack until u is reached to get the component
 */
public class SccTarjan {

    private final Graph graph;
    private final Metrics metrics;

    private int time;
    private int[] disc;      // discovery time
    private int[] low;       // low-link value
    private boolean[] onStack;
    private Stack<Integer> stack;
    private List<List<Integer>> sccs;

    /**
     * Constructor.
     * @param graph input directed graph
     * @param metrics metrics tracker
     */
    public SccTarjan(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Find all strongly connected components.
     * @return list of SCCs, each SCC is a list of vertices
     */
    public List<List<Integer>> findSCCs() {
        int n = graph.getN();
        disc = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();

        Arrays.fill(disc, -1);
        Arrays.fill(low, -1);
        time = 0;

        metrics.startTimer();

        // DFS from each unvisited vertex
        for (int u = 0; u < n; u++) {
            if (disc[u] == -1) {
                dfs(u);
            }
        }

        metrics.stopTimer();

        return sccs;
    }

    /**
     * DFS traversal with Tarjan's logic.
     * @param u current vertex
     */
    private void dfs(int u) {
        metrics.increment("dfs_visits");

        // Initialize discovery time and low value
        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;

        // Visit all neighbors
        for (Edge e : graph.getEdges(u)) {
            int v = e.getV();
            metrics.increment("edge_checks");

            if (disc[v] == -1) {
                // Tree edge: v not visited
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Back edge: v is in current SCC
                low[u] = Math.min(low[u], disc[v]);
            }
            // Cross edge: v in different SCC, ignore
        }

        // If u is root of SCC, pop the component
        if (low[u] == disc[u]) {
            List<Integer> scc = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                scc.add(v);
                metrics.increment("stack_pops");
            } while (v != u);

            Collections.sort(scc); // for consistent output
            sccs.add(scc);
            metrics.increment("sccs_found");
        }
    }

    /**
     * Get component assignment for each vertex.
     * @param sccs list of SCCs
     * @return array where component[v] = SCC index of vertex v
     */
    public static int[] getComponentMapping(List<List<Integer>> sccs) {
        int maxVertex = 0;
        for (List<Integer> scc : sccs) {
            for (int v : scc) {
                maxVertex = Math.max(maxVertex, v);
            }
        }

        int[] component = new int[maxVertex + 1];
        Arrays.fill(component, -1);

        for (int i = 0; i < sccs.size(); i++) {
            for (int v : sccs.get(i)) {
                component[v] = i;
            }
        }

        return component;
    }

    /**
     * Format SCCs for output.
     * @param sccs list of SCCs
     * @return formatted string
     */
    public static String formatSCCs(List<List<Integer>> sccs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Strongly Connected Components (").append(sccs.size()).append("):\n");
        for (int i = 0; i < sccs.size(); i++) {
            List<Integer> scc = sccs.get(i);
            sb.append("  SCC ").append(i).append(" (size=").append(scc.size()).append("): ");
            sb.append(scc).append("\n");
        }
        return sb.toString();
    }
}