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

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PipelineRunner {
    public static void main(String[] args) throws Exception {
        String dataDir = "data";
        Path outDir = Paths.get("report");
        Files.createDirectories(outDir);
        File csv = outDir.resolve("results.csv").toFile();

        try (PrintWriter pw = new PrintWriter(new FileWriter(csv))) {
            // Header
            pw.println("file,n,edges,directed,scc_count,avg_scc_size,condensed_n,condensed_edges,is_condensation_dag,topo_time_ms,scc_time_ms,dags_short_ms,dags_long_ms,dfs_visits,edge_checks,stack_pops,queue_pushes,edge_relaxations,successful_relaxations");

            ObjectMapper mapper = new ObjectMapper();

            try (var stream = Files.list(Paths.get(dataDir))) {
                List<Path> files = stream
                        .filter(p -> p.toString().endsWith(".json"))
                        .sorted()
                        .toList();

                for (Path path : files) {
                    GraphData data = mapper.readValue(path.toFile(), GraphData.class);
                    Graph g = Graph.fromGraphData(data);

                    MetricsImpl metrics = new MetricsImpl();

                    // --- SCC ---
                    SccTarjan scc = new SccTarjan(g, metrics);
                    long sccStart = System.nanoTime();
                    List<List<Integer>> sccs = scc.findSCCs();
                    long sccEnd = System.nanoTime();

                    int sccCount = sccs.size();
                    double avgScc = sccs.stream().mapToInt(List::size).average().orElse(0.0);

                    // --- Condensation ---
                    Condensation cond = new Condensation(g, sccs);
                    Graph cg = cond.getCondensationGraph();
                    boolean isDag = cond.isDAG();

                    // --- Topo ---
                    MetricsImpl topoMetrics = new MetricsImpl();
                    TopologicalSort topo = new TopologicalSort(cg, topoMetrics);
                    long topoStart = System.nanoTime();
                    List<Integer> order = topo.kahnSort();
                    long topoEnd = System.nanoTime();

                    // --- DAG Shortest/Longest Paths ---
                    MetricsImpl dagMetrics = new MetricsImpl();
                    long dshortStart = System.nanoTime();
                    try {
                        new DagShortestPath(cg, dagMetrics).compute(0);
                    } catch (Exception ignore) {}
                    long dshortEnd = System.nanoTime();

                    long dlongStart = System.nanoTime();
                    try {
                        new DagLongestPath(cg, dagMetrics).compute(0);
                    } catch (Exception ignore) {}
                    long dlongEnd = System.nanoTime();

                    // --- Collect metrics ---
                    Map<String, Long> allCounts = metrics.getAllCounts();

                    pw.printf(
                            Locale.ROOT,
                            "%s,%d,%d,%b,%d,%.2f,%d,%d,%b,%.3f,%.3f,%.3f,%.3f,%d,%d,%d,%d,%d,%d%n",
                            path.getFileName().toString(),
                            g.getN(),
                            g.getEdgeCount(),
                            g.isDirected(),
                            sccCount,
                            avgScc,
                            cg.getN(),
                            cg.getEdgeCount(),
                            isDag,
                            (topoEnd - topoStart) / 1_000_000.0,
                            (sccEnd - sccStart) / 1_000_000.0,
                            (dshortEnd - dshortStart) / 1_000_000.0,
                            (dlongEnd - dlongStart) / 1_000_000.0,
                            allCounts.getOrDefault("dfs_visits", 0L),
                            allCounts.getOrDefault("edge_checks", 0L),
                            allCounts.getOrDefault("stack_pops", 0L),
                            allCounts.getOrDefault("queue_pushes", 0L),
                            allCounts.getOrDefault("edge_relaxations", 0L),
                            allCounts.getOrDefault("successful_relaxations", 0L)
                    );
                }
            }

            System.out.println("✅ Written report/results.csv successfully!");
        }
    }
}
