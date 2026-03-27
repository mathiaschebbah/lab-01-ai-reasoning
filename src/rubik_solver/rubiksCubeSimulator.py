import tkinter as tk
from tkinter import messagebox
from typing import ClassVar

from rubik_solver.astar import Astar
from rubik_solver.cube import Cube
from rubik_solver.heuristic import HeuristicLabel
from rubik_solver.searchSupport import actionLabels, applyAction


class RubiksCubeSimulator:

    stickerColors: ClassVar[dict[str, str]] = {
        "W": "#f5f5f5",
        "R": "#c41e3a",
        "G": "#009b48",
        "B": "#0046ad",
        "Y": "#ffd500",
        "O": "#ff5800",
    }

    def __init__(self) -> None:
        self.cube = Cube()
        self.root = tk.Tk()
        self.root.title("Rubik's Cube")
        self.canvas = tk.Canvas(self.root, width=420, height=320, bg="#2b2b2b", highlightthickness=0)
        self.canvas.pack(padx=8, pady=8)
        self.faceSize = 28
        self.gap = 4
        self.buttonFrame = tk.Frame(self.root)
        self.buttonFrame.pack(pady=(0, 8))
        self.buildControls()
        self.drawCube()

    def buildControls(self) -> None:
        moves: list[tuple[str, str]] = [
            ("U", "U"),
            ("U'", "U'"),
            ("D", "D"),
            ("D'", "D'"),
            ("F", "F"),
            ("F'", "F'"),
            ("B", "B"),
            ("B'", "B'"),
            ("L", "L"),
            ("L'", "L'"),
            ("R", "R"),
            ("R'", "R'"),
        ]
        for i, (label, action) in enumerate(moves):
            tk.Button(self.buttonFrame, text=label, width=3, command=lambda a=action: self.actionPerformed(a)).grid(
                row=i // 6, column=i % 6, padx=2, pady=2
            )
        tk.Button(self.buttonFrame, text="Reset", command=lambda: self.actionPerformed("reset")).grid(
            row=2, column=0, columnspan=3, pady=4
        )
        tk.Button(self.buttonFrame, text="Solve", command=lambda: self.actionPerformed("solve")).grid(
            row=2, column=3, columnspan=3, pady=4
        )

    def faceOrigin(self, name: str) -> tuple[int, int]:
        s = self.faceSize + self.gap
        ox, oy = 40, 30
        if name == "top":
            return ox + s * 3, oy
        if name == "left":
            return ox + s * 0, oy + s
        if name == "front":
            return ox + s * 1, oy + s
        if name == "right":
            return ox + s * 2, oy + s
        if name == "back":
            return ox + s * 3, oy + s
        if name == "bottom":
            return ox + s * 3, oy + s * 2
        raise ValueError(name)

    def drawFace(self, face: list[list[str]], origin: tuple[int, int]) -> None:
        x0, y0 = origin
        cell = self.faceSize
        for i in range(3):
            for j in range(3):
                color = self.stickerColors.get(face[i][j], "#888888")
                x1 = x0 + j * cell
                y1 = y0 + i * cell
                self.canvas.create_rectangle(
                    x1, y1, x1 + cell - 1, y1 + cell - 1, fill=color, outline="#1a1a1a", width=1
                )

    def drawCube(self) -> None:
        self.canvas.delete("all")
        self.drawFace(self.cube.top, self.faceOrigin("top"))
        self.drawFace(self.cube.left, self.faceOrigin("left"))
        self.drawFace(self.cube.front, self.faceOrigin("front"))
        self.drawFace(self.cube.right, self.faceOrigin("right"))
        self.drawFace(self.cube.back, self.faceOrigin("back"))
        self.drawFace(self.cube.bottom, self.faceOrigin("bottom"))

    def actionPerformed(self, action: str) -> None:
        if action == "solve":
            solver = Astar(self.cube, HeuristicLabel())
            moves = solver.solve()
            if not moves and not self.cube.isSolved():
                messagebox.showerror("Solve", "Aucune solution trouvée (frontière vide).")
                return
            for label in moves:
                applyAction(self.cube, actionLabels.index(label))
            self.drawCube()
            messagebox.showinfo("Solve", f"{len(moves)} coup(s).")
            return
        if action == "reset":
            self.cube = Cube()
            self.drawCube()
            return
        if action == "U":
            self.cube.turnUp()
        elif action == "U'":
            self.cube.returnUp()
        elif action == "D":
            self.cube.turnDown()
        elif action == "D'":
            self.cube.returnDown()
        elif action == "F":
            self.cube.turnFront()
        elif action == "F'":
            self.cube.returnFront()
        elif action == "B":
            self.cube.turnBack()
        elif action == "B'":
            self.cube.returnBack()
        elif action == "L":
            self.cube.turnLeft()
        elif action == "L'":
            self.cube.returnLeft()
        elif action == "R":
            self.cube.turnRight()
        elif action == "R'":
            self.cube.returnRight()
        else:
            return
        self.drawCube()

    def run(self) -> None:
        self.root.mainloop()


def main() -> None:
    RubiksCubeSimulator().run()
