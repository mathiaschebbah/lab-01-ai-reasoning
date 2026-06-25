package fr.dauphine.miage.ai.rubik.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.dauphine.miage.ai.rubik.heuristic.HeuristicCube;
import fr.dauphine.miage.ai.rubik.heuristic.HeuristicLabel;
import fr.dauphine.miage.ai.rubik.heuristic.Heuristic;
import fr.dauphine.miage.ai.rubik.heuristic.ZeroHeuristic;
import fr.dauphine.miage.ai.rubik.model.Cube;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End to end tests of the A* solver under the three heuristics.
 *
 * <p>These tests check correctness (the returned action sequence really solves
 * the cube) and optimality (uniform cost search and admissible A* return the
 * same optimal length).</p>
 */
class AstarTest {

    @AfterEach
    void resetHeuristic() {
        State.heuristic = new ZeroHeuristic();
    }

    @Test
    @DisplayName("Solving an already solved cube returns an empty plan")
    void alreadySolved() {
        State.heuristic = new ZeroHeuristic();
        Astar astar = new Astar(new Cube());
        List<String> plan = astar.solve();
        assertNotNull(plan);
        assertTrue(plan.isEmpty(), "A solved cube needs no move");
    }

    @Test
    @DisplayName("A single scramble move is undone with one optimal move")
    void singleMove() {
        State.heuristic = new HeuristicCube();
        for (int action = 0; action < 12; action++) {
            Cube cube = new Cube();
            cube.applyAction(action);
            Astar astar = new Astar(cube);
            List<String> plan = astar.solve();
            assertNotNull(plan, "A one move scramble must be solvable");
            assertEquals(1, plan.size(), "Optimal plan length for a one move scramble is 1");
            assertTrue(applyAndCheck(cube, plan), "Plan must solve the cube");
        }
    }

    @Test
    @DisplayName("Uniform cost search solves short scrambles correctly")
    void uniformCostSolvesShortScrambles() {
        State.heuristic = new ZeroHeuristic();
        for (int seed = 0; seed < 10; seed++) {
            Cube cube = scramble(4, seed);
            Astar astar = new Astar(cube);
            List<String> plan = astar.solve();
            assertNotNull(plan, "Scramble of depth 4 must be solvable by UCS (seed " + seed + ")");
            assertTrue(plan.size() <= 4, "Optimal plan cannot be longer than the scramble");
            assertTrue(applyAndCheck(cube, plan));
        }
    }

    @Test
    @DisplayName("A* with HeuristicCube solves scrambles and matches the optimal length")
    void heuristicCubeMatchesOptimal() {
        for (int seed = 0; seed < 8; seed++) {
            Cube cube = scramble(5, seed + 100);

            State.heuristic = new ZeroHeuristic();
            int optimal = new Astar(cube).solve().size();

            State.heuristic = new HeuristicCube();
            List<String> plan = new Astar(cube).solve();
            assertNotNull(plan);
            assertEquals(optimal, plan.size(),
                    "A* with an admissible heuristic must return the optimal length (seed " + seed + ")");
            assertTrue(applyAndCheck(cube, plan));
        }
    }

    @Test
    @DisplayName("A* with HeuristicLabel solves scrambles and matches the optimal length")
    void heuristicLabelMatchesOptimal() {
        for (int seed = 0; seed < 8; seed++) {
            Cube cube = scramble(5, seed + 200);

            State.heuristic = new ZeroHeuristic();
            int optimal = new Astar(cube).solve().size();

            State.heuristic = new HeuristicLabel();
            List<String> plan = new Astar(cube).solve();
            assertNotNull(plan);
            assertEquals(optimal, plan.size(),
                    "A* with HeuristicLabel must return the optimal length (seed " + seed + ")");
            assertTrue(applyAndCheck(cube, plan));
        }
    }

    @Test
    @DisplayName("The search reports the states it generated and its branching")
    void generatedAndBranchingAreReported() {
        State.heuristic = new HeuristicCube();
        Cube cube = scramble(6, 321);
        Astar astar = new Astar(cube);
        astar.solve();

        long expanded = astar.getExpandedCount();
        long generated = astar.getGeneratedCount();
        assertTrue(expanded > 0, "A non trivial scramble expands at least one state");
        assertTrue(generated >= expanded,
                "Every expanded state was generated first, so generated (" + generated
                        + ") >= expanded (" + expanded + ")");
        // The effective branching factor is generated per expanded; with twelve
        // moves and duplicate detection it must stay within (0, 12].
        double branching = (double) generated / expanded;
        assertTrue(branching > 0 && branching <= 12.0,
                "Effective branching " + branching + " must be in (0, 12]");
    }

