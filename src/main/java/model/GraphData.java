package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data model for JSON deserialization.
 * Represents the structure of input JSON files (tasks.json, etc.).
 *
 * Example JSON:
 * {
 *   "directed": true,
 *   "n": 8,
 *   "edges": [{"u": 0, "v": 1, "w": 3}, ...],
 *   "source": 4,
 *   "weight_model": "edge"
 * }
 */
public class GraphData {

    @JsonProperty("directed")
    private boolean directed;

    @JsonProperty("n")
    private int n;

    @JsonProperty("edges")
    private List<Edge> edges;

    @JsonProperty("source")
    private int source;

    @JsonProperty("weight_model")
    private String weightModel;

    // Default constructor for Jackson
    public GraphData() {
    }

    /**
     * Full constructor.
     */
    public GraphData(boolean directed, int n, List<Edge> edges, int source, String weightModel) {
        this.directed = directed;
        this.n = n;
        this.edges = edges;
        this.source = source;
        this.weightModel = weightModel;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getWeightModel() {
        return weightModel;
    }

    public void setWeightModel(String weightModel) {
        this.weightModel = weightModel;
    }

    @Override
    public String toString() {
        return String.format("GraphData{directed=%s, n=%d, edges=%d, source=%d, model=%s}",
                directed, n, edges != null ? edges.size() : 0, source, weightModel);
    }
}