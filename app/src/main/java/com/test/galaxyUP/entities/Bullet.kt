package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R

enum class BulletOwner {
    PLAYER,
    ENEMY
}

class Bullet(
    context: Context,
    startX: Int,
    startY: Int,
    var owner: BulletOwner
) {

    var bitmap: Bitmap
    var x: Int
    var y: Int
    var width: Int
    var height: Int

    private val speedY: Int

    val collisionRect: Rect

    init {
        if (owner == BulletOwner.PLAYER) {
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.player_bullet1)
            speedY = -30
            // Dejamos la bala del jugador pequeña
            width = bitmap.width
            height = bitmap.height
        } else { // ENEMY
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.enemy_bullet2)
            speedY = 20
            // --- AJUSTE: Aumentar el tamaño de la bala enemiga ---
            // Ahora la bala será la mitad del tamaño original, en lugar de un cuarto.
            width = bitmap.width
            height = bitmap.height
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        // Posicionar la bala en el punto de origen (centrada)
        x = startX - (width / 2)
        y = startY

        collisionRect = Rect(x, y, x + width, y + height)
    }

    fun update(deltaTime: Float) {
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