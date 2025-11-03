package model;

import java.util.*;

/**
 * Graph representation using adjacency list.
 * Supports directed/undirected graphs with weighted edges.
 *
 * Complexity: O(V + E) space
 */
public class Graph {

    private final int n; // number of vertices
    private final List<List<Edge>> adj; // adjacency list
    private final boolean directed;

    /**
     * Construct graph from number of vertices.
     * @param n number of vertices
     * @param directed true if directed graph
     */
    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    /**
     * Build graph from GraphData (JSON model).
     * @param data parsed JSON data
     * @return constructed Graph
     */
    public static Graph fromGraphData(GraphData data) {
        Graph g = new Graph(data.getN(), data.isDirected());
        for (Edge e : data.getEdges()) {
            g.addEdge(e.getU(), e.getV(), e.getW());
        }
        return g;
    }

    /**
     * Add a directed edge u → v with weight w.
     * If graph is undirected, also adds v → u.
     * @param u source vertex
     * @param v destination vertex
     * @param w edge weight
     */
    public void addEdge(int u, int v, int w) {
        adj.get(u).add(new Edge(u, v, w));
        if (!directed) {
            adj.get(v).add(new Edge(v, u, w));
        }
    }

    /**
     * Get all outgoing edges from vertex u.
     * @param u vertex
     * @return list of edges
     */
    public List<Edge> getEdges(int u) {
        return adj.get(u);
    }

    /**
     * Get number of vertices.
     */
    public int getN() {
        return n;
    }

    /**
     * Check if graph is directed.
     */
    public boolean isDirected() {
        return directed;
    }

    /**
     * Get reverse graph (all edges reversed).
     * Used in Kosaraju's algorithm.
     * @return reversed graph
     */
    public Graph reverse() {
        Graph rev = new Graph(n, true);
        for (int u = 0; u < n; u++) {
            for (Edge e : adj.get(u)) {
                rev.addEdge(e.getV(), e.getU(), e.getW());
            }
        }
        return rev;
    }

    /**
     * Get total number of edges.
     */
    public int getEdgeCount() {
        int count = 0;
        for (int u = 0; u < n; u++) {
            count += adj.get(u).size();
        }
        return directed ? count : count / 2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Graph: %d vertices, %d edges, %s\n",
                n, getEdgeCount(), directed ? "directed" : "undirected"));
        for (int u = 0; u < n; u++) {
            if (!adj.get(u).isEmpty()) {
                sb.append(u).append(" → ");
                for (Edge e : adj.get(u)) {
                    sb.append(String.format("%d(w=%d) ", e.getV(), e.getW()));
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}