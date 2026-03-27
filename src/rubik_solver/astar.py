from rubik_solver.cube import Cube
from rubik_solver.heuristic import Heuristic, UniformCost
from rubik_solver.priorityQueueState import PriorityQueueState
from rubik_solver.searchSupport import actionLabels
from rubik_solver.state import State
from rubik_solver.stateSet import StateSet


class Astar:
    def __init__(self, cube: Cube, heuristic: Heuristic | None = None) -> None:
        h = heuristic if heuristic is not None else UniformCost()
        c0 = Cube(cube)
        self.root = State(c0, 0, None, -1, h)
        self.explored = StateSet()
        self.frontier = PriorityQueueState()

    def solve(self, maxNodes: int = 200_000) -> list[str]:
        self.nodesExplored = 0
        self.frontier.push(self.root)
        while True:
            current = self.frontier.pop()
            if current is None:
                return []
            if current.cube.isSolved():
                return self.pathFrom(current)
            if self.explored.contains(current):
                continue
            self.explored.add(current)
            self.nodesExplored += 1
            if self.nodesExplored >= maxNodes:
                return []
            for child in current.expand():
                self.frontier.push(child)

    def pathFrom(self, state: State) -> list[str]:
        labels: list[str] = []
        s: State | None = state
        while s is not None and s.pere is not None:
            aid = s.actionPere
            if 0 <= aid < len(actionLabels):
                labels.append(actionLabels[aid])
            s = s.pere
        labels.reverse()
        return labels
