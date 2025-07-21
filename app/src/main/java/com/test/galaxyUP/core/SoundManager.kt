package com.test.galaxyUP.core

import android.content.Context
import android.media.MediaPlayer
import com.test.galaxyUP.R
import java.io.IOException

// Usamos 'object' para crear un Singleton de forma sencilla en Kotlin.
object SoundManager {

    private var menuPlayer: MediaPlayer? = null
    private var gamePlayer: MediaPlayer? = null
    private var currentPlaying: MediaPlayer? = null

    // Crea un reproductor de forma segura y lo configura para el bucle
    private fun createPlayer(context: Context, resId: Int): MediaPlayer {
        return MediaPlayer.create(context, resId).apply {
            // La propiedad isLooping es la forma correcta y debería funcionar.
            // La reforzaremos asegurándonos de que el estado del reproductor sea siempre el correcto.
            isLooping = true
        }
    }

    fun playMenuMusic(context: Context) {
        // No hacer nada si ya está sonando la música del menú
        if (currentPlaying == menuPlayer && menuPlayer?.isPlaying == true) {
            return
        }

        // Pausar la música del juego si estaba sonando
        if (gamePlayer?.isPlaying == true) {
            gamePlayer?.pause()
        }

        // Crear el reproductor si es la primera vez
        if (menuPlayer == null) {
            menuPlayer = createPlayer(context.applicationContext, R.raw.menu_music)
        }

        // Iniciar la reproducción
        menuPlayer?.start()
        currentPlaying = menuPlayer
    }

    fun playGameMusic(context: Context) {
        // No hacer nada si ya está sonando la música del juego
        if (currentPlaying == gamePlayer && gamePlayer?.isPlaying == true) {
            return
        }

        // Pausar la música del menú si estaba sonando
        if (menuPlayer?.isPlaying == true) {
            menuPlayer?.pause()
        }

        // Crear el reproductor si es la primera vez
        if (gamePlayer == null) {
            gamePlayer = createPlayer(context.applicationContext, R.raw.game_music)
        }

        // Iniciar la reproducción
        gamePlayer?.start()
        currentPlaying = gamePlayer
    }

    fun pauseMusic() {
        // Pausamos la música que esté sonando actualmente
        if (currentPlaying?.isPlaying == true) {
            currentPlaying?.pause()
        }
    }

    fun resumeMusic() {
        // Reanudamos la música que estaba sonando si no está sonando ya
        if (currentPlaying != null && currentPlaying?.isPlaying == false) {
            currentPlaying?.start()
        }
    }

    // Este método es para cuando quieres detener y resetear la música,
    // por ejemplo al volver al menú principal desde el juego.
    fun stopMusic() {
        currentPlaying?.let {
            if (it.isPlaying) {
                it.pause() // Usar pause y seekTo(0) es más seguro que stop() para el bucle
                it.seekTo(0)
            }
        }
        currentPlaying = null
    }

    // ¡Muy importante! Libera todos los recursos cuando la app se cierra por completo.
    fun release() {
        menuPlayer?.release()
        menuPlayer = null
        gamePlayer?.release()
        gamePlayer = null
        currentPlaying = null
    }
}