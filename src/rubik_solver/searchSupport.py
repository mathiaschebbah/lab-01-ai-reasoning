from rubik_solver.cube import Cube

actionLabels: list[str] = [
    "U",
    "L",
    "F",
    "R",
    "D",
    "B",
    "U'",
    "L'",
    "F'",
    "R'",
    "D'",
    "B'",
]


def applyAction(cube: Cube, actionId: int) -> None:
    (
        cube.turnUp,
        cube.turnLeft,
        cube.turnFront,
        cube.turnRight,
        cube.turnDown,
        cube.turnBack,
        cube.returnUp,
        cube.returnLeft,
        cube.returnFront,
        cube.returnRight,
        cube.returnDown,
        cube.returnBack,
    )[actionId]()
