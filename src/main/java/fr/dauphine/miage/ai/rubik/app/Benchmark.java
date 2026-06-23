package fr.dauphine.miage.ai.rubik.app;

import fr.dauphine.miage.ai.rubik.heuristic.Heuristic;
import fr.dauphine.miage.ai.rubik.heuristic.HeuristicCube;
import fr.dauphine.miage.ai.rubik.heuristic.HeuristicLabel;
import fr.dauphine.miage.ai.rubik.heuristic.ZeroHeuristic;
import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.search.Astar;
import fr.dauphine.miage.ai.rubik.search.State;
import fr.dauphine.miage.ai.rubik.util.Scrambler;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Command line benchmark answering the question of the lab statement: from how
 * many random moves can each strategy no longer solve the cube efficiently?
 *
 * <p>For every selected strategy and for an increasing scramble depth, it solves
 * several cubes under a time budget and a node limit. It prints, per depth, the
 * success rate, the average expanded states, the average time and the average
 * solution length.</p>
 *
 * <h2>Why this benchmark is easy to read and never hangs</h2>
 * <ul>
 *   <li>Every instance has a <b>time budget</b> ({@code timeMs}, 5 seconds by
 *       default), so a hopeless search is dropped quickly instead of grinding for
 *       minutes.</li>
 *   <li>A strategy <b>stops on its own</b> as soon as a depth solves nothing: it
 *       would only fail more slowly at deeper scrambles. The run then says how far
 *       that strategy stayed practical.</li>
 *   <li>You can pick the strategies to run, so you never wait for the uniform cost
 *       search when you only care about A*.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * java -cp target/classes fr.dauphine.miage.ai.rubik.app.Benchmark [key=value ...]
 *
 *   strategies=ucs,cube,label   which strategies to run        (default: all)
 *   maxDepth=12                  deepest scramble to try         (default: 12)
 *   instances=3                  cubes solved per depth          (default: 3)
 *   timeMs=5000                  time budget per cube, ms        (default: 5000)
 *   nodeLimit=20000000           max expanded states per cube    (default: 20000000)
 *   prune=false                  skip inverse moves              (default: false)
 *
 *   --help                       print this help and exit
 * </pre>
 *
 * <p>For backward compatibility the old positional form is still accepted:
 * {@code Benchmark maxDepth instances nodeLimit prune}.</p>
 */
public final class Benchmark {

    private Benchmark() {
    }

    /** A single named heuristic configuration to evaluate. */
    private record Strategy(String key, String name, Heuristic heuristic) {
    }

    /** All strategies known by key. */
    private static final List<Strategy> ALL_STRATEGIES = List.of(
            new Strategy("ucs", "UCS (h=0)", new ZeroHeuristic()),
            new Strategy("cube", "A* HeuristicCube", new HeuristicCube()),
            new Strategy("label", "A* HeuristicLabel", new HeuristicLabel()));

    /** Parsed configuration of one benchmark run. */
    private record Config(List<Strategy> strategies, int maxDepth, int instances,
                          long timeMs, long nodeLimit, boolean prune) {
    }

    /** Aggregated results for one (strategy, depth) cell. */
    private static final class Stats {
        int solved;
        int timedOut;
        int hitNodeLimit;
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

