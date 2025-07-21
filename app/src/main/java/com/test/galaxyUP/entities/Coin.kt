package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R

class Coin(
    context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int,
    val value: Int
) {
    var x: Float = 0f
    var y: Float = 0f
    val width: Int
    val height: Int
    val collisionRect: Rect
    private val bitmap: Bitmap
    private val speed: Float = 6f

    init {
        // Seleccionar imagen según el valor
        val drawableId = when (value) {
            1 -> R.drawable.moneda1
            2 -> R.drawable.moneda2
            5 -> R.drawable.moneda5
            10 -> R.drawable.moneda10
            else -> R.drawable.moneda1
        }
        val bmp = BitmapFactory.decodeResource(context.resources, drawableId)
        width = (screenWidth * 0.07f).toInt()
        height = (width * bmp.height) / bmp.width
        bitmap = Bitmap.createScaledBitmap(bmp, width, height, false)
        x = (0..(screenWidth - width)).random().toFloat()
        y = -height.toFloat()
        collisionRect = Rect(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    fun update() {
        y += speed
        collisionRect.set(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }

    fun isOutOfScreen(): Boolean = y > screenHeight
}

