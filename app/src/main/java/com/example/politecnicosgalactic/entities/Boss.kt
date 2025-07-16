package com.example.politecnicosgalactic.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.example.politecnicosgalactic.R

class Boss(context: Context, private val screenWidth: Int, screenHeight: Int) {

    // Bitmaps para cada fase
    private val bitmapPhase1: Bitmap
    private val bitmapPhase2: Bitmap
    private var currentBitmap: Bitmap

    var x: Float
    var y: Float
    var width: Int
    var height: Int

    // Propiedades de estado
    var health: Int = 20
    var isAlive: Boolean = true
    private var currentPhase = 1

    // Propiedades de movimiento
    private var speedX = 150f
    private var directionX = 1

    // Propiedades de disparo
    private var shootTimer = 0f
    private val shootInterval = 0.8f // Dispara cada 0.8 segundos
    var canShoot = false
        private set

    val collisionRect: Rect

    init {
        // Cargar y escalar las imágenes del jefe
        val originalPhase1 = BitmapFactory.decodeResource(context.resources, R.drawable.enemyboss1)
        val originalPhase2 = BitmapFactory.decodeResource(context.resources, R.drawable.enemyboss2)

        width = (screenWidth * 0.5f).toInt() // El jefe ocupa la mitad de la pantalla
        height = (width * originalPhase1.height) / originalPhase1.width
        bitmapPhase1 = Bitmap.createScaledBitmap(originalPhase1, width, height, false)
        bitmapPhase2 = Bitmap.createScaledBitmap(originalPhase2, width, height, false)

        currentBitmap = bitmapPhase1 // Empezar con la fase 1

        // Posición inicial
        x = (screenWidth / 2f) - (width / 2f)
        y = -height.toFloat() // Empieza fuera de la pantalla

        collisionRect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        // Moverse lentamente hacia la posición de batalla
        if (y < 50f) {
            y += 50f * deltaTime
        } else {
            // Movimiento de lado a lado
            x += speedX * directionX * deltaTime
            if (x <= 0 || x + width >= screenWidth) {
                directionX *= -1 // Rebotar en los bordes
            }
        }

        // Manejo del temporizador de disparo
        shootTimer += deltaTime
        if (shootTimer >= shootInterval) {
            shootTimer = 0f
            canShoot = true
        }

        // Actualizar rectángulo de colisión
        collisionRect.left = x.toInt()
        collisionRect.top = y.toInt()
        collisionRect.right = (x + width).toInt()
        collisionRect.bottom = (y + height).toInt()
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
            // Cambiar a fase 2
            currentPhase = 2
            currentBitmap = bitmapPhase2
            speedX *= 1.5f // Se mueve más rápido en la fase 2
        }

        if (health <= 0) {
            isAlive = false
        }
    }

    // Método para que GameView reinicie la bandera de disparo
    fun resetShootFlag() {
        canShoot = false
    }
}