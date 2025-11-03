import com.fasterxml.jackson.databind.ObjectMapper;
import graph.dagsp.DagLongestPath;
import graph.dagsp.DagShortestPath;
import graph.scc.Condensation;
import graph.scc.SccTarjan;
import graph.topo.TopologicalSort;
import metrics.Metrics;
import metrics.MetricsImpl;
import model.Graph;
import model.GraphData;

import java.io.File;
import java.util.List;

/**
 * Main application class for Assignment 4.
 *
 * Pipeline:
 * 1. Read JSON input (graph with edges, source, weight model)
 * 2. Find SCCs using Tarjan's algorithm
 * 3. Build condensation DAG
 * 4. Compute topological sort of condensation
 * 5. Find shortest and longest paths in DAG
 * 6. Print results and metrics
 *
 * Usage:
 *   mvn exec:java -Dexec.args="data/tasks.json"
 *   java -cp target/assignment4-1.0.jar App data/tasks.json
 */
public class App {

    public static void main(String[] args) {
        // Default input file
        String inputFile = "data/tasks.json";

        if (args.length > 0) {
            inputFile = args[0];
        }

        System.out.println("=".repeat(60));
        System.out.println("Assignment 4: SCC, Topological Sort, DAG Shortest/Longest Paths");
        System.out.println("=".repeat(60));
        System.out.println("Input file: " + inputFile);
        System.out.println();

        try {
            // Step 1: Read JSON
            ObjectMapper mapper = new ObjectMapper();
            GraphData data = mapper.readValue(new File(inputFile), GraphData.class);

            System.out.println("Graph loaded: " + data);
            System.out.println();

            // Step 2: Build graph
            Graph graph = Graph.fromGraphData(data);
            System.out.println(graph);

            // Step 3: Find SCCs
            System.out.println("-".repeat(60));
            System.out.println("STEP 1: Finding Strongly Connected Components (Tarjan)");
            System.out.println("-".repeat(60));

            Metrics sccMetrics = new MetricsImpl();
            SccTarjan sccAlgo = new SccTarjan(graph, sccMetrics);
            List<List<Integer>> sccs = sccAlgo.findSCCs();

            System.out.println(SccTarjan.formatSCCs(sccs));
            System.out.println("Metrics:");
            System.out.println("  DFS visits: " + sccMetrics.getCount("dfs_visits"));
            System.out.println("  Edge checks: " + sccMetrics.getCount("edge_checks"));
            System.out.println("  Stack pops: " + sccMetrics.getCount("stack_pops"));
            System.out.println("  SCCs found: " + sccMetrics.getCount("sccs_found"));
            System.out.println("  Time: " + sccMetrics.getElapsedMillis() + " ms");
            System.out.println();

            // Step 4: Build condensation
            System.out.println("-".repeat(60));
            System.out.println("STEP 2: Building Condensation DAG");
            System.out.println("-".repeat(60));

            Condensation condensation = new Condensation(graph, sccs);
            System.out.println(condensation);

            // Step 5: Topological sort of condensation
            System.out.println("-".repeat(60));
            System.out.println("STEP 3: Topological Sort (Kahn's Algorithm)");
            System.out.println("-".repeat(60));

            Metrics topoMetrics = new MetricsImpl();
            TopologicalSort topoSort = new TopologicalSort(condensation.getCondensationGraph(), topoMetrics);
            List<Integer> componentOrder = topoSort.kahnSort();

            if (componentOrder == null) {
                System.out.println("ERROR: Condensation has a cycle (should not happen!)");
            } else {
                System.out.println(TopologicalSort.formatOrder(componentOrder, "Components"));

                // Expand to original vertices
                List<Integer> vertexOrder = TopologicalSort.expandToVertices(componentOrder, sccs);
                System.out.println(TopologicalSort.formatOrder(vertexOrder, "Original Vertices"));

                System.out.println("Metrics:");
                System.out.println("  Queue pushes: " + topoMetrics.getCount("queue_pushes"));
                System.out.println("  Queue pops: " + topoMetrics.getCount("queue_pops"));
                System.out.println("  Edge relaxations: " + topoMetrics.getCount("edge_relaxations"));
                System.out.println("  Time: " + topoMetrics.getElapsedMillis() + " ms");
            }
            System.out.println();

            // Step 6: Shortest paths in condensation DAG
            System.out.println("-".repeat(60));
            System.out.println("STEP 4: Shortest Paths in Condensation DAG");
            System.out.println("-".repeat(60));

            int sourceComponent = condensation.getComponent(data.getSource());
            System.out.println("Source vertex: " + data.getSource() + " (in component " + sourceComponent + ")");
            System.out.println();

            Metrics spMetrics = new MetricsImpl();
            DagShortestPath spAlgo = new DagShortestPath(condensation.getCondensationGraph(), spMetrics);
            DagShortestPath.ShortestPathResult spResult = spAlgo.compute(sourceComponent);

            System.out.println(spResult.format());
            System.out.println("Metrics:");
            System.out.println("  Vertices processed: " + spMetrics.getCount("vertices_processed"));
            System.out.println("  Edge relaxations: " + spMetrics.getCount("edge_relaxations"));
            System.out.println("  Successful relaxations: " + spMetrics.getCount("successful_relaxations"));
            System.out.println("  Time: " + spMetrics.getElapsedMillis() + " ms");
            System.out.println();

            // Step 7: Longest path (Critical Path)
            System.out.println("-".repeat(60));
            System.out.println("STEP 5: Longest Path (Critical Path) in Condensation DAG");
            System.out.println("-".repeat(60));

            Metrics lpMetrics = new MetricsImpl();
            DagLongestPath lpAlgo = new DagLongestPath(condensation.getCondensationGraph(), lpMetrics);
            DagLongestPath.CriticalPathResult cpResult = lpAlgo.findCriticalPath(sourceComponent);

            System.out.println(cpResult.format());
            System.out.println("Metrics:");
            System.out.println("  Vertices processed: " + lpMetrics.getCount("vertices_processed"));
            System.out.println("  Edge relaxations: " + lpMetrics.getCount("edge_relaxations"));
            System.out.println("  Successful relaxations: " + lpMetrics.getCount("successful_relaxations"));
            System.out.println("  Time: " + lpMetrics.getElapsedMillis() + " ms");
            System.out.println();

            // Summary
            System.out.println("=".repeat(60));
            System.out.println("SUMMARY");
            System.out.println("=".repeat(60));
            System.out.println("Graph: " + data.getN() + " vertices, " + data.getEdges().size() + " edges");
            System.out.println("SCCs found: " + sccs.size());
            System.out.println("Condensation: " + condensation.getCondensationGraph().getN() + " components, "
                    + condensation.getCondensationGraph().getEdgeCount() + " edges");
            System.out.println("Is DAG: " + condensation.isDAG());
            System.out.println();
            System.out.println("Total execution time:");
            System.out.println("  SCC: " + sccMetrics.getElapsedMillis() + " ms");
            System.out.println("  Topo Sort: " + topoMetrics.getElapsedMillis() + " ms");
            System.out.println("  Shortest Path: " + spMetrics.getElapsedMillis() + " ms");
            System.out.println("  Longest Path: " + lpMetrics.getElapsedMillis() + " ms");
            System.out.println("=".repeat(60));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}