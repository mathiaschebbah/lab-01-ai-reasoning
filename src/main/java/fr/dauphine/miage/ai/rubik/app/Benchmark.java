package fr.dauphine.miage.ai.rubik.app;

import fr.dauphine.miage.ai.rubik.heuristic.Heuristic;
import fr.dauphine.miage.ai.rubik.heuristic.HeuristicCube;
import fr.dauphine.miage.ai.rubik.heuristic.HeuristicLabel;
import fr.dauphine.miage.ai.rubik.heuristic.ZeroHeuristic;
import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.search.Astar;
import fr.dauphine.miage.ai.rubik.search.State;
import fr.dauphine.miage.ai.rubik.util.Scrambler;
import java.util.List;

/**
 * Command line benchmark answering the questions of the lab statement: from how
 * many random moves can each strategy no longer solve the cube efficiently?
 *
 * <p>For every heuristic (zero, which makes A* a uniform cost search; the piece
 * heuristic; and the sticker heuristic) and for an increasing scramble depth, it
 * runs several instances under a node limit and reports the success rate, the
 * average number of expanded states, the average solving time and the average
 * solution length. The Markdown table it prints feeds the results section of the
 * report.</p>
 */
public final class Benchmark {

    private Benchmark() {
    }

    /** A single named heuristic configuration to evaluate. */
    private record Strategy(String name, Heuristic heuristic) {
    }

    /** Aggregated results for one (strategy, depth) cell. */
    private static final class Stats {
        int solved;
        int attempts;
        long totalExpanded;
        long totalNanos;
        long totalLength;

        double successRate() {
            return attempts == 0 ? 0 : (100.0 * solved / attempts);
        }

        double avgExpanded() {
            return solved == 0 ? 0 : (double) totalExpanded / solved;
        }

        double avgMillis() {
            return solved == 0 ? 0 : (totalNanos / 1_000_000.0) / solved;
        }

        double avgLength() {
            return solved == 0 ? 0 : (double) totalLength / solved;
        }
    }

    public static void main(String[] args) {
        int maxDepth = intArg(args, 0, 9);
        int instancesPerDepth = intArg(args, 1, 5);
        long nodeLimit = longArg(args, 2, 2_000_000L);
        boolean prune = args.length > 3 && Boolean.parseBoolean(args[3]);

        Strategy[] strategies = {
                new Strategy("UCS (h=0)", new ZeroHeuristic()),
                new Strategy("A* HeuristicCube", new HeuristicCube()),
                new Strategy("A* HeuristicLabel", new HeuristicLabel()),
        };

        System.out.println("Rubik's Cube A* benchmark");
        System.out.println("max scramble depth = " + maxDepth
                + ", instances per depth = " + instancesPerDepth
                + ", node limit = " + nodeLimit
                + ", inverse-move pruning = " + prune);
        System.out.println();

        for (Strategy strategy : strategies) {
            State.heuristic = strategy.heuristic();
            System.out.println("### " + strategy.name());
            System.out.println("| depth | success | avg expanded | avg time (ms) | avg length |");
            System.out.println("|------:|--------:|-------------:|--------------:|-----------:|");
            for (int depth = 1; depth <= maxDepth; depth++) {
                Stats stats = run(depth, instancesPerDepth, nodeLimit, prune);
                System.out.printf("| %5d | %6.0f%% | %12.0f | %13.1f | %10.1f |%n",
                        depth, stats.successRate(), stats.avgExpanded(),
                        stats.avgMillis(), stats.avgLength());
            }
            System.out.println();
        }
    }

    private static Stats run(int depth, int instances, long nodeLimit, boolean prune) {
        Stats stats = new Stats();
        for (int i = 0; i < instances; i++) {
            // Deterministic seed per (depth, instance) for reproducible numbers.
            Scrambler scrambler = new Scrambler(1_000L * depth + i);
            Cube cube = scrambler.scrambledCube(depth);

            Astar astar = new Astar(cube);
            astar.setExpansionLimit(nodeLimit);
            astar.setPruneInverseMoves(prune);

            long start = System.nanoTime();
            List<String> plan = astar.solve();
            long elapsed = System.nanoTime() - start;

            stats.attempts++;
            if (plan != null) {
                stats.solved++;
                stats.totalExpanded += astar.getExpandedCount();
                stats.totalNanos += elapsed;
                stats.totalLength += plan.size();
            }
        }
        return stats;
    }

    private static int intArg(String[] args, int index, int fallback) {
        return args.length > index ? Integer.parseInt(args[index]) : fallback;
    }

    private static long longArg(String[] args, int index, long fallback) {
        return args.length > index ? Long.parseLong(args[index]) : fallback;
    }
}
