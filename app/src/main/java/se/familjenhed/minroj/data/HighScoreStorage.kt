package se.familjenhed.minroj.data

import android.content.Context

interface HighScoreStorage {
    fun load(key: String): String
    fun save(key: String, value: String)
}

class SharedPrefsHighScoreStorage(context: Context) : HighScoreStorage {
    private val prefs = context.getSharedPreferences("highscores", Context.MODE_PRIVATE)
    override fun load(key: String) = prefs.getString(key, "") ?: ""
    override fun save(key: String, value: String) { prefs.edit().putString(key, value).apply() }
}
