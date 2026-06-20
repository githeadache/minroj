package se.familjenhed.minroj.domain

data class Cell(
    val row: Int,
    val col: Int,
    val hasMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val adjacentMines: Int = 0
)
