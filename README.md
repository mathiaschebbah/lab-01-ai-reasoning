# Rubik Solver

Implementation du lab IA 1 autour de la résolution d'un Rubik's Cube avec A*.

## Contenu

- `Cube` modélise le cube 3x3 et expose les 12 rotations demandées.
- `Astar` implémente la recherche avec frontière prioritaire et ensemble `explored`.
- `HeuristicCube` et `HeuristicLabel` fournissent deux heuristiques admissibles.
- `RubiksCubeSimulator` affiche une interface Tkinter avec mélange, solve et reset.

## Lancer le projet

```bash
uv run python -m rubik_solver
```
