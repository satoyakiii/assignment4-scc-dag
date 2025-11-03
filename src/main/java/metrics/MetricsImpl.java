package metrics;

import java.util.HashMap;
import java.util.Map; /**
 * Default implementation of Metrics interface.
 * Thread-safe for single-threaded use (no synchronization).
 */
public class MetricsImpl implements Metrics {

    private final Map<String, Long> counters = new HashMap<>();
    private long startTime = -1;
    private long endTime = -1;

    @Override
    public void increment(String key) {
        add(key, 1);
    }

    @Override
    public void add(String key, long amount) {
        counters.put(key, counters.getOrDefault(key, 0L) + amount);
    }

    @Override
    public long getCount(String key) {
        return counters.getOrDefault(key, 0L);
    }

    @Override
    public void startTimer() {
        startTime = System.nanoTime();
        endTime = -1;
    }

    @Override
    public void stopTimer() {
        endTime = System.nanoTime();
    }

    @Override
    public long getElapsedNanos() {
        if (startTime == -1) return -1;
        if (endTime == -1) return -1;
        return endTime - startTime;
    }

    @Override
    public double getElapsedMillis() {
        long nanos = getElapsedNanos();
        return nanos == -1 ? -1.0 : nanos / 1_000_000.0;
    }

    @Override
    public void reset() {
        counters.clear();
        startTime = -1;
        endTime = -1;
    }

    @Override
    public Map<String, Long> getAllCounts() {
        return new HashMap<>(counters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Metrics:\n");
        counters.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
        sb.append("  elapsed_time_ms: ").append(getElapsedMillis()).append("\n");
        return sb.toString();
    }
}
