package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R

class Mine(context: Context, startX: Float, startY: Float, val mineSize: Int) {

    private val bitmap: Bitmap
    var x: Float = startX
    var y: Float = startY
    val width: Int = mineSize
    val height: Int = mineSize

    private val speedY = 200f // Velocidad de caída de las minas
    val collisionRect: Rect

    init {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.big_mine)
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)
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