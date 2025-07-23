package com.test.galaxyUP.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.test.galaxyUP.R
import com.test.galaxyUP.core.SoundManager
import com.test.galaxyUP.game.GameView

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        val selectedSkinRes = intent.getIntExtra("selected_skin_res", R.drawable.ship1blue)
        gameView = GameView(this, selectedSkinRes)
        setContentView(gameView)

        // Acción centralizada para volver al menú y devolver un resultado.
        val returnToMenuAction = {
            val coinsCollected = gameView.getCoinsCollected()
            val resultIntent = Intent().apply {
                putExtra("coins_collected", coinsCollected)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            SoundManager.stopMusic()
            finish()
        }

        // Configuración para el menú de Game Over
        gameView.gameOverAction = { action ->
            if (action == 0) { // Volver a Jugar
                gameView.setupGame()
            } else if (action == 1) { // Volver al Menú
                returnToMenuAction()
            }
        }

        // Configuración para el menú de Pausa
        gameView.pauseAction = { actionIndex ->
            if (actionIndex == 1) { // Volver al Menú
                returnToMenuAction()
            }
        }

        // Manejar la pulsación del botón "Atrás" del sistema.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Ejecuta la misma lógica que si se usara el menú para salir.
                returnToMenuAction()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        SoundManager.playGameMusic(this)
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
        gameView.pause()
    }
}