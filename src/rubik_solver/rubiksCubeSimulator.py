import random
import threading
import tkinter as tk
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
        self.cellSize = 40
        self.gap = 6
        self.faceBlock = 3 * self.cellSize + self.gap
        cw = 4 * self.faceBlock + 40
        ch = 3 * self.faceBlock + 40
        self.canvas = tk.Canvas(self.root, width=cw, height=ch, bg="#2b2b2b", highlightthickness=0)
        self.canvas.pack(padx=8, pady=8)
        self.statusLabel = tk.Label(self.root, text="", fg="white", bg="#2b2b2b", font=("Helvetica", 12))
        self.statusLabel.pack()
        self.buttonFrame = tk.Frame(self.root)
        self.buttonFrame.pack(pady=(0, 8))
        self.solving = False
        self.shuffleCount = 0
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
            row=2, column=0, columnspan=2, pady=4
        )
        tk.Button(self.buttonFrame, text="Shuffle", command=lambda: self.actionPerformed("shuffle")).grid(
            row=2, column=2, columnspan=2, pady=4
        )
        tk.Button(self.buttonFrame, text="Solve", command=lambda: self.actionPerformed("solve")).grid(
            row=2, column=4, columnspan=2, pady=4
        )

    # Cross layout:        col 0   col 1   col 2   col 3
    #   row 0                      Top
    #   row 1             Left    Front   Right    Back
    #   row 2                     Bottom
    FACE_POSITIONS: ClassVar[dict[str, tuple[int, int]]] = {
        "top":    (1, 0),
        "left":   (0, 1),
        "front":  (1, 1),
        "right":  (2, 1),
        "back":   (3, 1),
        "bottom": (1, 2),
    }

    FACE_LABELS: ClassVar[dict[str, str]] = {
        "top": "U", "bottom": "D", "front": "F",
        "back": "B", "right": "R", "left": "L",
    }

    def faceOrigin(self, name: str) -> tuple[int, int]:
        col, row = self.FACE_POSITIONS[name]
        ox, oy = 20, 20
        return ox + col * self.faceBlock, oy + row * self.faceBlock

    def drawFace(self, face: list[list[str]], origin: tuple[int, int], label: str) -> None:
        x0, y0 = origin
        cell = self.cellSize
        for i in range(3):
            for j in range(3):
                color = self.stickerColors.get(face[i][j], "#888888")
                x1 = x0 + j * cell
                y1 = y0 + i * cell
                self.canvas.create_rectangle(
                    x1, y1, x1 + cell - 1, y1 + cell - 1,
                    fill=color, outline="#1a1a1a", width=2,
                )
        self.canvas.create_text(
            x0 + cell * 1.5, y0 - 8,
            text=label, fill="#888888", font=("Helvetica", 11, "bold"),
        )

    def drawCube(self) -> None:
        self.canvas.delete("all")
        for name in self.FACE_POSITIONS:
            face = getattr(self.cube, name)
            label = self.FACE_LABELS[name]
            self.drawFace(face, self.faceOrigin(name), label)

    def setButtons(self, state: str) -> None:
        for child in self.buttonFrame.winfo_children():
            child.configure(state=state)

    def animateSolve(self, moves: list[str], idx: int, total: int) -> None:
        if idx >= len(moves):
            self.statusLabel.config(text=f"Résolu en {total} coup(s)")
            self.solving = False
            self.setButtons("normal")
            return
        label = moves[idx]
        applyAction(self.cube, actionLabels.index(label))
        self.drawCube()
        self.statusLabel.config(text=f"Coup {idx + 1}/{total} : {label}")
        self.root.after(400, self.animateSolve, moves, idx + 1, total)

    def solveInThread(self) -> None:
        cubeCopy = Cube(self.cube)
        self.currentSolver = Astar(cubeCopy, HeuristicLabel())
        self.solveH0 = self.currentSolver.root.valH
        moves = self.currentSolver.solve()
        self.root.after(0, self.onSolveDone, moves, self.currentSolver.nodesExplored)

    def pollSolveProgress(self) -> None:
        if not self.solving:
            return
        solver = getattr(self, "currentSolver", None)
        if solver is not None:
            n = solver.nodesExplored
            h0 = getattr(self, "solveH0", "?")
            self.statusLabel.config(text=f"Résolution... h₀={h0}  nœuds: {n:,}")
        self.root.after(200, self.pollSolveProgress)

    def onSolveDone(self, moves: list[str], nodesExplored: int) -> None:
        h0 = getattr(self, "solveH0", "?")
        if not moves and not self.cube.isSolved():
            self.statusLabel.config(
                text=f"Abandon — h₀={h0}, {nodesExplored:,} nœuds explorés (limite atteinte)"
            )
            self.solving = False
            self.setButtons("normal")
            return
        if not moves:
            self.statusLabel.config(text="Déjà résolu")
            self.solving = False
            self.setButtons("normal")
            return
        self.statusLabel.config(
            text=f"Solution : {len(moves)} coups, {nodesExplored:,} nœuds (h₀={h0})"
        )
        self.root.after(1000, self.animateSolve, moves, 0, len(moves))

    def actionPerformed(self, action: str) -> None:
        if self.solving:
            return
        if action == "solve":
            self.solving = True
            self.setButtons("disabled")
            self.statusLabel.config(text="Résolution en cours...")
            threading.Thread(target=self.solveInThread, daemon=True).start()
            self.root.after(200, self.pollSolveProgress)
            return
        if action == "shuffle":
            applyAction(self.cube, random.randint(0, 11))
            self.shuffleCount += 1
            self.statusLabel.config(text=f"Mélangé ({self.shuffleCount} coup(s))")
            self.drawCube()
            return
        if action == "reset":
            self.cube = Cube()
            self.shuffleCount = 0
            self.statusLabel.config(text="")
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
