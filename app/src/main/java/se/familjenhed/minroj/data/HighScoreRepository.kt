package se.familjenhed.minroj.data

import se.familjenhed.minroj.domain.Difficulty
import se.familjenhed.minroj.domain.HighScore

class HighScoreRepository(private val storage: HighScoreStorage) {

    fun getTopScores(difficulty: Difficulty): List<HighScore> =
        deserialize(storage.load(difficulty.name))
            .sortedBy { it.timeSeconds }
            .take(MAX_ENTRIES)

    fun addScore(difficulty: Difficulty, timeSeconds: Int): Boolean {
        val existing = getTopScores(difficulty)
        val isFastest = existing.isEmpty() || timeSeconds < existing.first().timeSeconds
        val qualifies = existing.size < MAX_ENTRIES || timeSeconds < existing.last().timeSeconds
        if (qualifies) {
            val updated = (existing + HighScore(timeSeconds, System.currentTimeMillis()))
                .sortedBy { it.timeSeconds }
                .take(MAX_ENTRIES)
            storage.save(difficulty.name, serialize(updated))
        }
        return isFastest
    }

    private fun serialize(scores: List<HighScore>): String =
        scores.joinToString(",") { "${it.timeSeconds}|${it.date}" }

    private fun deserialize(raw: String): List<HighScore> {
        if (raw.isBlank()) return emptyList()
        return raw.split(",").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size == 2) HighScore(
                timeSeconds = parts[0].toIntOrNull() ?: return@mapNotNull null,
                date = parts[1].toLongOrNull() ?: return@mapNotNull null
            ) else null
        }
    }

    companion object {
        const val MAX_ENTRIES = 10
    }
}
