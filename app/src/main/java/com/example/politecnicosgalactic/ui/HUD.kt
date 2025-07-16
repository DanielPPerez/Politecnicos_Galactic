package com.example.politecnicosgalactic.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.politecnicosgalactic.R

class HUD(context: Context, private val screenWidth: Int, screenHeight: Int) {

    // Bitmaps para los elementos del HUD (sin el numbersBitmap)
    private val scoreBarBitmap: Bitmap
    private val clockBitmap: Bitmap
    private val heartFullBitmap: Bitmap
    private val heartEmptyBitmap: Bitmap

    // --- NUEVO: Objetos Paint para dibujar texto ---
    private val scorePaint: Paint
    private val timePaint: Paint

    // Dimensiones de los elementos
    private val heartWidth: Int
    private val heartHeight: Int
    private val scoreBarHeight: Int
    private val margin = 30f

    companion object {
        const val MAX_LIVES = 3
    }

    init {
        // Cargar y escalar la barra de score
        val scoreBarOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.hud_score_bar)
        val newScoreBarWidth = (screenWidth * 0.35f).toInt()
        scoreBarHeight = (newScoreBarWidth * scoreBarOriginal.height) / scoreBarOriginal.width
        scoreBarBitmap = Bitmap.createScaledBitmap(scoreBarOriginal, newScoreBarWidth, scoreBarHeight, false)

        // Cargar y escalar corazones
        val heartOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.hud_heart_full)
        heartHeight = (scoreBarHeight * 0.7f).toInt()
        heartWidth = (heartHeight * heartOriginal.width) / heartOriginal.height
        heartFullBitmap = Bitmap.createScaledBitmap(heartOriginal, heartWidth, heartHeight, false)
        heartEmptyBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.hud_heart_empty), heartWidth, heartHeight, false)

        // Cargar el reloj
        clockBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.hud_clock)

        // --- ELIMINADO: Carga del spritesheet de números ---

        // --- NUEVO: Inicializar los objetos Paint ---
        scorePaint = Paint().apply {
            color = Color.WHITE
            textSize = scoreBarHeight * 0.8f // Tamaño del texto del score
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        timePaint = Paint().apply {
            color = Color.WHITE
            textSize = scoreBarHeight * 0.7f // Tamaño del texto del tiempo
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    fun draw(canvas: Canvas, score: Int, lives: Int, time: Float) {
        // --- Posiciones de Anclaje ---
        val scoreBarX = margin
        val scoreBarY = margin

        // 1. Dibujar la barra de score
        canvas.drawBitmap(scoreBarBitmap, scoreBarX, scoreBarY, null)

        // 2. Dibujar corazones SOBRE la barra
        val heartsY = scoreBarY + (scoreBarHeight - heartHeight) / 2f
        var currentHeartX = scoreBarX + scoreBarBitmap.width * 0.45f
        for (i in 0 until MAX_LIVES) {
            val heartBitmap = if (i < lives) heartFullBitmap else heartEmptyBitmap
            canvas.drawBitmap(heartBitmap, currentHeartX, heartsY, null)
            currentHeartX += heartWidth + 10
        }

        // 3. Dibujar el puntaje AL LADO de la barra usando Paint
        val scoreString = score.toString().padStart(6, '0')
        val scoreX = scoreBarX + scoreBarBitmap.width + 20f
        // Fórmula para centrar verticalmente el texto
        val scoreY = scoreBarY + scoreBarHeight / 2f - (scorePaint.descent() + scorePaint.ascent()) / 2f
        canvas.drawText(scoreString, scoreX, scoreY, scorePaint)

        // 4. Dibujar el tiempo (esquina superior derecha) usando Paint
        val timeString = formatTime(time)
        val timeStringWidth = timePaint.measureText(timeString)
        val timeX = screenWidth - margin - timeStringWidth
        val clockY = margin + (timePaint.textSize - clockBitmap.height) / 2f // Alinear reloj con el texto
        val clockX = timeX - clockBitmap.width - 15f
        // Centrar verticalmente el texto del tiempo con el icono del reloj
        val timeY = clockY + clockBitmap.height / 2f - (timePaint.descent() + timePaint.ascent()) / 2f
        canvas.drawBitmap(clockBitmap, clockX, clockY, null)
        canvas.drawText(timeString, timeX, timeY, timePaint)
    }

    private fun formatTime(time: Float): String {
        val minutes = (time / 60).toInt()
        val seconds = (time % 60).toInt()
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    // --- ELIMINADOS: Los métodos calculateStringWidth y drawNumberString ya no son necesarios ---
}