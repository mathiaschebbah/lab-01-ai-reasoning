# Rubik's Cube Solver with A*

Artificial Intelligence and Reasoning, Lab 1.
Master 1 MIAGE, Universite Paris Dauphine, PSL.

Authors: Mathias CHEBBAH, Lamine BETRAOUI, Bouna SALL.

This project solves a Rubik's Cube with the A* search algorithm seen in Lecture
2. It models the cube as a search problem, runs a uniform cost search, and then
guides the search with two admissible heuristics built from relaxations of the
problem.

## Requirements

- Java 17 or later (built and tested with Java 25)
- Apache Maven 3.9 or later

## Build and test

```bash
mvn clean test        # compile and run the 24 unit tests
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
# arguments: maxDepth instancesPerDepth nodeLimit pruneInverseMoves
java -Xmx4g -cp target/classes fr.dauphine.miage.ai.rubik.app.Benchmark 8 5 1500000 false
```

The benchmark prints, for each strategy and scramble depth, the success rate, the
average number of expanded states, the average time, and the average solution
length.

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
