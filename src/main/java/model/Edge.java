package model;

/**
 * Represents a directed edge in a graph.
 * Used for JSON deserialization and graph construction.
 *
 * Fields:
 * - u: source vertex
 * - v: destination vertex
 * - w: edge weight (for weighted graphs)
 */

public class Edge {
    private int u;
    private int v;
    private int w;

    // Default constructor for Jackson
    public Edge() {
    }

    /**
     * Constructor for creating an edge.
     * @param u source vertex
     * @param v destination vertex
     * @param w edge weight
     */
    public Edge(int u, int v, int w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public int getU() {
        return u;
    }

    public void setU(int u) {
        this.u = u;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    @Override
    public String toString() {
        return String.format("(%d → %d, w=%d)", u, v, w);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Edge)) return false;
        Edge other = (Edge) obj;
        return u == other.u && v == other.v && w == other.w;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * u + v) + w;
    }
}