        /** A short word explaining why the failures, if any, happened. */
        String note() {
            if (solved == attempts) {
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

    public static void main(String[] args) {
        if (hasFlag(args, "--help") || hasFlag(args, "-h")) {
            printHelp();
            return;
        }
        Config config = parse(args);

        System.out.println("Rubik's Cube A* benchmark");
        System.out.printf(Locale.US,
                "strategies = %s | maxDepth = %d | instances = %d | "
                        + "timeMs = %d | nodeLimit = %d | prune = %b%n%n",
                keysOf(config.strategies()), config.maxDepth(), config.instances(),
                config.timeMs(), config.nodeLimit(), config.prune());

        for (Strategy strategy : config.strategies()) {
            runStrategy(strategy, config);
        }
    }

    /** Runs one strategy across the depths, stopping when a depth solves nothing. */
    private static void runStrategy(Strategy strategy, Config config) {
        State.heuristic = strategy.heuristic();
        System.out.println("### " + strategy.name());
        System.out.println("| depth | success | avg expanded | avg time (ms) | avg length | note       |");
        System.out.println("|------:|--------:|-------------:|--------------:|-----------:|:-----------|");

        int lastSolvedDepth = 0;
        for (int depth = 1; depth <= config.maxDepth(); depth++) {
            Stats stats = run(strategy, depth, config);
            System.out.printf(Locale.US,
                    "| %5d | %6.0f%% | %12.0f | %13.1f | %10.1f | %-10s |%n",
                    depth, stats.successRate(), stats.avgExpanded(),
                    stats.avgMillis(), stats.avgLength(), stats.note());

            if (stats.solved > 0) {
                lastSolvedDepth = depth;
            } else {
                // This strategy now solves nothing within the budget. Deeper
                // scrambles can only be harder, so we stop it here.
                System.out.printf(Locale.US,
                        "_stopped at depth %d (%s); practical up to depth %d._%n%n",
                        depth, stats.note(), lastSolvedDepth);
                return;
            }
        }
        System.out.printf(Locale.US,
                "_practical up to depth %d (the configured maximum)._%n%n",
                lastSolvedDepth);
    }

    private static Stats run(Strategy strategy, int depth, Config config) {
        Stats stats = new Stats();
        for (int i = 0; i < config.instances(); i++) {
            // Deterministic seed per (depth, instance) for reproducible numbers.
            Scrambler scrambler = new Scrambler(1_000L * depth + i);
            Cube cube = scrambler.scrambledCube(depth);

            Astar astar = new Astar(cube);
            astar.setExpansionLimit(config.nodeLimit());
            astar.setTimeBudgetMillis(config.timeMs());
            astar.setPruneInverseMoves(config.prune());

            long start = System.nanoTime();
            List<String> plan = astar.solve();
            long elapsed = System.nanoTime() - start;

            stats.attempts++;
            if (plan != null) {
                stats.solved++;
                stats.totalExpanded += astar.getExpandedCount();
                stats.totalNanos += elapsed;
                stats.totalLength += plan.size();
            } else if (astar.getLastOutcome() == Astar.Outcome.TIMEOUT) {
                stats.timedOut++;
            } else if (astar.getLastOutcome() == Astar.Outcome.NODE_LIMIT) {
                stats.hitNodeLimit++;
            }
        }
        return stats;
    }

    // ------------------------------------------------------------------
    // Argument parsing: named key=value, with a positional fallback.
    // ------------------------------------------------------------------

    private static Config parse(String[] args) {
        // Defaults chosen so a plain run is short and informative.
        List<Strategy> strategies = ALL_STRATEGIES;
        int maxDepth = 12;
        int instances = 3;
        long timeMs = 5_000L;
        long nodeLimit = 20_000_000L;
        boolean prune = false;

        boolean named = false;
        for (String arg : args) {
            int eq = arg.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            named = true;
            String key = arg.substring(0, eq).trim().toLowerCase(Locale.US);
            String value = arg.substring(eq + 1).trim();
            switch (key) {
                case "strategies" -> strategies = parseStrategies(value);
                case "maxdepth" -> maxDepth = Integer.parseInt(value);
                case "instances" -> instances = Integer.parseInt(value);
                case "timems" -> timeMs = Long.parseLong(value);
                case "nodelimit" -> nodeLimit = Long.parseLong(value);
                case "prune" -> prune = Boolean.parseBoolean(value);
                default -> System.err.println("Unknown option ignored: " + key);
            }
        }

        // Backward compatible positional form: maxDepth instances nodeLimit prune.
        if (!named && args.length > 0) {
            maxDepth = intArg(args, 0, maxDepth);
            instances = intArg(args, 1, instances);
            nodeLimit = longArg(args, 2, nodeLimit);
            prune = args.length > 3 && Boolean.parseBoolean(args[3]);
        }

        return new Config(strategies, maxDepth, instances, timeMs, nodeLimit, prune);
    }

    private static List<Strategy> parseStrategies(String csv) {
        List<Strategy> chosen = new ArrayList<>();
        for (String token : csv.split(",")) {
            String key = token.trim().toLowerCase(Locale.US);
            if (key.isEmpty()) {
                continue;
            }
            Strategy match = ALL_STRATEGIES.stream()
                    .filter(s -> s.key().equals(key))
                    .findFirst()
                    .orElse(null);
            if (match == null) {
                System.err.println("Unknown strategy ignored: " + key
                        + " (use ucs, cube or label)");
            } else if (!chosen.contains(match)) {
                chosen.add(match);
            }
        }
        return chosen.isEmpty() ? ALL_STRATEGIES : chosen;
    }

    private static String keysOf(List<Strategy> strategies) {
        List<String> keys = new ArrayList<>();
        for (Strategy s : strategies) {
            keys.add(s.key());
        }
        return String.join(",", keys);
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) {
                return true;
            }
        }
        return false;
    }

    private static int intArg(String[] args, int index, int fallback) {
        return args.length > index ? Integer.parseInt(args[index]) : fallback;
    }

    private static long longArg(String[] args, int index, long fallback) {
        return args.length > index ? Long.parseLong(args[index]) : fallback;
    }

    private static void printHelp() {
        System.out.println("""
                Rubik's Cube A* benchmark

                Usage:
                  java -cp target/classes fr.dauphine.miage.ai.rubik.app.Benchmark [key=value ...]

                Options (all optional):
                  strategies=ucs,cube,label   which strategies to run        (default: all)
                  maxDepth=12                  deepest scramble to try         (default: 12)
                  instances=3                  cubes solved per depth          (default: 3)
                  timeMs=5000                  time budget per cube, ms        (default: 5000)
                  nodeLimit=20000000           max expanded states per cube    (default: 20000000)
                  prune=false                  skip inverse moves              (default: false)
                  --help                       print this help and exit

                Each strategy stops on its own once a depth solves nothing within
                the time budget, and reports how deep it stayed practical.

                Examples:
                  # quick look at A* only, no waiting on UCS
                  Benchmark strategies=cube,label maxDepth=11

                  # reproduce the report table (UCS only makes sense up to depth 6)
                  Benchmark strategies=ucs maxDepth=8 timeMs=8000

                Backward compatible positional form (maxDepth instances nodeLimit prune):
                  Benchmark 8 5 1500000 false
                """);
    }
}
