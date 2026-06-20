package se.familjenhed.minroj.viewmodel

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.familjenhed.minroj.MainDispatcherRule
import se.familjenhed.minroj.domain.Difficulty
import se.familjenhed.minroj.domain.GameStatus
import se.familjenhed.minroj.domain.MineGenerator

class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noMines = MineGenerator { _, _, _ -> emptySet() }
    private val mineAt00 = MineGenerator { _, _, _ -> setOf(0 to 0) }

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel(mineGenerator = noMines)
    }

    @Test
    fun `initial state matches small difficulty`() = runTest {
        val state = viewModel.uiState.value

        assertEquals(Difficulty.SMALL, state.difficulty)
        assertEquals(GameStatus.IDLE, state.status)
        assertEquals(9, state.cells.size)
        assertEquals(9, state.cells[0].size)
    }

    @Test
    fun `startGame resets board and timer`() = runTest {
        viewModel.onCellClick(4, 4)   // advance game
        viewModel.startGame(Difficulty.MEDIUM)

        val state = viewModel.uiState.value
        assertEquals(Difficulty.MEDIUM, state.difficulty)
        assertEquals(GameStatus.IDLE, state.status)
        assertEquals(0, state.elapsedSeconds)
        assertEquals(16, state.cells.size)
    }

    @Test
    fun `clicking a cell reveals it`() = runTest {
        viewModel.onCellClick(4, 4)

        assertTrue(viewModel.uiState.value.cells[4][4].isRevealed)
    }

    @Test
    fun `first click transitions status to PLAYING`() = runTest {
        viewModel.onCellClick(4, 4)

        assertEquals(GameStatus.PLAYING, viewModel.uiState.value.status)
    }

    @Test
    fun `clicking a mine results in LOST`() = runTest {
        val vm = GameViewModel(mineGenerator = mineAt00)
        vm.onCellClick(8, 8)   // start safely
        vm.onCellClick(0, 0)   // mine

        assertEquals(GameStatus.LOST, vm.uiState.value.status)
    }

    @Test
    fun `flag mode starts as inactive`() = runTest {
        assertFalse(viewModel.uiState.value.isFlagMode)
    }

    @Test
    fun `toggleFlagMode activates flag mode`() = runTest {
        viewModel.toggleFlagMode()

        assertTrue(viewModel.uiState.value.isFlagMode)
    }

    @Test
    fun `toggleFlagMode twice deactivates flag mode`() = runTest {
        viewModel.toggleFlagMode()
        viewModel.toggleFlagMode()

        assertFalse(viewModel.uiState.value.isFlagMode)
    }

    @Test
    fun `clicking in flag mode places a flag without revealing`() = runTest {
        viewModel.toggleFlagMode()
        viewModel.onCellClick(3, 3)

        val cell = viewModel.uiState.value.cells[3][3]
        assertTrue(cell.isFlagged)
        assertFalse(cell.isRevealed)
        assertEquals(GameStatus.IDLE, viewModel.uiState.value.status)
    }

    @Test
    fun `remaining flags decreases after placing a flag`() = runTest {
        val before = viewModel.uiState.value.remainingFlags
        viewModel.toggleFlagMode()
        viewModel.onCellClick(3, 3)

        assertEquals(before - 1, viewModel.uiState.value.remainingFlags)
    }

    @Test
    fun `no action possible after LOST`() = runTest {
        val vm = GameViewModel(mineGenerator = mineAt00)
        vm.onCellClick(8, 8)
        vm.onCellClick(0, 0)   // LOST
        val snapshot = vm.uiState.value.cells[7][7].isRevealed

        vm.onCellClick(7, 7)

        assertEquals(snapshot, vm.uiState.value.cells[7][7].isRevealed)
    }
}
