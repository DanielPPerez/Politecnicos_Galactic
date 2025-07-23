// En: com/test/galaxyUP/activities/HighscoreActivity.kt
package com.test.galaxyUP.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.galaxyUP.R
import com.test.galaxyUP.core.ScoreManager
import com.test.galaxyUP.ui.HighscoreAdapter

class HighscoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highscore)

        val scoresRecyclerView = findViewById<RecyclerView>(R.id.scoresRecyclerView)
        scoresRecyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar los puntajes y pasarlos al adaptador
        val scores = ScoreManager.loadScores(this)
        scoresRecyclerView.adapter = HighscoreAdapter(scores)
    }
}