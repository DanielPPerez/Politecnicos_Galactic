// En: com/test/galaxyUP/core/ScoreManager.kt
package com.test.galaxyUP.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.test.galaxyUP.entities.ScoreEntry

object ScoreManager {
    private const val PREFS_NAME = "game_scores"
    private const val SCORES_KEY = "high_scores"
    private const val MAX_SCORES = 20 // Guardaremos solo los 20 mejores puntajes

    private val gson = Gson()

    // Carga la lista de puntajes desde SharedPreferences
    fun loadScores(context: Context): List<ScoreEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(SCORES_KEY, null)
        if (json != null) {
            // Usa TypeToken para decirle a Gson que estamos convirtiendo a una Lista de ScoreEntry
            val type = object : TypeToken<List<ScoreEntry>>() {}.type
            return gson.fromJson(json, type)
        }
        return emptyList() // Devuelve una lista vacía si no hay nada guardado
    }

    // Guarda un nuevo puntaje
    fun saveScore(context: Context, newEntry: ScoreEntry) {
        // No guardamos puntajes de 0
        if (newEntry.score <= 0) return

        val currentScores = loadScores(context).toMutableList()
        currentScores.add(newEntry)

        // Ordena la lista de mayor a menor puntaje y toma los mejores
        val sortedScores = currentScores.sortedByDescending { it.score }.take(MAX_SCORES)

        // Convierte la lista ordenada a formato JSON
        val json = gson.toJson(sortedScores)

        // Guarda el JSON en SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SCORES_KEY, json).apply()
    }
}