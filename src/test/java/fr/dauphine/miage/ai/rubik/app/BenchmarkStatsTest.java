package fr.dauphine.miage.ai.rubik.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the metric aggregation of the benchmark.
 *
 * <p>The benchmark itself only prints a table, but the way it turns raw per cube
 * measurements into averages and derived quantities (effective branching factor,
 * throughput, peak memory) is plain arithmetic worth pinning down with tests.</p>
 */
class BenchmarkStatsTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("A fresh Stats reports zero everywhere")
    void emptyStats() {
        BenchmarkStats stats = new BenchmarkStats();
        assertEquals(0, stats.attempts());
        assertEquals(0.0, stats.successRate(), EPS);
        assertEquals(0.0, stats.avgExpanded(), EPS);
        assertEquals(0.0, stats.avgGenerated(), EPS);
        assertEquals(0.0, stats.avgMillis(), EPS);
        assertEquals(0.0, stats.avgLength(), EPS);
        assertEquals(0.0, stats.avgPeakFrontier(), EPS);
        assertEquals(0.0, stats.effectiveBranching(), EPS);
        assertEquals(0.0, stats.expandedPerSecond(), EPS);
    }

    @Test
    @DisplayName("Averages divide solved totals by the number of solved cubes")
    void averagesOverSolvedOnly() {
        BenchmarkStats stats = new BenchmarkStats();
        // Two solved cubes, then one timeout that must not pollute the averages.
        stats.recordSolved(100, 1200, 10_000_000L, 6, 80);
        stats.recordSolved(300, 3600, 30_000_000L, 8, 160);
        stats.recordTimeout();

        assertEquals(3, stats.attempts());
        assertEquals(2, stats.solved());
        assertEquals(1, stats.timedOut());
        // Success rate is over all attempts.
        assertEquals(100.0 * 2 / 3, stats.successRate(), EPS);
        // Averages are over the two solved cubes.
        assertEquals(200.0, stats.avgExpanded(), EPS);
        assertEquals(2400.0, stats.avgGenerated(), EPS);
        assertEquals(7.0, stats.avgLength(), EPS);
        assertEquals(120.0, stats.avgPeakFrontier(), EPS);
        // Time is summed in nanoseconds and reported in milliseconds per cube.
        assertEquals((40_000_000L / 1_000_000.0) / 2, stats.avgMillis(), EPS);
    }

    @Test
    @DisplayName("Effective branching is total generated over total expanded")
    void effectiveBranching() {
        BenchmarkStats stats = new BenchmarkStats();
        stats.recordSolved(100, 1000, 5_000_000L, 5, 50);
        stats.recordSolved(100, 1200, 5_000_000L, 5, 50);
        // (1000 + 1200) / (100 + 100) = 11.0
        assertEquals(11.0, stats.effectiveBranching(), EPS);
    }

    @Test
    @DisplayName("Throughput is total expanded over total seconds")
    void throughput() {
        BenchmarkStats stats = new BenchmarkStats();
        // 200000 states expanded in 2 seconds total -> 100000 states per second.
        stats.recordSolved(200_000, 400_000, 2_000_000_000L, 7, 5000);
        assertEquals(100_000.0, stats.expandedPerSecond(), EPS);
    }

    @Test
    @DisplayName("Every outcome counts as an attempt, so the success rate is honest")
    void everyOutcomeIsAnAttempt() {
        BenchmarkStats stats = new BenchmarkStats();
        stats.recordSolved(10, 100, 1_000_000L, 5, 8);
        stats.recordExhausted();
        // One solved, one exhausted: the success rate must be 50%, not 100%.
        assertEquals(2, stats.attempts());
        assertEquals(1, stats.solved());
        assertEquals(50.0, stats.successRate(), EPS);
        assertEquals("partial", stats.note());
    }

    @Test
    @DisplayName("The note explains the dominant failure cause")
    void noteReflectsOutcome() {
        BenchmarkStats allSolved = new BenchmarkStats();
        allSolved.recordSolved(1, 1, 1, 1, 1);
        assertEquals("solved", allSolved.note());

        BenchmarkStats timeout = new BenchmarkStats();
        timeout.recordTimeout();
        assertEquals("timeout", timeout.note());

        BenchmarkStats nodeLimit = new BenchmarkStats();
        nodeLimit.recordNodeLimit();
        assertEquals("node-limit", nodeLimit.note());

        BenchmarkStats partial = new BenchmarkStats();
        partial.recordSolved(1, 1, 1, 1, 1);
        partial.recordTimeout();
        assertEquals("partial", partial.note());
    }
}
