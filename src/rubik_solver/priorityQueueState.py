from __future__ import annotations

import heapq

from rubik_solver.state import State


class PriorityQueueState:
    """Frontière : push / pop ; push conserve la meilleure copie pour une même configuration."""

    def __init__(self) -> None:
        self.heap: list[tuple[int, int, State]] = []
        self.counter = 0
        self.bestF: dict[tuple[tuple[tuple[str, ...], ...], ...], int] = {}

    def push(self, state: State) -> None:
        key = state.cubeKey()
        f = state.f()
        if key in self.bestF and f >= self.bestF[key]:
            return
        self.bestF[key] = f
        heapq.heappush(self.heap, (f, self.counter, state))
        self.counter += 1

    def pop(self) -> State | None:
        while self.heap:
            f, _, state = heapq.heappop(self.heap)
            key = state.cubeKey()
            if key not in self.bestF:
                continue
            if self.bestF[key] != f:
                continue
            del self.bestF[key]
            return state
        return None
