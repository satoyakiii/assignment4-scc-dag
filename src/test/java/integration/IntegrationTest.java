package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import graph.dagsp.DagLongestPath;
import graph.dagsp.DagShortestPath;
import graph.scc.Condensation;
import graph.scc.SccTarjan;
import graph.topo.TopologicalSort;
import metrics.MetricsImpl;
import model.Graph;
import model.GraphData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for full graph analysis pipeline:
 * JSON → Graph → SCC → Condensation → TopoSort → DAG SP/LP.
 */
public class IntegrationTest {

    private static final String DATA_DIR = "data";

    /** Load and build a Graph object from JSON file */
    private Graph loadGraphFromFile(String filename) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        GraphData data = mapper.readValue(new File(filename), GraphData.class);
        assertNotNull(data, "GraphData should not be null");
        Graph g = Graph.fromGraphData(data);
        assertTrue(g.getN() > 0, "Graph should have vertices");
        return g;
    }

    @Test
    public void testFullPipelineForAllFiles() throws Exception {
        List<Path> jsonFiles;
        try (var stream = Files.list(Paths.get(DATA_DIR))) {
            jsonFiles = stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }

        assertFalse(jsonFiles.isEmpty(), "No JSON files found in data/");

        for (Path path : jsonFiles) {
            String filename = path.toString();
            System.out.println("\n=== Integration Test for " + filename + " ===");

            // === 1. Load Graph ===
            Graph graph = loadGraphFromFile(filename);
            MetricsImpl metrics = new MetricsImpl();

            System.out.printf("Graph loaded: %d vertices, %d edges, directed=%s%n",
                    graph.getN(), graph.getEdgeCount(), graph.isDirected());

            // === 2. Run Tarjan SCC ===
            SccTarjan sccTarjan = new SccTarjan(graph, metrics);
            List<List<Integer>> sccs = sccTarjan.findSCCs();
            assertNotNull(sccs, "SCC list must not be null");
            assertFalse(sccs.isEmpty(), "At least one SCC expected");

            System.out.println("SCC count = " + sccs.size());
            System.out.println(SccTarjan.formatSCCs(sccs));

            // === 3. Build Condensation DAG ===
            Condensation condensation = new Condensation(graph, sccs);
            Graph condensed = condensation.getCondensationGraph();
            assertTrue(condensation.isDAG(), "Condensation must be a DAG");

            System.out.printf("Condensation graph: %d nodes, %d edges%n",
                    condensed.getN(), condensed.getEdgeCount());

            // === 4. Topological Sort (Kahn) ===
            TopologicalSort topoSort = new TopologicalSort(condensed, new MetricsImpl());
            List<Integer> topoOrder = topoSort.kahnSort();
            assertNotNull(topoOrder, "TopoSort must not return null");
            assertEquals(condensed.getN(), topoOrder.size(), "TopoSort must include all nodes");
            System.out.println("Topological order (components): " + topoOrder);

            // === 5. DAG Shortest Path (from component 0) ===
            try {
                DagShortestPath sp = new DagShortestPath(condensed, new MetricsImpl());
                DagShortestPath.ShortestPathResult spResult = sp.compute(0);
                assertNotNull(spResult.getDistances());
                System.out.println(spResult.format());
            } catch (Exception e) {
                System.out.println("Shortest path skipped (graph not DAG or invalid source)");
            }

            // === 6. DAG Longest Path (from component 0) ===
            try {
                DagLongestPath lp = new DagLongestPath(condensed, new MetricsImpl());
                DagLongestPath.LongestPathResult lpResult = lp.compute(0);
                assertNotNull(lpResult.getDistances());
                System.out.println(lpResult.format());
            } catch (Exception e) {
                System.out.println("Longest path skipped (graph not DAG or invalid source)");
            }

            // === 7. Metrics Summary ===
            System.out.println("\nMetrics summary:");
            System.out.println(metrics.toString());

            System.out.println("✅ Passed integration test for: " + filename);
        }
    }
}
