from rubik_solver.cube import Cube
from rubik_solver.priorityQueueState import PriorityQueueState
from rubik_solver.searchSupport import actionLabels
from rubik_solver.state import State
from rubik_solver.stateSet import StateSet


class Astar:
    def __init__(self, cube: Cube) -> None:
        c0 = Cube(cube)
        self.root = State(c0, 0, None, -1)
        self.explored = StateSet()
        self.frontier = PriorityQueueState()

    def solve(self) -> list[str]:
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
