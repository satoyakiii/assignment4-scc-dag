package graph.scc;

import model.Edge;
import model.Graph;

import java.util.*;

/**
 * Condensation graph construction from SCCs.
 * Converts a graph with cycles into a DAG where each SCC becomes a single vertex.
 *
 * Time Complexity: O(V + E)
 * Space Complexity: O(V + E)
 *
 * Properties:
 * - Always a DAG (no cycles)
 * - Edge (i, j) exists if there's an edge between SCCs i and j
 * - Preserves minimum edge weight between components
 */
public class Condensation {

    private final Graph condensationGraph;
    private final List<List<Integer>> sccs;
    private final int[] componentMap; // vertex → component index

    /**
     * Build condensation graph from original graph and its SCCs.
     * @param originalGraph input graph
     * @param sccs list of strongly connected components
     */
    public Condensation(Graph originalGraph, List<List<Integer>> sccs) {
        this.sccs = sccs;
        this.componentMap = SccTarjan.getComponentMapping(sccs);
        this.condensationGraph = buildCondensation(originalGraph);
    }

    /**
     * Construct the condensation DAG.
     * @param original original graph
     * @return condensation graph
     */
    private Graph buildCondensation(Graph original) {
        int numComponents = sccs.size();
        Graph cond = new Graph(numComponents, true);

        // Track edges between components (avoid duplicates, keep min weight)
        Map<String, Integer> edgeWeights = new HashMap<>();

        // For each edge in original graph
        for (int u = 0; u < original.getN(); u++) {
            int compU = componentMap[u];

            for (Edge e : original.getEdges(u)) {
                int v = e.getV();
                int compV = componentMap[v];

                // Only add edge if between different components
                if (compU != compV) {
                    String key = compU + "->" + compV;

                    // Keep minimum weight edge between components
                    if (!edgeWeights.containsKey(key) || e.getW() < edgeWeights.get(key)) {
                        edgeWeights.put(key, e.getW());
                    }
                }
            }
        }

        // Add edges to condensation graph
        for (Map.Entry<String, Integer> entry : edgeWeights.entrySet()) {
            String[] parts = entry.getKey().split("->");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            cond.addEdge(from, to, entry.getValue());
        }

        return cond;
    }

    /**
     * Get the condensation DAG.
     * @return condensation graph
     */
    public Graph getCondensationGraph() {
        return condensationGraph;
    }

    /**
     * Get the list of SCCs.
     * @return list of components
     */
    public List<List<Integer>> getSccs() {
        return sccs;
    }

    /**
     * Get component index for a vertex.
     * @param vertex original vertex
     * @return component index
     */
    public int getComponent(int vertex) {
        return componentMap[vertex];
    }

    /**
     * Get all original vertices in a component.
     * @param componentIndex component index
     * @return list of vertices
     */
    public List<Integer> getVerticesInComponent(int componentIndex) {
        return sccs.get(componentIndex);
    }

    /**
     * Check if condensation is indeed a DAG (no cycles).
     * For debugging/validation.
     * @return true if DAG
     */
    public boolean isDAG() {
        int n = condensationGraph.getN();
        int[] color = new int[n]; // 0=white, 1=gray, 2=black

        for (int u = 0; u < n; u++) {
            if (color[u] == 0) {
                if (hasCycleDFS(u, color)) {
                    return false; // found cycle
                }
            }
        }
        return true;
    }

    private boolean hasCycleDFS(int u, int[] color) {
        color[u] = 1; // gray (in progress)

        for (Edge e : condensationGraph.getEdges(u)) {
            int v = e.getV();
            if (color[v] == 1) {
                return true; // back edge = cycle
            }
            if (color[v] == 0 && hasCycleDFS(v, color)) {
                return true;
            }
        }

        color[u] = 2; // black (finished)
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Condensation Graph:\n");
        sb.append("  Components: ").append(sccs.size()).append("\n");
        sb.append("  Edges: ").append(condensationGraph.getEdgeCount()).append("\n");
        sb.append("  Is DAG: ").append(isDAG()).append("\n");

        for (int i = 0; i < sccs.size(); i++) {
            sb.append("  Component ").append(i).append(": ").append(sccs.get(i)).append("\n");
        }

        sb.append("\nCondensation edges:\n");
        for (int u = 0; u < condensationGraph.getN(); u++) {
            for (Edge e : condensationGraph.getEdges(u)) {
                sb.append("  ").append(u).append(" → ").append(e.getV())
                        .append(" (w=").append(e.getW()).append(")\n");
            }
        }

        return sb.toString();
    }
}