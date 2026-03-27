from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

from rubik_solver.cube import Cube
from rubik_solver.searchSupport import applyAction

if TYPE_CHECKING:
    from rubik_solver.state import State

COLOR_GOAL_FACE: dict[str, str] = {
    "W": "top",
    "Y": "bottom",
    "G": "front",
    "B": "back",
    "R": "right",
    "O": "left",
}

FACE_ORDER: list[tuple[str, str]] = [
    ("top", "top"),
    ("bottom", "bottom"),
    ("front", "front"),
    ("back", "back"),
    ("right", "right"),
    ("left", "left"),
]


def stickerCoords(face: str, i: int, j: int) -> tuple[int, int, int]:
    """Coordonnées entières (x,y,z) dans {-1,0,1} pour la cellule (i,j) sur la face."""
    if face == "top":
        return (-1 + j, 1, -1 + i)
    if face == "bottom":
        return (-1 + j, -1, 1 - i)
    if face == "front":
        return (-1 + j, 1 - i, 1)
    if face == "back":
        return (1 - j, 1 - i, -1)
    if face == "right":
        return (1, 1 - i, 1 - j)
    if face == "left":
        return (-1, 1 - i, -1 + j)
    raise ValueError(face)


def goalCoordsForFace(face: str) -> list[tuple[int, int, int]]:
    out: list[tuple[int, int, int]] = []
    for i in range(3):
        for j in range(3):
            out.append(stickerCoords(face, i, j))
    return out


GOAL_BY_FACE: dict[str, list[tuple[int, int, int]]] = {
    f: goalCoordsForFace(f) for f in ("top", "bottom", "front", "back", "right", "left")
}


def manhattan3(a: tuple[int, int, int], b: tuple[int, int, int]) -> int:
    return abs(a[0] - b[0]) + abs(a[1] - b[1]) + abs(a[2] - b[2])


def rawStickerDistanceSum(cube: Cube) -> int:
    total = 0
    for face_name, attr in FACE_ORDER:
        face = getattr(cube, attr)
        for i in range(3):
            for j in range(3):
                color = face[i][j]
                goal_face = COLOR_GOAL_FACE[color]
                cur = stickerCoords(face_name, i, j)
                goals = GOAL_BY_FACE[goal_face]
                total += min(manhattan3(cur, g) for g in goals)
    return total


def computeStickersChangingFacePerMove() -> int:
    """Nombre maximal de stickers qui changent de face pour un quart de tour (0..11)."""
    c = Cube()
    for face_name, attr in FACE_ORDER:
        face = getattr(c, attr)
        for i in range(3):
            for j in range(3):
                face[i][j] = f"{face_name}_{i}_{j}"

    before: dict[str, str] = {}
    for face_name, attr in FACE_ORDER:
        face = getattr(c, attr)
        for i in range(3):
            for j in range(3):
                sid = face[i][j]
                before[sid] = face_name

    best = 0
    for aid in range(12):
        c2 = Cube(c)
        applyAction(c2, aid)
        after: dict[str, str] = {}
        for face_name, attr in FACE_ORDER:
            face = getattr(c2, attr)
            for i in range(3):
                for j in range(3):
                    sid = face[i][j]
                    after[sid] = face_name
        n = sum(1 for sid in before if before[sid] != after[sid])
        best = max(best, n)
    return max(best, 1)


stickersFaceChangePerMove: int = computeStickersChangingFacePerMove()


class Heuristic(ABC):
    @abstractmethod
    def value(self, state: State) -> int:
        ...


class HeuristicCube(Heuristic):
    """Relaxation : pièces (2–3 stickers) ; borne plus lâche que le brut / sticker."""

    def value(self, state: State) -> int:
        s = rawStickerDistanceSum(state.cube)
        return s // 8


class HeuristicLabel(Heuristic):
    """Relaxation : chaque sticker se déplace seul ; somme des distances 3D / diviseur."""

    def value(self, state: State) -> int:
        s = rawStickerDistanceSum(state.cube)
        return s // stickersFaceChangePerMove


class UniformCost(Heuristic):
    """Recherche à coût uniforme (h = 0)."""

    def value(self, state: State) -> int:
        return 0