    @Test
    @DisplayName("The search reports the peak size of the frontier and the explored set")
    void peakSizesAreReported() {
        State.heuristic = new HeuristicCube();
        Cube cube = scramble(6, 654);
        Astar astar = new Astar(cube);
        astar.solve();

        long peakFrontier = astar.getPeakFrontierSize();
        long peakExplored = astar.getPeakExploredSize();
        assertTrue(peakFrontier > 0, "A real search holds states in the frontier");
        assertTrue(peakExplored > 0, "A real search expands states into the explored set");
        // The explored set never holds more than the states actually expanded.
        assertTrue(peakExplored <= astar.getExpandedCount() + 1,
                "Peak explored (" + peakExplored + ") cannot exceed expanded ("
                        + astar.getExpandedCount() + ") by more than one");
    }

    @Test
    @DisplayName("Calling solve twice on the same object returns the same result")
    void solveIsRepeatable() {
        State.heuristic = new HeuristicCube();
        Cube cube = scramble(6, 909);
        Astar astar = new Astar(cube);

        List<String> first = astar.solve();
        long firstExpanded = astar.getExpandedCount();
        assertNotNull(first);

        // A second call must reset the frontier and the explored set, otherwise it
        // would find the root already explored and fail with bogus metrics.
        List<String> second = astar.solve();
        assertNotNull(second, "A repeated solve must not fail on a stale explored set");
        assertEquals(Astar.Outcome.SOLVED, astar.getLastOutcome());
        assertEquals(first.size(), second.size(), "Both solves return the same length");
        assertEquals(firstExpanded, astar.getExpandedCount(),
                "A deterministic search expands the same number of states each time");
        assertTrue(applyAndCheck(cube, second));
    }

    @Test
    @DisplayName("An already solved cube reports a SOLVED outcome and no expansion")
    void solvedCubeOutcome() {
        State.heuristic = new ZeroHeuristic();
        Astar astar = new Astar(new Cube());
        astar.solve();
        assertEquals(Astar.Outcome.SOLVED, astar.getLastOutcome());
        assertEquals(0, astar.getExpandedCount(), "A solved cube expands nothing");
    }

    @Test
    @DisplayName("A heuristic search expands no more states than uniform cost")
    void heuristicReducesExpansions() {
        Cube cube = scramble(5, 4242);

        State.heuristic = new ZeroHeuristic();
        Astar ucs = new Astar(cube);
        ucs.solve();
        long ucsExpanded = ucs.getExpandedCount();

        State.heuristic = new HeuristicCube();
        Astar astar = new Astar(cube);
        astar.solve();
        long astarExpanded = astar.getExpandedCount();

        assertTrue(astarExpanded <= ucsExpanded,
                "A* (" + astarExpanded + ") should not expand more than UCS (" + ucsExpanded + ")");
    }

    @Test
    @DisplayName("A tiny time budget makes a deep search give up with a TIMEOUT outcome")
    void timeBudgetStopsHopelessSearch() {
        State.heuristic = new ZeroHeuristic();
        // A depth 9 scramble is far beyond what UCS solves in one millisecond.
        Cube cube = scramble(9, 7);
        Astar astar = new Astar(cube);
        astar.setTimeBudgetMillis(1);

        long start = System.nanoTime();
        List<String> plan = astar.solve();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertNull(plan, "An out of time search must return null");
        assertEquals(Astar.Outcome.TIMEOUT, astar.getLastOutcome(),
                "The search must report that it ran out of time");
        assertTrue(elapsedMs < 2_000,
                "The budget must stop the search quickly, took " + elapsedMs + " ms");
    }

    @Test
    @DisplayName("A time budget does not prevent solving an easy scramble")
    void timeBudgetStillSolvesEasyScramble() {
        State.heuristic = new HeuristicLabel();
        Cube cube = scramble(4, 11);
        Astar astar = new Astar(cube);
        astar.setTimeBudgetMillis(5_000);

        List<String> plan = astar.solve();
        assertNotNull(plan, "An easy scramble must still be solved within a generous budget");
        assertEquals(Astar.Outcome.SOLVED, astar.getLastOutcome());
        assertTrue(applyAndCheck(cube, plan));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Applies the action plan to a copy of the cube and checks it gets solved. */
    private static boolean applyAndCheck(Cube original, List<String> plan) {
        Cube cube = original.copy();
        for (String notation : plan) {
            cube.applyAction(actionOf(notation));
        }
        return cube.isSolved();
    }

    private static int actionOf(String notation) {
        for (int a = 0; a < 12; a++) {
            if (Astar.notationOf(a).equals(notation)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown notation " + notation);
    }

    private static Cube scramble(int moves, long seed) {
        Random random = new Random(seed);
        Cube cube = new Cube();
        for (int i = 0; i < moves; i++) {
            cube.applyAction(random.nextInt(12));
        }
        return cube;
    }
}
