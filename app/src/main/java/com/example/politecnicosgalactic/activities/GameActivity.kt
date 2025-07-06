package com.example.politecnicosgalactic.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.politecnicosgalactic.game.GameView

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

        gameView = GameView(this)
        setContentView(gameView)
    }

    // Se llama cuando la actividad pasa a segundo plano
    override fun onPause() {
        super.onPause()
        gameView.pause() // Esencial para detener el hilo del juego
    }

    // Se llama cuando la actividad vuelve al primer plano
    override fun onResume() {
        super.onResume()
        gameView.resume() // Esencial para iniciar/reanudar el hilo del juego
    }
}