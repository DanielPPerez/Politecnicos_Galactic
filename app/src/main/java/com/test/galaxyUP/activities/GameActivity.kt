package com.test.galaxyUP.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.test.galaxyUP.game.GameView
import com.test.galaxyUP.R
import android.content.SharedPreferences
import com.test.galaxyUP.core.SoundManager // --- NUEVA IMPORTACIÓN ---

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedSkinRes: Int = R.drawable.ship1blue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        selectedSkinRes = intent.getIntExtra("selected_skin_res", R.drawable.ship1blue)

        gameView = GameView(this, selectedSkinRes)
        setContentView(gameView)

        gameView.gameOverAction = { action ->
            val coins = gameView.getCoinsCollected()
            saveCoins(coins)

            if (action == 0) {
                gameView.setupGame()
            } else if (action == 1) {
                // --- MODIFICADO: Detener la música del juego antes de volver al menú ---
                SoundManager.stopMusic()
                finish()
            }
        }
    }

    private fun saveCoins(coins: Int) {
        if (coins > 0) {
            val prevCoins = sharedPreferences.getInt("coins", 0)
            sharedPreferences.edit().putInt("coins", prevCoins + coins).apply()
        }
    }

    // --- NUEVO: Manejo del ciclo de vida para la música ---
    override fun onResume() {
        super.onResume()
        // Inicia o reanuda la música del juego
        SoundManager.playGameMusic(this)
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        // Pausa la música y el juego
        SoundManager.pauseMusic()
        gameView.pause()
    }
}