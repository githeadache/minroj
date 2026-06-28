package se.familjenhed.minroj.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import se.familjenhed.minroj.domain.Difficulty

class HighScoreRepositoryTest {

    private lateinit var storage: InMemoryHighScoreStorage
    private lateinit var repository: HighScoreRepository

    @Before
    fun setUp() {
        storage = InMemoryHighScoreStorage()
        repository = HighScoreRepository(storage)
    }

    @Test
    fun `empty repository returns empty list`() {
        assertTrue(repository.getTopScores(Difficulty.SMALL).isEmpty())
    }

    @Test
    fun `first score is a new record`() {
        assertTrue(repository.addScore(Difficulty.SMALL, 120))
    }

    @Test
    fun `top-10 score that is not fastest does not count as new record`() {
        repository.addScore(Difficulty.SMALL, 100) // fastest so far

        val isRecord = repository.addScore(Difficulty.SMALL, 110) // top-10 but slower

        assertFalse(isRecord)
        assertEquals(2, repository.getTopScores(Difficulty.SMALL).size)
    }

    @Test
    fun `added score appears in top list`() {
        repository.addScore(Difficulty.SMALL, 120)

        val scores = repository.getTopScores(Difficulty.SMALL)
        assertEquals(1, scores.size)
        assertEquals(120, scores[0].timeSeconds)
    }

    @Test
    fun `scores are sorted by time ascending`() {
        repository.addScore(Difficulty.SMALL, 150)
        repository.addScore(Difficulty.SMALL, 90)
        repository.addScore(Difficulty.SMALL, 120)

        assertEquals(
            listOf(90, 120, 150),
            repository.getTopScores(Difficulty.SMALL).map { it.timeSeconds }
        )
    }

    @Test
    fun `list is capped at max entries`() {
        repeat(15) { repository.addScore(Difficulty.SMALL, 100 + it) }

        assertEquals(HighScoreRepository.MAX_ENTRIES, repository.getTopScores(Difficulty.SMALL).size)
    }

    @Test
    fun `worst scores are dropped when list is full`() {
        repeat(HighScoreRepository.MAX_ENTRIES) { repository.addScore(Difficulty.SMALL, 50 + it) }

        repository.addScore(Difficulty.SMALL, 30) // better than all — should push out the worst

        val scores = repository.getTopScores(Difficulty.SMALL)
        assertEquals(HighScoreRepository.MAX_ENTRIES, scores.size)
        assertEquals(30, scores.first().timeSeconds)
        assertFalse(scores.any { it.timeSeconds == 50 + HighScoreRepository.MAX_ENTRIES - 1 })
    }

    @Test
    fun `slow time does not qualify when list is full`() {
        repeat(HighScoreRepository.MAX_ENTRIES) { repository.addScore(Difficulty.SMALL, 50 + it) }

        val qualifies = repository.addScore(Difficulty.SMALL, 999)

        assertFalse(qualifies)
        assertTrue(repository.getTopScores(Difficulty.SMALL).none { it.timeSeconds == 999 })
    }

    @Test
    fun `scores are independent per difficulty`() {
        repository.addScore(Difficulty.SMALL, 100)

        assertTrue(repository.getTopScores(Difficulty.MEDIUM).isEmpty())
        assertTrue(repository.getTopScores(Difficulty.LARGE).isEmpty())
    }

    @Test
    fun `scores persist across repository instances`() {
        repository.addScore(Difficulty.SMALL, 77)

        val anotherInstance = HighScoreRepository(storage)
        assertEquals(77, anotherInstance.getTopScores(Difficulty.SMALL).first().timeSeconds)
    }
}

private class InMemoryHighScoreStorage : HighScoreStorage {
    private val map = mutableMapOf<String, String>()
    override fun load(key: String) = map[key] ?: ""
    override fun save(key: String, value: String) { map[key] = value }
}
