package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R

class BouncingBullet(
    context: Context,
    startX: Float,
    startY: Float,
    private var speedX: Float,
    private var speedY: Float
) {
    var bitmap: Bitmap
    var x: Float = startX
    var y: Float = startY
    var width: Int
    var height: Int

    val collisionRect: Rect

    init {
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.enemy_bullet2)
        width = bitmap.width
        height = bitmap.height
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        collisionRect = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }

    fun update(screenWidth: Int, screenHeight: Int) {
        x += speedX
        y += speedY

        // --- LÓGICA DE REBOTE CORREGIDA ---
        // Rebota en los bordes laterales
        if (x <= 0 || x + width >= screenWidth) {
            speedX *= -1 // Invertir dirección horizontal
        }
        // Rebota en el borde superior
        if (y <= 0) {
            speedY *= -1 // Invertir dirección vertical
        }
        // Nota: No rebota en el borde inferior para que eventualmente salgan del juego.

        // Actualizar rectángulo de colisión
        collisionRect.set(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}