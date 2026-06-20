package se.familjenhed.minroj.domain

fun interface MineGenerator {
    fun generate(difficulty: Difficulty, safeRow: Int, safeCol: Int): Set<Pair<Int, Int>>
}

class RandomMineGenerator : MineGenerator {
    override fun generate(difficulty: Difficulty, safeRow: Int, safeCol: Int): Set<Pair<Int, Int>> {
        val candidates = (0 until difficulty.rows).flatMap { row ->
            (0 until difficulty.cols).map { col -> row to col }
        }.filter { (row, col) ->
            row !in (safeRow - 1)..(safeRow + 1) || col !in (safeCol - 1)..(safeCol + 1)
        }.shuffled()

        return candidates.take(difficulty.mines).toSet()
    }
}
