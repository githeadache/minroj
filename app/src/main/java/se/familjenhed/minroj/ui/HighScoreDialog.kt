package se.familjenhed.minroj.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import se.familjenhed.minroj.R
import se.familjenhed.minroj.domain.Difficulty
import se.familjenhed.minroj.domain.HighScore
import se.familjenhed.minroj.viewmodel.GameViewModel

object HighScoreDialog {

    fun show(context: Context, viewModel: GameViewModel, initialDifficulty: Difficulty) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_high_score, null)
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleDifficulty)
        val tvScores = view.findViewById<TextView>(R.id.tvScores)

        fun updateScores(difficulty: Difficulty) {
            tvScores.text = buildScoreText(viewModel.getHighScores(difficulty))
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val difficulty = when (checkedId) {
                R.id.btnToggleSmall -> Difficulty.SMALL
                R.id.btnToggleMedium -> Difficulty.MEDIUM
                else -> Difficulty.LARGE
            }
            updateScores(difficulty)
        }

        val initialButtonId = when (initialDifficulty) {
            Difficulty.SMALL -> R.id.btnToggleSmall
            Difficulty.MEDIUM -> R.id.btnToggleMedium
            Difficulty.LARGE -> R.id.btnToggleLarge
        }
        toggleGroup.check(initialButtonId)

        MaterialAlertDialogBuilder(context)
            .setTitle("🏆 Bästa tider")
            .setView(view)
            .setPositiveButton("Stäng", null)
            .show()
    }

    private fun buildScoreText(scores: List<HighScore>): String {
        if (scores.isEmpty()) return "Inga tider registrerade ännu."
        return scores.mapIndexed { i, score ->
            "%2d.   %s   %s".format(i + 1, score.formattedTime(), score.formattedDate())
        }.joinToString("\n")
    }
}
