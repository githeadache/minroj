package se.familjenhed.minroj.viewmodel

import se.familjenhed.minroj.domain.Cell
import se.familjenhed.minroj.domain.Difficulty
import se.familjenhed.minroj.domain.GameStatus

data class GameUiState(
    val cells: List<List<Cell>> = emptyList(),
    val difficulty: Difficulty = Difficulty.SMALL,
    val status: GameStatus = GameStatus.IDLE,
    val remainingFlags: Int = 0,
    val elapsedSeconds: Int = 0,
    val isFlagMode: Boolean = false
)
