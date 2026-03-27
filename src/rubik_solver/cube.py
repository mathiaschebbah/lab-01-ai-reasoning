class Cube:
    """Chaque face est une matrice 3x3 (ligne 0 en haut, colonne 0 à gau), vue depuis l'extérieur de la face."""

    solvedColors = {
        "top": "W",
        "bottom": "Y",
        "front": "G",
        "back": "B",
        "right": "R",
        "left": "O",
    }
    # Rotate a face clockwise
    @classmethod
    def rotateFaceClockwise(cls, face: list[list[str]]) -> None:
        c = [row[:] for row in face]
        for i in range(3):
            for j in range(3):
                face[i][j] = c[2 - j][i]

    def __init__(self, other: "Cube | None" = None) -> None:
        if other is not None:
            self.top = [row[:] for row in other.top]
            self.bottom = [row[:] for row in other.bottom]
            self.front = [row[:] for row in other.front]
            self.back = [row[:] for row in other.back]
            self.right = [row[:] for row in other.right]
            self.left = [row[:] for row in other.left]
        else:
            sc = self.solvedColors
            self.top = [[sc["top"]] * 3 for _ in range(3)]
            self.bottom = [[sc["bottom"]] * 3 for _ in range(3)]
            self.front = [[sc["front"]] * 3 for _ in range(3)]
            self.back = [[sc["back"]] * 3 for _ in range(3)]
            self.right = [[sc["right"]] * 3 for _ in range(3)]
            self.left = [[sc["left"]] * 3 for _ in range(3)]

    def isSolved(self) -> bool:
        for name, face in (
            ("top", self.top),
            ("bottom", self.bottom),
            ("front", self.front),
            ("back", self.back),
            ("right", self.right),
            ("left", self.left),
        ):
            w = self.solvedColors[name]
            for i in range(3):
                for j in range(3):
                    if face[i][j] != w:
                        return False
        return True

    def turnUp(self) -> None:
        self.rotateFaceClockwise(self.top)
        t = self.front[0][:]
        self.front[0] = self.right[0][:]
        self.right[0] = self.back[0][:]
        self.back[0] = self.left[0][:]
        self.left[0] = t

    def turnDown(self) -> None:
        self.rotateFaceClockwise(self.bottom)
        t = self.front[2][:]
        self.front[2] = self.left[2][:]
        self.left[2] = self.back[2][:]
        self.back[2] = self.right[2][:]
        self.right[2] = t

    def turnFront(self) -> None:
        self.rotateFaceClockwise(self.front)
        u = [self.top[2][i] for i in range(3)]
        r = [self.right[i][0] for i in range(3)]
        d = [self.bottom[0][i] for i in range(3)]
        l = [self.left[i][2] for i in range(3)]
        for i in range(3):
            self.right[i][0] = u[i]
            self.bottom[0][i] = r[i]
            self.left[i][2] = d[2 - i]
            self.top[2][i] = l[2 - i]

    def turnBack(self) -> None:
        self.rotateFaceClockwise(self.back)
        u = [self.top[0][i] for i in range(3)]
        r = [self.right[i][2] for i in range(3)]
        d = [self.bottom[2][i] for i in range(3)]
        l = [self.left[i][0] for i in range(3)]
        for i in range(3):
            self.top[0][i] = r[i]
            self.right[i][2] = d[2 - i]
            self.bottom[2][i] = l[2 - i]
            self.left[i][0] = u[i]

    def turnRight(self) -> None:
        self.rotateFaceClockwise(self.right)
        u = [self.top[i][2] for i in range(3)]
        f = [self.front[i][2] for i in range(3)]
        d = [self.bottom[i][2] for i in range(3)]
        b = [self.back[2 - i][0] for i in range(3)]
        for i in range(3):
            self.front[i][2] = u[i]
            self.bottom[i][2] = f[i]
            self.back[2 - i][0] = d[i]
            self.top[i][2] = b[i]

    def turnLeft(self) -> None:
        self.rotateFaceClockwise(self.left)
        u = [self.top[i][0] for i in range(3)]
        f = [self.front[i][0] for i in range(3)]
        d = [self.bottom[i][0] for i in range(3)]
        b = [self.back[2 - i][2] for i in range(3)]
        for i in range(3):
            self.top[i][0] = f[i]
            self.front[i][0] = d[i]
            self.bottom[i][0] = b[i]
            self.back[2 - i][2] = u[i]

    def returnUp(self) -> None:
        self.turnUp()
        self.turnUp()
        self.turnUp()

    def returnDown(self) -> None:
        self.turnDown()
        self.turnDown()
        self.turnDown()

    def returnFront(self) -> None:
        self.turnFront()
        self.turnFront()
        self.turnFront()

    def returnBack(self) -> None:
        self.turnBack()
        self.turnBack()
        self.turnBack()

    def returnRight(self) -> None:
        self.turnRight()
        self.turnRight()
        self.turnRight()

    def returnLeft(self) -> None:
        self.turnLeft()
        self.turnLeft()
        self.turnLeft()
