package se.familjenhed.minroj.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CellTest {

    @Test
    fun `new cell defaults to unrevealed, unflagged, no mine`() {
        val cell = Cell(row = 0, col = 0)

        assertFalse(cell.isRevealed)
        assertFalse(cell.isFlagged)
        assertFalse(cell.hasMine)
        assertEquals(0, cell.adjacentMines)
    }

    @Test
    fun `cells at different positions are not equal`() {
        val a = Cell(0, 0)
        val b = Cell(0, 1)

        assert(a != b)
    }

    @Test
    fun `copy preserves all fields except the changed one`() {
        val original = Cell(2, 3, hasMine = false, isRevealed = false, adjacentMines = 3)
        val revealed = original.copy(isRevealed = true)

        assert(revealed.isRevealed)
        assertEquals(original.adjacentMines, revealed.adjacentMines)
        assertEquals(original.row, revealed.row)
        assertEquals(original.col, revealed.col)
    }
}
