package com.test.galaxyUP.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.test.galaxyUP.R

class HUD(context: Context, private val screenWidth: Int, screenHeight: Int) {

    private val scoreBarBitmap: Bitmap
    private val clockBitmap: Bitmap
    private val heartFullBitmap: Bitmap
    private val heartEmptyBitmap: Bitmap
    // --- NUEVO: Bitmap para el icono de la moneda ---
    private val coinIconBitmap: Bitmap

    private val scorePaint: Paint
    private val timePaint: Paint
    // --- NUEVO: Paint para el texto de las monedas ---
    private val coinPaint: Paint

    private val heartWidth: Int
    private val heartHeight: Int
    private val scoreBarHeight: Int
    private val margin = 30f

    companion object {
        const val MAX_LIVES = 3
    }

    init {
        val scoreBarOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.hud_score_bar)
        val newScoreBarWidth = (screenWidth * 0.35f).toInt()
        scoreBarHeight = (newScoreBarWidth * scoreBarOriginal.height) / scoreBarOriginal.width
        scoreBarBitmap = Bitmap.createScaledBitmap(scoreBarOriginal, newScoreBarWidth, scoreBarHeight, false)

        val heartOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.hud_heart_full)
        heartHeight = (scoreBarHeight * 0.7f).toInt()
        heartWidth = (heartHeight * heartOriginal.width) / heartOriginal.height
        heartFullBitmap = Bitmap.createScaledBitmap(heartOriginal, heartWidth, heartHeight, false)
        heartEmptyBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.hud_heart_empty), heartWidth, heartHeight, false)

        clockBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.hud_clock)
        // --- NUEVO: Cargar y escalar el icono de moneda ---
        val coinIconOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.moneda1)
        coinIconBitmap = Bitmap.createScaledBitmap(coinIconOriginal, 60, 60, false)


        scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = scoreBarHeight * 0.8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        timePaint = Paint().apply {
            color = Color.WHITE
            textSize = scoreBarHeight * 0.7f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        // --- NUEVO: Inicializar el Paint para las monedas ---
        coinPaint = Paint().apply {
            color = Color.YELLOW
            textSize = scoreBarHeight * 0.7f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    // --- CORREGIDO: Añadido el parámetro coinsCollected ---
    fun draw(canvas: Canvas, score: Int, lives: Int, time: Float, coinsCollected: Int) {
        // Posiciones de Anclaje
        val baseX = margin
        var currentY = margin

        // 1. Dibujar corazones (arriba a la izquierda)
        var currentHeartX = baseX
        for (i in 0 until MAX_LIVES) {
            val heartBitmap = if (i < lives) heartFullBitmap else heartEmptyBitmap
            canvas.drawBitmap(heartBitmap, currentHeartX, currentY, null)
            currentHeartX += heartWidth + 10
        }
        currentY += heartHeight + 15f

        // 2. Dibujar barra de score debajo de corazones
        canvas.drawBitmap(scoreBarBitmap, baseX, currentY, null)

        // 3. Dibujar el puntaje centrado en la barra
        val scoreString = score.toString().padStart(6, '0')
        val scoreX = baseX + scoreBarBitmap.width / 2f - scorePaint.measureText(scoreString) / 2f
        val scoreY = currentY + scoreBarHeight / 2f - (scorePaint.descent() + scorePaint.ascent()) / 2f
        canvas.drawText(scoreString, scoreX, scoreY, scorePaint)
        currentY += scoreBarHeight + 15f

        // 4. Dibujar el contador de monedas debajo del score
        val coinIconX = baseX
        val coinIconY = currentY
        canvas.drawBitmap(coinIconBitmap, coinIconX, coinIconY, null)
        val coinText = "x $coinsCollected"
        val coinTextY = coinIconY + coinIconBitmap.height / 2f - (coinPaint.descent() + coinPaint.ascent()) / 2f
        canvas.drawText(coinText, coinIconX + coinIconBitmap.width + 10f, coinTextY, coinPaint)

        // 5. Dibujar el tiempo (esquina superior derecha, alineado horizontalmente)
        val timeString = formatTime(time)
        val timeStringWidth = timePaint.measureText(timeString)
        val timeX = screenWidth - margin - timeStringWidth
        val clockX = timeX - clockBitmap.width - 15f
        val clockY = margin + (timePaint.textSize - clockBitmap.height) / 2f
        val timeY = clockY + clockBitmap.height / 2f - (timePaint.descent() + timePaint.ascent()) / 2f
        canvas.drawBitmap(clockBitmap, clockX, clockY, null)
        canvas.drawText(timeString, timeX, timeY, timePaint)
    }

    private fun formatTime(time: Float): String {
        val minutes = (time / 60).toInt()
        val seconds = (time % 60).toInt()
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}