package com.test.galaxyUP.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.test.galaxyUP.R
import com.test.galaxyUP.api.ApiClient
import com.test.galaxyUP.api.ScorePostRequest
import com.test.galaxyUP.core.SoundManager
import com.test.galaxyUP.game.GameView
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)

        val selectedSkinRes = intent.getIntExtra("selected_skin_res", R.drawable.ship1blue)
        gameView = GameView(this, selectedSkinRes)
        setContentView(gameView)

        val returnToMenuAction = { saveScoreAndFinish() }

        gameView.gameOverAction = { action ->
            if (action == 0) {
                saveScoreToServer()
                gameView.setupGame()
            } else if (action == 1) {
                returnToMenuAction()
            }
        }
        gameView.pauseAction = { actionIndex -> if (actionIndex == 1) returnToMenuAction() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { returnToMenuAction() }
        })
    }

    private fun saveScoreAndFinish() {
        Toast.makeText(this, "Sincronizando...", Toast.LENGTH_SHORT).show()
        saveScoreToServer(andThenFinish = true)
    }

    private fun saveScoreToServer(andThenFinish: Boolean = false) {
        val authToken = sharedPreferences.getString("auth_token", null)
        if (authToken == null) {
            if (andThenFinish) finish()
            return
        }

        val score = gameView.getScore()
        val timePlayed = gameView.getTimePlayed().toInt()
        if (score <= 0) {
            if (andThenFinish) finish()
            return
        }

        lifecycleScope.launch {
            try {
                val request = ScorePostRequest(puntuacion = score, tiempo_jugado = timePlayed)
                ApiClient.instance.saveScore("Bearer $authToken", request)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("SAVE_SCORE", "Error al guardar puntuación: ${e.message}")
                }
            } finally {
                if (andThenFinish) {
                    SoundManager.stopMusic()
                    finish()
                }
            }
        }
    }

    override fun onResume() { super.onResume(); SoundManager.playGameMusic(this); gameView.resume() }
    override fun onPause() { super.onPause(); SoundManager.pauseMusic(); gameView.pause() }
}