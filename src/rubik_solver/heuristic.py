from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

from rubik_solver.cube import Cube
from rubik_solver.searchSupport import applyAction

if TYPE_CHECKING:
    from rubik_solver.state import State


class Heuristic(ABC):

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

    _GOAL_BY_FACE: dict[str, list[tuple[int, int, int]]] | None = None

    @staticmethod
    def stickerCoords(face: str, i: int, j: int) -> tuple[int, int, int]:
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

    @staticmethod
    def manhattan3(a: tuple[int, int, int], b: tuple[int, int, int]) -> int:
        return abs(a[0] - b[0]) + abs(a[1] - b[1]) + abs(a[2] - b[2])

    @classmethod
    def _getGoalByFace(cls) -> dict[str, list[tuple[int, int, int]]]:
        if cls._GOAL_BY_FACE is None:
            cls._GOAL_BY_FACE = {
                f: [cls.stickerCoords(f, i, j) for i in range(3) for j in range(3)]
                for f in ("top", "bottom", "front", "back", "right", "left")
            }
        return cls._GOAL_BY_FACE

    @classmethod
    def rawStickerDistanceSum(cls, cube: Cube) -> int:
        goalByFace = cls._getGoalByFace()
        total = 0
        for face_name, attr in cls.FACE_ORDER:
            face = getattr(cube, attr)
            for i in range(3):
                for j in range(3):
                    color = face[i][j]
                    goal_face = cls.COLOR_GOAL_FACE[color]
                    cur = cls.stickerCoords(face_name, i, j)
                    goals = goalByFace[goal_face]
                    total += min(cls.manhattan3(cur, g) for g in goals)
        return total

    @abstractmethod
    def value(self, state: State) -> int:
        ...


class HeuristicCube(Heuristic):

    def value(self, state: State) -> int:
        s = Heuristic.rawStickerDistanceSum(state.cube)
        return s // 8


class HeuristicLabel(Heuristic):

    _stickersFaceChangePerMove: int | None = None

    @classmethod
    def _computeStickersChangingFacePerMove(cls) -> int:
        c = Cube()
        for face_name, attr in Heuristic.FACE_ORDER:
            face = getattr(c, attr)
            for i in range(3):
                for j in range(3):
                    face[i][j] = f"{face_name}_{i}_{j}"

        before: dict[str, str] = {}
        for face_name, attr in Heuristic.FACE_ORDER:
            face = getattr(c, attr)
            for i in range(3):
                for j in range(3):
                    before[face[i][j]] = face_name

        best = 0
        for aid in range(12):
            c2 = Cube(c)
            applyAction(c2, aid)
            after: dict[str, str] = {}
            for face_name, attr in Heuristic.FACE_ORDER:
                face = getattr(c2, attr)
                for i in range(3):
                    for j in range(3):
                        after[face[i][j]] = face_name
            n = sum(1 for sid in before if before[sid] != after[sid])
            best = max(best, n)
        return max(best, 1)

    def value(self, state: State) -> int:
        if HeuristicLabel._stickersFaceChangePerMove is None:
            HeuristicLabel._stickersFaceChangePerMove = (
                HeuristicLabel._computeStickersChangingFacePerMove()
            )
        s = Heuristic.rawStickerDistanceSum(state.cube)
        return s // HeuristicLabel._stickersFaceChangePerMove


class UniformCost(Heuristic):

    def value(self, state: State) -> int:
        return 0
