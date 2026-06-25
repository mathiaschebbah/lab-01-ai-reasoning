package fr.dauphine.miage.ai.rubik.app;

/**
 * Aggregated measurements for one (strategy, scramble depth) cell of the
 * benchmark.
 *
 * <p>It accumulates the raw numbers reported by the solver over several cubes and
 * turns them into the quantities the report cares about, all defined in Lecture
 * 2:</p>
 *
 * <ul>
 *   <li><b>success rate</b>: how often the strategy solved the cube within the
 *       budget (a proxy for completeness in practice);</li>
 *   <li><b>average expanded</b> and <b>average generated</b>: the time complexity,
 *       measured as the number of states the search touched;</li>
 *   <li><b>effective branching factor</b>: generated per expanded, the practical
 *       value of the branching factor {@code b} once duplicate detection prunes
 *       the twelve raw successors;</li>
 *   <li><b>average peak frontier</b>: the space complexity, that is the memory the
 *       search needed at its peak;</li>
 *   <li><b>throughput</b>: expanded states per second, the raw speed of the
 *       search regardless of how many states it had to look at.</li>
 * </ul>
 *
 * <p>Averages are taken over the cubes that were actually solved, so a timeout or
 * a node-limit failure never distorts them. Counts (attempts, solved, timed out,
 * node-limit) are kept separately to explain the failures in a short note.</p>
 */
public final class BenchmarkStats {

    private int solved;
    private int timedOut;
    private int hitNodeLimit;
    private int attempts;
    private long totalExpanded;
    private long totalGenerated;
    private long totalNanos;
    private long totalLength;
    private long totalPeakFrontier;

    /**
     * Records a solved cube with its raw measurements.
     *
     * @param expanded     states expanded for this cube
     * @param generated    states generated (pushed to the frontier) for this cube
     * @param nanos        wall-clock time spent solving this cube, in nanoseconds
     * @param length       length of the returned solution, in moves
     * @param peakFrontier largest frontier size reached while solving this cube
     */
    public void recordSolved(long expanded, long generated, long nanos, long length,
                             long peakFrontier) {
        attempts++;
        solved++;
        totalExpanded += expanded;
        totalGenerated += generated;
        totalNanos += nanos;
        totalLength += length;
        totalPeakFrontier += peakFrontier;
    }

    /** Records a cube that the search gave up on after exceeding the time budget. */
    public void recordTimeout() {
        attempts++;
        timedOut++;
    }

    /** Records a cube that the search gave up on after exceeding the node limit. */
    public void recordNodeLimit() {
        attempts++;
        hitNodeLimit++;
    }

    /** @return the number of cubes attempted in this cell. */
    public int attempts() {
        return attempts;
    }

    /** @return the number of cubes solved within the budget. */
    public int solved() {
        return solved;
    }

    /** @return the number of cubes dropped for exceeding the time budget. */
    public int timedOut() {
        return timedOut;
    }

    /** @return the number of cubes dropped for exceeding the node limit. */
    public int hitNodeLimit() {
        return hitNodeLimit;
    }

    /** @return the percentage of attempts that were solved. */
    public double successRate() {
        return attempts == 0 ? 0 : (100.0 * solved / attempts);
    }

    /** @return the average number of expanded states over the solved cubes. */
    public double avgExpanded() {
        return solved == 0 ? 0 : (double) totalExpanded / solved;
    }

    /** @return the average number of generated states over the solved cubes. */
    public double avgGenerated() {
        return solved == 0 ? 0 : (double) totalGenerated / solved;
    }

    /** @return the average solving time over the solved cubes, in milliseconds. */
    public double avgMillis() {
        return solved == 0 ? 0 : (totalNanos / 1_000_000.0) / solved;
    }

    /** @return the average solution length over the solved cubes, in moves. */
    public double avgLength() {
        return solved == 0 ? 0 : (double) totalLength / solved;
    }

    /** @return the average peak frontier size over the solved cubes. */
    public double avgPeakFrontier() {
        return solved == 0 ? 0 : (double) totalPeakFrontier / solved;
    }

    /**
     * Returns the effective branching factor: the number of states generated per
     * state expanded, summed over all solved cubes. With twelve raw successors per
     * node, duplicate detection brings this below twelve; the gap measures how
     * many successors were already known.
     *
     * @return the effective branching factor, or 0 when nothing was expanded
     */
    public double effectiveBranching() {
        return totalExpanded == 0 ? 0 : (double) totalGenerated / totalExpanded;
    }

    /**
     * Returns the search throughput: the number of states expanded per second of
     * wall-clock time, summed over all solved cubes. It isolates raw speed from
     * the number of states a strategy has to look at.
     *
     * @return the expanded states per second, or 0 when no time was spent
     */
    public double expandedPerSecond() {
        return totalNanos == 0 ? 0 : totalExpanded / (totalNanos / 1_000_000_000.0);
    }

    /**
     * Returns a short word explaining the dominant outcome of the cell: whether it
     * was fully solved, partially solved, or which failure dominated.
     *
     * @return one of "solved", "partial", "timeout", "node-limit" or "failed"
     */
    public String note() {
        if (attempts > 0 && solved == attempts) {
            return "solved";
        }
        if (solved == 0 && timedOut >= hitNodeLimit && timedOut > 0) {
            return "timeout";
        }
        if (solved == 0 && hitNodeLimit > 0) {
            return "node-limit";
        }
        if (solved == 0) {
            return "failed";
        }
        return "partial";
    }
}
