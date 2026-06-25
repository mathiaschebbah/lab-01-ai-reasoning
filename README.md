# Rubik's Cube Solver with A*

Artificial Intelligence and Reasoning, Lab 1.
Master 1 MIAGE, Universite Paris Dauphine, PSL.

Authors: Mathias CHEBBAH, Lamine BETRAOUI, Bouna SALL, Hugues BARBIER.

This project solves a Rubik's Cube with the A* search algorithm seen in Lecture
2. It models the cube as a search problem, runs a uniform cost search, and then
guides the search with two admissible heuristics built from relaxations of the
problem.

## Requirements

- Java 17 or later (built and tested with Java 25)
- Apache Maven 3.9 or later

## Build and test

```bash
mvn clean test        # compile and run the 36 unit tests
mvn package           # build the executable jar in target/
```

## Run the graphical interface

```bash
java -jar target/rubiks-cube-astar.jar
```

The window shows the cube as an unfolded net. The twelve buttons apply the
quarter turns. The Scramble button mixes the cube with the number of random moves
chosen in the adjacent `moves` field (8 by default; A* stays practical up to about
nine or ten). The Solve button runs A* with the selected strategy and then
animates the solution. The strategy menu chooses between the two heuristics and
the uniform cost search.

## Run the benchmark

```bash
# A plain run: all strategies, sensible defaults, never hangs.
java -cp target/classes fr.dauphine.miage.ai.rubik.app.Benchmark

# A* only, deeper scrambles, no waiting on the uniform cost search.
java -cp target/classes fr.dauphine.miage.ai.rubik.app.Benchmark strategies=cube,label maxDepth=11

# See every option.
java -cp target/classes fr.dauphine.miage.ai.rubik.app.Benchmark --help
```

Arguments are named `key=value` and all optional:

| key | meaning | default |
|-----|---------|---------|
| `strategies` | which strategies to run (`ucs`, `cube`, `label`) | all |
| `maxDepth` | deepest scramble to try | `12` |
| `instances` | cubes solved per depth | `3` |
| `timeMs` | time budget per cube, in milliseconds | `5000` |
| `nodeLimit` | maximum expanded states per cube | `20000000` |
| `prune` | skip inverse moves | `false` |

The benchmark prints, for each strategy and depth, the success rate, the average
expanded and generated states, the effective branching factor (states added to
the frontier per expanded node), the average peak frontier size (the memory the
search needs), the average time, the search throughput in states per second, the
average solution length, and a short note (`solved`, `timeout`, `node-limit`,
`mixed`). A closing summary lists the practical scramble depth per strategy. Each
strategy stops on its own once a depth solves nothing within the time budget, and
reports how deep it stayed practical. The old positional form
(`Benchmark maxDepth instances nodeLimit prune`) still works.

## Project layout

```
src/main/java/fr/dauphine/miage/ai/rubik/
  model/      Cube, Face, Color, Move, CubeGeometry
  search/     State, Astar, StateSet, PriorityQueueState
  heuristic/  Heuristic, ZeroHeuristic, HeuristicLabel, HeuristicCube
  ui/         RubiksCubeSimulator, CubePanel
  util/       Scrambler
  app/        Benchmark
src/test/java/...   unit tests (Cube, heuristics, A*)
report/             LaTeX report and its figures
presentation/       LaTeX Beamer presentation
```

## How the classes map to the statement

- `Cube`: the six face matrices, the twelve `turn*` and `return*` methods, and
  `isSolved`.
- `State`: a node with `nbrActions`, `pere`, `actionPere`, `valH` and `expand`.
- `Astar`: the `solve` method, with the `explored` set and the `frontier`.
- `StateSet`: the explored set, with `add` and `contains`.
- `PriorityQueueState`: the frontier, with `push` and `pop`.
- `Heuristic`, `HeuristicLabel`, `HeuristicCube`: the heuristic interface and its
  two relaxations.
- `RubiksCubeSimulator`: the interface, with `main` and `actionPerformed`.
```
