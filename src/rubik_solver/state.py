from __future__ import annotations

from rubik_solver.cube import Cube
from rubik_solver.heuristic import HeuristicLabel
from rubik_solver.linkedList import LinkedList
from rubik_solver.searchSupport import applyAction


class State:
    heuristic = HeuristicLabel()

    def __init__(
        self,
        cube: Cube,
        nbrActions: int,
        pere: State | None,
        actionPere: int,
    ) -> None:
        self.cube = cube
        self.nbrActions = nbrActions
        self.pere = pere
        self.actionPere = actionPere
        self.valH = State.heuristic.value(self)

    def f(self) -> int:
        return self.nbrActions + self.valH

    def cubeKey(self) -> tuple[tuple[tuple[str, ...], ...], ...]:
        c = self.cube
        return (
            tuple(tuple(row) for row in c.top),
            tuple(tuple(row) for row in c.bottom),
            tuple(tuple(row) for row in c.front),
            tuple(tuple(row) for row in c.back),
            tuple(tuple(row) for row in c.right),
            tuple(tuple(row) for row in c.left),
        )

    def expand(self) -> LinkedList[State]:
        children: LinkedList[State] = LinkedList()
        for actionId in range(12):
            c = Cube(self.cube)
            applyAction(c, actionId)
            children.append(
                State(c, self.nbrActions + 1, self, actionId)
            )
        return children
