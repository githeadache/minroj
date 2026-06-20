package se.familjenhed.minroj.domain

class Board(
    val difficulty: Difficulty,
    private val mineGenerator: MineGenerator = RandomMineGenerator()
) {
    private val grid: Array<Array<Cell>> = Array(difficulty.rows) { row ->
        Array(difficulty.cols) { col -> Cell(row, col) }
    }

    val cells: List<List<Cell>>
        get() = grid.map { it.toList() }

    var status: GameStatus = GameStatus.IDLE
        private set

    val remainingFlags: Int
        get() = difficulty.mines - grid.sumOf { row -> row.count { it.isFlagged } }

    fun reveal(row: Int, col: Int) {
        if (status == GameStatus.WON || status == GameStatus.LOST) return
        val cell = grid[row][col]
        if (cell.isRevealed || cell.isFlagged) return

        if (status == GameStatus.IDLE) {
            placeMines(row, col)
            status = GameStatus.PLAYING
        }

        if (grid[row][col].hasMine) {
            grid[row][col] = grid[row][col].copy(isRevealed = true)
            revealAllMines()
            status = GameStatus.LOST
            return
        }

        floodReveal(row, col)
        checkWin()
    }

    fun toggleFlag(row: Int, col: Int) {
        if (status == GameStatus.WON || status == GameStatus.LOST) return
        val cell = grid[row][col]
        if (cell.isRevealed) return
        grid[row][col] = cell.copy(isFlagged = !cell.isFlagged)
    }

    private fun placeMines(safeRow: Int, safeCol: Int) {
        mineGenerator.generate(difficulty, safeRow, safeCol).forEach { (row, col) ->
            grid[row][col] = grid[row][col].copy(hasMine = true)
        }
        recalculateAdjacentCounts()
    }

    private fun recalculateAdjacentCounts() {
        for (row in 0 until difficulty.rows) {
            for (col in 0 until difficulty.cols) {
                if (!grid[row][col].hasMine) {
                    val count = neighbors(row, col).count { (r, c) -> grid[r][c].hasMine }
                    grid[row][col] = grid[row][col].copy(adjacentMines = count)
                }
            }
        }
    }

    private fun floodReveal(row: Int, col: Int) {
        val cell = grid[row][col]
        if (cell.isRevealed || cell.isFlagged || cell.hasMine) return

        grid[row][col] = cell.copy(isRevealed = true)

        if (cell.adjacentMines == 0) {
            neighbors(row, col).forEach { (r, c) -> floodReveal(r, c) }
        }
    }

    private fun revealAllMines() {
        for (row in 0 until difficulty.rows) {
            for (col in 0 until difficulty.cols) {
                if (grid[row][col].hasMine) {
                    grid[row][col] = grid[row][col].copy(isRevealed = true)
                }
            }
        }
    }

    private fun checkWin() {
        val allSafeCellsRevealed = grid.all { row -> row.all { cell -> cell.hasMine || cell.isRevealed } }
        if (allSafeCellsRevealed) status = GameStatus.WON
    }

    private fun neighbors(row: Int, col: Int): List<Pair<Int, Int>> =
        (-1..1).flatMap { dr ->
            (-1..1).mapNotNull { dc ->
                if (dr == 0 && dc == 0) null
                else {
                    val r = row + dr
                    val c = col + dc
                    if (r in 0 until difficulty.rows && c in 0 until difficulty.cols) r to c else null
                }
            }
        }
}
