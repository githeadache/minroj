package se.familjenhed.minroj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.familjenhed.minroj.data.HighScoreRepository
import se.familjenhed.minroj.domain.MineGenerator

class GameViewModelFactory(
    private val mineGenerator: MineGenerator,
    private val highScoreRepository: HighScoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GameViewModel(mineGenerator, highScoreRepository) as T
}
