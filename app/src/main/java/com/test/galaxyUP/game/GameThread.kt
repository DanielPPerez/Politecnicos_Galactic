package com.test.galaxyUP.game

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {

    @Volatile // Asegura que los cambios en 'running' sean visibles para todos los hilos
    private var running: Boolean = false
    private val targetFPS = 60

    fun setRunning(isRunning: Boolean) {
        this.running = isRunning
    }

    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long
        val targetTime = (1000 / targetFPS).toLong()
        var lastUpdateTime = System.nanoTime()

        while (running) {
            startTime = System.nanoTime()
            var canvas: Canvas? = null

            val now = System.nanoTime()
            val deltaTime = (now - lastUpdateTime) / 1_000_000_000f
            lastUpdateTime = now

            // Comprobar si la superficie es válida antes de intentar bloquearla
            if (!surfaceHolder.surface.isValid) {
                continue // Saltar este ciclo si la superficie no está lista
            }

            try {
                canvas = this.surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        this.gameView.updateWithDelta(deltaTime)
                        this.gameView.draw(canvas)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000
            waitTime = targetTime - timeMillis

            try {
                if (waitTime > 0) {
                    sleep(waitTime)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}