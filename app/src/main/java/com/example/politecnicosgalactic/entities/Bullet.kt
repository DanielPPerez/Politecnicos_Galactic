package com.example.politecnicosgalactic.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.example.politecnicosgalactic.R

// <<< NUEVO: Enum para definir el tipo de propietario de la bala >>>
enum class BulletOwner {
    PLAYER,
    ENEMY
}

class Bullet(
    context: Context,
    startX: Int,
    startY: Int,
    // <<< NUEVO: Parámetro para indicar quién dispara >>>
    val owner: BulletOwner
) {

    var bitmap: Bitmap
    var x: Int
    var y: Int
    var width: Int
    var height: Int

    private val speedY: Int

    val collisionRect: Rect

    init {
        // <<< NUEVO: Elegir la imagen y velocidad según el propietario >>>
        if (owner == BulletOwner.PLAYER) {
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.missile_a)
            speedY = -30 // Hacia arriba
        } else { // ENEMY
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bullet)
            speedY = 20  // Hacia abajo
        }

        // <<< MODIFICADO: Reducimos el tamaño de las balas >>>
        // Ahora las balas serán 1/4 de su tamaño original, haciéndolas más pequeñas.
        // Puedes ajustar el divisor (ej. /3, /4) para cambiar el tamaño.
        width = bitmap.width / 4
        height = bitmap.height / 4
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        // Posicionar la bala en el punto de origen (centrada)
        x = startX - (width / 2)
        y = startY

        collisionRect = Rect(x, y, x + width, y + height)
    }

    fun update() {
        y += speedY
        collisionRect.left = x
        collisionRect.top = y
        collisionRect.right = x + width
        collisionRect.bottom = y + height
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
    }
}