package com.test.galaxyUP.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import com.test.galaxyUP.R
import kotlin.math.sin
import kotlin.random.Random

class Enemy(context: Context, private val screenWidth: Int, screenHeight: Int) {

    private val bitmap: Bitmap
    var x: Float
    var y: Float
    val width: Int
    val height: Int

    private val speedY: Float

    private val initialX: Float
    private val amplitude: Float
    private val frequency: Float
    private var time: Float = 0f

    var canShoot = false
        private set
    private var shootTimer: Long = 0L
    private val shootInterval: Long

    val collisionRect: Rect

    init {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.enemy1a)
        width = screenWidth / 12
        height = (width * originalBitmap.height) / originalBitmap.width
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        x = Random.nextInt(0, screenWidth - width).toFloat()
        y = -height.toFloat()

        initialX = x
        amplitude = Random.nextInt(50, screenWidth / 4).toFloat()
        frequency = Random.nextFloat() * 2f + 1f

        // --- AJUSTE: Reducir la velocidad vertical de las naves ---
        speedY = Random.nextInt(80, 200).toFloat() // Era 150-400

        shootInterval = Random.nextLong(1500, 4000)

        collisionRect = Rect(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }

    fun update(deltaTime: Float) {
        y += speedY * deltaTime
        time += deltaTime
        x = initialX + amplitude * sin(time * frequency)
        x = x.coerceIn(0f, (screenWidth - width).toFloat())

        shootTimer += (deltaTime * 1000).toLong()
        if (shootTimer >= shootInterval) {
            canShoot = true
            shootTimer = 0L
        }

        collisionRect.left = x.toInt()
        collisionRect.top = y.toInt()
        collisionRect.right = x.toInt() + width
        collisionRect.bottom = y.toInt() + height
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }

    fun resetShootFlag() {
        canShoot = false
    }
}