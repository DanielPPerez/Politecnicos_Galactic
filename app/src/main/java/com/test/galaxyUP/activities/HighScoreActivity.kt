package com.test.galaxyUP.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.galaxyUP.R
import com.test.galaxyUP.api.ApiClient
import com.test.galaxyUP.ui.HighscoreAdapter
import kotlinx.coroutines.launch

class HighscoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highscore)

        val scoresRecyclerView = findViewById<RecyclerView>(R.id.scoresRecyclerView)
        scoresRecyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar los puntajes desde la API
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getLeaderboard()
                if (response.isSuccessful && response.body()?.success == true) {
                    val leaderboard = response.body()?.data ?: emptyList()
                    scoresRecyclerView.adapter = HighscoreAdapter(leaderboard)
                } else {
                    Toast.makeText(this@HighscoreActivity, "No se pudo cargar el ranking.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HighscoreActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}