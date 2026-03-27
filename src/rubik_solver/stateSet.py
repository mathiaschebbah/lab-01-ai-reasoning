from rubik_solver.state import State


class StateSet:
    def __init__(self) -> None:
        self.keys: set[tuple[tuple[tuple[str, ...], ...], ...]] = set()

    def add(self, state: State) -> None:
        self.keys.add(state.cubeKey())

    def contains(self, state: State) -> bool:
        return state.cubeKey() in self.keys
