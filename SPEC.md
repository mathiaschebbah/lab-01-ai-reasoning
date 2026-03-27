# SPEC.md — Rubik's Cube Solver (AI Lab 1)

## Objectif

Résoudre un Rubik's Cube 3x3 avec l'algorithme **A\*** et visualiser la résolution dans une GUI Tkinter.

## Architecture

```
                    ┌──────────────────────────┐
                    │   RubiksCubeSimulator     │  ← GUI (Tkinter)
                    │ (rubiksCubeSimulator.py)  │
                    └────────────┬─────────────┘
                                 │ clique "Solve"
                                 ▼
                    ┌──────────────────────────┐
                    │          Astar           │  ← Algorithme A*
                    │       (astar.py)         │
                    └────────────┬─────────────┘
                                 │ utilise
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                  ▼
     ┌────────────────┐ ┌──────────────┐ ┌────────────────┐
     │ PriorityQueue  │ │   StateSet   │ │     State      │
     │    State       │ │  (explored)  │ │  (state.py)    │
     │  (frontière)   │ └──────────────┘ └───────┬────────┘
     └────────────────┘                          │ contient
                                                 ▼
                                    ┌──────────────────────┐
                                    │        Cube          │ ← Modèle du cube
                                    │     (cube.py)        │
                                    └──────────────────────┘
```

## Les classes

### Cube (`cube.py`)

Le modèle du Rubik's Cube. 6 faces (`top`, `bottom`, `front`, `back`, `right`, `left`), chacune une matrice 3x3 de caractères (W/R/G/B/Y/O).

- **12 rotations** : `turnUp`, `turnLeft`, `turnFront`, `turnRight`, `turnDown`, `turnBack` (horaire) et `returnUp`...`returnBack` (anti-horaire, implémentées comme 3 rotations horaires).
- **`isSolved()`** : retourne `True` si chaque face est d'une couleur uniforme.
- **Constructeur** : `Cube()` crée un cube résolu, `Cube(other)` copie un cube existant.

### State (`state.py`)

Un nœud de l'arbre de recherche A\*.

| Attribut | Description |
|----------|-------------|
| `cube` | La configuration du cube à cet état |
| `nbrActions` | Coût g (nombre de coups depuis la racine) |
| `pere` | État parent |
| `actionPere` | ID de l'action (0–11) qui a produit cet état |
| `valH` | Valeur heuristique (calculée à la construction) |
| `heuristic` | Instance de l'heuristique utilisée |

- **`f()`** → `nbrActions + valH` (coût total pour A\*).
- **`cubeKey()`** → tuple hashable représentant la configuration du cube (pour déduplication).
- **`expand()`** → `LinkedList[State]` de 12 enfants (un par rotation possible).

### Astar (`astar.py`)

L'algorithme A\*.

1. Push la racine dans la frontière
2. Boucle : pop l'état avec le f minimal
3. Si résolu → reconstruit le chemin via `pathFrom()`
4. Si déjà exploré → skip
5. Sinon : marque comme exploré, expand les 12 enfants, push dans la frontière
6. Limite à 200 000 nœuds explorés (abandon au-delà)

`nodesExplored` compte les nœuds pour le suivi en temps réel dans l'UI.

### PriorityQueueState (`priorityQueueState.py`)

La frontière (open list). Min-heap trié par f-cost.

- **`push(state)`** : déduplique via `cubeKey()` — si le même cube existe déjà avec un f meilleur ou égal, on ignore.
- **`pop()`** : retourne l'état avec le plus petit f. Les entrées obsolètes dans le heap sont ignorées (lazy deletion).

### StateSet (`stateSet.py`)

L'ensemble des états explorés (closed list). Set de `cubeKey()` tuples.

- **`add(state)`** : ajoute la configuration.
- **`contains(state)`** : vérifie si déjà exploré.

### Heuristic (`heuristic.py`)

Interface abstraite `value(state) → int` avec trois implémentations :

| Classe | Formule | Relaxation |
|--------|---------|------------|
| `UniformCost` | h = 0 | Aucune (BFS / Dijkstra) |
| `HeuristicCube` | `rawStickerDistanceSum // 8` | Les pièces (2–3 stickers) bougent indépendamment |
| `HeuristicLabel` | `rawStickerDistanceSum // stickersFaceChangePerMove` | Chaque sticker bouge indépendamment |

**`rawStickerDistanceSum`** : pour chacun des 54 stickers, calcule la distance Manhattan 3D minimale vers n'importe quelle position sur sa face cible, puis somme le tout.

**`stickersFaceChangePerMove`** : nombre maximal de stickers qui changent de face lors d'un quart de tour (≈12). Sert de diviseur pour garantir l'admissibilité.

### Modules support

- **`searchSupport.py`** : `actionLabels` (liste des notations U, L, F, R, D, B, U'...) et `applyAction(cube, actionId)`.
- **`linkedList.py`** : `LinkedList[T]` générique, utilisée par `State.expand()`.

## GUI (`rubiksCubeSimulator.py`)

### Affichage

Patron en croix du cube déplié :

```
         [U]
[L]  [F]  [R]  [B]
         [D]
```

Cellules de 40px avec labels de face. Barre de statut affichant h₀ et le compteur de nœuds en temps réel.

### Contrôles

- **12 boutons de rotation** : U, U', D, D', F, F', B, B', L, L', R, R'
- **Reset** : remet le cube résolu
- **Shuffle** : applique 1 mouvement aléatoire (cumulable, compteur affiché)
- **Solve** : lance A\* dans un thread séparé, puis anime la solution coup par coup (400ms)

### Threading

A\* tourne dans un thread daemon pour ne pas bloquer l'UI. Le thread principal poll le compteur de nœuds toutes les 200ms et met à jour la barre de statut.

## Flux de résolution

1. L'utilisateur mélange le cube (boutons de rotation ou Shuffle)
2. Clic **Solve** → A\* démarre dans un thread
3. L'UI affiche en temps réel : `h₀` (estimation initiale) et le compteur de nœuds
4. Si solution trouvée → animation coup par coup
5. Si limite de 200k nœuds atteinte → message d'abandon avec stats

## Limites

L'heuristique `HeuristicLabel` donne des valeurs basses (h₀ ≈ 2–4 même pour un cube bien mélangé), car la distance minimale vers *n'importe quelle* position sur la face cible est une borne très lâche. A\* explore donc quasi en BFS. Au-delà de ~3–4 coups de mélange, l'espace de recherche explose (branching factor = 12).

C'est exactement ce que l'énoncé demande d'évaluer :
- **Section 5.1** : "Determine from how many random moves the algorithm can no longer find a solution efficiently."
- **Section 5.2** : "Evaluate how scrambled a cube can be while still being solvable using A\* with your heuristic."

## Build & Run

```bash
uv sync
uv run python src/rubik_solver/simulator.py
```
