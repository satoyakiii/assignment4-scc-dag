package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Edge;
import model.GraphData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for test graph datasets in JSON format.
 * Creates 9 files: 3 small, 3 medium, 3 large with varying structures.
 *
 * Categories:
 * - Small: 6-10 nodes
 * - Medium: 10-20 nodes
 * - Large: 20-50 nodes
 *
 * Variants: DAG, cyclic, mixed SCCs, sparse/dense
 */
public class DataGenerator {

    private static final Random random = new Random(42); // fixed seed for reproducibility
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) {
        String outputDir = "data";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // Small datasets (6-10 nodes)
            generateSmall1(outputDir); // Simple DAG
            generateSmall2(outputDir); // Single cycle
            generateSmall3(outputDir); // Two SCCs

            // Medium datasets (10-20 nodes)
            generateMedium1(outputDir); // Sparse DAG
            generateMedium2(outputDir); // Dense with cycles
            generateMedium3(outputDir); // Multiple SCCs

            // Large datasets (20-50 nodes)
            generateLarge1(outputDir); // Sparse long paths
            generateLarge2(outputDir); // Dense complex
            generateLarge3(outputDir); // Many small SCCs

            System.out.println("Successfully generated 9 datasets in " + outputDir + "/");

        } catch (IOException e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== SMALL DATASETS ====================

    /**
     * Small 1: Simple DAG (8 nodes, linear + branches)
     */
    private static void generateSmall1(String dir) throws IOException {
        List<Edge> edges = List.of(
                new Edge(0, 1, 2),
                new Edge(0, 2, 3),
                new Edge(1, 3, 1),
                new Edge(2, 3, 2),
                new Edge(3, 4, 4),
                new Edge(4, 5, 1)
        );

        GraphData data = new GraphData(true, 6, edges, 0, "edge");
        saveToFile(data, dir + "/data_small_1.json");
        System.out.println("Generated data_small_1.json: 6 nodes, DAG, sparse");
    }

    /**
     * Small 2: Single cycle (7 nodes, one SCC)
     */
    private static void generateSmall2(String dir) throws IOException {
        List<Edge> edges = List.of(
                new Edge(0, 1, 3),
                new Edge(1, 2, 2),
                new Edge(2, 3, 4),
                new Edge(3, 1, 1), // creates cycle 1→2→3→1
                new Edge(0, 4, 5),
                new Edge(4, 5, 2)
        );

        GraphData data = new GraphData(true, 6, edges, 0, "edge");
        saveToFile(data, dir + "/data_small_2.json");
        System.out.println("Generated data_small_2.json: 6 nodes, 1 cycle (SCC: {1,2,3})");
    }

    /**
     * Small 3: Two SCCs (9 nodes)
     */
    private static void generateSmall3(String dir) throws IOException {
        List<Edge> edges = List.of(
                // SCC 1: {0, 1, 2}
                new Edge(0, 1, 2),
                new Edge(1, 2, 3),
                new Edge(2, 0, 1),
                // SCC 2: {3, 4}
                new Edge(3, 4, 2),
                new Edge(4, 3, 1),
                // Connection between SCCs
                new Edge(2, 3, 5),
                // Isolated nodes
                new Edge(5, 6, 3),
                new Edge(6, 7, 2)
        );

        GraphData data = new GraphData(true, 8, edges, 0, "edge");
        saveToFile(data, dir + "/data_small_3.json");
        System.out.println("Generated data_small_3.json: 8 nodes, 2 SCCs");
    }

    // ==================== MEDIUM DATASETS ====================

    /**
     * Medium 1: Sparse DAG (15 nodes)
     */
    private static void generateMedium1(String dir) throws IOException {
        List<Edge> edges = new ArrayList<>();

        // Create a tree-like DAG
        for (int i = 0; i < 7; i++) {
            edges.add(new Edge(i, 2*i+1, random.nextInt(10) + 1));
            edges.add(new Edge(i, 2*i+2, random.nextInt(10) + 1));
        }

        GraphData data = new GraphData(true, 15, edges, 0, "edge");
        saveToFile(data, dir + "/data_medium_1.json");
        System.out.println("Generated data_medium_1.json: 15 nodes, sparse DAG (tree-like)");
    }

    /**
     * Medium 2: Dense with cycles (12 nodes)
     */
    private static void generateMedium2(String dir) throws IOException {
        List<Edge> edges = new ArrayList<>();
        int n = 12;

        // Dense connections with some cycles
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 3; j++) {
                int target = (i + j + 1) % n;
                edges.add(new Edge(i, target, random.nextInt(8) + 1));
            }
        }

        GraphData data = new GraphData(true, n, edges, 0, "edge");
        saveToFile(data, dir + "/data_medium_2.json");
        System.out.println("Generated data_medium_2.json: 12 nodes, dense with cycles");
    }

    /**
     * Medium 3: Multiple SCCs (18 nodes)
     */
    private static void generateMedium3(String dir) throws IOException {
        List<Edge> edges = new ArrayList<>();

        // Create 4 SCCs of varying sizes
        // SCC 1: {0,1,2,3}
        edges.add(new Edge(0, 1, 2));
        edges.add(new Edge(1, 2, 3));
        edges.add(new Edge(2, 3, 1));
        edges.add(new Edge(3, 0, 2));

        // SCC 2: {4,5,6}
        edges.add(new Edge(4, 5, 2));
        edges.add(new Edge(5, 6, 1));
        edges.add(new Edge(6, 4, 3));

        // SCC 3: {7,8}
        edges.add(new Edge(7, 8, 4));
        edges.add(new Edge(8, 7, 2));

        // SCC 4: single nodes {9}, {10}, {11}

        // Inter-SCC edges
        edges.add(new Edge(3, 4, 5));
        edges.add(new Edge(6, 7, 3));
        edges.add(new Edge(8, 9, 2));
        edges.add(new Edge(9, 10, 1));
        edges.add(new Edge(10, 11, 4));

        GraphData data = new GraphData(true, 12, edges, 0, "edge");
        saveToFile(data, dir + "/data_medium_3.json");
        System.out.println("Generated data_medium_3.json: 12 nodes, 4+ SCCs");
    }

    // ==================== LARGE DATASETS ====================

    /**
     * Large 1: Sparse with long paths (30 nodes)
     */
    private static void generateLarge1(String dir) throws IOException {
        List<Edge> edges = new ArrayList<>();
        int n = 30;

        // Create long chains
        for (int i = 0; i < n - 1; i += 3) {
            edges.add(new Edge(i, i+1, random.nextInt(10) + 1));
            edges.add(new Edge(i+1, i+2, random.nextInt(10) + 1));
            if (i + 3 < n) {
                edges.add(new Edge(i+2, i+3, random.nextInt(10) + 1));
            }
        }

        // Add some cross edges
        for (int i = 0; i < n - 5; i += 5) {
            edges.add(new Edge(i, i+5, random.nextInt(15) + 5));
        }

        GraphData data = new GraphData(true, n, edges, 0, "edge");
        saveToFile(data, dir + "/data_large_1.json");
        System.out.println("Generated data_large_1.json: 30 nodes, sparse, long paths");
    }

    /**
     * Large 2: Dense complex (25 nodes)
     */
    private static void generateLarge2(String dir) throws IOException {
        List<Edge> edges = new ArrayList<>();
        int n = 25;
        double density = 0.15; // 15% of possible edges

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && random.nextDouble() < density) {
                    edges.add(new Edge(i, j, random.nextInt(20) + 1));
                }
            }
        }

        GraphData data = new GraphData(true, n, edges, 0, "edge");
        saveToFile(data, dir + "/data_large_2.json");
        System.out.println("Generated data_large_2.json: 25 nodes, dense (~15% edges)");
    }

    /**
     * Large 3: Many small SCCs (40 nodes)
     */
    private static void generateLarge3(String dir) throws IOException {
        List<Edge> edges = new ArrayList<>();
        int n = 40;

        // Create 10 SCCs of size 3-5 each
        int node = 0;
        for (int scc = 0; scc < 10; scc++) {
            int sccSize = 3 + random.nextInt(3); // 3-5 nodes
            int start = node;

            // Create cycle within SCC
            for (int i = 0; i < sccSize; i++) {
                int from = start + i;
                int to = start + ((i + 1) % sccSize);
                if (from < n && to < n) {
                    edges.add(new Edge(from, to, random.nextInt(8) + 1));
                }
            }

            // Connect to next SCC
            if (node + sccSize < n) {
                edges.add(new Edge(node + sccSize - 1, node + sccSize, random.nextInt(10) + 1));
            }

            node += sccSize;
            if (node >= n) break;
        }

        GraphData data = new GraphData(true, n, edges, 0, "edge");
        saveToFile(data, dir + "/data_large_3.json");
        System.out.println("Generated data_large_3.json: 40 nodes, ~10 SCCs");
    }

    // ==================== UTILITY ====================

    private static void saveToFile(GraphData data, String filename) throws IOException {
        mapper.writeValue(new File(filename), data);
    }
}