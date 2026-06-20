package se.familjenhed.minroj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.familjenhed.minroj.domain.Board
import se.familjenhed.minroj.domain.Difficulty
import se.familjenhed.minroj.domain.GameStatus
import se.familjenhed.minroj.domain.MineGenerator
import se.familjenhed.minroj.domain.RandomMineGenerator

class GameViewModel(
    private val mineGenerator: MineGenerator = RandomMineGenerator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var board: Board = Board(Difficulty.SMALL, mineGenerator)
    private var timerJob: Job? = null

    init {
        startGame(Difficulty.SMALL)
    }

    fun startGame(difficulty: Difficulty) {
        timerJob?.cancel()
        board = Board(difficulty, mineGenerator)
        _uiState.value = GameUiState(
            cells = board.cells,
            difficulty = difficulty,
            status = GameStatus.IDLE,
            remainingFlags = difficulty.mines,
            elapsedSeconds = 0,
            isFlagMode = false
        )
    }

    fun onCellClick(row: Int, col: Int) {
        val state = _uiState.value
        if (state.status == GameStatus.WON || state.status == GameStatus.LOST) return

        if (state.isFlagMode) {
            board.toggleFlag(row, col)
        } else {
            val wasIdle = board.status == GameStatus.IDLE
            board.reveal(row, col)
            if (wasIdle && board.status == GameStatus.PLAYING) startTimer()
        }

        syncState()

        if (board.status == GameStatus.WON || board.status == GameStatus.LOST) timerJob?.cancel()
    }

    fun toggleFlagMode() {
        _uiState.value = _uiState.value.copy(isFlagMode = !_uiState.value.isFlagMode)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _uiState.value = _uiState.value.copy(
                    elapsedSeconds = (_uiState.value.elapsedSeconds + 1).coerceAtMost(999)
                )
            }
        }
    }

    private fun syncState() {
        _uiState.value = _uiState.value.copy(
            cells = board.cells,
            status = board.status,
            remainingFlags = board.remainingFlags
        )
    }
}
