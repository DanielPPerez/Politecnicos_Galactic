package com.example.politecnicosgalactic.game

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {

    private var running: Boolean = false
    private var targetFPS = 60 // Nuestro objetivo de fotogramas por segundo
    private var lastUpdateTime: Long = System.nanoTime()
    var deltaTime: Float = 0f // En segundos

    fun setRunning(isRunning: Boolean) {
        this.running = isRunning
    }

    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long
        val targetTime = (1000 / targetFPS).toLong()

        while (running) {
            startTime = System.nanoTime()
            var canvas: Canvas? = null

            // Calcular deltaTime en segundos
            val now = System.nanoTime()
            deltaTime = (now - lastUpdateTime) / 1_000_000_000f
            lastUpdateTime = now

            try {
                // Bloqueamos el canvas para poder dibujar en él
                canvas = this.surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    // Llamamos a los métodos de actualización y dibujado de nuestra GameView
                    this.gameView.updateWithDelta(deltaTime)
                    this.gameView.draw(canvas)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        // Liberamos el canvas y mostramos lo dibujado en pantalla
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Calculamos cuánto tardó el ciclo para ver si necesitamos esperar
            timeMillis = (System.nanoTime() - startTime) / 1000000
            waitTime = targetTime - timeMillis

            try {
                if (waitTime > 0) {
                    // Si fuimos muy rápidos, dormimos el hilo para mantener los FPS estables
                    sleep(waitTime)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
