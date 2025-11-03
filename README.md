
# Assignment 4 — SCC, Topological Sort & DAG Shortest/Longest Paths  
**Course:** Design and Analysis of Algorithms  
**Instructor:** Zarina Sayakulova  
**Student:** Assem Rakhmanova  
**Group:** SE-2428  

*Report written by me.*

---

## Overview
This assignment implements a complete pipeline for processing **scheduling and dependency graphs**, relevant to Smart City / Smart Campus applications.

**Pipeline stages:**  
`JSON graph → SCC (Tarjan) → Condensation (component DAG) → Topological Sort → DAG Shortest & Longest Paths`

Each stage is instrumented with timing and operation counters. Results are automatically collected into `report/results.csv`, ensuring full reproducibility and ease of analysis.

---

## Implemented Components
- **Tarjan’s SCC algorithm:** `graph.scc.SccTarjan`  
- **Graph condensation (component DAG):** `graph.scc.Condensation`  
- **Topological sorting (Kahn & DFS):** `graph.topo.TopologicalSort`  
- **DAG shortest and longest paths (critical path):** `graph.dagsp.*`  
- **Instrumentation:** `metrics.Metrics`, `metrics.MetricsImpl` (timers + counters)  
- **Dataset generator:** `tools.DataGenerator` (9 datasets under `/data/`)  
- **Pipeline runner:** `integration.PipelineRunner` → generates `report/results.csv`

The focus was on **reproducibility**, **clarity**, and **concise output**, rather than theoretical discussion.

---

## How to Reproduce

**Requirements:** JDK 17 + Maven

```bash
# Build the project
mvn clean package -DskipTests

# (Optional) Regenerate datasets
mvn -Dexec.mainClass="tools.DataGenerator" exec:java

# Run the full pipeline
mvn -Dexec.mainClass="integration.PipelineRunner" exec:java

# Output file:
report/results.csv
````

**CSV fields:**

`file,n,edges,directed,scc_count,avg_scc_size,condensed_n,condensed_edges,is_condensation_dag,topo_time_ms,scc_time_ms,dags_short_ms,dags_long_ms,dfs_visits,edge_checks,stack_pops,queue_pushes,edge_relaxations,successful_relaxations`

---

## Datasets

All graph datasets are located in `/data/`.
A total of **9 generated graphs** (small, medium, large) plus `tasks.json` were used.

| File               |  n | Edges | Type         | Description          |
| ------------------ | -: | ----: | ------------ | -------------------- |
| data_small_1.json  |  6 |     6 | DAG          | Simple acyclic graph |
| data_small_2.json  |  6 |     6 | Cyclic       | Single cycle         |
| data_small_3.json  |  8 |     8 | Cyclic       | Two SCCs + extras    |
| data_medium_1.json | 15 |    14 | DAG          | Sparse, tree-like    |
| data_medium_2.json | 12 |    36 | Dense/Cyclic | Large SCC            |
| data_medium_3.json | 12 |    14 | Mixed        | Several SCCs         |
| data_large_1.json  | 30 |    34 | DAG          | Long sparse chains   |
| data_large_2.json  | 25 |    79 | Dense/Cyclic | Many cross edges     |
| data_large_3.json  | 40 |    46 | Mixed        | Many small SCCs      |
| tasks.json         |  8 |     7 | Mixed        | Example task graph   |

---

## Results (Extracted from `report/results.csv`)

All timings are in milliseconds.

| File               |  n | Edges | SCC | Avg SCC size | Condensed n | Condensed edges | SCC (ms) | Topo (ms) | Shortest (ms) | Longest (ms) |
| ------------------ | -: | ----: | --: | -----------: | ----------: | --------------: | -------: | --------: | ------------: | -----------: |
| data_small_1.json  |  6 |     6 |   6 |         1.00 |           6 |               6 |    0.035 |     0.018 |         0.016 |        0.014 |
| data_small_2.json  |  6 |     6 |   4 |         1.50 |           4 |               3 |    0.031 |     0.015 |         0.012 |        0.009 |
| data_small_3.json  |  8 |     8 |   5 |         1.60 |           5 |               3 |    0.027 |     0.014 |         0.012 |        0.010 |
| data_medium_1.json | 15 |    14 |  15 |         1.00 |          15 |              14 |    0.078 |     0.159 |         0.037 |        0.034 |
| data_medium_2.json | 12 |    36 |   1 |        12.00 |           1 |               0 |    0.050 |     0.014 |         0.011 |        0.009 |
| data_medium_3.json | 12 |    14 |   6 |         2.00 |           6 |               5 |    0.119 |     0.014 |         0.016 |        0.013 |
| data_large_1.json  | 30 |    34 |  30 |         1.00 |          30 |              34 |    0.418 |     0.158 |         1.345 |        1.393 |
| data_large_2.json  | 25 |    79 |   3 |         8.33 |           3 |               2 |    0.097 |     0.021 |         0.018 |        0.013 |
| data_large_3.json  | 40 |    46 |  14 |         2.86 |          14 |              10 |    0.092 |     0.024 |         0.027 |        0.020 |
| tasks.json         |  8 |     7 |   6 |         1.33 |           6 |               4 |    0.030 |     0.019 |         0.014 |        0.012 |

---

## Analysis

* **SCC (Tarjan):** Performs reliably. Dense graphs compress into fewer components (e.g., `data_medium_2` → 1 SCC). Execution time follows *O(V+E)* and remains minimal for all test sizes.
* **Condensation:** Always yields a valid DAG. Strongly reduces problem size in cyclic/dense cases.
* **Topological sort:** Extremely fast (<0.2 ms). Once condensed, most graphs are trivial to sort.
* **Shortest/Longest paths:** Both scale linearly (*O(V+E)*). Small graphs run in ~0.01 ms; larger DAGs with long chains reach ~1.3 ms. Longest path identifies the **critical chain** for scheduling.
* **Overall:** The pipeline demonstrates near-linear scalability with respect to *V+E*, efficient for graphs up to 40 nodes and ~80 edges.

## Conclusion

This assignment successfully demonstrates a complete and efficient workflow for analyzing directed graphs with dependencies — from raw JSON input to final shortest and longest path computation on a condensed DAG.

All implemented components — Tarjan’s SCC, condensation, topological sorting, and DAG path algorithms — work together seamlessly and produce consistent, reproducible results. Empirical tests confirm that each stage operates within linear time complexity O(V+E), even for dense or cyclic graphs.

The approach is robust, modular, and scalable, making it suitable for real-world dependency management and scheduling tasks (e.g., task planning, build systems, smart infrastructure coordination). The SCC condensation step notably simplifies complex cyclic graphs into manageable acyclic structures, enabling efficient subsequent analysis.

In summary, the pipeline achieves its main goals:

Correctly identifies strongly connected components and their condensation;

Produces valid topological orderings for all test cases;

Computes critical (longest) and optimal (shortest) paths for scheduling analysis;

Maintains excellent performance and clarity of results.

Further extensions — such as adding weighted nodes, visualization, or statistical performance plots — could make the framework even more practical for applied optimization and scheduling scenarios.