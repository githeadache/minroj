package se.familjenhed.minroj.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BoardTest {

    // Mines at fixed positions (far from common safe-click at 5,5)
    private val minesTopRow = MineGenerator { _, _, _ -> setOf(0 to 1, 0 to 2) }
    private val mineAt33 = MineGenerator { _, _, _ -> setOf(3 to 3) }

    private lateinit var board: Board

    @Before
    fun setUp() {
        board = Board(Difficulty.SMALL, minesTopRow)
    }

    // --- initial state ---

    @Test
    fun `board dimensions match difficulty`() {
        assertEquals(Difficulty.SMALL.rows, board.cells.size)
        assertEquals(Difficulty.SMALL.cols, board.cells[0].size)
    }

    @Test
    fun `initial status is IDLE`() {
        assertEquals(GameStatus.IDLE, board.status)
    }

    @Test
    fun `all cells start unrevealed and unflagged`() {
        board.cells.flatten().forEach { cell ->
            assertFalse(cell.isRevealed)
            assertFalse(cell.isFlagged)
        }
    }

    // --- first reveal transitions to PLAYING ---

    @Test
    fun `revealing first cell starts the game`() {
        board.reveal(5, 5)

        assertEquals(GameStatus.PLAYING, board.status)
        assertTrue(board.cells[5][5].isRevealed)
    }

    // --- hitting a mine ---

    @Test
    fun `revealing a mine results in LOST`() {
        val b = Board(Difficulty.SMALL, mineAt33)
        b.reveal(8, 8)   // start safely
        b.reveal(3, 3)   // mine

        assertEquals(GameStatus.LOST, b.status)
    }

    @Test
    fun `all mines are revealed after losing`() {
        val b = Board(Difficulty.SMALL, minesTopRow)
        b.reveal(5, 5)   // start
        b.reveal(0, 1)   // mine

        val revealedMines = b.cells.flatten().filter { it.hasMine }
        assertTrue(revealedMines.all { it.isRevealed })
    }

    // --- adjacent mine count ---

    @Test
    fun `cells adjacent to mine show correct count`() {
        // mines at (0,1) and (0,2) — cell (0,0) touches (0,1), so adjacentMines == 1
        board.reveal(5, 5)

        assertEquals(1, board.cells[0][0].adjacentMines)
        assertEquals(2, board.cells[1][1].adjacentMines)  // touches both mines
    }

    // --- flood reveal ---

    @Test
    fun `revealing empty area expands to neighbors`() {
        // Mines only at row 0, cols 1–2.  Row 5,5 is far from any mine → adjacentMines==0 → flood fill
        board.reveal(5, 5)

        // Several cells in the far corner should be auto-revealed by flood fill
        assertTrue(board.cells[8][8].isRevealed)
        assertTrue(board.cells[7][7].isRevealed)
    }

    // --- flags ---

    @Test
    fun `toggling flag marks cell`() {
        board.toggleFlag(3, 3)

        assertTrue(board.cells[3][3].isFlagged)
    }

    @Test
    fun `toggling flag twice removes the flag`() {
        board.toggleFlag(3, 3)
        board.toggleFlag(3, 3)

        assertFalse(board.cells[3][3].isFlagged)
    }

    @Test
    fun `cannot reveal a flagged cell`() {
        board.toggleFlag(5, 5)
        board.reveal(5, 5)

        assertEquals(GameStatus.IDLE, board.status)
        assertFalse(board.cells[5][5].isRevealed)
    }

    @Test
    fun `remaining flags decreases when flag is placed`() {
        val before = board.remainingFlags
        board.toggleFlag(3, 3)

        assertEquals(before - 1, board.remainingFlags)
    }

    @Test
    fun `remaining flags restores when flag is removed`() {
        val before = board.remainingFlags
        board.toggleFlag(3, 3)
        board.toggleFlag(3, 3)

        assertEquals(before, board.remainingFlags)
    }

    // --- game-over guard ---

    @Test
    fun `no action possible after LOST`() {
        val b = Board(Difficulty.SMALL, mineAt33)
        b.reveal(8, 8)
        b.reveal(3, 3)   // triggers LOST
        val snapshotRevealed = b.cells[7][7].isRevealed

        b.reveal(7, 7)

        assertEquals(snapshotRevealed, b.cells[7][7].isRevealed)
    }

    @Test
    fun `cannot flag after LOST`() {
        val b = Board(Difficulty.SMALL, mineAt33)
        b.reveal(8, 8)
        b.reveal(3, 3)   // LOST

        b.toggleFlag(0, 0)

        assertFalse(b.cells[0][0].isFlagged)
    }

    // --- win condition ---

    @Test
    fun `revealing all safe cells results in WON`() {
        val singleMine = MineGenerator { d, _, _ -> setOf((d.rows - 1) to (d.cols - 1)) }
        val b = Board(Difficulty.SMALL, singleMine)

        // Reveal every cell except the mine
        for (row in 0 until Difficulty.SMALL.rows) {
            for (col in 0 until Difficulty.SMALL.cols) {
                if (row != Difficulty.SMALL.rows - 1 || col != Difficulty.SMALL.cols - 1) {
                    b.reveal(row, col)
                }
            }
        }

        assertEquals(GameStatus.WON, b.status)
    }
}
