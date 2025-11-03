package metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Metrics interface and implementation for algorithm instrumentation.
 * Tracks operation counts and execution time.
 *
 * Usage:
 * <pre>
 * Metrics m = new MetricsImpl();
 * m.startTimer();
 * m.increment("dfs_visits");
 * m.stopTimer();
 * System.out.println(m.getCount("dfs_visits")); // prints count
 * System.out.println(m.getElapsedNanos()); // prints elapsed time
 * </pre>
 */
public interface Metrics {

    /**
     * Increment a named counter by 1.
     * @param key counter name (e.g., "dfs_visits", "relaxations")
     */
    void increment(String key);

    /**
     * Increment a named counter by a specific amount.
     * @param key counter name
     * @param amount value to add
     */
    void add(String key, long amount);

    /**
     * Get current value of a counter.
     * @param key counter name
     * @return current count, or 0 if not found
     */
    long getCount(String key);

    /**
     * Start the timer (captures System.nanoTime()).
     */
    void startTimer();

    /**
     * Stop the timer.
     */
    void stopTimer();

    /**
     * Get elapsed time in nanoseconds.
     * @return elapsed time, or -1 if timer not stopped
     */
    long getElapsedNanos();

    /**
     * Get elapsed time in milliseconds.
     * @return elapsed time in ms
     */
    double getElapsedMillis();

    /**
     * Reset all counters and timer.
     */
    void reset();

    /**
     * Get all counter values as a map.
     * @return map of counter names to values
     */
    Map<String, Long> getAllCounts();
}

