package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R
import kotlin.random.Random

class Boss(context: Context, private val screenWidth: Int, screenHeight: Int) {

    private val bitmapPhase1: Bitmap
    private val bitmapPhase2: Bitmap
    private var currentBitmap: Bitmap

    var x: Float
    var y: Float
    var width: Int
    var height: Int

    var health: Int = 20
    var isAlive: Boolean = true
    private var currentPhase = 1

    private var speedX = 150f
    private var directionX = 1

    private var mineDropTimer = 0f
    // --- AJUSTE DE BALANCE: Aumentar el tiempo entre filas de minas ---
    private var mineDropInterval = 2.5f // Era 1.5f. Ahora el jugador tiene un segundo más.
    var shouldDropMines = false
        private set
    var gapPosition: Int = -1
        private set
    private var lastGapPosition = -1

    val collisionRect: Rect

    init {
        val originalPhase1 = BitmapFactory.decodeResource(context.resources, R.drawable.enemyboss1)
        val originalPhase2 = BitmapFactory.decodeResource(context.resources, R.drawable.enemyboss2)

        width = (screenWidth * 0.5f).toInt()
        height = (width * originalPhase1.height) / originalPhase1.width
        bitmapPhase1 = Bitmap.createScaledBitmap(originalPhase1, width, height, false)
        bitmapPhase2 = Bitmap.createScaledBitmap(originalPhase2, width, height, false)

        currentBitmap = bitmapPhase1

        x = (screenWidth / 2f) - (width / 2f)
        y = -height.toFloat()

        collisionRect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        if (y < 50f) {
            y += 50f * deltaTime
        } else {
            x += speedX * directionX * deltaTime
            if (x <= 0 || x + width >= screenWidth) {
                directionX *= -1
            }
        }

        mineDropTimer += deltaTime
        if (mineDropTimer >= mineDropInterval) {
            mineDropTimer = 0f
            shouldDropMines = true
            generateNextGapPosition()
        }

        collisionRect.set(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    private fun generateNextGapPosition() {
        // El número de columnas debe coincidir con el de GameView (7)
        // El hueco ocupa 2 columnas, así que puede empezar desde la 0 hasta la 5 (7 - 2)
        val numberOfColumns = 7
        val maxGapStartPosition = numberOfColumns - 2
        var nextGap: Int

        do {
            nextGap = Random.nextInt(0, maxGapStartPosition + 1)
        } while (lastGapPosition != -1 && kotlin.math.abs(nextGap - lastGapPosition) > 2)

        gapPosition = nextGap
        lastGapPosition = nextGap
    }

    fun draw(canvas: Canvas) {
        if (isAlive) {
            canvas.drawBitmap(currentBitmap, x, y, null)
        }
    }

    fun takeHit() {
        if (!isAlive) return
        health--

        if (health <= 10 && currentPhase == 1) {
            currentPhase = 2
            currentBitmap = bitmapPhase2
            speedX *= 1.5f
            // --- AJUSTE DE BALANCE: También en la fase 2, pero más rápido que la fase 1 ---
            mineDropInterval = 1.8f // Era 1.0f. Sigue siendo un desafío.
        }

        if (health <= 0) {
            isAlive = false
        }
    }

    fun resetMineDropFlag() {
        shouldDropMines = false
        gapPosition = -1
    }
}