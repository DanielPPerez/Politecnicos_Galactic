package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R
import kotlin.random.Random

// --- MODIFICADO: Añadido LASER al enum ---
enum class PowerUpType {
    SHIELD,
    MULTIPLIER,
    LASER
}

class PowerUp(context: Context, screenWidth: Int, screenHeight: Int, val type: PowerUpType) {

    var bitmap: Bitmap
    var x: Float
    var y: Float
    var width: Int
    var height: Int

    private val speedY = 150f
    val collisionRect: Rect

    init {
        // --- MODIFICADO: Añadido el caso para el drawable del láser ---
        val drawableId = when (type) {
            PowerUpType.SHIELD -> R.drawable.shield
            PowerUpType.MULTIPLIER -> R.drawable.multiplier
            PowerUpType.LASER -> R.drawable.laser
        }
        val originalBitmap = BitmapFactory.decodeResource(context.resources, drawableId)

        width = screenWidth / 15
        height = (width * originalBitmap.height) / originalBitmap.width
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        x = Random.nextInt(0, screenWidth - width).toFloat()
        y = -height.toFloat()

        collisionRect = Rect(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    fun update(deltaTime: Float) {
        y += speedY * deltaTime
        collisionRect.set(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }
}