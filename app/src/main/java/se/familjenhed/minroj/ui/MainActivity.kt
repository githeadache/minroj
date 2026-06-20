package se.familjenhed.minroj.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import se.familjenhed.minroj.data.HighScoreRepository
import se.familjenhed.minroj.data.SharedPrefsHighScoreStorage
import se.familjenhed.minroj.databinding.ActivityMainBinding
import se.familjenhed.minroj.domain.Difficulty
import se.familjenhed.minroj.domain.GameStatus
import se.familjenhed.minroj.domain.RandomMineGenerator
import se.familjenhed.minroj.viewmodel.GameViewModel
import se.familjenhed.minroj.viewmodel.GameViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: GameViewModel by viewModels {
        GameViewModelFactory(
            mineGenerator = RandomMineGenerator(),
            highScoreRepository = HighScoreRepository(SharedPrefsHighScoreStorage(this))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDifficultyButtons()
        setupBoardView()
        setupControlButtons()
        observeState()
    }

    private fun setupDifficultyButtons() {
        binding.btnSmall.setOnClickListener { viewModel.startGame(Difficulty.SMALL) }
        binding.btnMedium.setOnClickListener { viewModel.startGame(Difficulty.MEDIUM) }
        binding.btnLarge.setOnClickListener { viewModel.startGame(Difficulty.LARGE) }
    }

    private fun setupBoardView() {
        binding.gameBoardView.cellClickListener = GameBoardView.CellClickListener { row, col ->
            viewModel.onCellClick(row, col)
        }
    }

    private fun setupControlButtons() {
        binding.btnReset.setOnClickListener {
            viewModel.startGame(viewModel.uiState.value.difficulty)
        }
        binding.btnFlagMode.setOnClickListener {
            viewModel.toggleFlagMode()
        }
        binding.btnHighScore.setOnClickListener {
            HighScoreDialog.show(this, viewModel, viewModel.uiState.value.difficulty)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.gameBoardView.cells = state.cells
                binding.gameBoardView.gameStatus = state.status

                binding.tvMineCount.text = state.remainingFlags.coerceIn(-99, 999)
                    .toString().padStart(3, '0')
                binding.tvTimer.text = state.elapsedSeconds.toString().padStart(3, '0')

                binding.btnReset.text = when (state.status) {
                    GameStatus.WON -> "😎"
                    GameStatus.LOST -> "😵"
                    else -> "🙂"
                }

                binding.btnFlagMode.isSelected = state.isFlagMode
                binding.btnFlagMode.text = if (state.isFlagMode) "✅ Markera" else "🚩 Markera"

                if (state.isNewHighScore) {
                    Snackbar.make(binding.root, "🏆 Nytt rekord!", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
