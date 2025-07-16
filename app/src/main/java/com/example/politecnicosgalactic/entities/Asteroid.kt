package com.example.politecnicosgalactic.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import com.example.politecnicosgalactic.R
import kotlin.random.Random

class Asteroid(context: Context, private val screenWidth: Int, screenHeight: Int) {

    var bitmap: Bitmap
    var x: Float
    var y: Float
    var width: Int
    var height: Int

    private val speed: Float
    val collisionRect: Rect

    private var health: Int = 3
    private val rotationSpeed: Float
    private var currentRotation: Float = 0f
    var isAlive: Boolean = true

    companion object {
        private val ASTEROID_DRAWABLES = listOf(
            R.drawable.asteorid1big,
            R.drawable.asteroid1medium,
            R.drawable.asteorid2medium,
            R.drawable.asteroid2small
        )
    }

    init {
        val randomDrawable = ASTEROID_DRAWABLES.random()
        val originalBitmap = BitmapFactory.decodeResource(context.resources, randomDrawable)

        val scale = Random.nextFloat() * 0.5f + 0.5f
        width = (screenWidth / 12 * scale).toInt()
        height = (width * originalBitmap.height) / originalBitmap.width
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        x = Random.nextInt(0, screenWidth - width).toFloat()
        y = -height.toFloat()

        // --- AJUSTE: Reducir la velocidad de los asteroides ---
        speed = Random.nextInt(60, 180).toFloat() // Era 100-400
        rotationSpeed = Random.nextInt(-25, 25).toFloat()

        collisionRect = Rect(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        y += speed * deltaTime
        currentRotation += rotationSpeed * deltaTime

        collisionRect.left = x.toInt()
        collisionRect.top = y.toInt()
        collisionRect.right = x.toInt() + width
        collisionRect.bottom = y.toInt() + height
    }

    fun draw(canvas: Canvas) {
        if (!isAlive) return

        val matrix = Matrix().apply {
            postRotate(currentRotation, (width / 2).toFloat(), (height / 2).toFloat())
            postTranslate(x, y)
        }
        canvas.drawBitmap(bitmap, matrix, null)
    }

    fun takeHit() {
        health--
        if (health <= 0) {
            isAlive = false
        }
    }
}