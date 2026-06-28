package se.familjenhed.minroj.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RandomMineGeneratorTest {

    private val generator = RandomMineGenerator()

    @Test
    fun `safe cell is never a mine`() {
        repeat(50) {
            val mines = generator.generate(Difficulty.SMALL, 4, 4)
            assertFalse(mines.contains(4 to 4))
        }
    }

    @Test
    fun `entire 3x3 zone around safe cell is mine-free`() {
        repeat(50) {
            val mines = generator.generate(Difficulty.SMALL, 4, 4)
            for (dr in -1..1) for (dc in -1..1) {
                assertFalse("mine found at (${4+dr},${4+dc})", mines.contains((4 + dr) to (4 + dc)))
            }
        }
    }

    @Test
    fun `returns correct number of mines`() {
        Difficulty.entries.forEach { difficulty ->
            val mines = generator.generate(difficulty, 0, 0)
            assertEquals(difficulty.mines, mines.size)
        }
    }

    @Test
    fun `works at board corner where safe zone extends outside board`() {
        repeat(50) {
            val mines = generator.generate(Difficulty.SMALL, 0, 0)
            for (dr in -1..1) for (dc in -1..1) {
                assertFalse(mines.contains((0 + dr) to (0 + dc)))
            }
        }
    }
}
